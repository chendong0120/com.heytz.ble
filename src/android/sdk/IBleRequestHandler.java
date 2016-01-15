package com.heytz.ble.sdk;

/**
 * Created by chendongdong on 16/1/15.
 */

public interface IBleRequestHandler {

    public boolean connect(String address);

    /**
     * @param address
     * @param characteristic
     * @return
     */
    public boolean readCharacteristic(String address,
                                      BleGattCharacteristic characteristic);

    /**
     * @param address
     * @param characteristic
     * @return
     */
    public boolean characteristicNotification(String address,
                                              BleGattCharacteristic characteristic);

    /**
     * @param address
     * @param characteristic
     * @return
     */
    public boolean writeCharacteristic(String address,
                                       BleGattCharacteristic characteristic);
}

