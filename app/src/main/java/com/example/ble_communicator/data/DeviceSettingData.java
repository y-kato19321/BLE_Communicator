package com.example.ble_communicator.data;

import java.nio.ByteBuffer;

public class DeviceSettingData {

    //温度誤差(初期値を設定)
    public double calibration = 5.0;

    //高温閾値(初期値を設定)
    public double hightemperaturethreshold = 37.3;

    //基準温度更新時間(初期値を設定)
    public int referenceTemperatureUpdateTime = 20;

    /** 温度誤差 最小 */
    private final double CALIBRATION_MIN = 0.0;
    /** 温度誤差 最大 */
    private final double CALIBRATION_MAX = 6.0;
    /** 高温閾値 最小 */
    private final double HIGHTEMPERATURETHRESHOLD_MIN = 36.8;
    /** 高温閾値 最大 */
    private final double HIGHTEMPERATURETHRESHOLD_MAX = 40.5;
    /** 基準温度更新時間 最小 */
    private final int REFERENCETEMPERATUREUPDATETIME_MIN = 10;
    /** 基準温度更新時間 最大 */
    private final int REFERENCETEMPERATUREUPDATETIME_MAX = 60;


    // アプリで扱っているデータをサーバーで扱えるように変換
    public byte[] createByteDataList()
    {
        byte[] array = new byte[4];

        array[1] = (byte)(calibration * 10);
        array[2] = (byte)(hightemperaturethreshold * 10 - 300);
        array[3] = (byte)(referenceTemperatureUpdateTime);

        return array;
    }

    // サーバーからのデータをアプリで扱うデータに変換
    // 範囲を超えた値が来たら、範囲内の値に丸め込む
    public void setByteDataList(byte[] array)
    {
        calibration = ((double)array[1] / 10);
        if (calibration < CALIBRATION_MIN) {
            calibration = CALIBRATION_MIN;
        } else if (CALIBRATION_MAX < calibration) {
            calibration = CALIBRATION_MAX;
        }

        hightemperaturethreshold = ((double)(array[2] + 300)) / 10;
        if (hightemperaturethreshold < HIGHTEMPERATURETHRESHOLD_MIN) {
            hightemperaturethreshold = HIGHTEMPERATURETHRESHOLD_MIN;
        } else if (HIGHTEMPERATURETHRESHOLD_MAX < hightemperaturethreshold) {
            hightemperaturethreshold = HIGHTEMPERATURETHRESHOLD_MAX;
        }


        referenceTemperatureUpdateTime = (int)array[3];
        if (referenceTemperatureUpdateTime < REFERENCETEMPERATUREUPDATETIME_MIN) {
            referenceTemperatureUpdateTime = REFERENCETEMPERATUREUPDATETIME_MIN;
        } else if (REFERENCETEMPERATUREUPDATETIME_MAX < referenceTemperatureUpdateTime) {
            referenceTemperatureUpdateTime = REFERENCETEMPERATUREUPDATETIME_MAX;
        }
    }

}
