package org.apmem.tools.example.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.apmem.tools.example.R;
import org.apmem.tools.layouts.AstroFlowLayout;
import org.apmem.tools.listeners.ChipListener;
import org.apmem.tools.model.ChipInterface;

import java.util.List;

/**
 * Created by kaustubh on 30/10/17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.FlowViewHolder> {

    // Tell recycler adapter how many rows we want
    private static final int INPUT_COUNT = 3;

    // Chips listener
    private ChipListener mChipListener;

    // We need just 3 rows.
    private AstroFlowLayout mToView;
    private AstroFlowLayout mCcView;
    private AstroFlowLayout mBccView;

    public RecyclerAdapter() {
    }

    @Override
    public FlowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 0:
                view = mToView;
                break;

            case 1:
                view = mCcView;
                break;

            case 2:
                view = mBccView;
                break;
        }
        if(view == null) {
            throw new IllegalStateException("view should not be null");
        }
        return new FlowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlowViewHolder holder, int position) {
        holder.mAstroFlowLayout.setChipListener(mChipListener);
    }

    @Override
    public int getItemCount() {
        return INPUT_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class FlowViewHolder extends RecyclerView.ViewHolder {

        AstroFlowLayout mAstroFlowLayout;

        public FlowViewHolder(View itemView) {
            super(itemView);

            mAstroFlowLayout = (AstroFlowLayout) itemView.findViewById(R.id.flowLayout);
        }
    }

    public void setChipListener(ChipListener chipListener) {
        mChipListener = chipListener;
    }

    public void setToView(AstroFlowLayout toView) {
        mToView = toView;
    }

    public void setBccView(AstroFlowLayout bccView) {
        mBccView = bccView;
    }

    public void setCcView(AstroFlowLayout ccView) {
        mCcView = ccView;
    }

    public List<ChipInterface> getToData() {
        return mToView.getObjects();
    }

    public List<ChipInterface> getCcData() {
        return mCcView.getObjects();
    }

    public List<ChipInterface> getBccData() {
        return mBccView.getObjects();
    }
}
