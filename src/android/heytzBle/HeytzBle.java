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
    private static final String SCAN = "scan";
    private static final String STOPSCAN = "stopScan";
    private static final String IS_ENABLED = "isEnabled";
    private static final String CONNECT = "connect";
    private static final String DISCONNECT = "disconnect";
    private static final String STARTNOTIFICATION = "startNotification"; // register for characteristic notification
    private static final String STOPNOTIFICATION = "stopNotification";   // unregister for characteristic notification
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
    private BleGattCharacteristic notifyCharacteristic;
    private BleGattCharacteristic writeCharacteristic;
    private String mDeviceAddress;          //当前监听的设备

    private boolean mNotifyStarted;
    private Context context;
    private CallbackContext _callbackcontext;
    private CallbackContext scancallbackcontext;
    private CallbackContext connectCallbackcontext;
    private CallbackContext rawDataAvailableCallback;

    private final BroadcastReceiver mBleReceiver = new BroadcastReceiver() {

        /**
         * 接收扫描到的新设备
         *
         * @param context
         * @param intent
         */
        private void scanOnReceive(Context context, Intent intent) {
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
                if (scancallbackcontext != null)
                    scancallbackcontext.sendPluginResult(result);

            } else if (BleService.BLE_NO_BT_ADAPTER.equals(action)) {
                LOG.w(TAG, "No bluetooth adapter");
                //todo 没有蓝牙设备
            }
        }

        /**
         * 接收写入消息的状态
         *
         * @param context
         * @param intent
         */
        private void writeOnReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            if (mDeviceAddress == null || extras == null || writeCharacteristic == null) return;
            if (!mDeviceAddress.equals(extras.getString(BleService.EXTRA_ADDR))) {
                return;
            }

            String uuid = extras.getString(BleService.EXTRA_UUID);
            if (uuid != null && !writeCharacteristic.getUuid().toString().equals(uuid)) {
                return;
            }

            if (BleService.BLE_CHARACTERISTIC_WRITE.equals(action)) {//写入消息成功.
                LOG.w(TAG, "Write success!");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("state", "WriteSuccess");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (_callbackcontext != null) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
                    _callbackcontext.sendPluginResult(result);
                }
            }
        }

        /**
         * 设备状态的回调
         *
         * @param context
         * @param intent
         */
        private void deviceOnReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BleService.BLE_GATT_CONNECTED.equals(action)) {//设备已连接

            } else if (BleService.BLE_GATT_DISCONNECTED.equals(action)) {
                //设备断开连接
                LOG.w(TAG, "Device disconnected...");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("state", "disconnected");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //连接状态的回调,如果设备断开连接那么就发送disconnected;
                if (connectCallbackcontext != null) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
                    connectCallbackcontext.sendPluginResult(result);
                    // connectCallbackcontext = null;
                }
            } else if (BleService.BLE_SERVICE_DISCOVERED.equals(action)) {//设备的服务被发现
                //加载设备的信息,各个通道的信息.
                try {
                    displayGattServices(mBle.getServices(mDeviceAddress));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 数据监听的回调
         *
         * @param context
         * @param intent
         */
        private void notificationOnReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            if (mDeviceAddress == null || extras == null) return;
            if (!mDeviceAddress.equals(extras.getString(BleService.EXTRA_ADDR))) {
                return;
            }

            String uuid = extras.getString(BleService.EXTRA_UUID);
            if (uuid != null
                    && !notifyCharacteristic.getUuid().toString().equals(uuid)) {
                return;
            }
            if (BleService.BLE_CHARACTERISTIC_READ.equals(action)
                    || BleService.BLE_CHARACTERISTIC_CHANGED.equals(action)) {  //当蓝牙设备有消息读取或者消息改变,
                byte[] data = extras.getByteArray(BleService.EXTRA_VALUE);
//              new String(Hex.encodeHex(val));
                if (data != null && data.length > 0) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                    result.setKeepCallback(true);
                    rawDataAvailableCallback.sendPluginResult(result);
                }
            } else if (BleService.BLE_CHARACTERISTIC_NOTIFICATION.equals(action)) {//通知的状态.
                LOG.w(TAG, "Notification state changed!");
                mNotifyStarted = extras.getBoolean(BleService.EXTRA_VALUE);
                JSONObject jsonObject = new JSONObject();
                if (mNotifyStarted) {
                    LOG.w(TAG, "Start Notify");
                    try {
                        jsonObject.put("state", "StartNotify");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
//                    result.setKeepCallback(true);
//                    rawDataAvailableCallback.sendPluginResult(result);

                } else {
                    LOG.w(TAG, "Stop Notify");
                    try {
                        jsonObject.put("state", "StopNotify");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (rawDataAvailableCallback != null) {
                        PluginResult result = new PluginResult(PluginResult.Status.ERROR, jsonObject);
                        rawDataAvailableCallback.sendPluginResult(result);
                    }
                    if (_callbackcontext != null) {
                        PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
                        _callbackcontext.sendPluginResult(result);
                    }
                }
            } else if (BleService.BLE_CHARACTERISTIC_INDICATION.equals(action)) {//指示状态改变
                LOG.w(TAG, "Indication state changed!");
            } else if (BleService.BLE_GATT_DISCONNECTED.equals(action)) {
                //设备断开连接
                LOG.w(TAG, "Device disconnected...");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("state", "disconnected");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //数据监听的回调,如果设备短裤那么清空这个errorCallback
                if (rawDataAvailableCallback != null) {
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, jsonObject);
                    rawDataAvailableCallback.sendPluginResult(result);
                    rawDataAvailableCallback = null;
                }
            }

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //搜索设备列表的回调
            scanOnReceive(context, intent);
            //写入数据的状态回调
            writeOnReceive(context, intent);
            //设备状态的回调
            deviceOnReceive(context, intent);
            //数据监听的回调
            notificationOnReceive(context, intent);
        }
    };

    /**
     * 将device 转换为json对象
     *
     * @param device
     * @return
     */
    public JSONObject deviceToJSONObject(BluetoothDevice device) {

        JSONObject json = new JSONObject();

        try {
            json.put("id", device.getAddress()); // mac address
//            json.put("getUuids", device.getUuids());
            json.put("name", device.getName());
//            json.put("address", device.getAddress()); // mac address
//            json.put("getBluetoothClass", device.getBluetoothClass());
//            json.put("getBondState", device.getBondState());
//            json.put("getType", device.getType());
//            json.put("getClass", device.getClass());
//            json.put("describeContents", device.describeContents());
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

    /**
     * 获取ble
     */
    private void initmBle() {
        BleApplication app = (BleApplication) cordova.getActivity().getApplication();
        mBle = app.getIBle();
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        this.initmBle();
        if (action.equals(SCAN)) {
            scancallbackcontext = callbackContext;
            JSONArray jsonArray = args.getJSONArray(0);
            int scanSeconds = args.getInt(1);
            if (jsonArray.length() > 0) {
                UUID[] serviceUUIDs = parseServiceUUIDList(jsonArray);
                scanLeDevice(true, serviceUUIDs, scanSeconds);
            } else {
                scanLeDevice(true, null, scanSeconds);
            }
            return true;
        }
        /**
         * 扫描蓝牙设备
         */
        if (action.equals(STARTSCAN)) {
            scancallbackcontext = callbackContext;
            JSONArray jsonArray = args.getJSONArray(0);
            if (jsonArray.length() > 0) {
                UUID[] serviceUUIDs = parseServiceUUIDList(args.getJSONArray(0));
                scanLeDevice(true, serviceUUIDs, 0);
            } else {
                scanLeDevice(true, null, 0);
            }
            return true;
        }
        /**
         * 停止扫描
         */
        if (action.equals(STOPSCAN)) {
            scancallbackcontext = null;
            scanLeDevice(false, null, 0);
            callbackContext.success();
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
            connectCallbackcontext = callbackContext;
            String macAddress = args.getString(0);
            mDeviceAddress = macAddress;
            this.connect(macAddress, callbackContext);
            return true;
        }
        /**
         * 断开连接.
         */
        if (action.equals(DISCONNECT)) {
            connectCallbackcontext = callbackContext;
            String macAddress = args.getString(0);
            mDeviceAddress = macAddress;
            this.disconnect(macAddress);
            return true;
        }
        /**
         * 开始监听消息.
         */
        if (action.equals(STARTNOTIFICATION)) {
            String macAddress = args.getString(0);
            UUID serviceUUID = uuidFromString(args.getString(1));//uuidFromString("F200");//
            UUID characteristicUUID = uuidFromString(args.getString(2));//uuidFromString("F201");//

            this.startNotification(macAddress, serviceUUID, characteristicUUID, callbackContext);
            return true;
        }
        /**
         * 停止监听
         */
        if (action.equals(STOPNOTIFICATION)) {
            String macAddress = args.getString(0);
            UUID serviceUUID = uuidFromString(args.getString(1));//uuidFromString("F200");//
            UUID characteristicUUID = uuidFromString(args.getString(2));//uuidFromString("F201");//

            this.stopNotification(macAddress, serviceUUID, characteristicUUID, callbackContext);
            return true;
        }
        /**
         * 发送信息到指定mac
         */
        if (action.equals(WRITE)) {
            String macAddress = args.getString(0);
            UUID serviceUUID = uuidFromString(args.getString(1));
            UUID characteristicUUID = uuidFromString(args.getString(2));
            byte[] val = args.getArrayBuffer(3);
            this.write(macAddress, serviceUUID, characteristicUUID, val);
            return true;
        }
        return false;
    }

    /**
     * 扫描设备
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable, UUID[] uuids, int scanSeconds) {
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
            }, scanSeconds <= 0 ? SCAN_PERIOD : scanSeconds * 1000);
            if (mBle != null) {
                if (uuids != null) {
                    mBle.startScan(uuids);
                } else {
                    mBle.startScan();

                }
            }
        } else {
            if (mBle != null) {
                mBle.stopScan();
            }
        }
    }

    /**
     * 连接指定设备
     *
     * @param macAddress
     * @param callbackContext
     */
    private void connect(String macAddress, CallbackContext callbackContext) {
        if (mBle.requestConnect(macAddress)) {
//            callbackContext.success();
        } else {
            callbackContext.error("Could not connect to " + macAddress);
        }
    }

    /**
     * 关闭连接
     *
     * @param macAddress
     */
    private void disconnect(String macAddress) {
        if (mBle != null) {
            mBle.disconnect(macAddress);
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
//        ArrayList<BleGattService> bleGattServices = mBle.getServices(macAddress);
//        for (int i = 0; i < bleGattServices.size(); i++) {
//            LOG.w(TAG, bleGattServices.get(i).getName());
//            LOG.w(TAG, bleGattServices.get(i).getUuid().toString());
//        }
        BleGattService bleGattService = mBle.getService(macAddress, serviceUUID);
        if (bleGattService != null) {
            notifyCharacteristic = bleGattService.getCharacteristic(characteristicUUID);
            mBle.requestCharacteristicNotification(macAddress, notifyCharacteristic);
        }
    }

    private void stopNotification(String macAddress, UUID serviceUUID, UUID characteristicUUID, CallbackContext callbackContext) {
        rawDataAvailableCallback = null;
        _callbackcontext = callbackContext;
//        BleGattService bleGattService = mBle.getService(macAddress, serviceUUID);
//        if (bleGattService != null) {
//               notifyCharacteristic = bleGattService.getCharacteristic(characteristicUUID);
        if (notifyCharacteristic != null) {
            if (mBle.requestStopNotification(macAddress, notifyCharacteristic)) {
//                _callbackcontext.success();
            } else {
                _callbackcontext.error("stopNotification is error");
            }
        }
    }

    /**
     * 写入消息
     */
    private void write(String macAddress, UUID serviceUUID, UUID characteristicUUID, byte[] val) {
        try {
            writeCharacteristic = mBle.getService(macAddress, serviceUUID).getCharacteristic(characteristicUUID);
            //byte[] data = Hex.decodeHex(val.toCharArray());
            writeCharacteristic.setValue(val);
            if (mBle.requestWriteCharacteristic(mDeviceAddress,
                    writeCharacteristic, "")) {
                // _callbackcontext.success();
            } else {
                _callbackcontext.error("write is error!");
            }
        } catch (Exception e) {
            _callbackcontext.error("write is error!" + e.getMessage());
        }
    }


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
        if (connectCallbackcontext != null) {
            connectCallbackcontext.sendPluginResult(result);

        }

    }

    /**
     * 根据string转换为uuid
     *
     * @param uuid
     * @return
     */
    private UUID uuidFromString(String uuid) {
        if (uuid.length() > 4 && "0x".equals(uuid.substring(0, 2))) {
            return HeytzUUIDHelper.uuidFromString(uuid.substring(2));
        } else {
            return HeytzUUIDHelper.uuidFromString(uuid);
        }

    }

    private UUID[] parseServiceUUIDList(JSONArray jsonArray) throws JSONException {
        List<UUID> serviceUUIDs = new ArrayList<UUID>();

        for (int i = 0; i < jsonArray.length(); i++) {
            String uuidString = jsonArray.getString(i);
            serviceUUIDs.add(uuidFromString(uuidString));
        }

        return serviceUUIDs.toArray(new UUID[jsonArray.length()]);
    }
}
