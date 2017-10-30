/**
 * AstroFlowLayout.java
 *
 * Created by kaustubh on 26/10/17.
 *
 * AstroFlowLayout extends {@link org.apmem.tools.layouts.FlowLayout} which abstract
 * Helps in expanding/collapsing layout
 * Also helps in creating & removing views
 */

package org.apmem.tools.layouts;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apmem.tools.layouts.logic.LineDefinition;
import org.apmem.tools.layouts.logic.ViewDefinition;
import org.apmem.tools.listeners.AstroDragListener;
import org.apmem.tools.listeners.ChipListener;
import org.apmem.tools.model.Chip;
import org.apmem.tools.model.ChipInterface;
import org.apmem.tools.util.ViewUtil;
import org.apmem.tools.views.ChipView;
import org.apmem.tools.views.DetailedChipView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AstroFlowLayout extends FlowLayout {

    // TAG for logging
    private static final String LOG_TAG = AstroFlowLayout.class.getSimpleName();

    // When this FlowLayout is collapsed we hold data in this set
    private Set<View> mHiddenViews = new HashSet<>();

    // Simple Map to keep track of views & chips data associated with them
    // Helpful in getting final data & also in case of drag & drop
    private Map<View, ChipInterface> mChipMap = new HashMap<>();

    // ChipListener to track chips life cycle
    private ChipListener mChipListener;

    // Drag listener which listens to chips life like added/removed
    private AstroDragListener mAstroDragListener;

    public AstroFlowLayout(Context context) {
        this(context, null);
    }

    public AstroFlowLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AstroFlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        mAstroDragListener = new AstroDragListener();
    }

    /**
     * this is +n feature. When auto complete view loses its focus we try to collapse the view
     * & add the dummy CountTextView at the end
     */
    @Override
    public void collapse() {
        // There is nothing to collapse
        if(getChildCount() == 0) {
            return;
        }

        // First create temp list to hold all the views of first row of the flow layout
        List<View> views = new ArrayList<>();

        // First all all the views to hidden views
        for (int i = 0; i < getChildCount() - 1; i++) {
            mHiddenViews.add(getChildAt(i));
        }

        // Get the views present at first line / row
        LineDefinition lineDefinition = getLines().get(0);
        // Get count of the views present in the first row
        int count = lineDefinition.getViews().size();

        // So if first view contains only one view then add +n view to next line,
        // else add +n view to next of the first view of the first row
        // if the count is 1 then +n view should be added in the next line
        // that means we need to add this view in Views list & remove from hiddenviews as
        // this view will be visible
        if (count == 1) {
            views.add(lineDefinition.getViews().get(0).getView());
            mHiddenViews.remove(lineDefinition.getViews().get(0).getView());
        } else {
            for (int i = 0; i < count; i++) {
                ViewDefinition viewDefinition = lineDefinition.getViews().get(i);
                // if the count is more than one that means we need to add +n view to next of the
                // first view of the first row only show first view & hide all views
                if (i == 0) {
                    views.add(viewDefinition.getView());
                    mHiddenViews.remove(viewDefinition.getView());
                }
            }
        }


        // just call remove all views to clear layout, this would remove our
        // autocomplete view also. Obviously!
        removeAllViews();

        // And add the views of first line one by one
        for (View view : views) {
            addView(view);
        }

        // So size of the hidden views is the count
        // if we have views to hide, show them as count
        if (mHiddenViews.size() > 0) {
            addView(getCountView("+" + mHiddenViews.size()));
        } else {
            // else add our beloved autocomplete view
            addAutoCompleteView();
        }
    }

    /**
     * This helps in expanding the collapsed state
     * expand is called when clicked in CountTextView, Clicked on VisibleChips
     */
    @Override
    public void expand() {
        // If we don't have any hidden views just return
        if (mHiddenViews.size() < 1) {
            return;
        }
        // Now force the expansion
        forceExpand();
    }

    /**
     * this is force expand, called when drag enter event is occurred
     */
    public void forceExpand() {
        // Remove the last count text view
        removeViewAt(getChildCount() - 1);

        // Iterate over all the hidden views
        for (View view : mHiddenViews) {
            // And add them one by one
            addView(view);
        }

        // Add over beloved Auto complete text view.
        addAutoCompleteView();

        // clear the added hidden views
        mHiddenViews.clear();
    }

    /**
     * generates TextVuew which holds count of the hidden views
     * @param text : count of views present after 1st line
     * @return
     */
    private View getCountView(String text) {
        TextView countView = new TextView(getContext());
        int padding = (int) getResources().getDimension(R.dimen.count_view_padding);
        countView.setPadding(padding, padding, padding, padding);
        countView.setText(text);
        countView.setTextColor(mCountViewTextColor);
        countView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                forceExpand();
            }
        });
        countView.setTextSize(mCountViewTextSize);
        // there is a possibility that user drags some view on this count view,
        // this case is handled by setting drag listener
        countView.setOnDragListener(new AstroDragListener());
        return countView;
    }

    /**
     * Removes chip at that particular position
     * @param position : position at which the chip is supposed to be removed
     */
    @Override
    public void removeChipAt(int position) {
        // Find the view at that position
        View view = getChildAt(position);

        removeChildView(view);
    }

    /**
     * Called by {@link #removeChipAt(int)} & {@link AstroDragListener} to remove the view
     * also updates cache
     * @param view
     */
    public void removeChildView(View view) {
        // Get the information of the view from cached map
        ChipInterface removedChip = mChipMap.get(view);

        // Remove the view at that particular position
        removeView(view);

        // remove the chip from cache
        mChipMap.remove(view);

        // invalidate the view
        invalidate();

        // reset the hint text
        if(getChildCount() == 1) {
            setHint(mHintText);
        }

        // Call onChipRemoved for removed chip
        if (mChipListener != null) {
            mChipListener.onChipRemoved(removedChip);
        }
    }

    /**
     * Adds chip at particular position
     * @param view
     * @param position
     */
    @Override
    public void addChipAt(View view, int position) {
        super.addView(view, position);
        if (mChipListener != null) {
            mChipListener.onChipAdded(mChipMap.get(view));
        }
    }

    /**
     * Helper method for setting chip listener
     * @param listener
     */
    public void setChipListener(ChipListener listener) {
        mChipListener = listener;
    }

    /**
     * Similar to {@link #addChipAt(View, int) but instead of taking value from cache,
     * chip's value is passed, helpful in case of drag/drop event to avoid data corruption
     * @param view
     * @param chipInterface
     * @param position
     */
    public void addChipAtPositionWithChip(View view, ChipInterface chipInterface, int position) {
        super.addView(view, position);
        // attach click listener again. Because this method is called from {@link AstroDragListener}
        // meaning, the view was removed & called again. So we need to attach click listener again
        view.setOnClickListener(new ChipClickListener());
        mChipMap.put(view, chipInterface);
        if (mChipListener != null) {
            mChipListener.onChipAdded(chipInterface);
        }
    }

    /**
     * This is called by parent class to add the view
     * Creates a ChipView with specific item. Which can be String or {@link Chip}
     * @param item
     * @return
     */
    @Override
    public View getObjectView(Object item) {
        ChipInterface chipInterface = null;
        if (item instanceof String) {
            chipInterface = new Chip((String) item, (String) item);
        }
        if (item instanceof ChipInterface) {
            chipInterface = (ChipInterface) item;
        }
        // Create a chip view
        final ChipView chipView = new ChipView(getContext());
        chipView.setLabel(chipInterface.getLabel(), 0, (int) getResources()
                .getDimension(R.dimen.chip_view_label_text_size));
        int padding = (int) getResources().getDimension(R.dimen.chip_view_text_padding);
        chipView.setPadding(padding, padding, padding, padding);
        chipView.setHasAvatarIcon(true);
        chipView.setChipBorderColor((int) getResources().getDimension(R.dimen.chip_view_border_size),
                mChipBorderColor);
        chipView.setOnClickListener(new ChipClickListener());
        chipView.setOnDragListener(mAstroDragListener);
        chipView.setLongClickable(true);
        chipView.setOnDeleteClicked(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FlowLayout flowLayout = (FlowLayout) chipView.getParent();
                int position = ViewUtil.getViewPositionInParent(flowLayout, chipView);
                if (position == -1) {
                    return;
                }
                removeChipAt(position);
            }
        });
        // set long click listener for drag & drop functionality
        chipView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // if a chip in a view is long clicked, but the parent is collapsed
                // simply expand it
                if (mHiddenViews.size() >= 1) {
                    expand();
                    return true;
                }
                ClipData data = ClipData.newPlainText(String.valueOf(view.getId()), "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                view.startDrag(data, shadowBuilder, view, 0);
                FlowLayout flowLayout = (FlowLayout) view.getParent();
                view.setVisibility(GONE);
                flowLayout.invalidate();
                return true;
            }
        });
        // Update cache
        mChipMap.put(chipView, chipInterface);
        return chipView;
    }

    /**
     * Click handler for ChipView
     */
    private class ChipClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            // if a chip in a view is long clicked, but the parent is collapsed
            // simply expand it
            if (mHiddenViews.size() >= 1) {
                expand();
                return;
            }

            // Show detailed view as a dialog
            final DetailedChipView detailedChipView = getDetailedChipView(mChipMap.get(v));
            new ChipsDetailsDialog(v, detailedChipView).show();
        }
    }

    /**
     * Creates a detailed view with chip data.
     * @param chip
     * @return
     */
    private DetailedChipView getDetailedChipView(ChipInterface chip) {
        return new DetailedChipView.Builder(getContext())
                .chip(chip)
                .textColor(mChipDetailedTextColor)
                .backgroundColor(mChipDetailedBackgroundColor)
                .deleteIconColor(mChipDetailedDeleteIconColor)
                .build();
    }


    /**
     * Chips details dialog will be shown to user when user clicks on any of the chips in the layout
     */
    private class ChipsDetailsDialog extends Dialog {

        // the views of detailed chip layout
        private View mClickedView;
        private DetailedChipView mDetailedView;

        /**
         * Constructor for ChipsDetailsDialog which is also known as ChipDetailsView
         * This dialog is aligned w.r.t. clicked token considering x & y coordinates
         */
        public ChipsDetailsDialog(View clickedView, DetailedChipView detailedView) {
            super(clickedView.getContext());
            mClickedView = clickedView;
            mDetailedView = detailedView;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Don't want the title on this dialog
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (mDetailedView.getLayoutParams() == null) {
                mDetailedView.setLayoutParams(new RelativeLayout.LayoutParams(
                        (int) getResources().getDimension(R.dimen.detail_view_width),
                        (int) getResources().getDimension(R.dimen.detail_view_height)));
            }
            mDetailedView.setVisibility(VISIBLE);
            setContentView(mDetailedView);

            // remove the chip once clicked on delete image
            mDetailedView.setOnDeleteClicked(new View.OnClickListener() {
                public void onClick(View v) {
                    FlowLayout flowLayout = (FlowLayout) mClickedView.getParent();
                    int position = ViewUtil.getViewPositionInParent(flowLayout, mClickedView);
                    if (position == -1) {
                        return;
                    }
                    removeChipAt(position);
                    dismiss();
                }
            });

            // Anchor the dialog to where the user clicked.
            WindowManager.LayoutParams wmlp = getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.START;
            wmlp.x = (int) mClickedView.getX();
            FlowLayout flowLayout = (FlowLayout) mClickedView.getParent();
            wmlp.y = (int) ((int) mClickedView.getY() + flowLayout.getY());

            // Don't let the dialog look like we are stealing all focus from the user.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            // If the user clicks outside the dialog, we should dismiss it.
            setCanceledOnTouchOutside(true);
        }
    }

    /**
     * Called by {@link AstroDragListener} to read Cache value
     * However updation is done only in {@link #addChipAt(View, int)} & {@link #removeChipAt(int)}
     * @return
     */
    public Map<View, ChipInterface> getChipMap() {
        return mChipMap;
    }

    /**
     * Returns the current chips' values.
     * @return
     */
    public List<ChipInterface> getObjects() {
        Set<View> keys = mChipMap.keySet();
        List<ChipInterface> chips = new ArrayList<>();
        for (View key : keys) {
            if (mChipMap.get(key) == null) continue;
            chips.add(mChipMap.get(key));
        }
        return chips;
    }

    /**
     * returns if the current layout is collapsed or not
     * @return
     */
    public boolean isCollapsed() {
        return mHiddenViews != null && mHiddenViews.size() > 0;
    }

    /**
     * Converts Parcelable to ChipInterface
     * @param list
     * @return
     */
    protected ArrayList<ChipInterface> convertParcelableArrayToObjectArray(ArrayList<Parcelable>
            list) {
        return (ArrayList<ChipInterface>) (ArrayList) list;
    }

    /**
     * Returns parcelables converted from our ChipInterface
     * @return
     */
    protected ArrayList<Parcelable> getParcelableObjects() {
        ArrayList<Parcelable> parcelables = new ArrayList<>();
        for (Object obj : getObjects()) {
            if (obj instanceof Parcelable) {
                parcelables.add((Parcelable) obj);
            } else {
                Log.e(LOG_TAG, "Unable to save '" + obj + "'");
            }
        }
        return parcelables;
    }

    /**
     * Handle saving the objects state
     */
    private static class SavedState extends BaseSavedState {
        ArrayList<Parcelable> baseObjects;

        SavedState(Parcel in) {
            super(in);
            baseObjects = in.readArrayList(null);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            out.writeList(baseObjects);
        }

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    @Override
    public Parcelable onSaveInstanceState() {
        ArrayList<Parcelable> baseObjects = getParcelableObjects();
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);
        state.baseObjects = baseObjects;
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        List<ChipInterface> list = convertParcelableArrayToObjectArray(ss.baseObjects);
        for (int i=0; i<list.size(); i++) {
            addChipAt(getObjectView(list.get(i)), i);
        }

        // If layout is collapsed then try to request the focus
        if (!isCollapsed()) {
            mAutoCompleteTextView.setVisibility(VISIBLE);
            mAutoCompleteTextView.requestFocus();
        }
    }
}
