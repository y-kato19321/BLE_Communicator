package com.example.ble_communicator.data;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class DeviceSettingData {

    //温度誤差(初期値を設定)
    public double offsetTemp = 5.0;

    //高温閾値(初期値を設定)
    //public double hightemperaturethreshold = 37.3;
    public double thresholdDegree = 37.3;

    //基準温度更新時間(初期値を設定)
    //public int referenceTemperatureUpdateTime = 20;
    public int updateTime = 20;

    // ログ表示
    public int mLogMode = 0;

    // 温度検出モード
    public int mTemperatureMode = 0;

    // 温度単位
    public int mTemperatureUnit = 0;

    // サーモ表示
    public int mThermographyMode = 0;


    /** 温度誤差 最小 */
    private final double OFFSETTEMP_MIN = 0.0;
    /** 温度誤差 最大 */
    private final double OFFSETTEMP_MAX = 6.0;
    /** 高温閾値 最小 */
    private final double HIGHTEMPERATURETHRESHOLD_MIN = 36.8;
    /** 高温閾値 最大 */
    private final double HIGHTEMPERATURETHRESHOLD_MAX = 40.5;
    /** 基準温度更新時間 最小 */
    private final int REFERENCETEMPERATUREUPDATETIME_MIN = 10;
    /** 基準温度更新時間 最大 */
    private final int REFERENCETEMPERATUREUPDATETIME_MAX = 60;
    /** ログ非表示  */
    private final int LOGMODE_OFF = 0;
    /** ログ表示  */
    private final int LOGMODE_ON = 1;
    /** 温度検出　手首  */
    private final int TEMPERATUREMODE_ARM = 0;
    /** 温度検出　顔1人  */
    private final int TEMPERATUREMODE_FACE1 = 1;
    /** 温度検出　顔2人  */
    private final int TEMPERATUREMODE_FACE2 = 2;
    /** 温度単位 摂氏 */
    private final int TEMPERATUREUNIT_CELSIUS = 0;
    /** 温度単位 華氏 */
    private final int TEMPERATUREUNIT_FAHRENHEIT = 1;
    /** サーモ非表示 */
    private final int THERMOGRAPHY_OFF = 0;
    /** サーモ表示 */
    private final int THERMOGRAPHY_ON = 1;



    // アプリで扱っているデータをサーバーで扱えるように変換
    public byte[] createByteDataList()
    {
        byte[] array = new byte[16];

        array[1] = (byte)(offsetTemp * 10);
        array[2] = (byte)(thresholdDegree * 10 - 300);
        array[3] = (byte)(updateTime);
        array[4] = (byte)(mLogMode);
        array[5] = (byte)(mTemperatureMode);
        array[6] = (byte)(mTemperatureUnit);
        array[7] = (byte)(mThermographyMode);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        byte[] bytes = ByteBuffer.allocate(8).putLong(seconds).array();
        System.arraycopy(bytes,0,array,8, bytes.length);

        return array;
    }

    // サーバーからのデータをアプリで扱うデータに変換
    // 範囲を超えた値が来たら、範囲内の値に丸め込む
    public void setByteDataList(byte[] array)
    {
        offsetTemp = ((double)array[1] / 10);
        if (offsetTemp < OFFSETTEMP_MIN) {
            offsetTemp = OFFSETTEMP_MIN;
        } else if (OFFSETTEMP_MAX < offsetTemp) {
            offsetTemp = OFFSETTEMP_MAX;
        }

        thresholdDegree = ((double)(array[2] + 300)) / 10;
        if (thresholdDegree < HIGHTEMPERATURETHRESHOLD_MIN) {
            thresholdDegree = HIGHTEMPERATURETHRESHOLD_MIN;
        } else if (HIGHTEMPERATURETHRESHOLD_MAX < thresholdDegree) {
            thresholdDegree = HIGHTEMPERATURETHRESHOLD_MAX;
        }

        updateTime = (int)array[3];
        if (updateTime < REFERENCETEMPERATUREUPDATETIME_MIN) {
            updateTime = REFERENCETEMPERATUREUPDATETIME_MIN;
        } else if (REFERENCETEMPERATUREUPDATETIME_MAX < updateTime) {
            updateTime = REFERENCETEMPERATUREUPDATETIME_MAX;
        }

        // 以下の判定：値の範囲が３つ以上になることを想定した実装(あとで見直すのも良し)。
        mLogMode = (int)array[4];
        if( (mLogMode < LOGMODE_OFF) || (mLogMode > LOGMODE_ON) ) {
            mLogMode = LOGMODE_OFF;
        }

        mTemperatureMode = (int)array[5];
        if( (mTemperatureMode < TEMPERATUREMODE_ARM) || (mTemperatureMode > TEMPERATUREMODE_FACE2)) {
            mTemperatureMode = TEMPERATUREMODE_ARM;
        }

        mTemperatureUnit = (int)array[6];
        if( (mTemperatureUnit < TEMPERATUREUNIT_CELSIUS) || (mTemperatureUnit > TEMPERATUREUNIT_FAHRENHEIT)) {
            mTemperatureUnit = TEMPERATUREUNIT_CELSIUS;
        }

        mThermographyMode = (int)array[7];
        if( (mThermographyMode < THERMOGRAPHY_OFF) || (mThermographyMode > THERMOGRAPHY_ON) ) {
            mThermographyMode = THERMOGRAPHY_OFF;
        }
    }

}
