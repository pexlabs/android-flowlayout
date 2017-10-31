package org.apmem.tools.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.MultiAutoCompleteTextView;

/**
 * Created by kaustubh on 31/10/17.
 */

public class AstroAutoCompleteView extends MultiAutoCompleteTextView {

    public AstroAutoCompleteView(Context context) {
        super(context);
    }

    public AstroAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AstroAutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("##AstroAutoCompleteView", ""+parentHeight + " " +parentWidth);
        this.setMeasuredDimension(parentWidth, parentHeight);
    }
}
