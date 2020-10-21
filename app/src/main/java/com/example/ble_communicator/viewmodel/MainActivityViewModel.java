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

    public void setDeviceSettingData(DeviceSettingData data) {
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
        npdata = new NumberPickerData();
        ldsettingdata = new MutableLiveData<DeviceSettingData>();
    }

    /**
     * BLEから受信したデータを翻訳し、メンバクラスに設定する
     *
     * @param bledata
     */
    public void setDeviceSettingFromBLEData(BluetoothGattCharacteristic bledata) {
        ldsettingdata.postValue(devicedata);
    }

    /**
     * LiveDataによるデバイス設定情報を返す
     *
     * @return
     */
    public MutableLiveData<DeviceSettingData> getDeviceSettingDataMutable() {
        ldsettingdata.postValue(devicedata);
        return ldsettingdata;
    }


    /**
     * 温度誤差のリストを返す
     *
     * @return 温度誤差のリスト
     */
    public String[] getOffsetTempList() {
        return npdata.getOffsetTempList();
    }

    /**
     * 指定の値のインデックスを返す
     *
     * @return 　
     */
    public int getOffsetTempSpinnerIndex() {
        return npdata.getOffsetTempSpinnerIndex(devicedata.offsetTemp);
    }

    /**
     * 温度誤差値の変更時の処理
     *
     * @param newv 選択された値
     */
    public void onOffsetTempSelectedListener(int newv) {
        devicedata.offsetTemp = npdata.getOffsetValue(newv);
        ldsettingdata.postValue(devicedata);
    }


    /**
     * 高温閾値に設定する文字列リストを返す
     *
     * @return 高温閾値リスト
     */
    public String[] getThresholddegreeList() {
        return npdata.getThresholddegreeList();
    }

    /**
     * 高温閾値のインデックスを返す
     *
     * @return 高温閾値
     */
    public int getThresholddegreeSpinnerIndex() {
        return npdata.getThresholddegreeSpinnerIndex(devicedata.thresholdDegree);
    }

    /**
     * 高温閾値の変更時の処理
     *
     * @param newv 選択された値
     */
    public void onThresholddegreeSelectedListener(int newv) {
        devicedata.thresholdDegree = npdata.getThresholddegreeValue(newv);
        ldsettingdata.postValue(devicedata);
    }

    /**
     * 基準温度更新時間に設定する文字列リストを返す
     *
     * @return 基準温度更新時間リスト
     */
    public String[] getUpdateTimeList() {
        return npdata.getUpdateTimeList();
    }

    /**
     * 基準温度更新時間のインデックスを返す
     *
     * @return 高温閾値
     */
    public int getUpdateTimeSpinnerIndex() {
        return npdata.getUpdatetimeSpinnerIndex(devicedata.updateTime);
    }

    /**
     * 基準温度更新時間値の変更時の処理
     *
     * @param newv 選択された値
     */
    public void  onUpdateTimeSelectedListener(int newv) {
        devicedata.updateTime = npdata.getUpdateTimeValue(newv);
        ldsettingdata.postValue(devicedata);
    }


    //public void setOnClickLog(int id) {
    //    devicedata.mLogMode = npdata.getLogMode(id);
    //    ldsettingdata.postValue(devicedata);
    //}

    //public int getCheckedRadioLogId() {
    //    return npdata.getRadioLogId(devicedata.mLogMode);
    //}

    /*
    public void setOnClickDetecMode(int id) {
        devicedata.mTemperatureMode = npdata.getTemperatureMode(id);
        ldsettingdata.postValue(devicedata);
    }

    public int getCheckedRadioDetecmodeId() {
        return npdata.getRadioTemperatureModeId(devicedata.mTemperatureMode);
    }
*/


    public String[] getDetecModeList() {
        return npdata.getDetecModeList();
    }

    public int getDetecModeSpinnerIndex() {
        return npdata.getDetecModeSpinnerIndex(devicedata.mTemperatureMode);
    }

    public void onDetecModeSelectedListener(int newv) {
       devicedata.mTemperatureMode = npdata.getDetecModeValue(newv);
        ldsettingdata.postValue(devicedata);
    }


    public void setOnClickTemperatureUnit(int id) {
        devicedata.mTemperatureUnit = npdata.getTemperatureUnit(id);
        ldsettingdata.postValue(devicedata);
    }

    public int getCheckedRadioTemperatureUnit() {
        return npdata.getRadioTemperatureUnitId(devicedata.mTemperatureUnit);
    }

    public void setOnClickThermographyMode(int id) {
        devicedata.mThermographyMode = npdata.getThermographyMode(id);
        ldsettingdata.postValue(devicedata);
    }

    public int getCheckedRadioThermographyMode() {
        return npdata.getRadioThermographyModeId(devicedata.mThermographyMode);
    }


}
