package myamamic.tp.devsupport;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class MyAlertDialogFragment extends DialogFragment {

    public interface OnButtonClickListener {
        public void onPositiveButtonClicked();
        public void onNegativeButtonClicked();
    }

    private OnButtonClickListener mOnButtonClickListener;

    public static MyAlertDialogFragment newInstance(int title) {
        MyAlertDialogFragment frag = new MyAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }
 
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnButtonClickListener == false) {
            throw new ClassCastException("The activity does not implement OnButtonClickListener.");
        }

        mOnButtonClickListener = (OnButtonClickListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        return new AlertDialog.Builder(getActivity())
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setPositiveButton(android.R.string.ok,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (mOnButtonClickListener != null) {
                        mOnButtonClickListener.onPositiveButtonClicked();
                    }
                }
            }
        )
        .setNegativeButton(android.R.string.cancel,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (mOnButtonClickListener != null) {
                        mOnButtonClickListener.onNegativeButtonClicked();
                    }
                }
            }
        )
        .create();
    }
}
