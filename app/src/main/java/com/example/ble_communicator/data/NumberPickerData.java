package com.example.ble_communicator.data;

import com.example.ble_communicator.R;

import java.util.Arrays;
import java.util.List;

public class NumberPickerData {

    private static String preplus = "+";
    private static String plamaizero = "±0.0";

    //温度誤差の設定
    final String[] spinner_offsettemp_list = {
            "+6.0", "+5.5", "+5.0", "+4.5", "+4.0", "+3.5", "+3.0", "+2.5", "+2.0", "+1.5", "+1.0", "+0.5",
            "±0.0"
    };

    //高温閾値の設定
    final String[] spinner_thresholddegree_list = {
            "40.5", "40.4", "40.3", "40.2", "40.1", "40.0",
            "39.9", "39.8", "39.7", "39.6", "39.5", "39.4", "39.3", "39.2", "39.1", "39.0",
            "38.9", "38.8", "38.7", "38.6", "38.5", "38.4", "38.3", "38.2", "38.1", "38.0",
            "37.9", "37.8", "37.7", "37.6", "37.5", "37.4", "37.3", "37.2", "37.1", "37.0",
            "36.9", "36.8"
    };


    final String[] spinner_updatetime_list = {
            "60", "50", "40", "30", "20", "10"
    };

    final String[] spinner_detecmode_list = {
            "手首", "顔1人", "顔2人"
    };


    public String[] getOffsetTempList() {
        return spinner_offsettemp_list;
    }

    /**
     * 指定値に対応するインデックスを返す
     * @return　値に対応するリストのインデックス
     */
    public int getOffsetTempSpinnerIndex(double OffsetTemp) {

        String strOffsetTemp = String.valueOf(OffsetTemp);

        //文字列に記号を追加
        if (OffsetTemp == 0.0) {
            strOffsetTemp = plamaizero;
        } else if (OffsetTemp > 0) {
            strOffsetTemp = preplus + strOffsetTemp;
        }
        List<String> list = Arrays.asList(spinner_offsettemp_list); //配列をList型オブジェクトに変換
        return list.indexOf(strOffsetTemp);
    }

    public double getOffsetValue(int index) {
        //indexの位置の文字列を取得する＝ユーザが設定した値
        String str = spinner_offsettemp_list[index];

        double value = 0.0;

        if (str == plamaizero) {
            value = 0.0;
        } else {
            if  (str.contains("+")) {
                str.substring(0);
            }
            value = Double.parseDouble(str);
        }
        return value;
    }

    /**
     * 高温閾値NumberPickerに設定する文字列リストを返す
     * @return
     */
    public String[] getThresholddegreeList() {
        return spinner_thresholddegree_list;
    }

    /**
     * 高温閾値NumberPickerの、指定値に対応するインデックスを返す
     * @return　高温閾値リスト
     */
    public int getThresholddegreeSpinnerIndex(double thresholddegree) {
        String strht = String.valueOf(thresholddegree);
        List<String> list = Arrays.asList(spinner_thresholddegree_list); //配列をList型オブジェクトに変換
        return list.indexOf(strht);
    }

    /**
     * 高温閾値NumberPickerの位置のDouble値を返す
     * @param index
     * @return
     */
    public double getThresholddegreeValue(int index) {
        //indexの位置の文字列を取得する＝ユーザが設定した値
        String str = spinner_thresholddegree_list[index];
        return Double.parseDouble(str);
    }


    /**
     * 基準温度更新時間に設定する文字列リストを返す
     * @return
     */
    public String[] getUpdateTimeList() {
        return spinner_updatetime_list;
    }

    /**
     * 基準温度更新時間の指定値に対応するインデックスを返す
     * @return　高温閾値リスト
     */
    public int getUpdatetimeSpinnerIndex(int updateTime) {
        String strht = String.valueOf(updateTime);
        List<String> list = Arrays.asList(spinner_updatetime_list); //配列をList型オブジェクトに変換
        return list.indexOf(strht);
    }

    /**
     * 基準温度更新時間の位置のDouble値を返す
     * @param index
     * @return
     */
    public int getUpdateTimeValue(int index) {
        //indexの位置の文字列を取得する＝ユーザが設定した値
        String str = spinner_updatetime_list[index];
        return Integer.valueOf(str);
    }


    public class Inner2 {
        public String str;
        public int value;

        public Inner2(String _str, int _value)
        {
            str = _str;
            value = _value;
        }
    }

    private final Inner2[] mDetecmodeList = {
            new Inner2("手首", 0),
            new Inner2("顔1人", 1),
            new Inner2("顔2人", 2)
    };

    public String[] getDetecModeList() {
        int legth = mDetecmodeList.length;
        String[] list = new String[legth];
        for(int i = 0; i < legth; i++){
            list[i] = mDetecmodeList[i].str;
        }

        return list;
    }

    public int getDetecModeSpinnerIndex(int detecMode){
        int index = 0;
        int size = mDetecmodeList.length;
        for(int i = 0; i < size; i++) {
            if(mDetecmodeList[i].value == detecMode)
            {
                index = i;
                break;
            }
        }

        return index;
    }

    public int getDetecModeValue(int index) {
        int value = 0;
        if(     (0 < index)
            &&  (index < mDetecmodeList.length))
        {
            value = mDetecmodeList[index].value;
        }

        return value;
    }



    private int getValue(Inner[] innerList, int id){
        int value = 0;

        for(int i = 0; i < innerList.length; i++)
        {
            if(innerList[i].id == id)
            {
                value = innerList[i].value;
                break;
            }
        }
        return value;
    }

    private int getId(Inner[] innerList, int value){
        int id = 0;

        for(int i = 0; i < innerList.length; i++)
        {
            if(innerList[i].value == value)
            {
                id = innerList[i].id;
                break;
            }
        }
        return id;
    }

    public class Inner {
        public int id;
        public int value;

        public Inner(int _id, int _value)
        {
            id = _id;
            value = _value;
        }
    }

    //////////////////////////////////////////////////
    //private final Inner[] mLogModeList = {
    //        new Inner(R.id.radiobutton_logmode_hide, 0),
    //        new Inner(R.id.radiobutton_logmode_open, 1)
    //};

    //public int getLogMode(int id){
    //    return getValue(mLogModeList, id);
    //}

    //public int getRadioLogId(int value) {
    //    return getId(mLogModeList, value);
    //}
    //////////////////////////////////////////////////

    //////////////////////////////////////////////////
/*    private  final Inner[] mTemperatureModeList = {
            new Inner(R.id.radiobutton_detecmode_arm, 0),
            new Inner(R.id.radiobutton_detecmode_face1, 1),
            new Inner(R.id.radiobutton_detecmode_face2, 2)
    };

    public int getTemperatureMode(int id){
        return getValue(mTemperatureModeList, id);
    }

    public int getRadioTemperatureModeId(int value){
        return getId(mTemperatureModeList, value);
    }
*/


    //////////////////////////////////////////////////
    private  final Inner[] mTemperatureUnitList = {
            new Inner(R.id.radiobutton_temperature_unit_celsius, 0),
            new Inner(R.id.radiobutton_temperature_unit_fahrenheit, 1)
    };

    public int getTemperatureUnit(int id) {
        return getValue(mTemperatureUnitList, id);
    }

    public int getRadioTemperatureUnitId(int value) {
        return getId(mTemperatureUnitList, value);
    }

    //////////////////////////////////////////////////
    private  final Inner[] mThermographyMode = {
            new Inner(R.id.radiobutton_thermographymode_hide, 0),
            new Inner(R.id.radiobutton_thermographymode_open, 1)
    };


    public int getThermographyMode(int id) {
        return  getValue(mThermographyMode, id);
    }

    public int getRadioThermographyModeId(int value) {
        return getId(mThermographyMode, value);
    }

}
