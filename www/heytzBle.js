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
        exec(success, error, "HeytzBle", "scan", []);
    },
    startScan: function (uuid, scanSeconds, success, error) {
        if (uuid === null) uuid = [];
        if (scanSeconds === null) scanSeconds = 0;
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
};