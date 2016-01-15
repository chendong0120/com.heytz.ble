package com.heytz.ble.sdk;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by chendongdong on 16/1/15.
 */

public interface IBle {

    public String getBTAdapterMacAddr();

    /**
     * Will receive broadcast {@link BleService#BLE_DEVICE_FOUND} if device
     * found.
     */
    public void startScan();

    /**
     * Stop BLE scan.
     */
    public void stopScan();

    /**
     * Check if bluetooth adapter is enabled.
     *
     * @return enabled
     */
    public boolean adapterEnabled();

    /**
     * Disconnect BLE device. Will receive
     * {@link BleService#BLE_GATT_DISCONNECTED} broadcast if device
     * disconnected.
     *
     * @param address
     *            BLE device address.
     */
    public void disconnect(String address);

    /**
     * Discover BLE services. Will receive
     * {@link BleService#BLE_SERVICE_DISCOVERED} broadcast if device service
     * discovered.
     *
     * @param address
     * @return
     */
    public boolean discoverServices(String address);

    /**
     * Get discovered services for BLE device. Call this function after
     * {@link BleService#BLE_SERVICE_DISCOVERED} broadcast is received.
     *
     * @param address
     * @return List of {@link BleGattService}
     */
    public ArrayList<BleGattService> getServices(String address);

    /**
     * Get discovered service by uuid. Call this function after
     * {@link BleService#BLE_SERVICE_DISCOVERED} broadcast is received.
     *
     * @param address
     * @param uuid
     * @return {@link BleGattService}
     */
    public BleGattService getService(String address, UUID uuid);

    /**
     * Request to connect a BLE device by address. Will receive
     * {@link BleService#BLE_GATT_CONNECTED} broadcast if device connected.
     *
     * @param address
     * @return if request be inserted into queue successfully.
     */
    public boolean requestConnect(String address);

    /**
     * Request to read characteristic. Will receive
     * {@link BleService#BLE_CHARACTERISTIC_READ} broadcast if characteristic
     * read.
     *
     * @param address
     * @param characteristic
     *            Get characteristic from {@link BleGattService}
     * @return if request be inserted into queue successfully.
     */
    public boolean requestReadCharacteristic(String address,
                                             BleGattCharacteristic characteristic);

    /**
     * Request characteristic notification. Will receive
     * {@link BleService#BLE_CHARACTERISTIC_NOTIFICATION} broadcast if
     * notification set OK. When the characteristic's value changed,
     * {@link BleService#BLE_CHARACTERISTIC_CHANGED} broadcast will be received
     * also.
     *
     * @param address
     * @param characteristic
     *            Get characteristic from {@link BleGattService}
     * @return if request be inserted into queue successfully.
     */
    public boolean requestCharacteristicNotification(String address,
                                                     BleGattCharacteristic characteristic);

    public boolean requestStopNotification(String address,
                                           BleGattCharacteristic characteristic);

    /**
     * Request characteristic indication. Will receive
     * {@link BleService#BLE_CHARACTERISTIC_INDICATION} broadcast if indication
     * set OK. When the characteristic's value changed,
     * {@link BleService#BLE_CHARACTERISTIC_CHANGED} broadcast will be received
     * also.
     *
     * @param address
     * @param characteristic
     *            Get characteristic from {@link BleGattService}
     * @return if request be inserted into queue successfully.
     */
    public boolean requestIndication(String address,
                                     BleGattCharacteristic characteristic);

    /**
     * Request write characteristic value. Will receive
     * {@link BleService#BLE_CHARACTERISTIC_WRITE} broadcast if characteristic
     * value be written.
     *
     * @param address
     * @param characteristic
     *            Get characteristic from {@link BleGattService}
     * @param remark
     *            For debug purpose.
     * @return if request be inserted into queue successfully.
     */
    public boolean requestWriteCharacteristic(String address,
                                              BleGattCharacteristic characteristic, String remark);
}

