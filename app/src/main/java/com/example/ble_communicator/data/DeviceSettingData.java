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
    // 範囲を超えた値が来たら、範囲内の値に丸め込む
    public void setByteDataList(byte[] array)
    {
        calibration = (double)array[1];
        if (calibration < 0.0) {
            calibration = 0.0;
        } else if (6.0 < calibration) {
            calibration = 6.0;
        }

        hightemperaturethreshold = ((double)(array[2] + 300)) / 10;
        if (hightemperaturethreshold < -34.5) {
            hightemperaturethreshold = -34.5;
        } else if (40.5 < hightemperaturethreshold) {
            hightemperaturethreshold = 40.5;
        }


        referenceTemperatureUpdateTime = (int)array[3];
        if (referenceTemperatureUpdateTime < 10) {
            referenceTemperatureUpdateTime = 10;
        } else if (60 < referenceTemperatureUpdateTime) {
            referenceTemperatureUpdateTime = 60;
        }
    }

}
