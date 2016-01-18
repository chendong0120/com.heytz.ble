var exec = require('cordova/exec');

module.exports = {
    startScan: function (success, error) {
        exec(success, error, "HeytzBle", "startScan", []);
    },
    stopScan: function (success, error) {
        exec(success, error, "HeytzBle", "stopScan", []);
    }
};