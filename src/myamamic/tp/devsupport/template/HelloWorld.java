package myamamic.tp.devsupport.template;


import myamamic.tp.devsupport.BaseTestActivity;
import myamamic.tp.devsupport.R;

import android.os.Bundle;

public class HelloWorld extends BaseTestActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hello_world);

        initialize();
    }

    protected void initialize() {
        // TODO : init component
    }
}