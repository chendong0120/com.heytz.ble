# com.heytz.ble
黑子信息科技ble控制

###IOS

    加入  BLUETOOTH_USAGE_DESCRIPTION 默认 " "
    
    使用 cordova plugin add com.heytz.ble  --variable BLUETOOTH_USAGE_DESCRIPTION="you usage message" 
    
# 更新日志
   
    17.07.10 

        1.取消Android需要配置AndroidManifest.xml application


# API

## Methods

- [HeytzBle.scan](#scan)
- [HeytzBle.startScan](#startScan)
- [HeytzBle.stopScan](#stopScan)
- [HeytzBle.connect](#connect)
- [HeytzBle.disconnect](#disconnect)
- [HeytzBle.write](#write)
- [HeytzBle.isEnabled](#isenabled)
- [HeytzBle.startNotification](#startNotification)
- [HeytzBle.stopNotification](#stopNotification)
- [HeytzBle.isConnected](#isconnected)
- [HeytzBle.read](#read)
- [HeytzBle.writeWithoutResponse](#writewithoutresponse)(IOS)
- [HeytzBle.showBluetoothSettings](#showbluetoothsettings)
- [HeytzBle.enable](#enable)(Android)

## scan
Scan and discover BLE peripherals.

    HeytzBle.scan(services, seconds, success, failure);

### Description

Function `scan` scans for BLE devices.  The success callback is called each time a peripheral is discovered. Scanning automatically stops after the specified number of seconds.

ios
    {
        "name": "TI SensorTag",
        "id": "BD922605-1B07-4D55-8D09-B66653E51BBA",
        "rssi": -79,
        "advertising": /* ArrayBuffer or map */
    }
android
    {
        "name": "TI SensorTag",
        "id": "B6:66:53:E5:1B:BA"
    }
Advertising information format varies depending on your platform. See [Advertising Data](#advertising-data) for more information.

### Parameters

- __services__: List of services to discover, or [] to find all devices
- __seconds__: Number of seconds to run discovery
- __success__: Success callback function that is invoked which each discovered device.
- __failure__: Error callback function, invoked when error occurs. [optional]

### Quick Example

    HeytzBle.scan([], 5, function(device) {
        console.log(JSON.stringify(device));
    }, failure);

## startScan

Scan and discover BLE peripherals.

    HeytzBle.startScan(services, success, failure);

### Description

Function `startScan` scans for BLE devices.  The success callback is called each time a peripheral is discovered. Scanning will continue until `stopScan` is called.
ios
    {
        "name": "TI SensorTag",
        "id": "BD922605-1B07-4D55-8D09-B66653E51BBA",
        "rssi": -79,
        "advertising": /* ArrayBuffer or map */
    }
android
    {
        "name": "TI SensorTag",
        "id": "BD922605-1B07-4D55-8D09-B66653E51BBA"
    }
Advertising information format varies depending on your platform. See [Advertising Data](#advertising-data) for more information.

### Parameters

- __services__: List of services to discover, or [] to find all devices
- __success__: Success callback function that is invoked which each discovered device.
- __failure__: Error callback function, invoked when error occurs. [optional]

### Quick Example

    HeytzBle.startScan([], function(device) {
        console.log(JSON.stringify(device));
    }, failure);

    setTimeout(HeytzBle.stopScan,
        5000,
        function() { console.log("Scan complete"); },
        function() { console.log("stopScan failed"); }
    );

## stopScan

Stop scanning for BLE peripherals.

    HeytzBle.stopScan(success, failure);

### Description

Function `stopScan` stops scanning for BLE devices.

### Parameters

- __success__: Success callback function, invoked when scanning is stopped. [optional]
- __failure__: Error callback function, invoked when error occurs. [optional]

### Quick Example

    HeytzBle.startScan([], function(device) {
        console.log(JSON.stringify(device));
    }, failure);

    setTimeout(HeytzBle.stopScan,
        5000,
        function() { console.log("Scan complete"); },
        function() { console.log("stopScan failed"); }
    );

    /* Alternate syntax
    setTimeout(function() {
        HeytzBle.stopScan(
            function() { console.log("Scan complete"); },
            function() { console.log("stopScan failed"); }
        );
    }, 5000);
    */

## connect

Connect to a peripheral.

    HeytzBle.connect(device_id, connectSuccess, connectFailure);

### Description

Function `connect` connects to a BLE peripheral. The callback is long running. Success will be called when the connection is successful. Service and characteristic info will be passed to the success callback in the [peripheral object](#peripheral-data). Failure is called if the connection fails, or later if the peripheral disconnects. An peripheral object is passed to the failure callback.

### Parameters

- __device_id__: UUID or MAC address of the peripheral
- __connectSuccess__: Success callback function that is invoked when the connection is successful.
- __connectFailure__: Error callback function, invoked when error occurs or the connection disconnects.

## disconnect

Disconnect.

    HeytzBle.disconnect(device_id, [success], [failure]);

### Description

Function `disconnect` disconnects the selected device.

### Parameters

- __device_id__: UUID or MAC address of the peripheral
- __success__: Success callback function that is invoked when the connection is successful. [optional]
- __failure__: Error callback function, invoked when error occurs. [optional]

## write

Writes data to a characteristic.

    HeytzBle.write(device_id, service_uuid, characteristic_uuid, value, success, failure);

### Description

Function `write` writes data to a characteristic.

### Parameters
- __device_id__: UUID or MAC address of the peripheral
- __service_uuid__: UUID of the BLE service
- __characteristic_uuid__: UUID of the BLE characteristic
- __data__: binary data, use an [ArrayBuffer](#typed-arrays)
- __success__: Success callback function that is invoked when the connection is successful. [optional]
- __failure__: Error callback function, invoked when error occurs. [optional]

### Quick Example

Use an [ArrayBuffer](#typed-arrays) when writing data.

    // send 1 byte to switch a light on
    var data = new Uint8Array(1);
    data[0] = 1;
    HeytzBle.write(device_id, "FF10", "FF11", data.buffer, success, failure);

    // send a 3 byte value with RGB color
    var data = new Uint8Array(3);
    data[0] = 0xFF;  // red
    data[0] = 0x00; // green
    data[0] = 0xFF; // blue
    HeytzBle.write(device_id, "ccc0", "ccc1", data.buffer, success, failure);

    // send a 32 bit integer
    var data = new Uint32Array(1);
    data[0] = counterInput.value;
    HeytzBle.write(device_id, SERVICE, CHARACTERISTIC, data.buffer, success, failure);

    // send a array
    var data = [];
    data[0] = 162;
    HeytzBle.write(device_id, SERVICE, CHARACTERISTIC, data, success, failure);


## startNotification

Register to be notified when the value of a characteristic changes.

    HeytzBle.startNotification(device_id, service_uuid, characteristic_uuid, success, failure);

### Description

Function `startNotification` registers a callback that is called *every time* the value of a characteristic changes. This method handles both `notifications` and `indications`. The success callback is called multiple times.

Raw data is passed from native code to the success callback as an [ArrayBuffer](#typed-arrays).

### Parameters

- __device_id__: UUID or MAC address of the peripheral
- __service_uuid__: UUID of the BLE service
- __characteristic_uuid__: UUID of the BLE characteristic
- __success__: Success callback function invoked every time a notification occurs
- __failure__: Error callback function, invoked when error occurs. [optional]

### Quick Example

    var onData = function(buffer) {
        // Decode the ArrayBuffer into a typed Array based on the data you expect
        var data = new Uint8Array(buffer);
        alert("Button state changed to " + data[0]);
    }

    HeytzBle.startNotification(device_id, "FFE0", "FFE1", onData, failure);


## stopNotification

Stop being notified when the value of a characteristic changes.

HeytzBle.stopNotification(device_id, service_uuid, characteristic_uuid, success, failure);

### Description

Function `stopNotification` stops a previously registered notification callback.

### Parameters

- __device_id__: UUID or MAC address of the peripheral
- __service_uuid__: UUID of the BLE service
- __characteristic_uuid__: UUID of the BLE characteristic
- __success__: Success callback function that is invoked when the notification is removed. [optional]
- __failure__: Error callback function, invoked when error occurs. [optional]


## isConnected

Reports the connection status.

    HeytzBle.isConnected(device_id, success, failure);

### Description

Function `isConnected` calls the success callback when the peripheral is connected and the failure callback when *not* connected.

### Parameters

- __device_id__: UUID or MAC address of the peripheral
- __success__: Success callback function that is invoked with a boolean for connected status.
- __failure__: Error callback function, invoked when error occurs. [optional]

### Quick Example

    HeytzBle.isConnected(
        'FFCA0B09-CB1D-4DC0-A1EF-31AFD3EDFB53',
        function() {
            console.log("Peripheral is connected");
        },
        function() {
            console.log("Peripheral is *not* connected");
        }
    );


## read

Reads the value of a characteristic.

    ble.read(device_id, service_uuid, characteristic_uuid, success, failure);

### Description

Function `read` reads the value of the characteristic.

Raw data is passed from native code to the callback as an [ArrayBuffer](#typed-arrays).

### Parameters

- __device_id__: UUID or MAC address of the peripheral
- __service_uuid__: UUID of the BLE service
- __characteristic_uuid__: UUID of the BLE characteristic
- __success__: Success callback function that is invoked when the connection is successful. [optional]
- __failure__: Error callback function, invoked when error occurs. [optional]



## showBluetoothSettings

Show the Bluetooth settings on the device.

    HeytzBle.showBluetoothSettings(success, failure);

### Description

Function `showBluetoothSettings` opens the Bluetooth settings for the operating systems.




## enable

Enable Bluetooth on the device.

    HeytzBle.enable(success, failure);

### Description

Function `enable` prompts the user to enable Bluetooth.

#### Android

`enable` is only supported on Android and does not work on iOS.

If `enable` is called when Bluetooth is already enabled, the user will not prompted and the success callback will be invoked.

### Parameters

- __success__: Success callback function, invoked if the user enabled Bluetooth.
- __failure__: Error callback function, invoked if the user does not enabled Bluetooth.

### Quick Example

    HeytzBle.enable(
        function() {
            console.log("Bluetooth is enabled");
        },
        function() {
            console.log("The user did *not* enable Bluetooth");
        }
    );

# Other Bluetooth Plugins

 * [BluetoothSerial](https://github.com/don/BluetoothSerial) - Connect to Arduino and other devices. Bluetooth Classic on Android, BLE on iOS.
 * [RFduino](https://github.com/don/cordova-plugin-rfduino) - RFduino specific plugin for iOS and Android.
 * [BluetoothLE](https://github.com/randdusing/BluetoothLE) - Rand Dusing's BLE plugin for Cordova
 * [PhoneGap Bluetooth Plugin](https://github.com/tanelih/phonegap-bluetooth-plugin) - Bluetooth classic pairing and connecting for Android
 * [cordova-plugin-ble-central](https://github.com/don/cordova-plugin-ble-central) -