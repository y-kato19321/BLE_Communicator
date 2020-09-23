package com.example.ble_communicator.viewmodel;

import android.bluetooth.BluetoothGattCharacteristic;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ble_communicator.data.DeviceSettingData;
import com.example.ble_communicator.data.NumberPickerData;

public class MainActivityViewModel extends ViewModel {

    //ユーザ設定情報
    DeviceSettingData devicedata = null;
    public MutableLiveData<DeviceSettingData> ldsettingdata;

    public DeviceSettingData getDeviceSettingData() {
        return ldsettingdata.getValue();
    }

    public void setDeviceSettingData(DeviceSettingData data)
    {
        devicedata = data;
        ldsettingdata.postValue(devicedata);
    }

    //NumberPickerに設定する情報を管理するクラス
    private NumberPickerData npdata = null;

    /**
     * コンストラクタ
     */
    public MainActivityViewModel() {

        ///////////////////////////////////////////////////////////////
        //メンバクラスのインスタンス作成
        devicedata = new DeviceSettingData();
        if (devicedata == null) {
            System.exit(0);
        }

        npdata = new NumberPickerData();
        if(npdata == null) {
            System.exit(0);
        }

        ldsettingdata = new MutableLiveData<DeviceSettingData>();
        if(ldsettingdata == null) {
            System.exit(0);
        }
    }

    /**
     * BLEから受信したデータを翻訳し、メンバクラスに設定する
     * @param bledata
     */
    public void setDeviceSettingFromBLEData(BluetoothGattCharacteristic bledata) {
        ldsettingdata.setValue(devicedata);
    }

    /**
     * LiveDataによるデバイス設定情報を返す
     * @return
     */
    public MutableLiveData<DeviceSettingData> getDeviceSettingDataMutable() {
        ldsettingdata.setValue(devicedata);
        return ldsettingdata;
    }

    /**
     * 温度誤差の設定可能数を返す
     * @return 温度誤差の設定可能数
     */
    public int getCalibralinNPMaxvalue() {
        return npdata.getCalibralinNPMaxvalue();
    }

    /**
     * 温度誤差のリストを返す
     * @return 温度誤差のリスト
     */
    public String[] getCalibrationListContents() {
        return npdata.getCalibrationListContents();
    }

    /**
     * 指定の値のキャリブレーションNumberPickerのインデックスを返す
     * @return 　キャリブレーション値に対応するリストのインデックス
     */
    public int getCalibrationNPIndexOf() {
        return npdata.getCalibrationNPIndexOf(devicedata.calibration);
    }

    /**
     * 高温閾値NumberPickerに設定する個数を返す
     * @return リスト個数
     */
    public int getHighTemperatureThresholdMaxvalue() {
        return npdata.getHighTemperatureThresholdMaxvalue();
    }

    /**
     * 高温閾値NumberPickerに設定する文字列リストを返す
     * @return 高温閾値リスト
     */
    public String[] getHighTemperatureThresholdListContents() {
        return npdata.getHighTemperatureThresholdListContents();
    }

    /**
     * 高温閾値NumberPickerのインデックスを返す
     * @return 高温閾値
     */
    public int getHighTemperatureThresholdNPIndexOf() {
        return npdata.getHighTemperatureThresholdNPIndexOf(devicedata.hightemperaturethreshold);
    }

    /**
     * 基準温度更新時間NumberPickerに設定する個数を返す
     * @return
     */
    public int getReferenceTemperatureUpdatetimeMaxvalue() {
        return npdata.getReferenceTemperatureUpdatetimeMaxvalue();
    }

    /**
     * 基準温度更新時間NNumberPickerに設定する文字列リストを返す
     * @return 基準温度更新時間リスト
     */
    public String[] getReferenceTemperatureUpdatetimeListContents() {
        return npdata.getReferenceTemperatureUpdatetimeListContents();
    }

    /**
     * 高温閾値NumberPickerのインデックスを返す
     * @return 高温閾値
     */
    public int getReferenceTemperatureUpdatetimeNPIndexOf() {
        return npdata.getReferenceTemperatureUpdatetimeNPIndexOf(devicedata.referenceTemperatureUpdateTime);
    }


    /**
     * 温度誤差値の変更時の処理
     * @param oldv
     * @param newv 選択された値
     */
    public void onCalibrationChange(int oldv, int newv) {
        devicedata.calibration = npdata.getCalibrationIndexDoublenum(newv);
        ldsettingdata.setValue(devicedata);
    }

    /**
     * 高温閾値の変更時の処理
     * @param oldv
     * @param newv 選択された値
     */
    public void onHighTemperatureThresholdChange(int oldv, int newv) {
        devicedata.hightemperaturethreshold = npdata.getHighTemperatureThresholdNPIndexDoublenum(newv);
        ldsettingdata.setValue(devicedata);
    }

    /**
     * 基準温度更新時間値の変更時の処理
     * @param oldv
     * @param newv 選択された値
     */
    public void onReferenceUDTimeChange(int oldv, int newv) {
        devicedata.referenceTemperatureUpdateTime = npdata.getReferenceUDTimeChangeIntnum(newv);
        ldsettingdata.setValue(devicedata);
    }

}
