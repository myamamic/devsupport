package myamamic.tp.devsupport.bluetooth;


import myamamic.tp.devsupport.BaseTestActivity;
import myamamic.tp.devsupport.CountView;
import myamamic.tp.devsupport.OkDialogFragment;
import myamamic.tp.devsupport.R;
import myamamic.tp.devsupport.Utility;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.ref.WeakReference;


public class BluetoothOnOffTest extends BaseTestActivity {
    private static final String TAG = "BluetoothOnOffTest";

    private static final int EVENT_ENABLE_BT = 1;
    private static final int EVENT_DISABLE_BT = 2;

    private static final String LOG_FILE_PATH = "/sdcard/bt_loop.txt";

    private static final SparseArray<String> sBtStateMap = new SparseArray<String>() {{
        put(BluetoothAdapter.STATE_ON,          "BT ON");
        put(BluetoothAdapter.STATE_TURNING_OFF, "Disabling BT ...");
        put(BluetoothAdapter.STATE_OFF,         "BT OFF");
        put(BluetoothAdapter.STATE_TURNING_ON,  "Enabling BT ...");
    }};

    private Button mBtButton;
    private Button mBtRunningStartButton;
    private Button mBtRunningStopButton;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothBroadcastReceiver mBtReceiver = null;
    private MyHandler mHandler = null;

    private TextView mBtState = null;
    private boolean isRunningTest = false;

    private long mWaitBtDisabled = 30000L;
    private long mWaitBtEnabled = 30000L;
    private int mBtOnCount = 0;
    private CountView mBtOnCountView ;
    private int mBtOnFailCount = 0;
    private CountView mBtOnFailCountView ;
    private int mBtOffCount = 0;
    private CountView mBtOffCountView;
    private int mBtOffFailCount = 0;
    private CountView mBtOffFailCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.bluetooth_onoff_control);

        mHandler = new MyHandler(this);
        mBtReceiver = new BluetoothBroadcastReceiver();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBtReceiver, filter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter == null) {
            Log.e(TAG, "Cannot get mBtAdapter handle.");
            finish();
            return;
        }
        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBtReceiver);
        mBtReceiver = null;
        mHandler = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBtButtonState(false, false);
        updateBtRunningButtonState();
        updateCount();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isRunningTest = savedInstanceState.getBoolean("isRunning", false);
        mBtOnCount = savedInstanceState.getInt("btOnCount", 0);
        mBtOnFailCount = savedInstanceState.getInt("btOnFailCount", 0);
        mBtOffCount = savedInstanceState.getInt("btOffCount", 0);
        mBtOffFailCount = savedInstanceState.getInt("btOffFailCount", 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRunning", isRunningTest);
        outState.putInt("btOnCount", mBtOnCount);
        outState.putInt("btOnFailCount", mBtOnFailCount);
        outState.putInt("btOffCount", mBtOffCount);
        outState.putInt("btOffFailCount", mBtOffFailCount);
    }

    @Override
    public void onPositiveButtonClicked() {
        stopBluetoothRunning();
        super.onPositiveButtonClicked();
    }

    public static class MyHandler extends Handler {
        private final WeakReference<BluetoothOnOffTest> mActivity;

        public MyHandler(BluetoothOnOffTest activity) {
            mActivity = new WeakReference<BluetoothOnOffTest>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BluetoothOnOffTest activity = mActivity.get();
            if (activity == null) {
                return;
            }

            switch(msg.what) {
                case EVENT_ENABLE_BT:
                    Log.d(TAG, "HANDLE EVENT_ENABLE_BT");
                    activity.enableBluetooth(true);
                    break;
                case EVENT_DISABLE_BT:
                    Log.d(TAG, "HANDLE EVENT_DISABLE_BT");
                    activity.enableBluetooth(false);
                    break;
                default:
                    break;
            }
        }
    }

    // Oneshot bluetooth on/off
    private OnClickListener mBtButtonClickedListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mBtAdapter.isEnabled()) {
                enableBluetooth(true);
            } else {
                enableBluetooth(false);
            }
        }
    };

    // Running start or stop button
    private OnClickListener mBtRunningButtonClickedListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.bt_button_running_start) {
                startBluetoothRunning();
            } else if (v.getId() == R.id.bt_button_running_stop) {
                stopBluetoothRunning();
            }
        }
    };

    private void initialize() {
        mBtButton = (Button)findViewById(R.id.bt_button);
        mBtButton.setOnClickListener(mBtButtonClickedListener);
        updateBtButtonState(false, false);
        mBtRunningStartButton = (Button)findViewById(R.id.bt_button_running_start);
        mBtRunningStartButton.setOnClickListener(mBtRunningButtonClickedListener);
        mBtRunningStopButton = (Button)findViewById(R.id.bt_button_running_stop);
        mBtRunningStopButton.setOnClickListener(mBtRunningButtonClickedListener);
        updateBtRunningButtonState();

        mBtState = (TextView)findViewById(R.id.bt_state);
        mBtState.setText(sBtStateMap.get(mBtAdapter.getState()));

        mBtOnCountView = (CountView)findViewById(R.id.bt_on_count);
        mBtOffCountView = (CountView)findViewById(R.id.bt_off_count);
        mBtOnFailCountView = (CountView)findViewById(R.id.bt_on_fail_count);
        mBtOffFailCountView = (CountView)findViewById(R.id.bt_off_fail_count);

        ((EditText)findViewById(R.id.bt_edittext_wait_enabled)).setText(String.valueOf(mWaitBtEnabled));
        ((EditText)findViewById(R.id.bt_edittext_wait_disabled)).setText(String.valueOf(mWaitBtDisabled));
    }

    private void enableBluetooth(boolean enable) {
        if (isRunningTest) {
            if (enable) {
                if (!mBtAdapter.enable()) { // fail
                    mBtOnFailCount++;
                    mBtOnFailCountView.updateCount(mBtOnFailCount);
                }
            } else {
                if (!mBtAdapter.disable()) { // fail
                    mBtOffFailCount++;
                    mBtOffFailCountView.updateCount(mBtOffFailCount);
                }
            }
        } else { // One shot
            updateBtButtonState(true, false);

            boolean ret = enable ?  mBtAdapter.enable() : mBtAdapter.disable(); 
            if (!ret) {
                updateBtButtonState(false, false);

                Log.e(TAG, "enableBluetooth(" + enable + ") : Could not bluetooth " + (enable ? "enable" : "disable"));

                OkDialogFragment newFragment = OkDialogFragment.newInstance(
                        enable ? R.string.bt_enabling_error : R.string.bt_disabling_error);
                newFragment.show(getSupportFragmentManager(), "dialog");
            }
        }
    }

    private void startBluetoothRunning() {
        Log.d(TAG, "startBluetoothRunning() IN");
        if (isRunningTest) {
            return;
        }
        isRunningTest = true;
        updateBtRunningButtonState();
        updateBtButtonState(true, false);

        // Remove result file.
        outputBluetoothOnOffCount(true, 0, false);

        mBtOffCount = mBtOnCount = mBtOffFailCount = mBtOnFailCount = 0;
        updateCount();

        mWaitBtEnabled = Long.parseLong(((EditText)findViewById(R.id.bt_edittext_wait_enabled)).getText().toString());
        mWaitBtDisabled = Long.parseLong(((EditText)findViewById(R.id.bt_edittext_wait_disabled)).getText().toString());

        if (mBtAdapter.isEnabled()) {
            Log.d(TAG, "startBluetoothRunning() : POST EVENT_DISABLE_BT");
            mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_DISABLE_BT), 10);
        } else {
            Log.d(TAG, "startBluetoothRunning() : POST EVENT_ENABLE_BT");
            mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_ENABLE_BT), 10);
        }
    }

    private void stopBluetoothRunning() {
        Log.d(TAG, "stopBluetoothRunning() IN");
        isRunningTest = false;
        if (mHandler.hasMessages(EVENT_ENABLE_BT)) {
            mHandler.removeMessages(EVENT_ENABLE_BT);
        }
        if (mHandler.hasMessages(EVENT_DISABLE_BT)) {
            mHandler.removeMessages(EVENT_DISABLE_BT);
        }
        updateBtRunningButtonState();
        updateBtButtonState(false, false);
    }

    class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBtState.setText(sBtStateMap.get(mBtAdapter.getState()));

            if (!isRunningTest) {
                updateBtButtonState(false, false);
                return;
            }

            // Running test started
            int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (newState) {
            case BluetoothAdapter.STATE_ON:
                mBtOnCount++;
                mBtOnCountView.updateCount(mBtOnCount);
                outputBluetoothOnOffCount(true, mBtOnCount, true); // Output file
                Log.i(TAG, "BT ON count=" + mBtOnCount);
                Log.d(TAG, "BluetoothBroadcastReceiver : POST EVENT_DISABLE_BT after " 
                        + mWaitBtEnabled + "s");
                mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_DISABLE_BT), mWaitBtEnabled);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                // noting to do
                break;
            case BluetoothAdapter.STATE_OFF:
                mBtOffCount++;
                mBtOffCountView.updateCount(mBtOffCount);
                outputBluetoothOnOffCount(false, mBtOffCount, true); // Output file
                Log.i(TAG, "BT OFF count=" + mBtOffCount);
                Log.d(TAG, "BluetoothBroadcastReceiver : POST EVENT_ENABLE_BT after " 
                        + mWaitBtDisabled + "s");
                mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_ENABLE_BT), mWaitBtDisabled);
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                // noting to do
                break;
            default:
                break;
            }
        }
    }

    private void outputBluetoothOnOffCount(boolean on, int count, boolean append) {
        String result = Utility.getDate() + "    " + (on ? "ON" : "OFF") + "(" + String.valueOf(count) + ")";
        Utility.writeResultToFile(LOG_FILE_PATH, result, append);
    }

    private void updateBtButtonState(boolean force, boolean forceState) {
        mBtButton.setText(mBtAdapter.isEnabled() ? getString(R.string.bt_off) : getString(R.string.bt_on));

        // When running test started, toggle button is disabled.
        if (isRunningTest) {
            mBtButton.setEnabled(false);
            return;
        }
        // Force update
        if (force) {
            mBtButton.setEnabled(forceState);
            return;
        }
        int state = mBtAdapter.getState();
        switch (state) {
        case BluetoothAdapter.STATE_ON:
        case BluetoothAdapter.STATE_OFF:
            mBtButton.setEnabled(true);
            break;
        case BluetoothAdapter.STATE_TURNING_ON:
        case BluetoothAdapter.STATE_TURNING_OFF:
            mBtButton.setEnabled(false);
            break;
        default:
            break;
        }
    }

    private void updateBtRunningButtonState() {
        if (isRunningTest) {
            mBtRunningStartButton.setEnabled(false);
            mBtRunningStopButton.setEnabled(true);
        } else {
            mBtRunningStartButton.setEnabled(true);
            mBtRunningStopButton.setEnabled(false);
        }
    }

    private void updateCount() {
        mBtOnCountView.updateCount(mBtOnCount);
        mBtOffCountView.updateCount(mBtOffCount);
        mBtOnFailCountView.updateCount(mBtOnFailCount);
        mBtOffFailCountView.updateCount(mBtOffFailCount);
    }
}
