package org.apmem.tools.example.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.apmem.tools.adapters.AutoCompleteAdapter;
import org.apmem.tools.example.R;
import org.apmem.tools.layouts.AstroFlowLayout;
import org.apmem.tools.listeners.ChipListener;
import org.apmem.tools.model.Chip;
import org.apmem.tools.model.ChipInterface;
import org.apmem.tools.util.Utils;
import org.apmem.tools.views.ChipView;

import java.util.List;

import static org.apmem.tools.example.helpers.Utils.getDummyData;

/**
 * Demo Activity to show working of {@link AstroFlowLayout} with ScrollView
 */
public class ScrollViewActivity extends Activity {

    // TAG for logging..
    private static final String LOG_TAG = "##ScrollViewActivity";

    private int mStyleClickCount = 0;
    private int mColorClickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_view);

        // Create instance of AutoCompleteAdapter
        final AutoCompleteAdapter<Chip> autoCompleteAdapter = new AutoCompleteAdapter<>(
                getApplicationContext());
        autoCompleteAdapter.setData(getDummyData());

        // Bind to/cc/bcc views one by one
        final AstroFlowLayout toView = (AstroFlowLayout) findViewById(R.id.to_view);
        toView.setAutoCompleteViewAdapter(autoCompleteAdapter);

        final AstroFlowLayout ccView = (AstroFlowLayout) findViewById(R.id.cc_view);
        ccView.setAutoCompleteViewAdapter(autoCompleteAdapter);

        final AstroFlowLayout bccView = (AstroFlowLayout) findViewById(R.id.bcc_view);
        bccView.setAutoCompleteViewAdapter(autoCompleteAdapter);

        // Create chip listener & share them with all the to/cc/bcc views
        ChipListener chipListener = new ChipListener() {
            @Override
            public void onChipRemoved(ChipInterface chip) {
                printToData(toView, ccView, bccView);
            }

            @Override
            public void onChipAdded(ChipInterface chip) {
                printToData(toView, ccView, bccView);
            }
        };

        // Set the same chip listener to all the views
        toView.setChipListener(chipListener);
        ccView.setChipListener(chipListener);
        bccView.setChipListener(chipListener);

        // Set text watcher for autocomplete view to get the events of text change
        toView.setTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(LOG_TAG, "onTextChanged " + s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        findViewById(R.id.change_style).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ChipView> chips = toView.getChips();
                if (chips == null) return;
                for (ChipView chipView : chips) {
                    if (mStyleClickCount % 2 == 0) {
                        chipView.setLabelStyle(ChipView.TextStyle.STRIKE_THROUGH);
                    } else {
                        chipView.setLabelStyle(ChipView.TextStyle.UNDERLINE);
                    }
                }
                mStyleClickCount++;
            }
        });

        findViewById(R.id.change_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ChipView> chips = toView.getChips();
                if (chips == null) return;
                for (ChipView chipView : chips) {
                    if (mColorClickCount % 2 == 0) {
                        chipView.setChipBackgroundColor(getResources().getColor(R.color.blue900));
                    } else {
                        chipView.setChipBackgroundColor(getResources().getColor(R.color.gold500A));
                    }
                }
                mColorClickCount++;

                ChipView chipView = toView.getChipViewAtPosition(1);
                if (chipView != null) {
                    if (mColorClickCount % 2 == 0) {
                        chipView.setChipBackgroundColor(getResources().getColor(R.color.blue900));
                    } else {
                        chipView.setChipBackgroundColor(getResources().getColor(R.color.gold500A));
                    }
                }
            }
        });

        findViewById(R.id.disable_views).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toView.setEnabled(false);
                ccView.setEnabled(false);
                bccView.setEnabled(false);
            }
        });

        findViewById(R.id.calculate_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ChipInterface> texts = Utils.getAllEmails(toView.getObjects(),
                        ccView.getObjects(), bccView.getObjects());

                String text = Utils.getSingleLineEmailString(texts, toView.getWidth(),
                        (int) toView.getAutoCompleteTextView().getTextSize(),
                        toView.getAutoCompleteTextView().getTypeface());

                Log.d(LOG_TAG , "Text = " + text);
                ((TextView) findViewById(R.id.calculated_text)).setText(text);
            }
        });
    }

    /**
     * helper method to print the data of the AstroFlowLayout ChipViews
     * @param to
     * @param cc
     * @param bcc
     */
    private void printToData(AstroFlowLayout to, AstroFlowLayout cc, AstroFlowLayout bcc) {
        Log.d(LOG_TAG, "####### TO DATA : ");
        for(ChipInterface chipInterface : to.getObjects()) {
            if(chipInterface == null) continue;
            if (Utils.isValidEmailAddress(chipInterface.getInfo())) {
                Log.d(LOG_TAG, chipInterface.getLabel());
            }
        }

        Log.d(LOG_TAG, "####### CC DATA : ");
        for(ChipInterface chipInterface : cc.getObjects()) {
            if(chipInterface == null) continue;
            if (Utils.isValidEmailAddress(chipInterface.getInfo())) {
                Log.d(LOG_TAG, chipInterface.getLabel());
            }
        }

        Log.d(LOG_TAG, "####### BCC DATA : ");
        for(ChipInterface chipInterface : bcc.getObjects()) {
            if(chipInterface == null) continue;
            if (Utils.isValidEmailAddress(chipInterface.getInfo())) {
                Log.d(LOG_TAG, chipInterface.getLabel());
            }
        }
    }
}
