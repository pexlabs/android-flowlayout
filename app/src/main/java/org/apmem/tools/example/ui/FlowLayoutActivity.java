package org.apmem.tools.example.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apmem.tools.adapters.AutoCompleteAdapter;
import org.apmem.tools.example.R;
import org.apmem.tools.layouts.AstroFlowLayout;
import org.apmem.tools.listeners.ChipListener;
import org.apmem.tools.model.Chip;
import org.apmem.tools.model.ChipInterface;

import java.util.List;

import static org.apmem.tools.example.helpers.Utils.getDummyData;

/**
 * Sample Demo Activity to show/test the features of AstroFlowLayout
 */
public class FlowLayoutActivity extends Activity {

    // TAG for logging
    private static final String LOG_TAG = "###FlowLayoutActivity";

    // dummy Strings for auto complete adapter
    private static final String[] EMAILS = new String[] {
            "kaustubh@astro-inc.com", "anthony@astro-inc.com", "ian@astro-inc.com",
            "andy@astro-inc.com" , "mithlesh@astro-inc.com", "san@astro-inc.com",
            "roland@astro-inc.com", "faisal@astro-inc.com", "parag@astro-inc.com"
    };

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
                R.layout.inflating_layout, EMAILS);
        // Set the adapter
        final AutoCompleteAdapter<Chip> autoCompleteAdapter = new AutoCompleteAdapter<>(
                getApplicationContext());

        autoCompleteAdapter.setData(getDummyData());
        mFlowLayout1.setAutoCompleteViewAdapter(autoCompleteAdapter);
        mFlowLayout2.setAutoCompleteViewAdapter(autoCompleteAdapter);

        // Don't forget to do this. Chip listener, observes chips behaviour
        // Add listener to both AstroFlowLayouts
        mFlowLayout1.setChipListener(new ChipListener() {
            @Override
            public void onChipRemoved(ChipInterface chip) {
                if(chip == null) {
                    return;
                }
                Log.d(LOG_TAG, "1. chip removed " + chip.getLabel());
            }

            @Override
            public void onChipAdded(ChipInterface chip) {
                if(chip == null) {
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
}