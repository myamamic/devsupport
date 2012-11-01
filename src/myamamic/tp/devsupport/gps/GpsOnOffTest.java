package myamamic.tp.devsupport.gps;

import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import myamamic.tp.devsupport.*;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class GpsOnOffTest extends BaseTestActivity {
    private static final String TAG = "GpsOnOffTest";

    private static final String LOG_FILE_PATH = "/sdcard/gps_loop.txt";

    private static final int EVENT_ENABLE_GPS  = 1;
    private static final int EVENT_DISABLE_GPS = 2;

    @SuppressWarnings("serial")
    private static final HashMap<Boolean, String> sGpsStateMap = new HashMap<Boolean, String>() {{
        put(Boolean.TRUE,   "GPS ON");
        put(Boolean.FALSE,  "GPS OFF");
    }};

    private Button mGpsButton;
    private Button mGpsRunningStartButton;
    private Button mGpsRunningStopButton;

    // These provide support for receiving notification when Location Manager settings change.
    // This is necessary because the Network Location Provider can change settings
    // if the user does not confirm enabling the provider.
    private ContentQueryMap mContentQueryMap;

    private TextView mGpsState = null;
    private boolean isRunningTest = false;

    private long mWaitGpsDisabled = 30000L;
    private long mWaitGpsEnabled = 30000L;
    private int mGpsOnCount = 0;
    private CountView mGpsOnCountView ;
    private int mGpsOnFailCount = 0;
    private CountView mGpsOnFailCountView ;
    private int mGpsOffCount = 0;
    private CountView mGpsOffCountView;
    private int mGpsOffFailCount = 0;
    private CountView mGpsOffFailCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.gps_onoff_control);
        initialize();
    }

    private void initialize() {
        mGpsButton = (Button)findViewById(R.id.gps_button);
        mGpsButton.setOnClickListener(mGpsButtonClickedListener);
        updateGpsButtonState(false, false);
        mGpsRunningStartButton = (Button)findViewById(R.id.gps_button_running_start);
        mGpsRunningStartButton.setOnClickListener(mGpsRunningButtonClickedListener);
        mGpsRunningStopButton = (Button)findViewById(R.id.gps_button_running_stop);
        mGpsRunningStopButton.setOnClickListener(mGpsRunningButtonClickedListener);
        updateGpsRunningButtonState();

        mGpsState = (TextView)findViewById(R.id.gps_state);
        mGpsState.setText(sGpsStateMap.get(Boolean.valueOf(isGpsEnabled())));

        mGpsOnCountView = (CountView)findViewById(R.id.gps_on_count);
        mGpsOffCountView = (CountView)findViewById(R.id.gps_off_count);
        mGpsOnFailCountView = (CountView)findViewById(R.id.gps_on_fail_count);
        mGpsOffFailCountView = (CountView)findViewById(R.id.gps_off_fail_count);

        ((EditText)findViewById(R.id.gps_edittext_wait_enabled)).setText(String.valueOf(mWaitGpsEnabled));
        ((EditText)findViewById(R.id.gps_edittext_wait_disabled)).setText(String.valueOf(mWaitGpsDisabled));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // listen for Location Manager settings changes
        Cursor settingsCursor = getContentResolver().query(Settings.Secure.CONTENT_URI, null,
                "(" + Settings.System.NAME + "=?)",
                new String[]{Settings.Secure.LOCATION_PROVIDERS_ALLOWED},
                null);
        mContentQueryMap = new ContentQueryMap(settingsCursor, Settings.System.NAME, true, null);
        mContentQueryMap.addObserver(mGpsObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mContentQueryMap.deleteObserver(mGpsObserver);
        mContentQueryMap.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGpsButtonState(false, false);
        updateGpsRunningButtonState();
        updateCount();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isRunningTest = savedInstanceState.getBoolean("isRunning", false);
        mGpsOnCount = savedInstanceState.getInt("gpsOnCount", 0);
        mGpsOnFailCount = savedInstanceState.getInt("gpsOnFailCount", 0);
        mGpsOffCount = savedInstanceState.getInt("gpsOffCount", 0);
        mGpsOffFailCount = savedInstanceState.getInt("gpsOffFailCount", 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRunning", isRunningTest);
        outState.putInt("gpsOnCount", mGpsOnCount);
        outState.putInt("gpsOnFailCount", mGpsOnFailCount);
        outState.putInt("gpsOffCount", mGpsOffCount);
        outState.putInt("gpsOffFailCount", mGpsOffFailCount);
    }

    @Override
    public void onPositiveButtonClicked() {
        stopGpsRunning();
        super.onPositiveButtonClicked();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case EVENT_ENABLE_GPS:
                    Log.d(TAG, "HANDLE EVENT_ENABLE_GPS");
                    enableGps(true);
                    break;
                case EVENT_DISABLE_GPS:
                    Log.d(TAG, "HANDLE EVENT_DISABLE_GPS");
                    enableGps(false);
                    break;
                default:
                    break;
            }
        }
    };

    private boolean isGpsEnabled() {
        ContentResolver res = getContentResolver();
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                res, LocationManager.GPS_PROVIDER);
        return gpsEnabled;
    }

    private boolean setGpsProviderEnabled(boolean enable) {
        try {
            Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                    LocationManager.GPS_PROVIDER, enable);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // Oneshot gps on/off
    private View.OnClickListener mGpsButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isGpsEnabled()) {
                enableGps(true);
            } else {
                enableGps(false);
            }
        }
    };

    // Running start or stop button
    private View.OnClickListener mGpsRunningButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.gps_button_running_start) {
                startGpsRunning();
            } else if (v.getId() == R.id.gps_button_running_stop) {
                stopGpsRunning();
            }
        }
    };

    private void enableGps(boolean enable) {
        if (isRunningTest) {
            if (!setGpsProviderEnabled(enable)) { // fail
                if (enable) {
                    mGpsOnFailCount++;
                    mGpsOnFailCountView.updateCount(mGpsOnFailCount);
                } else {
                    mGpsOffFailCount++;
                    mGpsOffFailCountView.updateCount(mGpsOffFailCount);
                }
            }
        } else { // One shot
            updateGpsButtonState(true, false);

            boolean ret = setGpsProviderEnabled(enable);
            if (!ret) {
                updateGpsButtonState(false, false);
                Log.e(TAG, "enableGps(" + enable + ") : Could not gps " + (enable ? "enable" : "disable"));

                OkDialogFragment newFragment = OkDialogFragment.newInstance(
                        enable ? R.string.gps_enabling_error : R.string.gps_disabling_error);
                newFragment.show(getSupportFragmentManager(), "dialog");
            }
        }
    }

    private void startGpsRunning() {
        Log.d(TAG, "startGpsRunning() IN");
        if (isRunningTest) {
            return;
        }
        isRunningTest = true;
        updateGpsRunningButtonState();
        updateGpsButtonState(true, false);

        // Remove result file.
        outputGpsOnOffCount(true, 0, false);

        mGpsOffCount = mGpsOnCount = mGpsOffFailCount = mGpsOnFailCount = 0;
        updateCount();

        mWaitGpsEnabled = Long.parseLong(((EditText)findViewById(R.id.gps_edittext_wait_enabled)).getText().toString());
        mWaitGpsDisabled = Long.parseLong(((EditText)findViewById(R.id.gps_edittext_wait_disabled)).getText().toString());

        if (isGpsEnabled()) {
            Log.d(TAG, "startGpsRunning() : POST EVENT_DISABLE_GPS");
            mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_DISABLE_GPS), 10);
        } else {
            Log.d(TAG, "startGpsRunning() : POST EVENT_ENABLE_GPS");
            mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_ENABLE_GPS), 10);
        }
    }

    private void stopGpsRunning() {
        Log.d(TAG, "stopGpsRunning() IN");
        isRunningTest = false;
        if (mHandler.hasMessages(EVENT_ENABLE_GPS)) {
            mHandler.removeMessages(EVENT_ENABLE_GPS);
        }
        if (mHandler.hasMessages(EVENT_DISABLE_GPS)) {
            mHandler.removeMessages(EVENT_DISABLE_GPS);
        }
        updateGpsRunningButtonState();
        updateGpsButtonState(false, false);
    }

    private Observer mGpsObserver = new Observer() {
        public void update(Observable o, Object arg) {
            //updateLocationToggles();

            boolean gpsEnabled = isGpsEnabled();
            mGpsState.setText(sGpsStateMap.get(gpsEnabled));

            if (!isRunningTest) {
                updateGpsButtonState(false, false);
                return;
            }

            // Running test started
            if (gpsEnabled) {
                mGpsOnCount++;
                mGpsOnCountView.updateCount(mGpsOnCount);
                outputGpsOnOffCount(true, mGpsOnCount, true); // Output file
                Log.i(TAG, "GPS ON count=" + mGpsOnCount);
                Log.d(TAG, "GpsObserver : POST EVENT_DISABLE_GPS after "
                        + mWaitGpsEnabled + "s");
                mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_DISABLE_GPS), mWaitGpsEnabled);
            } else {
                mGpsOffCount++;
                mGpsOffCountView.updateCount(mGpsOffCount);
                outputGpsOnOffCount(false, mGpsOffCount, true); // Output file
                Log.i(TAG, "GPS OFF count=" + mGpsOffCount);
                Log.d(TAG, "GpsObserver : POST EVENT_ENABLE_GPS after "
                        + mWaitGpsDisabled + "s");
                mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_ENABLE_GPS), mWaitGpsDisabled);
            }
        }
    };

    private void outputGpsOnOffCount(boolean on, int count, boolean append) {
        String result = Utility.getDate() + "    " + (on ? "ON" : "OFF") + "(" + String.valueOf(count) + ")";
        Utility.writeResultToFile(LOG_FILE_PATH, result, append);
    }

    private void updateGpsButtonState(boolean force, boolean forceState) {
        boolean gpsEnabled = isGpsEnabled();
        mGpsButton.setText(gpsEnabled ? getString(R.string.gps_off) : getString(R.string.gps_on));

        // When running test started, toggle button is disabled.
        if (isRunningTest) {
            mGpsButton.setEnabled(false);
            return;
        }
        // Force update
        if (force) {
            mGpsButton.setEnabled(forceState);
            return;
        }
        mGpsButton.setEnabled(true);
    }

    private void updateGpsRunningButtonState() {
        if (isRunningTest) {
            mGpsRunningStartButton.setEnabled(false);
            mGpsRunningStopButton.setEnabled(true);
        } else {
            mGpsRunningStartButton.setEnabled(true);
            mGpsRunningStopButton.setEnabled(false);
        }
    }

    private void updateCount() {
        mGpsOnCountView.updateCount(mGpsOnCount);
        mGpsOffCountView.updateCount(mGpsOffCount);
        mGpsOnFailCountView.updateCount(mGpsOnFailCount);
        mGpsOffFailCountView.updateCount(mGpsOffFailCount);
    }
}