package com.example.ble_communicator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

class BLEManager {

    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private Handler           mHandler;                            // UIスレッド操作ハンドラ : 「一定時間後にスキャンをやめる処理」で必要
    private Context mContext;
    private BluetoothAdapter  mBluetoothAdapter;        // BluetoothAdapter : Bluetooth処理で必要
    private boolean mScanning = false;                // スキャン中かどうかのフラグ
    private static final long   SCAN_PERIOD             = 10000;    // スキャン時間。単位はミリ秒。

    public void init(Context context)
    {
        mContext = context;

        // UIスレッド操作ハンドラの作成（「一定時間後にスキャンをやめる処理」で使用する）
        mHandler = new Handler();

        // Bluetoothアダプタの取得
        BluetoothManager bluetoothManager = (BluetoothManager)mContext.getSystemService( Context.BLUETOOTH_SERVICE );
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if( null == mBluetoothAdapter )
        {    // デバイス（＝スマホ）がBluetoothをサポートしていない
            Toast.makeText( mContext, R.string.bluetooth_is_not_supported, Toast.LENGTH_SHORT ).show();
            return;
        }

        startScan();

    }

    // スキャンの開始
    private void startScan()
    {
        // リストビューの内容を空にする。
        mDeviceList.clear();

        // BluetoothLeScannerの取得
        // ※Runnableオブジェクト内でも使用できるようfinalオブジェクトとする。
        final android.bluetooth.le.BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        if( null == scanner )
        {
            return;
        }


/*        HandlerThread handlerThread = new HandlerThread("foo");
        handlerThread.start();
        new Handler(handlerThread.getLooper()).postDelayed(new Runnable() {
            @Override
            public void run()
            {
                scanner.stopScan( mLeScanCallback );
                mScanning = false;
            }
        }, SCAN_PERIOD );

 */
        Timer timer = new Timer(false);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                scanner.stopScan( mLeScanCallback );
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                }
                mScanning = false;
            }
        };
        timer.schedule(task, SCAN_PERIOD);

        mScanning = true;
        scanner.startScan( mLeScanCallback );
    }

    public ArrayList<BluetoothDevice> getDeviceList()
    {
        return mDeviceList;
    }

    public boolean getScanning()
    {
        return mScanning;
    }



    // デバイススキャンコールバック
    private ScanCallback mLeScanCallback = new ScanCallback()
    {
        // スキャンに成功（アドバタイジングは一定間隔で常に発行されているため、本関数は一定間隔で呼ばれ続ける）
        @Override
        public void onScanResult( int callbackType, final ScanResult result )
        {
            //super.onScanResult( callbackType, result );
            //runOnUiThread( new Runnable()
            //{
            //    @Override
            //    public void run()
            //    {
            mDeviceList.add( result.getDevice() );
                    //mmDeviceList.add( result.getDevice() );
            //    }
            //} );
        }

        // スキャンに失敗
        @Override
        public void onScanFailed( int errorCode )
        {
            super.onScanFailed( errorCode );
        }
    };



}
