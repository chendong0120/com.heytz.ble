var exec = require('cordova/exec');
var stringToArrayBuffer = function (str) {
    var ret = new Uint8Array(str.length);
    for (var i = 0; i < str.length; i++) {
        ret[i] = str.charCodeAt(i);
    }
    return ret.buffer;
};

module.exports = {
    scan: function (success, error) {
        //todo 需要修改
        //exec(success, error, "HeytzBle", "scan", [services, seconds]);
        exec(success, error, "HeytzBle", "scan", []);
    },
    startScan: function (uuid, scanSeconds, success, error) {
        if (uuid === null) uuid = [];
        if (scanSeconds === null) scanSeconds = 0;
        //todo
        //exec(success, error, "HeytzBle", "startScan", [scanSeconds]);
        exec(success, error, "HeytzBle", "startScan", [uuid, scanSeconds]);
    },
    stopScan: function (success, error) {
        exec(success, error, "HeytzBle", "stopScan", []);
    },
    isEnabled: function (success, error) {
        exec(success, error, "HeytzBle", "isEnabled", []);
    },
    connect: function (device_id, success, error) {
        exec(success, error, 'HeytzBle', 'connect', [device_id]);
    },
    disconnect: function (device_id, success, error) {
        exec(success, error, 'HeytzBle', 'disconnect', [device_id]);
    },
    startNotification: function (device_id, serverUUID, characteristicUUID, success, error) {
        exec(success, error, 'HeytzBle', 'startNotification', [device_id, serverUUID, characteristicUUID]);
    },
    stopNotification: function (device_id, serverUUID, characteristicUUID, success, error) {
        exec(success, error, 'HeytzBle', 'stopNotification', [device_id, serverUUID, characteristicUUID]);
    },
    write: function (device_id, serverUUID, characteristicUUID, value, success, error) {
        // convert to ArrayBuffer
        if (typeof value === 'string') {
            value = stringToArrayBuffer(value);
        } else if (value instanceof Array) {
            // assuming array of interger
            value = new Uint8Array(value).buffer;
        } else if (value instanceof Uint8Array) {
            value = value.buffer;
        }

        exec(success, error, 'HeytzBle', 'write', [device_id, serverUUID, characteristicUUID, value]);
    },

//************************only ios***********************************
    // this will probably be removed
    list: function (success, failure) {
        exec(success, failure, 'HeytzBle', 'list', []);
    },
    // characteristic value comes back as ArrayBuffer in the success callback
    read: function (device_id, service_uuid, characteristic_uuid, success, failure) {
        cordova.exec(success, failure, 'HeytzBle', 'read', [device_id, service_uuid, characteristic_uuid]);
    },
    // value must be an ArrayBuffer
    writeWithoutResponse: function (device_id, service_uuid, characteristic_uuid, value, success, failure) {
        exec(success, failure, 'HeytzBle', 'writeWithoutResponse', [device_id, service_uuid, characteristic_uuid, value]);
    },

    // value must be an ArrayBuffer
    writeCommand: function (device_id, service_uuid, characteristic_uuid, value, success, failure) {
        console.log("WARNING: writeCommand is deprecated, use writeWithoutResponse");
        exec(success, failure, 'HeytzBle', 'writeWithoutResponse', [device_id, service_uuid, characteristic_uuid, value]);
    },

    // success callback is called on notification
    notify: function (device_id, service_uuid, characteristic_uuid, success, failure) {
        console.log("WARNING: notify is deprecated, use startNotification");
        exec(success, failure, 'HeytzBle', 'startNotification', [device_id, service_uuid, characteristic_uuid]);
    },
    isConnected: function (device_id, success, failure) {
        exec(success, failure, 'HeytzBle', 'isConnected', [device_id]);
    },
    enable: function (success, failure) {
        exec(success, failure, "HeytzBle", "enable", []);
    },

    showBluetoothSettings: function (success, failure) {
        exec(success, failure, "BLE", "showBluetoothSettings", []);
    }
};