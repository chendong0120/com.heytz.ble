var exec = require('cordova/exec');

module.exports = {
    startScan: function (success, error) {
        exec(success, error, "heytzBle", "startScan", []);
    },
    stopScan: function (success, error) {
        exec(success, error, "heytzBle", "stopScan", []);
    }
};