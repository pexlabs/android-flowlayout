package org.apmem.tools.example;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class FlowLayoutActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    AstroFlowLayout mFlowLayout1;
    AstroFlowLayout mFlowLayout2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flow_layout);

        mFlowLayout1 = (AstroFlowLayout) this.findViewById(R.id.flowLayout1);
        mFlowLayout2 = (AstroFlowLayout) this.findViewById(R.id.flowLayout2);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        mFlowLayout1.setAdapter(adapter);
        mFlowLayout2.setAdapter(adapter);
    }


    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };
}
