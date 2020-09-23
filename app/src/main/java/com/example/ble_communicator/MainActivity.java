package com.example.ble_communicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ble_communicator.data.DeviceSettingData;
import com.example.ble_communicator.viewmodel.MainActivityViewModel;

import java.nio.ByteBuffer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    //sato　add {
    //ViewModel
    private MainActivityViewModel viewmodel = null;
    //温度誤差
    private NumberPicker npcalibration = null;
    //高温閾値
    private NumberPicker nphightemperaturethreshold = null;
    //基準温度更新時間
    private NumberPicker npreferencetemperatureupdatetime = null;
    //sato　add }

    // 定数（Bluetooth LE Gatt UUID）
    // Private Service
    private static final UUID UUID_SERVICE_PRIVATE         = UUID.fromString( "13a28130-8883-49a8-8bdb-42bc1a7107f4" );
    private static final UUID UUID_CHARACTERISTIC_PRIVATE1 = UUID.fromString( "A2935077-201F-44EB-82E8-10CC02AD8CE1" );
    private static final UUID UUID_CHARACTERISTIC_PRIVATE2 = UUID.fromString( "FF6B1548-8FE6-11E7-ABC4-CEC278B6B50A" );

    // 定数
    private static final int REQUEST_ENABLEBLUETOOTH = 1; // Bluetooth機能の有効化要求時の識別コード
    private static final int REQUEST_CONNECTDEVICE   = 2; // デバイス接続要求時の識別コード

    // メンバー変数
    private BluetoothAdapter mBluetoothAdapter;    // BluetoothAdapter : Bluetooth処理で必要
    private String        mDeviceAddress = "";    // デバイスアドレス
    private BluetoothGatt mBluetoothGatt = null;    // Gattサービスの検索、キャラスタリスティックの読み書き

    // GUIアイテム
    private Button mButton_Connect;         // 接続ボタン
    private Button mButton_Disconnect;      // 切断ボタン
    private Button mButton_ReadData;        // キャラクタリスティック1の読み込みボタン
    private Button mButton_WriteData;       // キャラクタリスティック2への書き込みボタン

    // BluetoothGattコールバックオブジェクト
    private final BluetoothGattCallback mGattcallback = new BluetoothGattCallback()
    {
        // 接続状態変更（connectGatt()の結果として呼ばれる。）
        @Override
        public void onConnectionStateChange( BluetoothGatt gatt, int status, int newState )
        {
            if( BluetoothGatt.GATT_SUCCESS != status )
            {
                return;
            }

            if( BluetoothProfile.STATE_CONNECTED == newState )
            {    // 接続完了
                mBluetoothGatt.discoverServices();    // サービス検索
                runOnUiThread( new Runnable()
                {
                    public void run()
                    {
                        // GUIアイテムの有効無効の設定
                        // 切断ボタンを有効にする
                        mButton_Disconnect.setEnabled( true );
                    }
                } );
                return;
            }
            if( BluetoothProfile.STATE_DISCONNECTED == newState )
            {    // 切断完了（接続可能範囲から外れて切断された）
                // 接続可能範囲に入ったら自動接続するために、mBluetoothGatt.connect()を呼び出す。
                mBluetoothGatt.connect();
                runOnUiThread( new Runnable()
                {
                    public void run()
                    {
                        // GUIアイテムの有効無効の設定
                        // 読み込みボタンを無効にする（通知チェックボックスはチェック状態を維持。通知ONで切断した場合、再接続時に通知は再開するので）
                        mButton_ReadData.setEnabled( false );
                        //mButton_ReadChara2.setEnabled( false );
                    }
                } );
                return;
            }
        }

        // サービス検索が完了したときの処理（mBluetoothGatt.discoverServices()の結果として呼ばれる。）
        @Override
        public void onServicesDiscovered( BluetoothGatt gatt, int status )
        {
            if( BluetoothGatt.GATT_SUCCESS != status )
            {
                return;
            }

            // 発見されたサービスのループ
            for( BluetoothGattService service : gatt.getServices() )
            {
                // サービスごとに個別の処理
                if( ( null == service ) || ( null == service.getUuid() ) )
                {
                    continue;
                }
                UUID uuid = service.getUuid();
                if( UUID_SERVICE_PRIVATE.equals( service.getUuid() ) )
                {    // プライベートサービス
                    runOnUiThread( new Runnable()
                    {
                        public void run()
                        {
                            // GUIアイテムの有効無効の設定
                            mButton_ReadData.setEnabled( true );
                            //mButton_ReadChara2.setEnabled( true );
                            mButton_WriteData.setEnabled( true );
                            //mButton_WriteWorld.setEnabled( true );
                        }
                    } );
                    continue;
                }
            }
        }

        // キャラクタリスティックが読み込まれたときの処理
        @Override
        public void onCharacteristicRead( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status )
        {
            if( BluetoothGatt.GATT_SUCCESS != status )
            {
                return;
            }
            // キャラクタリスティックごとに個別の処理
            if( UUID_CHARACTERISTIC_PRIVATE1.equals( characteristic.getUuid() ) )
            {    // キャラクタリスティック１：データサイズは、2バイト（数値を想定。0～65,535）
                byte[]       byteChara = characteristic.getValue();
                ByteBuffer   bb        = ByteBuffer.wrap( byteChara );
                final String strChara  = String.valueOf( bb.getShort() );
                runOnUiThread( new Runnable()
                {
                    public void run()
                    {
                        // GUIアイテムへの反映
                        ( (TextView)findViewById( R.id.textview_readchara1 ) ).setText( strChara );
                    }
                } );
                return;
            }
        }

        // キャラクタリスティックが書き込まれたときの処理
        @Override
        public void onCharacteristicWrite( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status )
        {
            if( BluetoothGatt.GATT_SUCCESS != status )
            {
                return;
            }
            // キャラクタリスティックごとに個別の処理
            if( UUID_CHARACTERISTIC_PRIVATE2.equals( characteristic.getUuid() ) )
            {    // キャラクタリスティック２：データサイズは、8バイト（文字列を想定。半角文字8文字）
                runOnUiThread( new Runnable()
                {
                    public void run()
                    {
                        // GUIアイテムの有効無効の設定
                        // 書き込みボタンを有効にする
                        mButton_WriteData.setEnabled( true );
                    }
                } );
                return;
            }
        }
    };


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );


        //sato　add {
        //ViewModelのロード
        viewmodel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        //画面上のコントロールの初期設定
        //initControls();

        // Create the observer which updates the UI.
        final Observer<DeviceSettingData> settingObserver = new Observer<DeviceSettingData>() {

            @Override
            public void onChanged(DeviceSettingData deviceSettingData) {
                // Update the UI, in this case, a TextView.
                dsipDeviceSetting(deviceSettingData);
            }
        };

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewmodel.getDeviceSettingDataMutable().observe(this, settingObserver);
        //sato　add }

        // GUIアイテム
        mButton_Connect = (Button)findViewById( R.id.button_connect );
        mButton_Connect.setOnClickListener( this );
        mButton_Disconnect = (Button)findViewById( R.id.button_disconnect );
        mButton_Disconnect.setOnClickListener( this );
        mButton_ReadData = (Button)findViewById( R.id.button_readchara1 );
        mButton_ReadData.setOnClickListener( this );
        mButton_WriteData = (Button)findViewById( R.id.button_writehello );
        mButton_WriteData.setOnClickListener( this );


        // Android端末がBLEをサポートしてるかの確認
        if( !getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) )
        {
            Toast.makeText( this, R.string.ble_is_not_supported, Toast.LENGTH_SHORT ).show();
            finish();    // アプリ終了宣言
            return;
        }

        // Bluetoothアダプタの取得
        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService( Context.BLUETOOTH_SERVICE );
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if( null == mBluetoothAdapter )
        {    // Android端末がBluetoothをサポートしていない
            Toast.makeText( this, R.string.bluetooth_is_not_supported, Toast.LENGTH_SHORT ).show();
            finish();    // アプリ終了宣言
            return;
        }
    }

    // 初回表示時、および、ポーズからの復帰時
    @Override
    protected void onResume()
    {
        super.onResume();

        // Android端末のBluetooth機能の有効化要求
        requestBluetoothFeature();

        // GUIアイテムの有効無効の設定
        mButton_Connect.setEnabled( false );
        mButton_Disconnect.setEnabled( false );
        mButton_ReadData.setEnabled( false );
        //mButton_ReadChara2.setEnabled( false );
        mButton_WriteData.setEnabled( false );
        //mButton_WriteWorld.setEnabled( false );

        // デバイスアドレスが空でなければ、接続ボタンを有効にする。
        if( !mDeviceAddress.equals( "" ) )
        {
            mButton_Connect.setEnabled( true );
        }

        // 接続ボタンを押す
        mButton_Connect.callOnClick();
    }


    // 別のアクティビティ（か別のアプリ）に移行したことで、バックグラウンドに追いやられた時
    @Override
    protected void onPause()
    {
        super.onPause();

        // 切断
        disconnect();
    }

    // アクティビティの終了直前
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if( null != mBluetoothGatt )
        {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    // Android端末のBluetooth機能の有効化要求
    private void requestBluetoothFeature()
    {
        if( mBluetoothAdapter.isEnabled() )
        {
            return;
        }
        // デバイスのBluetooth機能が有効になっていないときは、有効化要求（ダイアログ表示）
        Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
        startActivityForResult( enableBtIntent, REQUEST_ENABLEBLUETOOTH );
    }

    // 機能の有効化ダイアログの操作結果
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        switch( requestCode )
        {
            case REQUEST_ENABLEBLUETOOTH: // Bluetooth有効化要求
                if( Activity.RESULT_CANCELED == resultCode )
                {    // 有効にされなかった
                    Toast.makeText( this, R.string.bluetooth_is_not_working, Toast.LENGTH_SHORT ).show();
                    finish();    // アプリ終了宣言
                    return;
                }
                break;
            case REQUEST_CONNECTDEVICE: // デバイス接続要求
                String strDeviceName;
                if( Activity.RESULT_OK == resultCode )
                {
                    // デバイスリストアクティビティからの情報の取得
                    strDeviceName = data.getStringExtra( DeviceListActivity.EXTRAS_DEVICE_NAME );
                    mDeviceAddress = data.getStringExtra( DeviceListActivity.EXTRAS_DEVICE_ADDRESS );
                }
                else
                {
                    strDeviceName = "";
                    mDeviceAddress = "";
                }
                ( (TextView)findViewById( R.id.textview_devicename ) ).setText( strDeviceName );
                ( (TextView)findViewById( R.id.textview_deviceaddress ) ).setText( mDeviceAddress );
                ( (TextView)findViewById( R.id.textview_readchara1 ) ).setText( "" );
                break;
        }
        super.onActivityResult( requestCode, resultCode, data );
    }

    // オプションメニュー作成時の処理
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.activity_main, menu );
        return true;
    }

    // オプションメニューのアイテム選択時の処理
    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.menuitem_search:
                Intent devicelistactivityIntent = new Intent( this, DeviceListActivity.class );
                startActivityForResult( devicelistactivityIntent, REQUEST_CONNECTDEVICE );
                return true;
        }
        return false;
    }

    @Override
    public void onClick( View v )
    {
        if( mButton_Connect.getId() == v.getId() )
        {
            mButton_Connect.setEnabled( false );    // 接続ボタンの無効化（連打対策）
            connect();            // 接続
            return;
        }
        if( mButton_Disconnect.getId() == v.getId() )
        {
            mButton_Disconnect.setEnabled( false );    // 切断ボタンの無効化（連打対策）
            disconnect();            // 切断
            return;
        }
        if( mButton_ReadData.getId() == v.getId() )
        {
            readCharacteristic( UUID_SERVICE_PRIVATE, UUID_CHARACTERISTIC_PRIVATE1 );
            return;
        }
        if( mButton_WriteData.getId() == v.getId() )
        {
            mButton_WriteData.setEnabled( false );    // 書き込みボタンの無効化（連打対策）
            //mButton_WriteWorld.setEnabled( false );    // 書き込みボタンの無効化（連打対策）

            //sato add {
            //現在の設定値を取得
            DeviceSettingData setdata = viewmodel.getDeviceSettingData();
            //※加藤さん、setdataから、送信するバイナリデータを作ってください
            //sato add }

            writeCharacteristic( UUID_SERVICE_PRIVATE, UUID_CHARACTERISTIC_PRIVATE2, "Hello" );
            return;
        }
    }

    // 接続
    private void connect()
    {
        if( mDeviceAddress.equals( "" ) )
        {    // DeviceAddressが空の場合は処理しない
            return;
        }

        if( null != mBluetoothGatt )
        {    // mBluetoothGattがnullでないなら接続済みか、接続中。
            return;
        }

        // 接続
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( mDeviceAddress );
        mBluetoothGatt = device.connectGatt( this, false, mGattcallback );
    }

    // 切断
    private void disconnect()
    {
        if( null == mBluetoothGatt )
        {
            return;
        }

        // 切断
        //   mBluetoothGatt.disconnect()ではなく、mBluetoothGatt.close()しオブジェクトを解放する。
        //   理由：「ユーザーの意思による切断」と「接続範囲から外れた切断」を区別するため。
        //   ①「ユーザーの意思による切断」は、mBluetoothGattオブジェクトを解放する。再接続は、オブジェクト構築から。
        //   ②「接続可能範囲から外れた切断」は、内部処理でmBluetoothGatt.disconnect()処理が実施される。
        //     切断時のコールバックでmBluetoothGatt.connect()を呼んでおくと、接続可能範囲に入ったら自動接続する。
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        // GUIアイテムの有効無効の設定
        // 接続ボタンのみ有効にする
        mButton_Connect.setEnabled( true );
        mButton_Disconnect.setEnabled( false );
        mButton_ReadData.setEnabled( false );
        //mButton_ReadChara2.setEnabled( false );
        mButton_WriteData.setEnabled( false );
        //mButton_WriteWorld.setEnabled( false );
    }

    // キャラクタリスティックの読み込み
    private void readCharacteristic( UUID uuid_service, UUID uuid_characteristic )
    {

        if( null == mBluetoothGatt )
        {
            return;
        }
        BluetoothGattCharacteristic blechar = mBluetoothGatt.getService( uuid_service ).getCharacteristic( uuid_characteristic );
        mBluetoothGatt.readCharacteristic( blechar );

        //sato　add {
        //受信したデータから設定値を取得
        //※加藤さん、受信データの内容をsetdataに格納し、setDeviceSettingDataに渡してください
        DeviceSettingData setdata = new DeviceSettingData();
        if (setdata != null) {
            viewmodel.setDeviceSettingData(setdata);
        }
        //受信に成功後、NumberPickerは設定する
        initNPControls();
        //受信したバイナリデータを変数に格納し、UIに表示する
        viewmodel.setDeviceSettingFromBLEData(null);
        //sato　add }
    }

    // キャラクタリスティックの書き込み
    private void writeCharacteristic( UUID uuid_service, UUID uuid_characteristic, String string )
    {
        if( null == mBluetoothGatt )
        {
            return;
        }

        BluetoothGattCharacteristic blechar = mBluetoothGatt.getService( uuid_service ).getCharacteristic( uuid_characteristic );
        blechar.setValue( string );
        mBluetoothGatt.writeCharacteristic( blechar );
    }

    ////////////////////////////////////////////////////////////////////////////
    // 以下　sato 実装

    /**
     * NumberPickerコントロールの初期設定を行る
     */
    private void initNPControls() {

        //温度誤差
        if (npcalibration == null) {

            npcalibration = (NumberPicker) findViewById(R.id.np_calibration);
        }
        npcalibration.setMaxValue(viewmodel.getCalibralinNPMaxvalue());
        npcalibration.setMinValue(0);
        npcalibration.setDisplayedValues(viewmodel.getCalibrationListContents());
        npcalibration.setWrapSelectorWheel(false);
        npcalibration.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                viewmodel.onCalibrationChange(oldVal, newVal);
            }
        });

        //高温閾値範囲設定
        if (nphightemperaturethreshold == null) {
            nphightemperaturethreshold = (NumberPicker) findViewById(R.id.np_hightemperaturethreshold);
        }
        nphightemperaturethreshold.setMaxValue(viewmodel.getHighTemperatureThresholdMaxvalue());
        nphightemperaturethreshold.setMinValue(0);
        nphightemperaturethreshold.setDisplayedValues(viewmodel.getHighTemperatureThresholdListContents());
        nphightemperaturethreshold.setWrapSelectorWheel(false);
        nphightemperaturethreshold.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                viewmodel.onHighTemperatureThresholdChange(oldVal, newVal);
            }
        });

        //基準温度更新時間
        if (npreferencetemperatureupdatetime == null) {
            npreferencetemperatureupdatetime = (NumberPicker) findViewById(R.id.np_referencetemperatureupdatetime);
        }
        npreferencetemperatureupdatetime.setMaxValue(viewmodel.getReferenceTemperatureUpdatetimeMaxvalue());
        npreferencetemperatureupdatetime.setMinValue(0);
        npreferencetemperatureupdatetime.setDisplayedValues(viewmodel.getReferenceTemperatureUpdatetimeListContents());
        npreferencetemperatureupdatetime.setWrapSelectorWheel(false);
        npreferencetemperatureupdatetime.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                viewmodel.onReferenceUDTimeChange(oldVal, newVal);
            }
        });
    }

    /**
     * 最新の設定を画面の各コントロールに表示する
     */
    private void dsipDeviceSetting(DeviceSettingData deviceSettingData) {

        if (npcalibration != null && nphightemperaturethreshold != null && npreferencetemperatureupdatetime != null ) {
            //キャリブレーション
            npcalibration.setValue(viewmodel.getCalibrationNPIndexOf());

            //高温閾値
            nphightemperaturethreshold.setValue(viewmodel.getHighTemperatureThresholdNPIndexOf());

            //基準温度更新時間
            npreferencetemperatureupdatetime.setValue(viewmodel.getReferenceTemperatureUpdatetimeNPIndexOf());
        }

    }
}
