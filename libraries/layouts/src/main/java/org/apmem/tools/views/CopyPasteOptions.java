package org.apmem.tools.views;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MultiAutoCompleteTextView;

import org.apmem.tools.layouts.FlowLayout;

public class CopyPasteOptions extends Dialog {

    private MultiAutoCompleteTextView mAutoCompleteTextView;

    /**
     * Constructor for ChipsDetailsDialog which is also known as ChipDetailsView
     * This dialog is aligned w.r.t. clicked token considering x & y coordinates
     */
    public CopyPasteOptions(Context context, MultiAutoCompleteTextView textView) {
        super(context);
        mAutoCompleteTextView = textView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Don't want the title on this dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final CopyPasteView copyPasteView = new CopyPasteView(getContext());
        setContentView(copyPasteView);

        Rect rect = new Rect();
        mAutoCompleteTextView.getGlobalVisibleRect(rect);
        FlowLayout flowLayout = (FlowLayout) mAutoCompleteTextView.getParent();
        // Anchor the dialog to where the user clicked.
        WindowManager.LayoutParams wmlp = getWindow().getAttributes();
        ViewGroup group = (ViewGroup) flowLayout.getParent();
        wmlp.gravity = Gravity.TOP | Gravity.START;
        wmlp.x = (int) mAutoCompleteTextView.getLeft() + 20;
        int delta = 0;
        if (flowLayout.getLines().size() > 1) {
            delta = mAutoCompleteTextView.getBottom() - mAutoCompleteTextView.getHeight();
        }
        wmlp.y = (int) group.getY() + delta;

        if (TextUtils.isEmpty(mAutoCompleteTextView.getText().toString().trim())) {
            copyPasteView.prepareForEmpty();
        } else {
            copyPasteView.showPasteSelectAll();
            mAutoCompleteTextView.setSelection(mAutoCompleteTextView.getText().length());
        }

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mAutoCompleteTextView.setSelection(mAutoCompleteTextView.getText().length());
            }
        });

        copyPasteView.getCopy().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(mAutoCompleteTextView.getText(),
                        mAutoCompleteTextView.getText());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }
                dismiss();
            }
        });

        copyPasteView.getPaste().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = clipboard.getPrimaryClip();
                    String text = clip.getItemAt(0).getText().toString();
                    int start = mAutoCompleteTextView.getSelectionStart();
                    mAutoCompleteTextView.setText(text);
                    mAutoCompleteTextView.setSelection(mAutoCompleteTextView.getText().length());
                }
                dismiss();
            }
        });

        copyPasteView.getShare().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = clipboard.getPrimaryClip();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, clip.getItemAt(0).getText().toString());
                    sendIntent.setType("text/plain");
                    getContext().startActivity(sendIntent);
                }
                dismiss();
            }
        });

        copyPasteView.getSelectAll().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoCompleteTextView.setSelection(0, mAutoCompleteTextView.getText().length());
                copyPasteView.prepareForNonEmpty();
            }
        });

        copyPasteView.getCut().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(mAutoCompleteTextView.getText(),
                        mAutoCompleteTextView.getText());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    mAutoCompleteTextView.setText(" ");
                }
                dismiss();
            }
        });

        // Don't let the dialog look like we are stealing all focus from the user.
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // If the user clicks outside the dialog, we should dismiss it.
        setCanceledOnTouchOutside(true);
    }
}