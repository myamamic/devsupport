package myamamic.tp.devsupport.apps;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import myamamic.tp.devsupport.BaseTestActivity;

import java.util.ArrayList;

public class AppInstallFromGooglePlay extends BaseTestActivity {
    private static final String TAG = "AppInstallFromGooglePlay";

    private AppListAdapter mAppListAdapter;
    private ArrayList<String> mAppList = new ArrayList<String>(); // TEST

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(getApplicationContext());
        setContentView(listView);

        initialize();

        mAppListAdapter = new AppListAdapter(getApplicationContext(), mAppList);
        listView.setAdapter(mAppListAdapter);
        listView.setOnItemClickListener(mOnItemClickListener);
    }

    protected void initialize() {
        // TEST
        mAppList.add("com.electricsheep.asi");
        mAppList.add("jp.smapho.battery_mix");
        mAppList.add("com.skype.raider");
        // TEST
    }

    private AdapterView.OnItemClickListener mOnItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            String name = (String)mAppListAdapter.getItem(pos);
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
