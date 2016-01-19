//
//  HeytzBLECommandContext.h
//  M-box
//
//  Created by 陈东东 on 16/1/19.
//
//


#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface HeytzBLECommandContext : NSObject

@property CBPeripheral *peripheral;
@property CBService *service;
@property CBCharacteristic *characteristic;

@end
