package org.apmem.tools.example.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.apmem.tools.adapters.AutoCompleteAdapter;
import org.apmem.tools.example.R;
import org.apmem.tools.layouts.AstroFlowLayout;
import org.apmem.tools.listeners.ChipListener;
import org.apmem.tools.model.Chip;
import org.apmem.tools.model.ChipInterface;

import static org.apmem.tools.example.helpers.Utils.getDummyData;

/**
 * Demo Activity to show working of {@link AstroFlowLayout} with ScrollView
 */
public class ScrollViewActivity extends Activity {

    private static final String LOG_TAG = "##ScrollViewActivity";

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
        //toView.setHint("to");

        final AstroFlowLayout ccView = (AstroFlowLayout) findViewById(R.id.cc_view);
        ccView.setAutoCompleteViewAdapter(autoCompleteAdapter);
        //ccView.setHint("cc");

        final AstroFlowLayout bccView = (AstroFlowLayout) findViewById(R.id.bcc_view);
        bccView.setAutoCompleteViewAdapter(autoCompleteAdapter);
        //bccView.setHint("bcc");

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
            Log.d(LOG_TAG, chipInterface.getLabel());
        }

        Log.d(LOG_TAG, "####### CC DATA : ");
        for(ChipInterface chipInterface : cc.getObjects()) {
            if(chipInterface == null) continue;
            Log.d(LOG_TAG, chipInterface.getLabel());
        }

        Log.d(LOG_TAG, "####### BCC DATA : ");
        for(ChipInterface chipInterface : bcc.getObjects()) {
            if(chipInterface == null) continue;
            Log.d(LOG_TAG, chipInterface.getLabel());
        }
    }
}
