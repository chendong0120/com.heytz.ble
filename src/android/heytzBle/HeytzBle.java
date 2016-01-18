package com.heytz.ble;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.heytz.ble.sdk.BleGattCharacteristic;
import com.heytz.ble.sdk.BleGattService;
import com.heytz.ble.sdk.BleService;
import com.heytz.ble.sdk.IBle;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class echoes a string called from JavaScript.
 */
public class HeytzBle extends CordovaPlugin {

    // actions
    private static final String STARTSCAN = "startScan";
    private static final String STOPSCAN = "stopScan";
    private static final String IS_ENABLED = "isEnabled";
    private static final String CONNECT = "connect";
    private static final String DISCONNECT = "disconnect";
    private static final String STARTNOTIFICATION = "startNotification"; // register for characteristic notification
    private static final String WRITE = "write";

    private static final long SCAN_PERIOD = 10000;
    private static final String TAG = "HeytzBle";

    private ArrayList<ArrayList<BleGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BleGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private final String CHARACTERISTICS = "Characteristics";

    private BleService mService;
    private IBle mBle;
    private Handler mHandler;
    private BleGattCharacteristic mCharacteristic;
    private String mDeviceAddress;          //当前监听的设备
    private boolean mNotifyStarted;
    private Context context;
    private CallbackContext _callbackcontext;
    private CallbackContext rawDataAvailableCallback;

    private final BroadcastReceiver mBleReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //扫描获取设备列表
            if (BleService.BLE_NOT_SUPPORTED.equals(action)) {
                LOG.w(TAG, "Ble not support");
                //todo 不支持ble
            } else if (BleService.BLE_DEVICE_FOUND.equals(action)) {
                // device found
                Bundle extras = intent.getExtras();
                final BluetoothDevice device = extras
                        .getParcelable(BleService.EXTRA_DEVICE);
                //todo 获取到新的device;
                LOG.w(TAG, device.getName());

                PluginResult result = new PluginResult(PluginResult.Status.OK, deviceToJSONObject(device));
                result.setKeepCallback(true);
                if (_callbackcontext != null)
                    _callbackcontext.sendPluginResult(result);

            } else if (BleService.BLE_NO_BT_ADAPTER.equals(action)) {
                LOG.w(TAG, "No bluetooth adapter");
                //todo 没有蓝牙设备
            }

            if (BleService.BLE_GATT_CONNECTED.equals(action)) {
                //连接状态
            } else if (BleService.BLE_GATT_DISCONNECTED.equals(action)) {
                //设备断开连接
            } else if (BleService.BLE_SERVICE_DISCOVERED.equals(action)) {
                //加载设备的信息,各个通道的信息.
                try {
                    displayGattServices(mBle.getServices(mDeviceAddress));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

//            Bundle extras = intent.getExtras();
//            if (!mDeviceAddress.equals(extras.getString(BleService.EXTRA_ADDR))) {
//                return;
//            }
//
//            String uuid = extras.getString(BleService.EXTRA_UUID);
//            if (uuid != null
//                    && !mCharacteristic.getUuid().toString().equals(uuid)) {
//                return;
//            }
//
//
//            if (BleService.BLE_GATT_DISCONNECTED.equals(action)) {//如果蓝牙断开链接.
//                LOG.w(TAG, "Device disconnected...");
//
//            } else if (BleService.BLE_CHARACTERISTIC_READ.equals(action)
//                    || BleService.BLE_CHARACTERISTIC_CHANGED.equals(action)) {  //当蓝牙设备有消息读取或者消息改变,
//                byte[] data = extras.getByteArray(BleService.EXTRA_VALUE);
////                tv_ascii.setText(new String(val));
////                tv_hex.setText("0x" + new String(Hex.encodeHex(val)));
//                if (data != null && data.length > 0) {
//                    PluginResult result = new PluginResult(PluginResult.Status.OK, data);
//                    result.setKeepCallback(true);
//                    rawDataAvailableCallback.sendPluginResult(result);
//                }
//
//            } else if (BleService.BLE_CHARACTERISTIC_NOTIFICATION
//                    .equals(action)) {//通知的状态.
//                LOG.w(TAG, "Notification state changed!");
//                mNotifyStarted = extras.getBoolean(BleService.EXTRA_VALUE);
//                if (mNotifyStarted) {
//                    LOG.w(TAG, "Stop Notify");
//                } else {
//                    LOG.w(TAG, "Start Notify");
//                }
//            } else if (BleService.BLE_CHARACTERISTIC_INDICATION.equals(action)) {//指示状态改变
//                LOG.w(TAG, "Indication state changed!");
//            } else if (BleService.BLE_CHARACTERISTIC_WRITE.equals(action)) {//写入消息成功.
//                LOG.w(TAG, "Write success!");
//            }
        }


    };

    public JSONObject deviceToJSONObject(BluetoothDevice device) {

        JSONObject json = new JSONObject();

        try {
            json.put("id", device.getAddress()); // mac address
//            json.put("getUuids", device.getUuids());
            json.put("name", device.getName());
            json.put("address", device.getAddress()); // mac address
//            json.put("getBluetoothClass", device.getBluetoothClass());
//            json.put("getBondState", device.getBondState());
//            json.put("getType", device.getType());
//            json.put("getClass", device.getClass());
            json.put("describeContents", device.describeContents());
        } catch (JSONException e) { // this shouldn't happen
            e.printStackTrace();
        }

        return json;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        // your init code here
        super.initialize(cordova, webView);
        context = cordova.getActivity().getApplicationContext();
        context.registerReceiver(mBleReceiver, BleService.getIntentFilter());
        mHandler = new Handler();

    }


    private void initmBle() {
        BleApplication app = (BleApplication) cordova.getActivity().getApplication();
        mBle = app.getIBle();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        _callbackcontext = callbackContext;
        this.initmBle();
        /**
         * 扫描蓝牙设备
         */
        if (action.equals(STARTSCAN)) {
            this.startScan();
            return true;
        }
        /**
         * 停止扫描
         */
        if (action.equals(STOPSCAN)) {
            this.stopScan();
            return true;
        }
        /**
         * 检查是否启用蓝牙适配器。
         */
        if (action.equals(IS_ENABLED)) {
            if (mBle != null) {
                if (mBle.adapterEnabled()) {
                    callbackContext.success();
                } else {
                    callbackContext.error("Bluetooth is disabled.");
                }
            }
            return true;
        }
        /**
         * 连接设备
         */
        if (action.equals(CONNECT)) {
            String macAddress = args.getString(0);
            mDeviceAddress = macAddress;
            this.connect(macAddress, callbackContext);
            return true;
        }
        /**
         * 开始监听消息.
         */
        if (action.equals(STARTNOTIFICATION)) {
            String macAddress = args.getString(0);
            UUID serviceUUID = uuidFromString("F200");// uuidFromString(args.getString(1));
            UUID characteristicUUID = uuidFromString("F201");// uuidFromString(args.getString(2));

            this.startNotification(macAddress, serviceUUID, characteristicUUID, callbackContext);
            return true;
        }
        if (action.equals(WRITE)) {
            String macAddress = args.getString(0);
            UUID serviceUUID = uuidFromString(args.getString(1));
            UUID characteristicUUID = uuidFromString(args.getString(2));
            String val = args.getString(3);
            this.write(macAddress, serviceUUID, characteristicUUID, val);
            return true;
        }
        return false;
    }

    private void startScan() {
        scanLeDevice(true);
    }

    private void stopScan() {
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {

        if (mBle == null) {
            return;
        }
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBle != null) {
                        mBle.stopScan();
                    }
                }
            }, SCAN_PERIOD);
            if (mBle != null) {
                mBle.startScan();
            }
        } else {
            if (mBle != null) {
                mBle.stopScan();
            }
        }
//        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
//        result.setKeepCallback(true);
//        _callbackcontext.sendPluginResult(result);
    }

    private void connect(String macAddress, CallbackContext callbackContext) {
        if (mBle.requestConnect(macAddress)) {
//            callbackContext.success();
        } else {
            callbackContext.error("Could not connect to " + macAddress);
        }
    }

    /**
     * 监听消息.
     *
     * @param macAddress
     * @param serviceUUID
     * @param characteristicUUID
     */
    private void startNotification(String macAddress, UUID serviceUUID, UUID characteristicUUID, CallbackContext callbackContext) {
        rawDataAvailableCallback = callbackContext;
        mDeviceAddress = macAddress;
        ArrayList<BleGattService> bleGattServices = mBle.getServices(macAddress);
        for (int i = 0; i < bleGattServices.size(); i++) {
            LOG.w(TAG, bleGattServices.get(i).getName());
            LOG.w(TAG, bleGattServices.get(i).getUuid().toString());
        }
        BleGattService bleGattService = mBle.getService(macAddress, serviceUUID);
        if (bleGattService != null) {
            mCharacteristic = bleGattService.getCharacteristic(characteristicUUID);
            mBle.requestCharacteristicNotification(macAddress, mCharacteristic);
        }
    }

    /**
     * 写入消息
     */
    private void write(String macAddress, UUID serviceUUID, UUID characteristicUUID, String val) {
        try {
            mCharacteristic = mBle.getService(macAddress, serviceUUID).getCharacteristic(characteristicUUID);
            byte[] data = Hex.decodeHex(val.toCharArray());
            mCharacteristic.setValue(data);
            mBle.requestWriteCharacteristic(mDeviceAddress,
                    mCharacteristic, "");
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }


    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    private void displayGattServices(List<BleGattService> gattServices) throws JSONException {
        if (gattServices == null)
            return;
        String uuid = null;
        String name = null;
        String unknownServiceString = "Unknown service";
        String unknownCharaString = "Unknown characteristic";

        JSONArray gattServiceDataJsonArray = new JSONArray();

        // Loops through available GATT Services.
        for (BleGattService gattService : gattServices) {

            uuid = gattService.getUuid().toString().toUpperCase();
            name = Utils.BLE_SERVICES
                    .containsKey(uuid) ? Utils.BLE_SERVICES.get(uuid)
                    : unknownServiceString;
            JSONObject currentServiceDataJsonObject = new JSONObject();

            currentServiceDataJsonObject.put(LIST_NAME, name);
            currentServiceDataJsonObject.put(LIST_UUID, uuid);


            JSONArray gattCharacteristicGroupDataJsonArray = new JSONArray();

            List<BleGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            // Loops through available Characteristics.
            for (BleGattCharacteristic gattCharacteristic : gattCharacteristics) {

                uuid = gattCharacteristic.getUuid().toString().toUpperCase();
                name = Utils.BLE_CHARACTERISTICS.containsKey(uuid) ? Utils.BLE_CHARACTERISTICS
                        .get(uuid) : unknownCharaString;

                JSONObject currentCharaDataObject = new JSONObject();
                currentCharaDataObject.put(LIST_NAME, name);
                currentCharaDataObject.put(LIST_UUID, uuid);
                gattCharacteristicGroupDataJsonArray.put(currentCharaDataObject);
            }
            currentServiceDataJsonObject.put(CHARACTERISTICS, gattCharacteristicGroupDataJsonArray);
            gattServiceDataJsonArray.put(currentServiceDataJsonObject);
        }

        PluginResult result = new PluginResult(PluginResult.Status.OK, gattServiceDataJsonArray);
        if (_callbackcontext != null)
            _callbackcontext.sendPluginResult(result);


    }

    /**
     * 根据string转换为uuid
     *
     * @param uuid
     * @return
     */
    private UUID uuidFromString(String uuid) {
        return HeytzUUIDHelper.uuidFromString(uuid);
    }
}
