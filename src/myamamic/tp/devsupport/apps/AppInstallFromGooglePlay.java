package myamamic.tp.devsupport.apps;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import myamamic.tp.devsupport.BaseTestActivity;

public class AppInstallFromGooglePlay extends BaseTestActivity {
    private static final String TAG = "AppInstallFromGooglePlay";

    private AppListAdapter mAppListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(getApplicationContext());
        setContentView(listView);

        mAppListAdapter = new AppListAdapter(getApplicationContext());
        listView.setAdapter(mAppListAdapter);
        listView.setOnItemClickListener(mOnItemClickListener);

        initialize();
    }

    protected void initialize() {
        // TEST
        if (mAppListAdapter != null) {
            mAppListAdapter.addItem("com.electricsheep.asi", "Android System Info - Display device information");
            mAppListAdapter.addItem("jp.smapho.battery_mix", "Battery Mix - Display battery info");
            mAppListAdapter.addItem("com.skype.raider", "Skype");
        }
        // TEST
    }

    private AdapterView.OnItemClickListener mOnItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            String name = mAppListAdapter.getItemAppName(pos);
            if (name != null) {
                OpenAppPageOnGooglePlay(name);
            }
        }
    };

    private boolean OpenAppPageOnGooglePlay(String packageName) {
        StringBuffer sb = new StringBuffer();
        sb.append("market://details?id=");
        sb.append(packageName);
        sb.append("&hl=ja");
        Log.i(TAG, "Open url: " + sb.toString());

        Uri uri = Uri.parse(sb.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
        return true;
    }
}
