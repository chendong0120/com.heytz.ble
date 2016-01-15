package com.heytz.ble;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder rawBinder) {
            mService = ((BleService.LocalBinder) rawBinder).getService();
            mBle = mService.getBle();
            if (mBle != null && !mBle.adapterEnabled()) {
                // TODO: enalbe adapter
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

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
        super.initialize(cordova, webView);
        Application attachApplication = cordova.getActivity().getApplication();
        Intent bindIntent = new Intent(attachApplication, BleService.class);
        attachApplication.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        // your init code here
        context = cordova.getActivity().getApplicationContext();
        mHandler = new Handler();

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        _callbackcontext = callbackContext;
        context.registerReceiver(mBleReceiver, BleService.getIntentFilter());
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
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

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void startScan() {
        scanLeDevice(true);
    }

    private void stopScan() {
        mBle.stopScan();
    }
    private void scanLeDevice(final boolean enable) {
       // BleApplication app = (BleApplication) getApplication();
        //mBle = app.getIBle();
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
                   // invalidateOptionsMenu();
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
