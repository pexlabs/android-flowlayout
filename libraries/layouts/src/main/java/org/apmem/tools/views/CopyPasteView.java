package org.apmem.tools.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apmem.tools.layouts.R;

/**
 * Created by kaustubh on 09/11/17.
 */

public class CopyPasteView extends RelativeLayout {

    private TextView mCopy;
    private TextView mPaste;
    private TextView mCut;
    private TextView mShare;
    private TextView mSelectAll;

    public CopyPasteView(Context context) {
        super(context);
        init(null);
    }

    public CopyPasteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CopyPasteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // inflate layout
        View rootView = inflate(getContext(), R.layout.copy_paste_layout, this);

        mCopy = (TextView) rootView.findViewById(R.id.copy);
        mPaste = (TextView) rootView.findViewById(R.id.paste);
        mCut = (TextView) rootView.findViewById(R.id.cut);
        mShare = (TextView) rootView.findViewById(R.id.share);
        mSelectAll = (TextView) rootView.findViewById(R.id.select_all);

        hideOnTouchOutside();
    }

    public TextView getCopy() {
        return mCopy;
    }

    public void setCopy(TextView copy) {
        mCopy = copy;
    }

    public TextView getPaste() {
        return mPaste;
    }

    public void setPaste(TextView paste) {
        mPaste = paste;
    }

    public TextView getCut() {
        return mCut;
    }

    public void setCut(TextView cut) {
        mCut = cut;
    }

    public TextView getShare() {
        return mShare;
    }

    public void setShare(TextView share) {
        mShare = share;
    }

    public TextView getSelectAll() {
        return mSelectAll;
    }

    public void setSelectAll(TextView selectAll) {
        mSelectAll = selectAll;
    }

    public void prepareForEmpty() {
        mCopy.setVisibility(GONE);
        mCut.setVisibility(GONE);
        mShare.setVisibility(GONE);
        mSelectAll.setVisibility(GONE);
        mPaste.setVisibility(VISIBLE);
    }

    public void prepareForNonEmpty() {
        mCopy.setVisibility(VISIBLE);
        mCut.setVisibility(VISIBLE);
        mShare.setVisibility(VISIBLE);
        mPaste.setVisibility(VISIBLE);
        mSelectAll.setVisibility(GONE);
    }

    public void showPasteSelectAll() {
        mCopy.setVisibility(GONE);
        mCut.setVisibility(GONE);
        mShare.setVisibility(GONE);
        mSelectAll.setVisibility(VISIBLE);
        mPaste.setVisibility(VISIBLE);
    }

    public void showSelectAll() {
        mCopy.setVisibility(GONE);
        mCut.setVisibility(GONE);
        mShare.setVisibility(GONE);
        mSelectAll.setVisibility(VISIBLE);
        mPaste.setVisibility(GONE);
    }

    /**
     * Hide the view on touch outside of it
     */
    private void hideOnTouchOutside() {
        // set focusable
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
    }
}
