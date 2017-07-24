//
//  HeytzBleCentralPlugin.m
//  M-box
//
//  Created by 陈东东 on 16/1/19.
//
//

#import "HeytzBleCentralPlugin.h"
#import <Cordova/CDV.h>

@interface HeytzBleCentralPlugin ()
- (CBPeripheral *)findPeripheralByUUID:(NSString *)uuid;

- (void)stopScanTimer:(NSTimer *)timer;
@end

@implementation HeytzBleCentralPlugin

@synthesize manager;
@synthesize peripherals;

//宏定义，判断是否是 iOS10.0以上
#define iOS10 ([[UIDevice currentDevice].systemVersion doubleValue] >= 10.0)

- (void)pluginInitialize {

    NSLog(@"Cordova Heytz Ble Central Plugin");
    NSLog(@"(c)2014-2015 Don Coleman");

    [super pluginInitialize];

    peripherals = [NSMutableSet set];
    manager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];

    connectCallbacks = [NSMutableDictionary new];
    connectCallbackLatches = [NSMutableDictionary new];
    autoConnectCallbackLatches = [NSMutableDictionary new];
    readCallbacks = [NSMutableDictionary new];
    writeCallbacks = [NSMutableDictionary new];
    notificationCallbacks = [NSMutableDictionary new];
    stopNotificationCallbacks = [NSMutableDictionary new];
}

#pragma mark - Cordova Plugin Methods

- (void)connect:(CDVInvokedUrlCommand *)command {

    NSLog(@"connect");
    NSString *uuid = [command.arguments objectAtIndex:0];

    CBPeripheral *peripheral = [self findPeripheralByUUID:uuid];

    if (peripheral) {
        NSLog(@"Connecting to peripheral with UUID : %@", uuid);

        [connectCallbacks setObject:[command.callbackId copy] forKey:[peripheral uuidAsString]];
        [manager connectPeripheral:peripheral options:nil];

    } else {
        NSString *error = [NSString stringWithFormat:@"Could not find peripheral %@.", uuid];
        NSLog(@"%@", error);
        CDVPluginResult *pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }

}

// disconnect: function (device_id, success, failure) {
- (void)disconnect:(CDVInvokedUrlCommand *)command {
    NSLog(@"disconnect");

    NSString *uuid = [command.arguments objectAtIndex:0];
    CBPeripheral *peripheral = [self findPeripheralByUUID:uuid];

    [connectCallbacks removeObjectForKey:uuid];
    autoConnectCallbackId = nil;
    if (peripheral && peripheral.state != CBPeripheralStateDisconnected) {
        [manager cancelPeripheralConnection:peripheral];
    }

    // always return OK
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)autoConnect:(CDVInvokedUrlCommand *)command {

    NSLog(@"autoConnect");
    int bluetoothState = [manager state];
    BOOL enabled = bluetoothState == CBCentralManagerStatePoweredOn;
    if (enabled) {
    autoConnectPeripheral = nil;
    autoConnectCallbackId = [command.callbackId copy];
    autoConnectDeviceName = [command.arguments objectAtIndex:0];
    NSNumber *timeoutSeconds = [command.arguments objectAtIndex:1];
    [manager scanForPeripheralsWithServices:nil options:nil];
  scenTimer=   [NSTimer scheduledTimerWithTimeInterval:[timeoutSeconds floatValue]
                                     target:self
                                   selector:@selector(stopScanTimer:)
                                   userInfo:[command.callbackId copy]
                                    repeats:NO];
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{@"type": @"enable", @"message": @"don't enable blueTooth"}];
         [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

// read: function (device_id, service_uuid, characteristic_uuid, success, failure) {
- (void)read:(CDVInvokedUrlCommand *)command {
    NSLog(@"read");

    HeytzBLECommandContext *context = [self getData:command prop:CBCharacteristicPropertyRead];
    if (context) {

        CBPeripheral *peripheral = [context peripheral];
        CBCharacteristic *characteristic = [context characteristic];

        NSString *key = [self keyForPeripheral:peripheral andCharacteristic:characteristic];
        [readCallbacks setObject:[command.callbackId copy] forKey:key];

        [peripheral readValueForCharacteristic:characteristic];  // callback sends value
    }

}

// write: function (device_id, service_uuid, characteristic_uuid, value, success, failure) {
- (void)write:(CDVInvokedUrlCommand *)command {

    HeytzBLECommandContext *context = [self getData:command prop:CBCharacteristicPropertyWrite];
    NSData *message = [command.arguments objectAtIndex:3]; // This is binary
    if (context) {

        if (message != nil) {

            CBPeripheral *peripheral = [context peripheral];
            CBCharacteristic *characteristic = [context characteristic];

            NSString *key = [self keyForPeripheral:peripheral andCharacteristic:characteristic];
            [writeCallbacks setObject:[command.callbackId copy] forKey:key];

            // TODO need to check the max length
            [peripheral writeValue:message forCharacteristic:characteristic type:CBCharacteristicWriteWithResponse];

            // response is sent from didWriteValueForCharacteristic

        } else {
            CDVPluginResult *pluginResult = nil;
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"message was null"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }

}

// writeWithoutResponse: function (device_id, service_uuid, characteristic_uuid, value, success, failure) {
- (void)writeWithoutResponse:(CDVInvokedUrlCommand *)command {
    NSLog(@"writeWithoutResponse");

    HeytzBLECommandContext *context = [self getData:command prop:CBCharacteristicPropertyWriteWithoutResponse];
    NSData *message = [command.arguments objectAtIndex:3]; // This is binary

    if (context) {
        CDVPluginResult *pluginResult = nil;
        if (message != nil) {
            CBPeripheral *peripheral = [context peripheral];
            CBCharacteristic *characteristic = [context characteristic];

            // TODO need to check the max length
            [peripheral writeValue:message forCharacteristic:characteristic type:CBCharacteristicWriteWithoutResponse];

            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"message was null"];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

// success callback is called on notification
// notify: function (device_id, service_uuid, characteristic_uuid, success, failure) {
- (void)startNotification:(CDVInvokedUrlCommand *)command {
    NSLog(@"registering for notification");

    HeytzBLECommandContext *context = [self getData:command prop:CBCharacteristicPropertyNotify]; // TODO name this better

    if (context) {
        CBPeripheral *peripheral = [context peripheral];
        CBCharacteristic *characteristic = [context characteristic];

        NSString *key = [self keyForPeripheral:peripheral andCharacteristic:characteristic];
        NSString *callback = [command.callbackId copy];
        [notificationCallbacks setObject:callback forKey:key];

        [peripheral setNotifyValue:YES forCharacteristic:characteristic];

    }

}

// stopNotification: function (device_id, service_uuid, characteristic_uuid, success, failure) {
- (void)stopNotification:(CDVInvokedUrlCommand *)command {
    NSLog(@"registering for notification");

    HeytzBLECommandContext *context = [self getData:command prop:CBCharacteristicPropertyNotify]; // TODO name this better

    if (context) {
        CBPeripheral *peripheral = [context peripheral];
        CBCharacteristic *characteristic = [context characteristic];

        NSString *key = [self keyForPeripheral:peripheral andCharacteristic:characteristic];
        NSString *callback = [command.callbackId copy];
        [stopNotificationCallbacks setObject:callback forKey:key];

        [peripheral setNotifyValue:NO forCharacteristic:characteristic];
        // callback sent from peripheral:didUpdateNotificationStateForCharacteristic:error:

    }

}

- (void)isEnabled:(CDVInvokedUrlCommand *)command {

    CDVPluginResult *pluginResult = nil;
    int bluetoothState = [manager state];

    BOOL enabled = bluetoothState == CBCentralManagerStatePoweredOn;

    if (enabled) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsInt:bluetoothState];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)scan:(CDVInvokedUrlCommand *)command {

    NSLog(@"scan");
    discoverPeripherialCallbackId = [command.callbackId copy];

    NSArray *serviceUUIDStrings = [command.arguments objectAtIndex:0];
    NSNumber *timeoutSeconds = [command.arguments objectAtIndex:1];
    NSMutableArray *serviceUUIDs = [NSMutableArray new];

    for (int i = 0; i < [serviceUUIDStrings count]; i++) {
        CBUUID *serviceUUID = [CBUUID UUIDWithString:[serviceUUIDStrings objectAtIndex:i]];
        [serviceUUIDs addObject:serviceUUID];
    }

    [manager scanForPeripheralsWithServices:serviceUUIDs options:nil];

   [NSTimer scheduledTimerWithTimeInterval:[timeoutSeconds floatValue]
                                     target:self
                                   selector:@selector(stopScanTimer:)
                                   userInfo:[command.callbackId copy]
                                    repeats:NO];

}

- (void)startScan:(CDVInvokedUrlCommand *)command {

    NSLog(@"startScan");
    discoverPeripherialCallbackId = [command.callbackId copy];
    NSArray *serviceUUIDStrings = [command.arguments objectAtIndex:0];
    NSMutableArray *serviceUUIDs = [NSMutableArray new];

    for (int i = 0; i < [serviceUUIDStrings count]; i++) {
        CBUUID *serviceUUID = [CBUUID UUIDWithString:[serviceUUIDStrings objectAtIndex:i]];
        [serviceUUIDs addObject:serviceUUID];
    }

    [manager scanForPeripheralsWithServices:serviceUUIDs options:nil];

}

- (void)stopScan:(CDVInvokedUrlCommand *)command {

    NSLog(@"stopScan");

    [manager stopScan];

    if (discoverPeripherialCallbackId) {
        discoverPeripherialCallbackId = nil;
    }

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}


- (void)isConnected:(CDVInvokedUrlCommand *)command {

    CDVPluginResult *pluginResult = nil;
    CBPeripheral *peripheral = [self findPeripheralByUUID:[command.arguments objectAtIndex:0]];

    if (peripheral && peripheral.state == CBPeripheralStateConnected) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Not connected"];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)showBluetoothSettings:(CDVInvokedUrlCommand *)command {
    NSString *urlStr = @"App-Prefs:root=Bluetooth";
    if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:urlStr]]) {
        if (iOS10) {
            //iOS10.0以上  使用的操作
            [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlStr] options:@{} completionHandler:nil];
        } else {
            //iOS10.0以下  使用的操作
            [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlStr]];
        }
    }
}

#pragma mark - timers

- (void)stopScanTimer:(NSTimer *)timer {
    NSLog(@"stopScanTimer");

    [manager stopScan];

    if (discoverPeripherialCallbackId) {
        discoverPeripherialCallbackId = nil;
    }
    if (autoConnectCallbackId) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{@"type": @"scan", @"message": @"time out"}];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:autoConnectCallbackId];
        NSLog(@"autoConnect, time out");
    }
}

#pragma mark - CBCentralManagerDelegate

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {

    [peripherals addObject:peripheral];
    [peripheral setAdvertisementData:advertisementData RSSI:RSSI];

    if (discoverPeripherialCallbackId) {
        CDVPluginResult *pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[peripheral asDictionary]];
        NSLog(@"Discovered %@", [peripheral asDictionary]);
        [pluginResult setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:discoverPeripherialCallbackId];
    }
    if (autoConnectDeviceName) {
        if ([[peripheral name] isEqualToString:autoConnectDeviceName]) {
            [manager stopScan];
            autoConnectDeviceName = nil;
            autoConnectPeripheral = peripheral;
            [manager connectPeripheral:peripheral options:nil];
        }
    }
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    NSLog(@"Status of CoreBluetooth central manager changed %ld %@", (long) central.state, [self centralManagerStateToString:central.state]);

    if (central.state == CBCentralManagerStateUnsupported) {
        NSLog(@"=============================================================");
        NSLog(@"WARNING: This hardware does not support Bluetooth Low Energy.");
        NSLog(@"=============================================================");
    }
}

/**
 * 设备连接通知中心
 * @param central
 * @param peripheral
 */
- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {

    NSLog(@"didConnectPeripheral");

    peripheral.delegate = self;

    // NOTE: it's inefficient to discover all services
    [peripheral discoverServices:nil];

    // NOTE: not calling connect success until characteristics are discovered
}

/**
 * 当外围设备的连接被断开时
 * @param central
 * @param peripheral
 * @param error
 */
- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {

    NSLog(@"didDisconnectPeripheral");

    NSString *connectCallbackId = [connectCallbacks valueForKey:[peripheral uuidAsString]];
    [connectCallbacks removeObjectForKey:[peripheral uuidAsString]];

    if (connectCallbackId) {
        CDVPluginResult *pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[peripheral asDictionary]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:connectCallbackId];
    }
    if (autoConnectPeripheral && autoConnectCallbackId && [[autoConnectPeripheral uuidAsString] isEqualToString:[peripheral uuidAsString]]) {
        autoConnectCallbackId=nil;
        CDVPluginResult *pluginResult =  [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{@"type": @"connect", @"message": @"connect error"}];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:autoConnectCallbackId];
    }
}

/**
 * 连接失败
 * @param central
 * @param peripheral
 * @param error
 */
- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {

    NSLog(@"didFailToConnectPeripheral");

    NSString *connectCallbackId = [connectCallbacks valueForKey:[peripheral uuidAsString]];
    [connectCallbacks removeObjectForKey:[peripheral uuidAsString]];

    CDVPluginResult *pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[peripheral asDictionary]];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:connectCallbackId];

    if (autoConnectPeripheral && autoConnectCallbackId && [[autoConnectPeripheral uuidAsString] isEqualToString:[peripheral uuidAsString]]) {
        CDVPluginResult *pluginResult =  [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{@"type": @"connect", @"message": @"connect error"}];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:autoConnectCallbackId];
        autoConnectCallbackId = nil;
        autoConnectDeviceName = nil;
        autoConnectPeripheral = nil;
        if([scenTimer isValid]){
            [scenTimer invalidate];
        }
    }
}

#pragma mark CBPeripheralDelegate

/**
 * 发现外设的可用服务时
 * @param peripheral
 * @param error
 */
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {

    NSLog(@"didDiscoverServices");

    // save the services to tell when all characteristics have been discovered
    NSMutableSet *servicesForPeriperal = [NSMutableSet new];
    [servicesForPeriperal addObjectsFromArray:peripheral.services];
    [connectCallbackLatches setObject:servicesForPeriperal forKey:[peripheral uuidAsString]];
    [autoConnectCallbackLatches setObject:servicesForPeriperal forKey:[peripheral uuidAsString]];

    for (CBService *service in peripheral.services) {
        [peripheral discoverCharacteristics:nil forService:service]; // discover all is slow
    }
}

/**
 * 发现指定服务的特征
 * @param peripheral
 * @param service
 * @param error
 */
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {

    NSLog(@"didDiscoverCharacteristicsForService");

    NSString *peripheralUUIDString = [peripheral uuidAsString];
    NSString *connectCallbackId = [connectCallbacks valueForKey:peripheralUUIDString];
    NSMutableSet *latch = [connectCallbackLatches valueForKey:peripheralUUIDString];

    [latch removeObject:service];

    if ([latch count] == 0) {
        // Call success callback for connect
        if (connectCallbackId) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[peripheral asDictionary]];
            [pluginResult setKeepCallbackAsBool:TRUE];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:connectCallbackId];
        }
        [connectCallbackLatches removeObjectForKey:peripheralUUIDString];
    }

    NSLog(@"Found characteristics for service %@", service);
    for (CBCharacteristic *characteristic in service.characteristics) {
        NSLog(@"Characteristic %@", characteristic);
    }

    NSString *autoPeripheralUUIDString = [peripheral uuidAsString];
    if (autoConnectPeripheral && [autoPeripheralUUIDString isEqualToString:[autoConnectPeripheral uuidAsString]]) {
        NSMutableSet *autoClatch = [autoConnectCallbackLatches valueForKey:autoPeripheralUUIDString];
        [autoClatch removeObject:service];
        if ([autoClatch count] == 0) {
            if (autoConnectCallbackId) {
                if([scenTimer isValid]){
                    [scenTimer invalidate];
                }
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[peripheral asDictionary]];
                [pluginResult setKeepCallbackAsBool:TRUE];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:autoConnectCallbackId];
            }
            [autoConnectCallbackLatches removeObjectForKey:autoPeripheralUUIDString];

        }
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    NSLog(@"didUpdateValueForCharacteristic");

    NSString *key = [self keyForPeripheral:peripheral andCharacteristic:characteristic];
    NSString *notifyCallbackId = [notificationCallbacks objectForKey:key];

    if (notifyCallbackId) {
        NSData *data = characteristic.value; // send RAW data to Javascript

        CDVPluginResult *pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArrayBuffer:data];
        [pluginResult setKeepCallbackAsBool:TRUE]; // keep for notification
        [self.commandDelegate sendPluginResult:pluginResult callbackId:notifyCallbackId];
    }

    NSString *readCallbackId = [readCallbacks objectForKey:key];

    if (readCallbackId) {
        NSData *data = characteristic.value; // send RAW data to Javascript

        CDVPluginResult *pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArrayBuffer:data];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:readCallbackId];

        [readCallbacks removeObjectForKey:key];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateNotificationStateForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {

    NSString *key = [self keyForPeripheral:peripheral andCharacteristic:characteristic];
    NSString *notificationCallbackId = [notificationCallbacks objectForKey:key];
    NSString *stopNotificationCallbackId = [stopNotificationCallbacks objectForKey:key];

    CDVPluginResult *pluginResult = nil;

    // we always call the stopNotificationCallbackId if we have a callback
    // we only call the notificationCallbackId on errors and if there is no stopNotificationCallbackId

    if (stopNotificationCallbackId) {

        if (error) {
            NSLog(@"%@", error);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:stopNotificationCallbackId];
        [stopNotificationCallbacks removeObjectForKey:key];
        [notificationCallbacks removeObjectForKey:key];

    } else if (notificationCallbackId && error) {

        NSLog(@"%@", error);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:notificationCallbackId];
    }

}


- (void)peripheral:(CBPeripheral *)peripheral didWriteValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    // This is the callback for write

    NSString *key = [self keyForPeripheral:peripheral andCharacteristic:characteristic];
    NSString *writeCallbackId = [writeCallbacks objectForKey:key];

    if (writeCallbackId) {
        CDVPluginResult *pluginResult = nil;
        if (error) {
            NSLog(@"%@", error);
            pluginResult = [CDVPluginResult
                    resultWithStatus:CDVCommandStatus_ERROR
                     messageAsString:[error localizedDescription]
            ];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:writeCallbackId];
        [writeCallbacks removeObjectForKey:key];
    }

}

#pragma mark - internal implemetation

- (CBPeripheral *)findPeripheralByUUID:(NSString *)uuid {

    CBPeripheral *peripheral = nil;

    for (CBPeripheral *p in peripherals) {

        NSString *other = p.identifier.UUIDString;

        if ([uuid isEqualToString:other]) {
            peripheral = p;
            break;
        }
    }
    return peripheral;
}

// RedBearLab
- (CBService *)findServiceFromUUID:(CBUUID *)UUID p:(CBPeripheral *)p {
    for (int i = 0; i < p.services.count; i++) {
        CBService *s = [p.services objectAtIndex:i];
        if ([self compareCBUUID:s.UUID UUID2:UUID])
            return s;
    }

    return nil; //Service not found on this peripheral
}

// RedBearLab
- (CBCharacteristic *)findCharacteristicFromUUID:(CBUUID *)UUID service:(CBService *)service prop:(CBCharacteristicProperties)prop {
    NSLog(@"Looking for %@", UUID);
    for (int i = 0; i < service.characteristics.count; i++) {
        CBCharacteristic *c = [service.characteristics objectAtIndex:i];
        if ((c.properties & prop) != 0x0 && [self compareCBUUID:c.UUID UUID2:UUID]) {
            return c;
        }
    }
    return nil; //Characteristic not found on this service
}

// RedBearLab
- (int)compareCBUUID:(CBUUID *)UUID1 UUID2:(CBUUID *)UUID2 {
    char b1[16];
    char b2[16];
    [UUID1.data getBytes:b1];
    [UUID2.data getBytes:b2];

    if (memcmp(b1, b2, UUID1.data.length) == 0)
        return 1;
    else
        return 0;
}

// expecting deviceUUID, serviceUUID, characteristicUUID in command.arguments
- (HeytzBLECommandContext *)getData:(CDVInvokedUrlCommand *)command prop:(CBCharacteristicProperties)prop {
    NSLog(@"getData");

    CDVPluginResult *pluginResult = nil;

    NSString *deviceUUIDString = [command.arguments objectAtIndex:0];
    NSString *serviceUUIDString = [command.arguments objectAtIndex:1];
    NSString *characteristicUUIDString = [command.arguments objectAtIndex:2];

    CBUUID *serviceUUID = [CBUUID UUIDWithString:serviceUUIDString];
    CBUUID *characteristicUUID = [CBUUID UUIDWithString:characteristicUUIDString];

    CBPeripheral *peripheral = [self findPeripheralByUUID:deviceUUIDString];

    if (!peripheral) {

        NSLog(@"Could not find peripherial with UUID %@", deviceUUIDString);

        NSString *errorMessage = [NSString stringWithFormat:@"Could not find peripherial with UUID %@", deviceUUIDString];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        return nil;
    }

    CBService *service = [self findServiceFromUUID:serviceUUID p:peripheral];

    if (!service) {
        NSLog(@"Could not find service with UUID %@ on peripheral with UUID %@",
                serviceUUIDString,
                peripheral.identifier.UUIDString);


        NSString *errorMessage = [NSString stringWithFormat:@"Could not find service with UUID %@ on peripheral with UUID %@",
                                                            serviceUUIDString,
                                                            peripheral.identifier.UUIDString];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        return nil;
    }

    CBCharacteristic *characteristic = [self findCharacteristicFromUUID:characteristicUUID service:service prop:prop];

    // Special handling for INDICATE. If charateristic with notify is not found, check for indicate.
    if (prop == CBCharacteristicPropertyNotify && !characteristic) {
        characteristic = [self findCharacteristicFromUUID:characteristicUUID service:service prop:CBCharacteristicPropertyIndicate];
    }

    if (!characteristic) {
        // NOTE: the characteristic might exist, but not have the right property
        NSLog(@"Could not find characteristic with UUID %@ on service with UUID %@ on peripheral with UUID %@",
                characteristicUUIDString,
                serviceUUIDString,
                peripheral.identifier.UUIDString);

        NSString *errorMessage = [NSString stringWithFormat:
                @"Could not find characteristic with UUID %@ on service with UUID %@ on peripheral with UUID %@",
                characteristicUUIDString,
                serviceUUIDString,
                peripheral.identifier.UUIDString];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        return nil;
    }

    HeytzBLECommandContext *context = [[HeytzBLECommandContext alloc] init];
    [context setPeripheral:peripheral];
    [context setService:service];
    [context setCharacteristic:characteristic];
    return context;

}

- (NSString *)keyForPeripheral:(CBPeripheral *)peripheral andCharacteristic:(CBCharacteristic *)characteristic {
    return [NSString stringWithFormat:@"%@|%@", [peripheral uuidAsString], [characteristic UUID]];
}

#pragma mark - util

- (NSString *)centralManagerStateToString:(int)state {
    switch (state) {
        case CBCentralManagerStateUnknown:
            return @"State unknown (CBCentralManagerStateUnknown)";
        case CBCentralManagerStateResetting:
            return @"State resetting (CBCentralManagerStateUnknown)";
        case CBCentralManagerStateUnsupported:
            return @"State BLE unsupported (CBCentralManagerStateResetting)";
        case CBCentralManagerStateUnauthorized:
            return @"State unauthorized (CBCentralManagerStateUnauthorized)";
        case CBCentralManagerStatePoweredOff:
            return @"State BLE powered off (CBCentralManagerStatePoweredOff)";
        case CBCentralManagerStatePoweredOn:
            return @"State powered up and ready (CBCentralManagerStatePoweredOn)";
        default:
            return @"State unknown";
    }

    return @"Unknown state";
}

@end
