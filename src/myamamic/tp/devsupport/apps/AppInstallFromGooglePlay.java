package myamamic.tp.devsupport.apps;


import android.os.Bundle;
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

        mAppListAdapter = new AppListAdapter(getApplicationContext(), null);
        listView.setAdapter(mAppListAdapter);

        initialize();
    }

    protected void initialize() {
        /*
        mInstallButton = (Button)findViewById(R.id.button_install);
        mInstallButton.setOnClickListener(new OnClickListener() {   
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=com.electricsheep.asi&hl=ja");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        */
    }
}
