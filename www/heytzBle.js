var exec = require('cordova/exec');
var stringToArrayBuffer = function (str) {
  var ret = new Uint8Array(str.length);
  for (var i = 0; i < str.length; i++) {
    ret[i] = str.charCodeAt(i);
  }
  return ret.buffer;
};

module.exports = {
  init: function (success, error) {
    exec(success, error, "HeytzBle", "init", []);
  }, scan: function (services, seconds, success, error) {
    if (services === null) services = [];
    if (seconds === null) seconds = 0;
    exec(success, error, "HeytzBle", "scan", [services, seconds]);
  },
  startScan: function (services, success, error) {
    if (services === null) services = [];
    exec(success, error, "HeytzBle", "startScan", [services]);
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
    } else if (value instanceof Uint32Array) {
      value = value.buffer;
    }
    exec(success, error, 'HeytzBle', 'write', [device_id, serverUUID, characteristicUUID, value]);
  },
  isConnected: function (device_id, success, failure) {
    exec(success, failure, 'HeytzBle', 'isConnected', [device_id]);
  },
  showBluetoothSettings: function (success, failure) {
    exec(success, failure, "HeytzBle", "showBluetoothSettings", []);
  },
  //************************only ios***********************************
  // this will probably be removed
  list: function (success, failure) {
    exec(success, failure, 'HeytzBle', 'list', []);
  },
  // characteristic value comes back as ArrayBuffer in the success callback
  read: function (device_id, service_uuid, characteristic_uuid, success, failure) {
    exec(success, failure, 'HeytzBle', 'read', [device_id, service_uuid, characteristic_uuid]);
  },
  // value must be an ArrayBuffer
  writeWithoutResponse: function (device_id, service_uuid, characteristic_uuid, value, success, failure) {
    exec(success, failure, 'HeytzBle', 'writeWithoutResponse', [device_id, service_uuid, characteristic_uuid, value]);
  },
  //************************Only Android***********************************
  enable: function (success, failure) {
    exec(success, failure, "HeytzBle", "enable", []);
  }
};

function onDeviceReady() {
  if (device.platform == "Android") {
    HeytzBle.init(function () {
      console.log("===HeytzBle init success====");
    });
  }
}
document.addEventListener("deviceready", onDeviceReady, false);
