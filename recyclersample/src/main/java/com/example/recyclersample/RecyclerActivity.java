package com.example.recyclersample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;

import org.apmem.tools.adapters.AutoCompleteAdapter;
import org.apmem.tools.layouts.AstroFlowLayout;
import org.apmem.tools.listeners.ChipListener;
import org.apmem.tools.model.Chip;
import org.apmem.tools.model.ChipInterface;

import java.util.ArrayList;
import java.util.List;

public class RecyclerActivity extends AppCompatActivity {

    private static final String LOG_TAG = "##RecyclerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        RecyclerView recyclerView = findViewById(R.id.recycler);

        final RecyclerAdapter adapter = new RecyclerAdapter();

        final AutoCompleteAdapter<Chip> autoCompleteAdapter = new AutoCompleteAdapter<>(
                getApplicationContext());

        autoCompleteAdapter.setData(getDummyData());

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));

        AstroFlowLayout astroToFlowLayout = (AstroFlowLayout) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.recycler_item, recyclerView, false);
        astroToFlowLayout.setAutoCompleteViewAdapter(autoCompleteAdapter);
        astroToFlowLayout.setHint("To");
        adapter.setToView(astroToFlowLayout);

        AstroFlowLayout astroCcFlowLayout = (AstroFlowLayout) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.recycler_item, recyclerView, false);
        astroCcFlowLayout.setAutoCompleteViewAdapter(autoCompleteAdapter);
        astroCcFlowLayout.setHint("cc");
        adapter.setCcView(astroCcFlowLayout);

        AstroFlowLayout astroBccFlowLayout = (AstroFlowLayout) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.recycler_item, recyclerView, false);
        astroBccFlowLayout.setAutoCompleteViewAdapter(autoCompleteAdapter);
        astroBccFlowLayout.setHint("bcc");
        adapter.setBccView(astroBccFlowLayout);

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

        recyclerView.setAdapter(adapter);
    }

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
