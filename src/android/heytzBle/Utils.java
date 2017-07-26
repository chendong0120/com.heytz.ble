package com.heytz.ble;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.heytz.ble.HeytzUUIDHelper.uuidFromString;

/**
 * Created by chendongdong on 16/1/18.
 */
public class Utils {
    static final String TAG = "======heytzBle Utils======\n";
    public static Map<String, String> BLE_SERVICES = new HashMap<String, String>();
    public static Map<String, String> BLE_CHARACTERISTICS = new HashMap<String, String>();

    static {
        BLE_SERVICES.put("00001811-0000-1000-8000-00805F9B34FB", "Alert Notification Service");
        BLE_SERVICES.put("0000180F-0000-1000-8000-00805F9B34FB", "Battery Service");
        BLE_SERVICES.put("00001810-0000-1000-8000-00805F9B34FB", "Blood Pressure");
        BLE_SERVICES.put("00001805-0000-1000-8000-00805F9B34FB", "Current Time Service");
        BLE_SERVICES.put("00001818-0000-1000-8000-00805F9B34FB", "Cycling Power");
        BLE_SERVICES.put("00001816-0000-1000-8000-00805F9B34FB", "Cycling Speed and Cadence");
        BLE_SERVICES.put("0000180A-0000-1000-8000-00805F9B34FB", "Device Information");
        BLE_SERVICES.put("00001800-0000-1000-8000-00805F9B34FB", "Generic Access");
        BLE_SERVICES.put("00001801-0000-1000-8000-00805F9B34FB", "Generic Attribute");
        BLE_SERVICES.put("00001808-0000-1000-8000-00805F9B34FB", "Glucose");
        BLE_SERVICES.put("00001809-0000-1000-8000-00805F9B34FB", "Health Thermometer");
        BLE_SERVICES.put("0000180D-0000-1000-8000-00805F9B34FB", "Heart Rate");
        BLE_SERVICES.put("00001812-0000-1000-8000-00805F9B34FB", "Human Interface Device");
        BLE_SERVICES.put("00001802-0000-1000-8000-00805F9B34FB", "Immediate Alert");
        BLE_SERVICES.put("00001803-0000-1000-8000-00805F9B34FB", "Link Loss");
        BLE_SERVICES.put("00001819-0000-1000-8000-00805F9B34FB", "Location and Navigation");
        BLE_SERVICES.put("00001807-0000-1000-8000-00805F9B34FB", "Next DST Change Service");
        BLE_SERVICES.put("0000180E-0000-1000-8000-00805F9B34FB", "Phone Alert Status Service");
        BLE_SERVICES.put("00001806-0000-1000-8000-00805F9B34FB", "Reference Time Update Service");
        BLE_SERVICES.put("00001814-0000-1000-8000-00805F9B34FB", "Running Speed and Cadence");
        BLE_SERVICES.put("00001813-0000-1000-8000-00805F9B34FB", "Scan Parameters");
        BLE_SERVICES.put("00001804-0000-1000-8000-00805F9B34FB", "Tx Power");

        BLE_CHARACTERISTICS.put("00002A43-0000-1000-8000-00805F9B34FB", "Alert Category ID");
        BLE_CHARACTERISTICS.put("00002A42-0000-1000-8000-00805F9B34FB", "Alert Category ID Bit Mask");
        BLE_CHARACTERISTICS.put("00002A06-0000-1000-8000-00805F9B34FB", "Alert Level");
        BLE_CHARACTERISTICS.put("00002A44-0000-1000-8000-00805F9B34FB", "Alert Notification Control Point");
        BLE_CHARACTERISTICS.put("00002A3F-0000-1000-8000-00805F9B34FB", "Alert Status");
        BLE_CHARACTERISTICS.put("00002A01-0000-1000-8000-00805F9B34FB", "Appearance");
        BLE_CHARACTERISTICS.put("00002A19-0000-1000-8000-00805F9B34FB", "Battery Level");
        BLE_CHARACTERISTICS.put("00002A49-0000-1000-8000-00805F9B34FB", "Blood Pressure Feature");
        BLE_CHARACTERISTICS.put("00002A35-0000-1000-8000-00805F9B34FB", "Blood Pressure Measurement");
        BLE_CHARACTERISTICS.put("00002A38-0000-1000-8000-00805F9B34FB", "Body Sensor Location");
        BLE_CHARACTERISTICS.put("00002A22-0000-1000-8000-00805F9B34FB", "Boot Keyboard Input Report");
        BLE_CHARACTERISTICS.put("00002A32-0000-1000-8000-00805F9B34FB", "Boot Keyboard Output Report");
        BLE_CHARACTERISTICS.put("00002A33-0000-1000-8000-00805F9B34FB", "Boot Mouse Input Report");
        BLE_CHARACTERISTICS.put("00002A5C-0000-1000-8000-00805F9B34FB", "CSC Feature");
        BLE_CHARACTERISTICS.put("00002A5B-0000-1000-8000-00805F9B34FB", "CSC Measurement");
        BLE_CHARACTERISTICS.put("00002A2B-0000-1000-8000-00805F9B34FB", "Current Time");
        BLE_CHARACTERISTICS.put("00002A66-0000-1000-8000-00805F9B34FB", "Cycling Power Control Point");
        BLE_CHARACTERISTICS.put("00002A65-0000-1000-8000-00805F9B34FB", "Cycling Power Feature");
        BLE_CHARACTERISTICS.put("00002A63-0000-1000-8000-00805F9B34FB", "Cycling Power Measurement");
        BLE_CHARACTERISTICS.put("00002A64-0000-1000-8000-00805F9B34FB", "Cycling Power Vector");
        BLE_CHARACTERISTICS.put("00002A08-0000-1000-8000-00805F9B34FB", "Date Time");
        BLE_CHARACTERISTICS.put("00002A0A-0000-1000-8000-00805F9B34FB", "Day Date Time");
        BLE_CHARACTERISTICS.put("00002A09-0000-1000-8000-00805F9B34FB", "Day of Week");
        BLE_CHARACTERISTICS.put("00002A00-0000-1000-8000-00805F9B34FB", "Device Name");
        BLE_CHARACTERISTICS.put("00002A0D-0000-1000-8000-00805F9B34FB", "DST Offset");
        BLE_CHARACTERISTICS.put("00002A0C-0000-1000-8000-00805F9B34FB", "Exact Time 256");
        BLE_CHARACTERISTICS.put("00002A26-0000-1000-8000-00805F9B34FB", "Firmware Revision String");
        BLE_CHARACTERISTICS.put("00002A51-0000-1000-8000-00805F9B34FB", "Glucose Feature");
        BLE_CHARACTERISTICS.put("00002A18-0000-1000-8000-00805F9B34FB", "Glucose Measurement");
        BLE_CHARACTERISTICS.put("00002A34-0000-1000-8000-00805F9B34FB", "Glucose Measurement Context");
        BLE_CHARACTERISTICS.put("00002A27-0000-1000-8000-00805F9B34FB", "Hardware Revision String");
        BLE_CHARACTERISTICS.put("00002A39-0000-1000-8000-00805F9B34FB", "Heart Rate Control Point");
        BLE_CHARACTERISTICS.put("00002A37-0000-1000-8000-00805F9B34FB", "Heart Rate Measurement");
        BLE_CHARACTERISTICS.put("00002A4C-0000-1000-8000-00805F9B34FB", "HID Control Point");
        BLE_CHARACTERISTICS.put("00002A4A-0000-1000-8000-00805F9B34FB", "HID Information");
        BLE_CHARACTERISTICS.put("00002A2A-0000-1000-8000-00805F9B34FB", "IEEE 11073-20601 Regulatory Certification Data List");
        BLE_CHARACTERISTICS.put("00002A36-0000-1000-8000-00805F9B34FB", "Intermediate Cuff Pressure");
        BLE_CHARACTERISTICS.put("00002A1E-0000-1000-8000-00805F9B34FB", "Intermediate Temperature");
        BLE_CHARACTERISTICS.put("00002A6B-0000-1000-8000-00805F9B34FB", "LN Control Point");
        BLE_CHARACTERISTICS.put("00002A6A-0000-1000-8000-00805F9B34FB", "LN Feature");
        BLE_CHARACTERISTICS.put("00002A0F-0000-1000-8000-00805F9B34FB", "Local Time Information");
        BLE_CHARACTERISTICS.put("00002A67-0000-1000-8000-00805F9B34FB", "Location and Speed");
        BLE_CHARACTERISTICS.put("00002A29-0000-1000-8000-00805F9B34FB", "Manufacturer Name String");
        BLE_CHARACTERISTICS.put("00002A21-0000-1000-8000-00805F9B34FB", "Measurement Interval");
        BLE_CHARACTERISTICS.put("00002A24-0000-1000-8000-00805F9B34FB", "Model Number String");
        BLE_CHARACTERISTICS.put("00002A68-0000-1000-8000-00805F9B34FB", "Navigation");
        BLE_CHARACTERISTICS.put("00002A46-0000-1000-8000-00805F9B34FB", "New Alert");
        BLE_CHARACTERISTICS.put("00002A04-0000-1000-8000-00805F9B34FB", "Peripheral Preferred Connection Parameters");
        BLE_CHARACTERISTICS.put("00002A02-0000-1000-8000-00805F9B34FB", "Peripheral Privacy Flag");
        BLE_CHARACTERISTICS.put("00002A50-0000-1000-8000-00805F9B34FB", "PnP ID");
        BLE_CHARACTERISTICS.put("00002A69-0000-1000-8000-00805F9B34FB", "Position Quality");
        BLE_CHARACTERISTICS.put("00002A4E-0000-1000-8000-00805F9B34FB", "Protocol Mode");
        BLE_CHARACTERISTICS.put("00002A03-0000-1000-8000-00805F9B34FB", "Reconnection Address");
        BLE_CHARACTERISTICS.put("00002A52-0000-1000-8000-00805F9B34FB", "Record Access Control Point");
        BLE_CHARACTERISTICS.put("00002A14-0000-1000-8000-00805F9B34FB", "Reference Time Information");
        BLE_CHARACTERISTICS.put("00002A4D-0000-1000-8000-00805F9B34FB", "Report");
        BLE_CHARACTERISTICS.put("00002A4B-0000-1000-8000-00805F9B34FB", "Report Map");
        BLE_CHARACTERISTICS.put("00002A40-0000-1000-8000-00805F9B34FB", "Ringer Control Point");
        BLE_CHARACTERISTICS.put("00002A41-0000-1000-8000-00805F9B34FB", "Ringer Setting");
        BLE_CHARACTERISTICS.put("00002A54-0000-1000-8000-00805F9B34FB", "RSC Feature");
        BLE_CHARACTERISTICS.put("00002A53-0000-1000-8000-00805F9B34FB", "RSC Measurement");
        BLE_CHARACTERISTICS.put("00002A55-0000-1000-8000-00805F9B34FB", "SC Control Point");
        BLE_CHARACTERISTICS.put("00002A4F-0000-1000-8000-00805F9B34FB", "Scan Interval Window");
        BLE_CHARACTERISTICS.put("00002A31-0000-1000-8000-00805F9B34FB", "Scan Refresh");
        BLE_CHARACTERISTICS.put("00002A5D-0000-1000-8000-00805F9B34FB", "Sensor Location");
        BLE_CHARACTERISTICS.put("00002A25-0000-1000-8000-00805F9B34FB", "Serial Number String");
        BLE_CHARACTERISTICS.put("00002A05-0000-1000-8000-00805F9B34FB", "Service Changed");
        BLE_CHARACTERISTICS.put("00002A28-0000-1000-8000-00805F9B34FB", "Software Revision String");
        BLE_CHARACTERISTICS.put("00002A47-0000-1000-8000-00805F9B34FB", "Supported New Alert Category");
        BLE_CHARACTERISTICS.put("00002A48-0000-1000-8000-00805F9B34FB", "Supported Unread Alert Category");
        BLE_CHARACTERISTICS.put("00002A23-0000-1000-8000-00805F9B34FB", "System ID");
        BLE_CHARACTERISTICS.put("00002A1C-0000-1000-8000-00805F9B34FB", "Temperature Measurement");
        BLE_CHARACTERISTICS.put("00002A1D-0000-1000-8000-00805F9B34FB", "Temperature Type");
        BLE_CHARACTERISTICS.put("00002A12-0000-1000-8000-00805F9B34FB", "Time Accuracy");
        BLE_CHARACTERISTICS.put("00002A13-0000-1000-8000-00805F9B34FB", "Time Source");
        BLE_CHARACTERISTICS.put("00002A16-0000-1000-8000-00805F9B34FB", "Time Update Control Point");
        BLE_CHARACTERISTICS.put("00002A17-0000-1000-8000-00805F9B34FB", "Time Update State");
        BLE_CHARACTERISTICS.put("00002A11-0000-1000-8000-00805F9B34FB", "Time with DST");
        BLE_CHARACTERISTICS.put("00002A0E-0000-1000-8000-00805F9B34FB", "Time Zone");
        BLE_CHARACTERISTICS.put("00002A07-0000-1000-8000-00805F9B34FB", "Tx Power Level");
        BLE_CHARACTERISTICS.put("00002A45-0000-1000-8000-00805F9B34FB", "Unread Alert Status");
    }

    private static Utils INSTANCE;

    private Utils() {
    }

    public static Utils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Utils();
        }
        return INSTANCE;
    }

    /**
     * ?device ???json??
     *
     * @param device
     * @return
     */
     static JSONObject deviceToJSONObject(BluetoothDevice device) {
        JSONObject json = new JSONObject();

        try {
            json.put("id", device.getAddress()); // mac address
            json.put("name", device.getName());
            json.put("getUuids", device.getUuids());
//            json.put("address", device.getAddress()); // mac address
//            json.put("getBluetoothClass", device.getBluetoothClass());
//            json.put("getBondState", device.getBondState());
//            json.put("getType", device.getType());
//            json.put("getClass", device.getClass());
//            json.put("describeContents", device.describeContents());
        } catch (JSONException e) { // this shouldn't happen
            Log.w(TAG, e.getMessage());
        } finally {
            Log.w(TAG, json.toString());
            return json;
        }
    }

    static UUID[] parseServiceUUIDList(JSONArray jsonArray) throws JSONException {
        List<UUID> serviceUUIDs = new ArrayList<UUID>();

        for (int i = 0; i < jsonArray.length(); i++) {
            String uuidString = jsonArray.getString(i);
            serviceUUIDs.add(uuidFromString(uuidString));
        }

        return serviceUUIDs.toArray(new UUID[jsonArray.length()]);
    }

}
