package com.example.ble_communicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ble_communicator.data.DeviceSettingData;
import com.example.ble_communicator.viewmodel.MainActivityViewModel;

import java.nio.ByteBuffer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    //ViewModel
    private MainActivityViewModel viewmodel = null;
    //温度誤差
    private Spinner spinner_offsetTemp = null;

    //高温閾値
    private Spinner spinner_thresholdDegree = null;

    //基準温度更新時間
    private Spinner spinner_updateTime = null;

    //private RadioGroup radiogroup_logMode = null;
    //private RadioButton radiobutton_logModeHide = null;
    //private RadioButton radiobutton_logModeOpen = null;

//    private RadioGroup radiogroup_detecMode = null;
//    private RadioButton radiobutton_detecModeArm = null;
//    private RadioButton radiobutton_detecModeFace1 = null;
//    private RadioButton radiobutton_detecModeFace2 = null;
    private Spinner spinner_detecMode = null;

    private RadioGroup radiogroup_temperatureUnit = null;
    private RadioButton radiobutton_temperatureUnitCelsius = null;
    private RadioButton radiobutton_temperatureUnitFahrenheit = null;

    private RadioGroup radiogroup_thermographyMode = null;
    private RadioButton radiobutton_thermographyModeHide = null;
    private RadioButton radiobutton_thermographyModeOpen = null;

    // 定数（Bluetooth LE Gatt UUID）
    // Private Service
    private static final UUID UUID_SERVICE_PRIVATE         = UUID.fromString( "13a28130-8883-49a8-8bdb-42bc1a7107f4" );
    private static final UUID UUID_CHARACTERISTIC_PRIVATE1 = UUID.fromString( "A2935077-201F-44EB-82E8-10CC02AD8CE1" );

    // 定数
    private static final int REQUEST_ENABLEBLUETOOTH = 1; // Bluetooth機能の有効化要求時の識別コード
    private static final int REQUEST_CONNECTDEVICE   = 2; // デバイス接続要求時の識別コード
    private static final int REQUEST_ACCESS_FINE_LOCAION  = 3; // ACCESS_FINE_LOCAION要求時の識別コード

    // メンバー変数
    private BluetoothAdapter mBluetoothAdapter;    // BluetoothAdapter : Bluetooth処理で必要
    private String        mDeviceAddress = "";    // デバイスアドレス
    private BluetoothGatt mBluetoothGatt = null;    // Gattサービスの検索、キャラスタリスティックの読み書き

    // GUIアイテム
    private Button mButton_StartScan;    //スキャン開始ボタン
    private Button mButton_Connect;         // 接続ボタン
    private Button mButton_Disconnect;      // 切断ボタン
    private Button mButton_Poweroff;        // システム終了
    private Button mButton_ReadData;        // キャラクタリスティック1の読み込みボタン
    private Button mButton_WriteData;       // キャラクタリスティック2への書き込みボタン
    private Button mButton_DisplayLog;      // ログ表示(非表示ボタン)

    private Boolean mIsDisplayLog = false;  // ログ表示画面 true:表示、false:非表示

    private Handler mHandler;               // UIスレッド操作ハンドラ : 「一定時間後にスキャンをやめる処理」で必要
    private boolean mCommunicating = false;      // スキャン中かどうかのフラグ
    private static final int COMMUNICATE_PERIOD = 5000;    // 単位はミリ秒。

    // データ識別
    private final byte DATAID_SETTING = 0;
    private final byte DATAID_SYSTEM_END = 2;
    private final byte DATAID_LOG = 3;

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
            //if( BluetoothProfile.STATE_DISCONNECTED == newState )
            //{    // 切断完了（接続可能範囲から外れて切断された）
            //    // 接続可能範囲に入ったら自動接続するために、mBluetoothGatt.connect()を呼び出す。
            //    mBluetoothGatt.connect();
            //    dispMessage("接続可能範囲から外れて切断されました。\n自動接続処理を行います。");
            //    mCommunicating = true;
            //    runOnUiThread( new Runnable()
            //    {
            //        public void run()
            //        {
            //            // GUIアイテムの有効無効の設定
            //            // 読み込みボタンを無効にする（通知チェックボックスはチェック状態を維持。通知ONで切断した場合、再接続時に通知は再開するので）
            //            mButton_ReadData.setEnabled( false );
            //            //mButton_ReadChara2.setEnabled( false );
            //        }
            //    } );
            //    return;
            //}
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
                {
                    mCommunicating = false;

                    //dispMessage("サービスを開始します");
                    // プライベートサービス
                    runOnUiThread( new Runnable()
                    {
                        public void run()
                        {
                            // GUIアイテムの有効無効の設定
                            mButton_Poweroff.setEnabled(true );
                            mButton_ReadData.setEnabled( true );
                            mButton_DisplayLog.setEnabled( true );
                            dispMessage(getString(R.string.toast_connect));
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

            // メッセージを表示
            // キャラクタリスティックごとにメッセージを表示したい時は、修正が必要。

            // キャラクタリスティックごとに個別の処理
            if( UUID_CHARACTERISTIC_PRIVATE1.equals( characteristic.getUuid() ) )
            {    // キャラクタリスティック１：データサイズは、2バイト（数値を想定。0～65,535）

                mCommunicating = false;

                byte[] byteDataList = characteristic.getValue();

                //受信したデータから設定値を取得
                DeviceSettingData setData = new DeviceSettingData();
                if (setData != null) {
                    setData.setByteDataList(byteDataList);
                    viewmodel.setDeviceSettingData(setData);
                }
                runOnUiThread( new Runnable()
                {
                    public void run()
                    {

                        mButton_WriteData.setEnabled(true);
                        //受信に成功後、Enableにする
                        setEnableNP(true);
                        dispMessage(getString(R.string.toast_setting_receive));
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
            if( UUID_CHARACTERISTIC_PRIVATE1.equals( characteristic.getUuid() ) )
            {    // キャラクタリスティック２：データサイズは、8バイト（文字列を想定。半角文字8文字）

                mCommunicating = false;

                byte[] byteDataList = characteristic.getValue();
                if(byteDataList[0] == DATAID_SETTING)
                {
                    runOnUiThread( new Runnable()
                    {
                        public void run()
                        {
                            // GUIアイテムの有効無効の設定
                            mButton_StartScan.setEnabled( true );
                            mButton_Disconnect.setEnabled( true );
                            mButton_Poweroff.setEnabled( true );
                            mButton_ReadData.setEnabled( true );
                            mButton_DisplayLog.setEnabled( true );
                            setEnableNP(false);
                            dispMessage(getString(R.string.toast_setting_send));
                        }
                    } );
                }
                else if(byteDataList[0] == DATAID_SYSTEM_END) {

                    //runOnUiThread( new Runnable()
                    //{
                    //    public void run()
                    //    {
                    //        dispMessage(getString(R.string.toast_system_off));
                    //    }
                    //} );
                }
                else if(byteDataList[0] == DATAID_LOG)
                {
                    // ログ画面非表示　→　表示
                    if(mIsDisplayLog == false)
                    {
                        mIsDisplayLog = true;
                        runOnUiThread( new Runnable()
                        {
                            public void run()
                            {
                                // GUIアイテムの有効無効の設定
                                mButton_DisplayLog.setEnabled( true );
                                //mButton_DisplayLog.setText(getString(R.string.display_log_hide));
                                setEnableNP(false);
                                dispMessage(getString(R.string.toast_disp_show));
                            }
                        } );
                    }
                    // 表示　→　ログ画面非表示
                    else
                    {
                        mIsDisplayLog = false;
                        runOnUiThread( new Runnable()
                        {
                            public void run()
                            {
                                // GUIアイテムの有効無効の設定
                                mButton_StartScan.setEnabled( true );
                                mButton_Disconnect.setEnabled( true );
                                mButton_Poweroff.setEnabled( true );
                                mButton_ReadData.setEnabled( true );
                                mButton_DisplayLog.setEnabled( true );
                                //mButton_DisplayLog.setText(getString(R.string.display_log_hide));
                                setEnableNP(false);
                                dispMessage(getString(R.string.toast_disp_hide));
                            }
                        } );
                    }
                }

                return;
            }
        }
    };


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        //ViewModelのロード
        viewmodel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        //画面上のコントロールの初期設定
        initControls();

        // Livedata受け取りをUIに反映
        final Observer<DeviceSettingData> settingObserver = new Observer<DeviceSettingData>() {

            @Override
            public void onChanged(DeviceSettingData deviceSettingData) {
                dsipDeviceSetting(deviceSettingData);
            }
        };

        // Livedata受け取り
        viewmodel.getDeviceSettingDataMutable().observe(this, settingObserver);

        //Buttonリスナセット
        mButton_StartScan = (Button)findViewById( R.id.button_startscan );
        mButton_StartScan.setOnClickListener( this );
        mButton_Connect = (Button)findViewById( R.id.button_connect );
        mButton_Connect.setOnClickListener( this );
        mButton_Disconnect = (Button)findViewById( R.id.button_disconnect );
        mButton_Disconnect.setOnClickListener( this );
        mButton_Poweroff = (Button)findViewById( R.id.button_poweroff );
        mButton_Poweroff.setOnClickListener( this );
        mButton_ReadData = (Button)findViewById( R.id.button_receive_settings );
        mButton_ReadData.setOnClickListener( this );
        mButton_WriteData = (Button)findViewById( R.id.button_send_settings );
        mButton_WriteData.setOnClickListener( this );
        mButton_DisplayLog =(Button)findViewById(R.id.button_display_log);
        mButton_DisplayLog.setOnClickListener( this );

        mHandler = new Handler();

        //ACCESS_FINE_LOCATIONの許可取得
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCAION);
            return;
        }

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
        initButton();
        //mButton_StartScan.setEnabled( true );
        //mButton_Connect.setEnabled( false );
        //mButton_Disconnect.setEnabled( false );
        //mButton_Poweroff.setEnabled( false );
        //mButton_ReadData.setEnabled( false );
        //mButton_WriteData.setEnabled( false );
        //mButton_DisplayLog.setEnabled( false );

        // デバイスアドレスが空でなければ、接続ボタンを有効にする。
        if( !mDeviceAddress.equals( "" ) )
        {
            mButton_Connect.setEnabled( true );
            // 接続ボタンを押す
            //mButton_Connect.callOnClick();
        }
    }

    // 別のアクティビティ（か別のアプリ）に移行したことで、バックグラウンドに追いやられた時
    @Override
    protected void onPause()
    {
        super.onPause();

        // 通信の切断をするため、通信の監視をやめる
        mHandler.removeCallbacksAndMessages( null );
        // 切断
        disconnect();

        mButton_StartScan.setEnabled( true );
        mButton_Poweroff.setEnabled( false );
        mButton_ReadData.setEnabled( false );
        mButton_WriteData.setEnabled( false );
        mButton_DisplayLog.setEnabled( false );

        //コネクト状態 + 受信後の場合のみ、ユーザはNumberPickerから設定可能とする
        setEnableNP(false);

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
                    strDeviceName = strDeviceName +"(" + mDeviceAddress + ")";
                }
                else
                {
                    strDeviceName = "";
                    mDeviceAddress = "";
                }
                ( (TextView)findViewById( R.id.textview_devicename ) ).setText( strDeviceName );
                //( (TextView)findViewById( R.id.textview_deviceaddress ) ).setText( mDeviceAddress );
                //( (TextView)findViewById( R.id.textview_readchara1 ) ).setText( "" );
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
    /*
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
    */

    @Override
    public void onClick( View v )
    {
        if( mButton_StartScan.getId() == v.getId() )//スキャン開始
        {
            Intent devicelistactivityIntent = new Intent( this, DeviceListActivity.class );
            devicelistactivityIntent.putExtra("UUID_SERVICE", UUID_SERVICE_PRIVATE);
            startActivityForResult( devicelistactivityIntent, REQUEST_CONNECTDEVICE );
            return;
        }

        // 通信状況を監視する
        mHandler.postDelayed( new Runnable()
        {
            @Override
            public void run()
            {
                if(mCommunicating == true) {
                    dispMessage(getString(R.string.toast_communicating_error));
                    mCommunicating = false;
                    //mIsDisplayLog = false;

                    runOnUiThread( new Runnable()
                    {
                        public void run()
                        {
                            // GUIアイテムの有効無効の設定
                            initButton();
                            //mButton_StartScan.setEnabled( true );
                            //mButton_Connect.setEnabled( false );
                            //mButton_Disconnect.setEnabled( false );
                            //mButton_Poweroff.setEnabled( false );
                            //mButton_ReadData.setEnabled( false );
                            //mButton_WriteData.setEnabled( false );
                            //mButton_DisplayLog.setEnabled( false );
                            setEnableNP(false);

                            mDeviceAddress = "";
                            ( (TextView)findViewById( R.id.textview_devicename ) ).setText("");
                        }
                    } );

                }
            }
        }, COMMUNICATE_PERIOD );


        if( mButton_Connect.getId() == v.getId() )
        {
            connect();            // 接続
            //ボタンの有効無効設定
            mButton_Connect.setEnabled( false );
            mButton_StartScan.setEnabled( true );

            return;
        }

        if( mButton_Disconnect.getId() == v.getId() )
        {
            mButton_Disconnect.setEnabled( false );    // 切断ボタンの無効化（連打対策）
            disconnect();            // 切断

            dispMessage(getString(R.string.toast_disconnect));
            mButton_StartScan.setEnabled( true );
            mButton_Poweroff.setEnabled( false );
            mButton_ReadData.setEnabled( false );
            mButton_WriteData.setEnabled( false );
            mButton_DisplayLog.setEnabled( false );

            //コネクト状態 + 受信後の場合のみ、ユーザはNumberPickerから設定可能とする
            setEnableNP(false);

            return;
        }
        if( mButton_Poweroff.getId() == v.getId() )
        {
            // ダイアログ表示
            new AlertDialog.Builder(this)
                    .setTitle(R.string.systemoff_title)
                    .setMessage(R.string.systemoff_message)
                    .setPositiveButton(R.string.systemoff_yes, mSystemOffClickListener)
                    .setNegativeButton(R.string.systemoff_cancel, null)
                    .show();
            return;
        }
        if( mButton_ReadData.getId() == v.getId() )
        {
            mButton_ReadData.setEnabled( false );

            mButton_StartScan.setEnabled( false );
            mButton_Connect.setEnabled( false );
            mButton_Disconnect.setEnabled( false );
            mButton_Poweroff.setEnabled(false);
            mButton_DisplayLog.setEnabled( false );

            readCharacteristic( UUID_SERVICE_PRIVATE, UUID_CHARACTERISTIC_PRIVATE1 );
            return;
        }
        if( mButton_WriteData.getId() == v.getId() )
        {
            mButton_WriteData.setEnabled( false );    // 書き込みボタンの無効化（連打対策）
            setEnableNP(false);

            //現在の設定値を取得
            DeviceSettingData setData = viewmodel.getDeviceSettingData();
            byte[] dataList = setData.createByteDataList();
            dataList[0] = DATAID_SETTING;

            // FW用のテストコード
            //dataList[1] = 61;   dataList[2] = 106;  dataList[3] = 61; dataList[4] = 2;
            //dataList[5] = 3;    dataList[6] = 2; dataList[7] = 2;

            writeCharacteristic( UUID_SERVICE_PRIVATE, UUID_CHARACTERISTIC_PRIVATE1, dataList );
            return;
        }
        if( mButton_DisplayLog.getId() == v.getId())
        {
            // ログ画面非表示の時
            if(mIsDisplayLog == false) {

                mButton_DisplayLog.setEnabled(false);

                mButton_StartScan.setEnabled(false);
                mButton_Connect.setEnabled(false);
                mButton_Disconnect.setEnabled(false);
                mButton_Poweroff.setEnabled(false);
                mButton_ReadData.setEnabled(false);
                mButton_WriteData.setEnabled(false);
            }
            // ログ画面非表示の時
            else {
                //mIsDisplayLog = false;
            }

            // mButton_DisplayLog.setEnabled( false );    // 書き込みボタンの無効化（連打対策）
            // バイトデータを送る場合は、配列を使用しなくてはいけないので 2 byte として送る
            byte[] dataList = new byte[2];
            dataList[0] = DATAID_LOG; //ログ表示(非表示)を意味する

            writeCharacteristic( UUID_SERVICE_PRIVATE, UUID_CHARACTERISTIC_PRIVATE1, dataList );

        }
    }

    // システム終了の処理
    private DialogInterface.OnClickListener mSystemOffClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mButton_Poweroff.setEnabled(false);
            // バイトデータを送る場合は、配列を使用しなくてはいけないので 2 byte として送る
            byte[] dataList = new byte[2];
            dataList[0] = DATAID_SYSTEM_END; //終了を意味する

            writeCharacteristic(UUID_SERVICE_PRIVATE, UUID_CHARACTERISTIC_PRIVATE1, dataList);

            initButton();
            //mButton_StartScan.setEnabled(true);
            //mButton_Connect.setEnabled(false);
            //mButton_Disconnect.setEnabled(false);
            //mButton_ReadData.setEnabled(false);
            //mButton_WriteData.setEnabled(false);
            //mButton_DisplayLog.setEnabled(false);
            setEnableNP(false);

            mDeviceAddress = "";
            disconnect();
            ( (TextView)findViewById( R.id.textview_devicename ) ).setText("");
            dispMessage(getString(R.string.toast_system_off));
        }
    };


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
        mCommunicating = true;
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
        // デバイスアドレスが空でなければ、接続ボタンを有効にする。
        if( !mDeviceAddress.equals( "" ) )
        {
            mButton_Connect.setEnabled( true );
        }
    }

    // キャラクタリスティックの読み込み
    private void readCharacteristic( UUID uuid_service, UUID uuid_characteristic )
    {
        if( null == mBluetoothGatt )
        {
            return;
        }
        BluetoothGattCharacteristic blechar = mBluetoothGatt.getService( uuid_service ).getCharacteristic( uuid_characteristic );
        mCommunicating = true;
        boolean res = mBluetoothGatt.readCharacteristic( blechar );
    }

    // キャラクタリスティックの書き込み
    private void writeCharacteristic( UUID uuid_service, UUID uuid_characteristic, byte[] dataList )
    {
        if( null == mBluetoothGatt )
        {
            return;
        }

        BluetoothGattCharacteristic blechar = mBluetoothGatt.getService( uuid_service ).getCharacteristic( uuid_characteristic );
        blechar.setValue( dataList );
        if(dataList[0] != DATAID_SYSTEM_END)
        {
            mCommunicating = true;
        }
        boolean res = mBluetoothGatt.writeCharacteristic( blechar );
    }

    private void initButton(){

        mButton_StartScan.setEnabled(true);
        mButton_Connect.setEnabled(false);
        mButton_Disconnect.setEnabled(false);
        mButton_Poweroff.setEnabled(false);
        mButton_ReadData.setEnabled(false);
        mButton_WriteData.setEnabled(false);
        mButton_DisplayLog.setEnabled(false);

        return;
    }

    /**
     * NumberPickerコントロールの初期設定を行る
     */
    private void initControls() {

        //温度誤差
        if(spinner_offsetTemp == null) {
            spinner_offsetTemp = (Spinner) findViewById(R.id.spinner_offsettemp);
        }
        spinner_offsetTemp.setAdapter(setAdapter(viewmodel.getOffsetTempList()));
        spinner_offsetTemp.setOnItemSelectedListener(mItemSelectedListener);

        //高温閾値範囲設定

        if(spinner_thresholdDegree == null) {
            spinner_thresholdDegree = (Spinner) findViewById(R.id.spinner_thresholddegree);
        }
        spinner_thresholdDegree.setAdapter((setAdapter(viewmodel.getThresholddegreeList())));
        spinner_thresholdDegree.setOnItemSelectedListener(mItemSelectedListener);


        //基準温度更新時間
        if(spinner_updateTime == null) {
            spinner_updateTime = (Spinner) findViewById(R.id.spinner_updatetime);
        }
        spinner_updateTime.setAdapter((setAdapter(viewmodel.getUpdateTimeList())));
        spinner_updateTime.setOnItemSelectedListener(mItemSelectedListener);


        //////////
        //if(radiogroup_logMode == null){
        //    radiogroup_logMode = (RadioGroup)findViewById(R.id.radiogroup_logmode);
        //}

        //if(radiobutton_logModeHide == null){
        //    radiobutton_logModeHide = (RadioButton)findViewById(R.id.radiobutton_logmode_hide);
        //}
        //radiobutton_logModeHide.setOnClickListener(radioLogModeListener);

        //if(radiobutton_logModeOpen == null){
        //    radiobutton_logModeOpen = (RadioButton)findViewById(R.id.radiobutton_logmode_open);
        //}
        //radiobutton_logModeOpen.setOnClickListener(radioLogModeListener);

        //////////
/*        if(radiogroup_detecMode == null){
            radiogroup_detecMode = (RadioGroup)findViewById(R.id.radiogroup_detecmode);
        }

        if(radiobutton_detecModeArm == null){
            radiobutton_detecModeArm = (RadioButton)findViewById(R.id.radiobutton_detecmode_arm);
        }
        radiobutton_detecModeArm.setOnClickListener(radioDetecModeListener);

        if(radiobutton_detecModeFace1 == null){
            radiobutton_detecModeFace1 = (RadioButton)findViewById(R.id.radiobutton_detecmode_face1);
        }
        radiobutton_detecModeFace1.setOnClickListener(radioDetecModeListener);

        if(radiobutton_detecModeFace2 == null){
            radiobutton_detecModeFace2 = (RadioButton)findViewById(R.id.radiobutton_detecmode_face2);
        }
        radiobutton_detecModeFace2.setOnClickListener(radioDetecModeListener);
*/
        if(spinner_detecMode == null) {
            spinner_detecMode = (Spinner) findViewById(R.id.spinner_detecmode);
        }
        spinner_detecMode.setAdapter((setAdapter(viewmodel.getDetecModeList())));
        spinner_detecMode.setOnItemSelectedListener(mItemSelectedListener);

        //////////
        if(radiogroup_temperatureUnit == null){
            radiogroup_temperatureUnit = (RadioGroup)findViewById(R.id.radiogroup_temperature_unit);
        }

        if(radiobutton_temperatureUnitCelsius == null){
            radiobutton_temperatureUnitCelsius = (RadioButton)findViewById(R.id.radiobutton_temperature_unit_celsius);
        }
        radiobutton_temperatureUnitCelsius.setOnClickListener(radioTemperatureUnitListener);

        if(radiobutton_temperatureUnitFahrenheit == null){
            radiobutton_temperatureUnitFahrenheit = (RadioButton)findViewById(R.id.radiobutton_temperature_unit_fahrenheit);
        }
        radiobutton_temperatureUnitFahrenheit.setOnClickListener(radioTemperatureUnitListener);

        //////////
        if(radiogroup_thermographyMode == null){
            radiogroup_thermographyMode = (RadioGroup)findViewById(R.id.radiogroup_thermographymode);
        }

        if(radiobutton_thermographyModeHide == null){
            radiobutton_thermographyModeHide = (RadioButton)findViewById(R.id.radiobutton_thermographymode_hide);
        }
        radiobutton_thermographyModeHide.setOnClickListener(radiogroupThermographyModeListener);

        if(radiobutton_thermographyModeOpen == null){
            radiobutton_thermographyModeOpen = (RadioButton)findViewById(R.id.radiobutton_thermographymode_open);
        }
        radiobutton_thermographyModeOpen.setOnClickListener(radiogroupThermographyModeListener);

        setEnableNP(false);
    }

    private ArrayAdapter setAdapter(String[] spinnerItemArray ){
        // とりあえず、adapterの中身はなしでインスタンスを生成
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        // 自分でspinnerの中身の配列を定義しています。
        // adapterに中身をセット
        for(String targetStr : spinnerItemArray) {
            adapter.add(targetStr);
        }

        return adapter;
    }

    private AdapterView.OnItemSelectedListener mItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Spinner spinner = (Spinner) adapterView;
            int nview = spinner.getSelectedItemPosition();

            if(spinner == spinner_offsetTemp)
            {
                viewmodel.onOffsetTempSelectedListener(nview);
            }
            else if(spinner == spinner_thresholdDegree)
            {
                viewmodel.onThresholddegreeSelectedListener(nview);
            }
            else if(spinner == spinner_updateTime)
            {
                viewmodel.onUpdateTimeSelectedListener(nview);
            }
            else if(spinner == spinner_detecMode)
            {
                viewmodel.onDetecModeSelectedListener(nview);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };


    /**
     * NumberPickerコントロールのEnable/Disable設定
     * @param enable
     */
    void setEnableNP(boolean enable) {

        if(spinner_offsetTemp != null) {
            spinner_offsetTemp.setEnabled(enable);
        }

        if(spinner_thresholdDegree != null) {
            spinner_thresholdDegree.setEnabled(enable);
        }

        if (spinner_updateTime != null ) {
            spinner_updateTime.setEnabled(enable);
        }

        //if(radiobutton_logModeHide != null){
        //    radiobutton_logModeHide.setEnabled(enable);
        //}

        //if(radiobutton_logModeOpen != null){
        //    radiobutton_logModeOpen.setEnabled(enable);
        //}


        if(spinner_detecMode != null){
            spinner_detecMode.setEnabled(enable);
        }
/*        if(radiobutton_detecModeArm != null){
            radiobutton_detecModeArm.setEnabled(enable);
        }

        if(radiobutton_detecModeFace1 != null){
            radiobutton_detecModeFace1.setEnabled(enable);
        }

        if(radiobutton_detecModeFace2 != null){
            radiobutton_detecModeFace2.setEnabled(enable);
        }
*/
        if(radiobutton_temperatureUnitCelsius != null){
            radiobutton_temperatureUnitCelsius.setEnabled(enable);
        }

        if(radiobutton_temperatureUnitFahrenheit != null){
            radiobutton_temperatureUnitFahrenheit.setEnabled(enable);
        }

        if(radiobutton_thermographyModeHide != null){
            radiobutton_thermographyModeHide.setEnabled(enable);
        }

        if(radiobutton_thermographyModeOpen != null){
            radiobutton_thermographyModeOpen.setEnabled(enable);
        }

    }

    /**
     * 最新の設定を画面の各コントロールに表示する
     */
    private void dsipDeviceSetting(DeviceSettingData deviceSettingData) {
        int id = 0;


        if(spinner_offsetTemp != null) {
            spinner_offsetTemp.setSelection(viewmodel.getOffsetTempSpinnerIndex());
        }

        if(spinner_thresholdDegree != null) {
            spinner_thresholdDegree.setSelection(viewmodel.getThresholddegreeSpinnerIndex());
        }

        if(spinner_updateTime != null) {
            spinner_updateTime.setSelection(viewmodel.getUpdateTimeSpinnerIndex());
        }


        //if(         (radiogroup_logMode != null)
        //        &&  (radiobutton_logModeHide != null)
        //        &&  (radiobutton_logModeOpen != null))
        //{
        //    id = viewmodel.getCheckedRadioLogId();
        //    if (id != 0) {
        //        radiogroup_logMode.check(id);
        //    }
        //}

/*        if(         (radiogroup_detecMode != null)
                &&  (radiobutton_detecModeArm != null)
                &&  (radiobutton_detecModeFace1 != null)
                &&  (radiobutton_detecModeFace2 != null))
        {
            id = viewmodel.getCheckedRadioDetecmodeId();
            if(id != 0){
                radiogroup_detecMode.check(id);
            }
        }
 */

        if(spinner_detecMode != null) {
            spinner_detecMode.setSelection(viewmodel.getDetecModeSpinnerIndex());
        }

        if(         (radiogroup_temperatureUnit != null)
                &&  (radiobutton_temperatureUnitCelsius != null)
                &&  (radiobutton_temperatureUnitFahrenheit != null))
        {
            id = viewmodel.getCheckedRadioTemperatureUnit();
            if(id != 0){
                radiogroup_temperatureUnit.check(id);
            }
        }

        if(         (radiogroup_thermographyMode != null)
                &&  (radiobutton_thermographyModeHide != null)
                &&  (radiobutton_thermographyModeOpen != null))
        {
            id = viewmodel.getCheckedRadioThermographyMode();
            if(id != 0){
                radiogroup_thermographyMode.check(id);
            }

        }

    }

    //private View.OnClickListener radioLogModeListener = new View.OnClickListener() {
    //    @Override
    //    public void onClick(View v) {
    //        if (radiogroup_logMode == null) {
    //            return;
    //        }
    //        int selectedId = radiogroup_logMode.getCheckedRadioButtonId();
    //        viewmodel.setOnClickLog(selectedId);
    //    }
    //};

    /*
    private View.OnClickListener radioDetecModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (radiogroup_detecMode == null) {
                return;
            }
            int selectedId = radiogroup_detecMode.getCheckedRadioButtonId();
            viewmodel.setOnClickDetecMode(selectedId);
        }
    };
    */

    private View.OnClickListener radioTemperatureUnitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (radiogroup_temperatureUnit == null) {
                return;
            }
            int selectedId = radiogroup_temperatureUnit.getCheckedRadioButtonId();
            viewmodel.setOnClickTemperatureUnit(selectedId);
        }
    };

    private View.OnClickListener radiogroupThermographyModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (radiogroup_thermographyMode == null) {
                return;
            }
            int selectedId = radiogroup_thermographyMode.getCheckedRadioButtonId();
            viewmodel.setOnClickThermographyMode(selectedId);
        }
    };


    private void dispMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        // 位置調整
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
