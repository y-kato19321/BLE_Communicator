package com.example.ble_communicator.data;

import java.nio.ByteBuffer;

public class DeviceSettingData {

    //温度誤差
    public double calibration = 0.0;

    //高温閾値
    public double hightemperaturethreshold = 37.5;

    //基準温度更新時間
    public int referenceTemperatureUpdateTime = 10;

    // アプリで扱っているデータをサーバーで扱えるように変換
    public byte[] createByteDataList()
    {
        byte[] array = new byte[4];

        array[1] = (byte)calibration;
        array[2] = (byte)(hightemperaturethreshold * 10 - 300);
        array[3] = (byte)(referenceTemperatureUpdateTime);

        return array;
    }

    // サーバーからのデータをアプリで扱うデータに変換
    public void setByteDataList(byte[] array)
    {
        calibration = (double)array[1];
        hightemperaturethreshold = ((double)(array[2] + 300)) / 10;
        referenceTemperatureUpdateTime = (int)array[3];
    }

}
