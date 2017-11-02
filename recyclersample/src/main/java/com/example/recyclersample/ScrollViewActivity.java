package com.example.recyclersample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.apmem.tools.adapters.AutoCompleteAdapter;
import org.apmem.tools.layouts.AstroFlowLayout;
import org.apmem.tools.listeners.ChipListener;
import org.apmem.tools.model.Chip;
import org.apmem.tools.model.ChipInterface;

import java.util.ArrayList;
import java.util.List;

public class ScrollViewActivity extends AppCompatActivity {

    private static final String LOG_TAG = "##ScrollViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_view);

        final AutoCompleteAdapter<Chip> autoCompleteAdapter = new AutoCompleteAdapter<>(
                getApplicationContext());
        autoCompleteAdapter.setData(getDummyData());

        final AstroFlowLayout toView = findViewById(R.id.to_view);
        toView.setAutoCompleteViewAdapter(autoCompleteAdapter);

        final AstroFlowLayout ccView = findViewById(R.id.cc_view);
        ccView.setAutoCompleteViewAdapter(autoCompleteAdapter);

        final AstroFlowLayout bccView = findViewById(R.id.bcc_view);
        bccView.setAutoCompleteViewAdapter(autoCompleteAdapter);

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

        toView.setChipListener(chipListener);
        ccView.setChipListener(chipListener);
        bccView.setChipListener(chipListener);
    }

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

    private List<Chip> getDummyData() {
        List<Chip> data = new ArrayList<>();
        data.add(new Chip("kaustubh@astro-inc.com", "Kaustubh"));
        data.add(new Chip("anthony@astro-inc.com", "Anthony"));
        data.add(new Chip("ian@astro-inc.com", "Ian"));
        data.add(new Chip("andy@astro-inc.com", "Andy"));
        data.add(new Chip("san@astro-inc.com", "San"));
        data.add(new Chip("roland@astro-inc.com", "Roland"));
        data.add(new Chip("omkar@astro-inc.com", "Omkar"));
        data.add(new Chip("faisal@astro-inc.com", "Faisal"));
        return data;
    }
}
