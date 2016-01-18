var exec = require('cordova/exec');

module.exports = {
    startScan: function (success, error) {
        exec(success, error, "HeytzBle", "startScan", []);
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
    startNotification: function (device_id, serverUUID, characteristicUUID, success, error) {
        exec(success, error, 'HeytzBle', 'startNotification', [device_id, serverUUID, characteristicUUID]);
    },
};