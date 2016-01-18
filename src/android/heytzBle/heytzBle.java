package com.heytz.ble;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.heytz.ble.sdk.BleService;
import com.heytz.ble.sdk.IBle;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class echoes a string called from JavaScript.
 */
public class HeytzBle extends CordovaPlugin {

    private static final long SCAN_PERIOD = 10000;

    private static final String TAG = "HeytzBle";

    private BleService mService;
    private IBle mBle;
    private boolean mScanning;
    private Handler mHandler;

    private Context context;
    private CallbackContext _callbackcontext;

    private final BroadcastReceiver mBleReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
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

            } else if (BleService.BLE_NO_BT_ADAPTER.equals(action)) {
                LOG.w(TAG, "No bluetooth adapter");
                //todo 没有蓝牙设备
            }
        }
    };

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        // your init code here
        super.initialize(cordova, webView);
        context = cordova.getActivity().getApplicationContext();
        context.registerReceiver(mBleReceiver, BleService.getIntentFilter());
        mHandler = new Handler();

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        _callbackcontext = callbackContext;
        if (action.equals("startScan")) {
            this.startScan();
            mScanning = false;
            return true;
        }
        if (action.equals("stopScan")) {
            this.stopScan();
            return true;
        }

        return false;
    }
    private void startScan() {
        scanLeDevice(true);
    }

    private void stopScan() {
        mBle.stopScan();
        scanLeDevice(false);
    }
    private void scanLeDevice(final boolean enable) {
        BleApplication app = (BleApplication) cordova.getActivity().getApplication();
        mBle = app.getIBle();
        if (mBle == null) {
            return;
        }
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if (mBle != null) {
                        mBle.stopScan();
                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            if (mBle != null) {
                mBle.startScan();
            }
        } else {
            mScanning = false;
            if (mBle != null) {
                mBle.stopScan();
            }
        }
        //invalidateOptionsMenu();
    }
}
