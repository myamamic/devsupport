package myamamic.tp.devsupport;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class OkDialogFragment extends DialogFragment {

    public interface OnButtonClickListener {
        public void onOkButtonClicked();
    }

    private OnButtonClickListener mOnButtonClickListener;

    public static OkDialogFragment newInstance(int title) {
        OkDialogFragment frag = new OkDialogFragment();
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
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (mOnButtonClickListener != null) {
                        mOnButtonClickListener.onOkButtonClicked();
                    }
                }
            }
        )
        .create();
    }
}
