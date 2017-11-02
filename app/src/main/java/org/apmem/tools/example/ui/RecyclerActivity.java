package org.apmem.tools.example.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;

import org.apmem.tools.adapters.AutoCompleteAdapter;
import org.apmem.tools.example.R;
import org.apmem.tools.example.helpers.RecyclerAdapter;
import org.apmem.tools.layouts.AstroFlowLayout;
import org.apmem.tools.listeners.ChipListener;
import org.apmem.tools.model.Chip;
import org.apmem.tools.model.ChipInterface;

import static org.apmem.tools.example.helpers.Utils.getDummyData;

/**
 * shows the working of {@link AstroFlowLayout} with RecyclerView
 */
public class RecyclerActivity extends Activity {

    private static final String LOG_TAG = "##RecyclerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        // Bind view to instance
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);

        // Create instance of Adapter. This is RecyclerView Adapter and not
        // AutoCompleteView adapter
        final RecyclerAdapter adapter = new RecyclerAdapter();

        // This is AutoCompleteView adapter which will be attached to AutoCompleteView
        final AutoCompleteAdapter<Chip> autoCompleteAdapter = new AutoCompleteAdapter<>(
                getApplicationContext());

        // Set dummy data to AutoCompleteView adapter
        autoCompleteAdapter.setData(getDummyData());

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));

        // Now we are done with sample data & adapter, just add the to/cc/bcc views to
        // RecyclerView
        // Create To view & set it to first row of RecyclerView
        AstroFlowLayout astroToFlowLayout = (AstroFlowLayout) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.recycler_item, recyclerView, false);
        astroToFlowLayout.setAutoCompleteViewAdapter(autoCompleteAdapter);
        astroToFlowLayout.setHint("To");
        // set it to first row of RecyclerView
        adapter.setToView(astroToFlowLayout);

        // Create cc view
        AstroFlowLayout astroCcFlowLayout = (AstroFlowLayout) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.recycler_item, recyclerView, false);
        astroCcFlowLayout.setAutoCompleteViewAdapter(autoCompleteAdapter);
        astroCcFlowLayout.setHint("cc");
        // & set it to second row of RecyclerView
        adapter.setCcView(astroCcFlowLayout);

        // Create bcc view
        AstroFlowLayout astroBccFlowLayout = (AstroFlowLayout) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.recycler_item, recyclerView, false);
        astroBccFlowLayout.setAutoCompleteViewAdapter(autoCompleteAdapter);
        astroBccFlowLayout.setHint("bcc");
        // & set it to third row of RecyclerView
        adapter.setBccView(astroBccFlowLayout);

        // Set the chip listener, this will be shared between to all the to/cc/bcc views
        adapter.setChipListener(new ChipListener() {
            @Override
            public void onChipRemoved(ChipInterface chip) {
                Log.d(LOG_TAG, "Removed chip " + chip.getLabel());
                printAllData(adapter);
            }

            @Override
            public void onChipAdded(ChipInterface chip) {
                Log.d(LOG_TAG, "Added chip " + chip.getLabel());
                printAllData(adapter);

            }
        });

        // Finally set RecyclerView Adapter
        recyclerView.setAdapter(adapter);
    }

    /**
     * helper method to print the data of the ChipViews, called by ChipListener
     * @param adapter
     */
    private void printAllData(RecyclerAdapter adapter) {
        Log.d(LOG_TAG, "####### TO DATA : ");
        for(ChipInterface chipInterface : adapter.getToData()) {
            if(chipInterface == null) continue;
            Log.d(LOG_TAG, chipInterface.getLabel());
        }

        Log.d(LOG_TAG, "####### CC DATA : ");
        for(ChipInterface chipInterface : adapter.getCcData()) {
            if(chipInterface == null) continue;
            Log.d(LOG_TAG, chipInterface.getLabel());
        }

        Log.d(LOG_TAG, "####### BCC DATA : ");
        for(ChipInterface chipInterface : adapter.getBccData()) {
            if(chipInterface == null) continue;
            Log.d(LOG_TAG, chipInterface.getLabel());
        }
    }
}
