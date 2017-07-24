//
//  HeytzBleCentralPlugin.h
//  M-box
//
//  Created by 陈东东 on 16/1/19.
//
//

#ifndef HeytzBleCentralPlugin_h
#define HeytzBleCentralPlugin_h


#import <Cordova/CDV.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "HeytzBLECommandContext.h"
#import "HeytzCBPeripheral+Extensions.h"

@interface HeytzBleCentralPlugin : CDVPlugin <CBCentralManagerDelegate, CBPeripheralDelegate> {
    NSString *discoverPeripherialCallbackId;
    NSString *autoConnectCallbackId;
    NSString *autoConnectDeviceName;
    CBPeripheral *autoConnectPeripheral;
    NSMutableDictionary *connectCallbacks;
    NSMutableDictionary *readCallbacks;
    NSMutableDictionary *writeCallbacks;
    NSMutableDictionary *notificationCallbacks;
    NSMutableDictionary *stopNotificationCallbacks;
    NSMutableDictionary *connectCallbackLatches;
    NSMutableDictionary *autoConnectCallbackLatches;
    NSTimer     *scenTimer;

}

@property(strong, nonatomic) NSMutableSet *peripherals;
@property(strong, nonatomic) CBCentralManager *manager;

- (void)scan:(CDVInvokedUrlCommand *)command;

- (void)startScan:(CDVInvokedUrlCommand *)command;

- (void)stopScan:(CDVInvokedUrlCommand *)command;

- (void)connect:(CDVInvokedUrlCommand *)command;

- (void)disconnect:(CDVInvokedUrlCommand *)command;

- (void)autoConnect:(CDVInvokedUrlCommand *)command;

- (void)read:(CDVInvokedUrlCommand *)command;

- (void)write:(CDVInvokedUrlCommand *)command;

- (void)writeWithoutResponse:(CDVInvokedUrlCommand *)command;

- (void)startNotification:(CDVInvokedUrlCommand *)command;

- (void)stopNotification:(CDVInvokedUrlCommand *)command;

- (void)isEnabled:(CDVInvokedUrlCommand *)command;

- (void)isConnected:(CDVInvokedUrlCommand *)command;

@end

#endif
