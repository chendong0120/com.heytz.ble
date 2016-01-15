var exec = require('cordova/exec');

module.exports = {
    coolMethod: function (arg0, success, error) {
        exec(success, error, "heytzBle", "coolMethod", [arg0]);
    },
    startScan: function (success, error) {
        exec(success, error, "heytzBle", "startScan", []);
    },
    stopScan: function (success, error) {
        exec(success, error, "heytzBle", "stopScan", []);
    }
};