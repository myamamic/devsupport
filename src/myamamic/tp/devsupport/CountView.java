package myamamic.tp.devsupport;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CountView extends LinearLayout {
    private Context mContext;
    private String mCounterText = String.valueOf(0);

    private TextView mLabelView;
    private TextView mCountView;

    public CountView(Context context) {
        super(context);
    }

    public CountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);

        mContext = context;
        TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.CountView);
        // app:label_text
        String label = tArray.getString(R.styleable.CountView_label_text);

        mLabelView = new TextView(context, attrs);
        addView(mLabelView);
        setViewParams(mLabelView, label + " :  ");

        mCountView = new TextView(context, attrs);
        addView(mCountView);
        setViewParams(mCountView, mCounterText);
    }

    public CountView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void updateCount(long count) {
        mCounterText = String.valueOf(count);
        mCountView.setText(mCounterText);
    }

    private void setViewParams(TextView view, String text) {
        view.setText(text);
        view.setTextAppearance(mContext, R.style.TextAppearance_info_label);
    }
}
