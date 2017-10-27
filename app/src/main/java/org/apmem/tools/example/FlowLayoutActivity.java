package org.apmem.tools.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apmem.tools.layouts.AstroFlowLayout;
import org.apmem.tools.listeners.ChipListener;
import org.apmem.tools.model.ChipInterface;

import java.util.List;

/**
 * Sample Demo Activity to show/test the features of AstroFlowLayout
 */
public class FlowLayoutActivity extends Activity {

    // TAG for logging
    private static final String LOG_TAG = "###FlowLayoutActivity";

    // Instances of AstroFlowLayout
    private AstroFlowLayout mFlowLayout1;
    private AstroFlowLayout mFlowLayout2;

    // Text view to show the objects/data of chips
    private TextView mValidateOutput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flow_layout);

        // Bind the views
        mFlowLayout1 = (AstroFlowLayout) this.findViewById(R.id.flowLayout1);
        mFlowLayout2 = (AstroFlowLayout) this.findViewById(R.id.flowLayout2);
        mValidateOutput = (TextView) this.findViewById(R.id.text);

        // For now keeping the autocomplete adapter very simple
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        // Set the adapter
        mFlowLayout1.setAdapter(adapter);
        mFlowLayout2.setAdapter(adapter);

        // Don't forget to do this. Chip listener, observes chips behaviour
        // Add listener to both AstroFlowLayouts

        mFlowLayout1.setChipListener(new ChipListener() {
            @Override
            public void onChipRemoved(ChipInterface chip) {
                if(chip == null) {
                    Log.d(LOG_TAG, "1. chip to be removed is null");
                    return;
                }
                Log.d(LOG_TAG, "1. chip removed " + chip.getLabel());
            }

            @Override
            public void onChipAdded(ChipInterface chip) {
                if(chip == null) {
                    Log.d(LOG_TAG, "1. chip to be added is null");
                    return;
                }
                Log.d(LOG_TAG, "1. chip added " + chip.getLabel());
            }
        });

        mFlowLayout2.setChipListener(new ChipListener() {
            @Override
            public void onChipRemoved(ChipInterface chip) {
                if(chip == null) {
                    Log.d(LOG_TAG, "2. chip to be removed is null");
                    return;
                }
                Log.d(LOG_TAG, "2. chip removed " + chip.getLabel());
            }

            @Override
            public void onChipAdded(ChipInterface chip) {
                if(chip == null) {
                    Log.d(LOG_TAG, "2. chip to be added is null");
                    return;
                }
                Log.d(LOG_TAG, "2. chip added " + chip.getLabel());
            }
        });

        // code to get objects from first flow layout
        // and write them to the text view
        findViewById(R.id.validate1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidateOutput.setText("");
                List<ChipInterface> out = mFlowLayout1.getObjects();
                StringBuilder builder = new StringBuilder();
                for(ChipInterface chipInterface : out) {
                    builder.append(chipInterface.getLabel() +" , ");
                }
                mValidateOutput.setText(builder.toString());
            }
        });

        // code to get objects from seconds flow layout
        // and write them to the text view
        findViewById(R.id.validate2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidateOutput.setText("");
                List<ChipInterface> out = mFlowLayout2.getObjects();
                StringBuilder builder = new StringBuilder();
                for(ChipInterface chipInterface : out) {
                    builder.append(chipInterface.getLabel() +" , ");
                }
                mValidateOutput.setText(builder.toString());
            }
        });
    }


    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };
}
