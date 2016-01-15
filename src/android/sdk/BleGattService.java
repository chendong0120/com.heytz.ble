package com.heytz.ble.sdk;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import com.heytz.ble.sdk.BleService.BLESDK;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by chendongdong on 16/1/15.
 */

@SuppressLint("NewApi")
public class BleGattService {

    private BLESDK mBleSDK;
    private com.samsung.android.sdk.bt.gatt.BluetoothGattService mGattServiceS;
    private com.broadcom.bt.gatt.BluetoothGattService mGattServiceB;
    private android.bluetooth.BluetoothGattService mGattServiceA;
    private String mName;

    public BleGattService(com.samsung.android.sdk.bt.gatt.BluetoothGattService s) {
        mBleSDK = BLESDK.SAMSUNG;
        mGattServiceS = s;
        initInfo();
    }

    public BleGattService(com.broadcom.bt.gatt.BluetoothGattService s) {
        mBleSDK = BLESDK.BROADCOM;
        mGattServiceB = s;
        initInfo();
    }

    public BleGattService(android.bluetooth.BluetoothGattService s) {
        mBleSDK = BLESDK.ANDROID;
        mGattServiceA = s;
        initInfo();
    }

    private void initInfo() {
        mName = "Unknown Service";
    }

    public UUID getUuid() {
        if (mBleSDK == BLESDK.BROADCOM) {
            return mGattServiceB.getUuid();
        } else if (mBleSDK == BLESDK.SAMSUNG) {
            return mGattServiceS.getUuid();
        } else if (mBleSDK == BLESDK.ANDROID) {
            return mGattServiceA.getUuid();
        }

        return null;
    }

    public List<BleGattCharacteristic> getCharacteristics() {
        ArrayList<BleGattCharacteristic> list = new ArrayList<BleGattCharacteristic>();
        if (mBleSDK == BLESDK.BROADCOM) {
            for (com.broadcom.bt.gatt.BluetoothGattCharacteristic c : mGattServiceB
                    .getCharacteristics()) {
                list.add(new BleGattCharacteristic(c));
            }
        } else if (mBleSDK == BLESDK.SAMSUNG) {
            for (Object o : mGattServiceS.getCharacteristics()) {
                com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic c = (com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic) o;
                list.add(new BleGattCharacteristic(c));
            }
        } else if (mBleSDK == BLESDK.ANDROID) {
            for (android.bluetooth.BluetoothGattCharacteristic c : mGattServiceA
                    .getCharacteristics()) {
                list.add(new BleGattCharacteristic(c));
            }
        }

        return list;
    }

    public BleGattCharacteristic getCharacteristic(UUID uuid) {
        if (mBleSDK == BLESDK.ANDROID) {
            BluetoothGattCharacteristic c = mGattServiceA
                    .getCharacteristic(uuid);
            if (c != null) {
                return new BleGattCharacteristic(c);
            }
        } else if (mBleSDK == BLESDK.SAMSUNG) {
            com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic c = mGattServiceS
                    .getCharacteristic(uuid);
            if (c != null) {
                return new BleGattCharacteristic(c);
            }
        } else if (mBleSDK == BLESDK.BROADCOM) {
            com.broadcom.bt.gatt.BluetoothGattCharacteristic c = mGattServiceB
                    .getCharacteristic(uuid);
            if (c != null) {
                return new BleGattCharacteristic(c);
            }
        }

        return null;
    }

    public void setInfo(JSONObject info) {
        if (info == null) {
            return;
        }

        try {
            setName(info.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }
}

