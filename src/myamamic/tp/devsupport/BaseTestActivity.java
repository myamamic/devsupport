package myamamic.tp.devsupport;


import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;

public class BaseTestActivity extends FragmentActivity
        implements MyAlertDialogFragment.OnButtonClickListener {

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showConfirmFinishDialog();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showConfirmFinishDialog() {
        MyAlertDialogFragment newFragment = MyAlertDialogFragment.newInstance(R.string.confirm_finish);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onPositiveButtonClicked() {
        finish();
    }

    @Override
    public void onNegativeButtonClicked() {
        // Cancel finish
    }
}