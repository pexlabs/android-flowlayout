/**
 * AstroFlowLayout.java
 * <p>
 * Created by kaustubh on 26/10/17.
 * <p>
 * AstroFlowLayout extends {@link org.apmem.tools.layouts.FlowLayout} which abstract
 * Helps in expanding/collapsing layout
 * Also helps in creating & removing views
 */

package org.apmem.tools.layouts;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.apmem.tools.layouts.logic.LineDefinition;
import org.apmem.tools.layouts.logic.ViewDefinition;
import org.apmem.tools.listeners.AstroDragListener;
import org.apmem.tools.listeners.ChipListener;
import org.apmem.tools.model.Chip;
import org.apmem.tools.model.ChipInterface;
import org.apmem.tools.util.Preconditions;
import org.apmem.tools.util.ViewUtil;
import org.apmem.tools.views.ChipView;
import org.apmem.tools.views.DetailedChipView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AstroFlowLayout extends FlowLayout {

    // This is the value by which we will be reducing width of AutoCompleteView
    private static final int MARGIN = 100;

    // TAG for logging
    private static final String LOG_TAG = AstroFlowLayout.class.getSimpleName();

    // Flag to tell if the current view is collapsed or not. Initialially view is not collapsed
    private boolean mIsCollapsed = false;

    // When this FlowLayout is collapsed we hold data in this set
    private Set<View> mHiddenViews = new HashSet<>();

    // Simple Map to keep track of views & chips data associated with them
    // Helpful in getting final data & also in case of drag & drop
    private Map<View, ChipInterface> mChipMap = Collections.synchronizedMap(
            new HashMap<View, ChipInterface>());

    // ChipListener to track chips life cycle
    private ChipListener mChipListener;

    public AstroFlowLayout(Context context) {
        this(context, null);
    }

    public AstroFlowLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AstroFlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        // The below lines work as a hack. As we are using MultiAutoCompleteTextView it should
        // resize itself on invalidation of the layout. Now this will not happen if we set with of
        // MultiAutoCompleteTextView to MATCH_PARENT. Also, we want MultiAutoCompleteTextView to be
        // the last member in the layout. So what we just set minimum width of MultiAutoCompleteTextView
        // to 20px, so whenever user types/copies/pastes it will expand. And flow layout will
        // decide where to put it. As we are setting its min width to 20 & its width to WRAP_CONTENT
        // it MultiAutoCompleteTextView will not listen to click it is clicked outside.
        // We are mocking click & drag behaviour
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAutoCompleteTextView.hasFocus()) {
                    ViewUtil.showSoftKeyboard(mAutoCompleteTextView);
                    return;
                }
                if (mAutoCompleteTextView.getVisibility() == GONE ||
                        mAutoCompleteTextView.getVisibility() == INVISIBLE) {
                    removeAddMoreImageView();
                }
                ViewUtil.showSoftKeyboard(mAutoCompleteTextView);
                mAutoCompleteTextView.requestFocus();
            }
        });
        // This is important as user can drop outside the view, so it is important that our view
        // listens to DragEvents
        setOnDragListener(mAstroDragListener);
    }

    /**
     * Keysets can experience concurrent modification exceptions so if we want to interate over
     * keys, we need to synchronize the code or we can just make a copy of the collection into one
     * that will not change
     * @param map
     * @return
     */
    private Set<View> safeGetKeySet(@NonNull Map<View, ChipInterface> map) {
        Set<View> returnSet = new HashSet<>();
        synchronized (map) {
            returnSet.addAll(map.keySet());
        }
        return returnSet;
    }

    /**
     * this is +n feature. When auto complete view loses its focus we try to collapse the view
     * & add the dummy CountTextView at the end
     */
    @Override
    public void collapse() {
        Preconditions.checkIfCollapseSupported(mCollapsible);
        // There is nothing to collapse
        if (getChildCount() == 0) {
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

        // Loop through all the views of first line. Only views of first line will be visible
        // along with +1 i.e. count view
        for (ViewDefinition viewDefinition : lineDefinition.getViews()) {
            views.add(viewDefinition.getView());
            mHiddenViews.remove(viewDefinition.getView());
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
        }

        // set collapsed to true
        mIsCollapsed = true;
    }

    public String getCollapsedString() {
        Preconditions.checkIfCollapseSupported(mCollapsible);
        // There is nothing to collapse
        if (getChildCount() == 0) {
            return "";
        }

        // First create temp list to hold all the views of first row of the flow layout
        List<ChipInterface> views = new ArrayList<>();
        Set<View> hiddenViews = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        // First all all the views to hidden views
        for (int i = 0; i < getChildCount() - 1; i++) {
            hiddenViews.add(getChildAt(i));
        }

        // Get the views present at first line / row
        LineDefinition lineDefinition = getLines().get(0);

        // Loop through all the views of first line. Only views of first line will be visible
        // along with +1 i.e. count view
        int i = 0;
        for (ViewDefinition viewDefinition : lineDefinition.getViews()) {
            builder.append(mChipMap.get(viewDefinition.getView()).getInfo());
            hiddenViews.remove(viewDefinition.getView());
        }

        // So size of the hidden views is the count
        // if we have views to hide, show them as count
        if (hiddenViews.size() > 0) {
            builder.append(getCountView("+" + hiddenViews.size()));
        }
        return builder.toString();
    }

    /**
     * This helps in expanding the collapsed state
     * expand is called when clicked in CountTextView, Clicked on VisibleChips
     */
    @Override
    public void expand() {
        Preconditions.checkIfCollapseSupported(mCollapsible);
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
        Preconditions.checkIfCollapseSupported(mCollapsible);
        // Remove the last count text view
        if (getChildCount() > 1) {
            View view = getChildAt(getChildCount() - 1);
            if (view instanceof AstroFlowLayout) {
                removeViewAt(getChildCount() - 1);
            }
            if (view instanceof TextView) {
                removeViewAt(getChildCount() - 1);
            }
        }

        // Iterate over all the hidden views
        for (View view : mHiddenViews) {
            // And add them one by one
            addView(view);
        }

        // Add over beloved Auto complete text view.
        addAutoCompleteView();

        // clear the added hidden views
        mHiddenViews.clear();

        // set collapsed to false
        mIsCollapsed = false;
    }

    /**
     * generates TextVuew which holds count of the hidden views
     *
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
     *
     * @param position : position at which the chip is supposed to be removed
     */
    @Override
    public void removeChipAt(int position) {
        // Find the view at that position
        View view = getChildAt(position);

        removeChildView(view);
    }

    /**
     * Removes chip of that particular id
     * Astro added function
     *
     * @param id : id of the chip is supposed to be removed
     */
    public void removeChipById(Object id) {
        View viewToDelete = null;
        Set<View> keys = safeGetKeySet(mChipMap);
        // Go over all the views, get the associated ChipInterface and
        // see if IDs match to determine if we should return that view.
        for (View key : keys) {
            if (mChipMap.get(key) == null) continue;
            ChipInterface chip = mChipMap.get(key);
            if (chip == null) continue;
            if (chip.getId() == id) {
                viewToDelete = key;
                break;
            }
        }
        if (viewToDelete != null) {
            removeChildView(viewToDelete);
        }
    }

    @Override
    @Nullable public ChipInterface getChipAt(int position) {
        if (position >= getChildCount() || getChildCount() == 0) {
            return null;
        }
        // Find the view at that position
        View view = getChildAt(position);
        return mChipMap.get(view);
    }

    /**
     * Called by {@link #removeChipAt(int)} & {@link AstroDragListener} to remove the view
     * also updates cache
     *
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
        if (getChildCount() == 1) {
            setHint(mHintText);
        }

        if (getChildCount() == 2) {
            removeAddMoreImageView();
        }

        // Call onChipRemoved for removed chip
        if (mChipListener != null) {
            mChipListener.onChipRemoved(removedChip);
        }
    }

    /**
     * Adds chip at particular position
     *
     * @param view
     * @param position
     */
    @Override
    public void addChipAt(View view, int position) {
        super.addView(view, position);
        if (mChipListener != null) {
            mChipListener.onChipAdded(mChipMap.get(view));
        }
        // This will help in showing the cursor for MultiAutoCompleteTextView
        mAutoCompleteTextView.setText(" ");
    }

    /**
     * Helper method for setting chip listener
     *
     * @param listener
     */
    public void setChipListener(ChipListener listener) {
        mChipListener = listener;
    }

    /**
     * Similar to {@link #addChipAt(View, int) but instead of taking value from cache,
     * chip's value is passed, helpful in case of drag/drop event to avoid data corruption
     *
     * @param view
     * @param chipInterface
     * @param position
     */
    public void addChipAtPositionWithChip(View view, ChipInterface chipInterface, int position) {
        super.addView(view, position);
        // attach click listener again. Because this method is called from {@link AstroDragListener}
        // meaning, the view was removed & called again. So we need to attach click listener again
        ((ChipView)view).setOnChipClicked(new ChipClickListener());
        mChipMap.put(view, chipInterface);
        if (mChipListener != null) {
            mChipListener.onChipAdded(chipInterface);
        }
        // This will help in showing the cursor for MultiAutoCompleteTextView
        mAutoCompleteTextView.setText(" ");

        addAddMoreImageButton();
    }

    /**
     * This is called by parent class to add the view
     * Creates a ChipView with specific item. Which can be String or {@link Chip}
     *
     * @param item
     * @return
     */
    @Override
    public View getObjectView(Object item, boolean isAutoCompleted) {
        ChipInterface chipInterface = null;
        if (item instanceof String) {
            chipInterface = new Chip((String) item, (String) item, isAutoCompleted);
        }
        if (item instanceof ChipInterface) {
            chipInterface = (ChipInterface) item;
        }
        // Create a chip view
        final ChipView chipView = new ChipView(getContext());
        chipView.setLabel(chipInterface.getLabel(), 0, (int) getResources()
                .getDimension(R.dimen.chip_view_label_text_size),
                getResources().getDimensionPixelSize(R.dimen.text_avatar_spacing_default));
        int padding = (int) getResources().getDimension(R.dimen.chip_view_text_padding);
        chipView.setPadding(padding, padding, padding, padding);
        chipView.setHasAvatarIcon(false);
        chipView.setOnChipClicked(new ChipClickListener());
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
        chipView.setAutoCompleted(isAutoCompleted);
        // set long click listener for drag & drop functionality
        chipView.setOnChipLongClicked(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // if a chip in a view is long clicked, but the parent is collapsed
                // simply expand it
                if (mCollapsible && isCollapsed()) {
                    forceExpand();
                    ViewUtil.hideSoftKeyboard(mAutoCompleteTextView);
                    return true;
                }
                // Make sure we are dealing with ChipView
                while (!(view instanceof ChipView)) {
                    view = (View) view.getParent();
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
        // Don't let chip view occupy full width
        chipView.setMaxWidth(mMaxWidth - MARGIN);
        // Update cache
        mChipMap.put(chipView, chipInterface);
        chipView.setEnabled(true);
        mIsClicked = false;
        return chipView;
    }

    /**
     * Gets total number of chips
     * @return
     */
    public int getChipsCount() {
        return mChipMap.size();
    }

    /**
     * Click handler for ChipView
     */
    private class ChipClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (!isEnabled()) {
                // Do nothing if this view is disabled
                return;
            }

            // if a chip in a view is long clicked, but the parent is collapsed
            // simply expand it
            if (mIsCollapsed) {
                forceExpand();
            }

            // Make sure we are dealing with ChipView
            while (!(v instanceof ChipView)) {
                v = (View) v.getParent();
            }
            ChipInterface chip = mChipMap.get(v);
            if (chip == null) {
                return;
            }
            if (((ChipView)v).isAutoCompleted()) {
                // Show detailed view as a dialog
                final DetailedChipView detailedChipView = getDetailedChipView(chip);
                new ChipsDetailsDialog(v, detailedChipView).show();
            } else {
                int position = ViewUtil.getViewPositionInParent(AstroFlowLayout.this, v);
                if (getChildCount() > 0 && position < getChildCount()) {
                    removeChipAt(position);
                }
                mIsClicked = true;
                mAutoCompleteTextView.setText(chip.getInfo());
            }
        }
    }

    /**
     * Creates a detailed view with chip data.
     *
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

            FlowLayout flowLayout = (FlowLayout) mClickedView.getParent();
            // Don't want the title on this dialog
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            mDetailedView.setVisibility(VISIBLE);
            setContentView(mDetailedView);

            ViewGroup.LayoutParams layoutParams = null;
            if (mDetailedView.getLayoutParams() != null) {
                layoutParams = mDetailedView.getLayoutParams();
                layoutParams.width = flowLayout.getWidth();
                layoutParams.height = LayoutParams.WRAP_CONTENT;
            } else {
                layoutParams = new ViewGroup.LayoutParams(flowLayout.getWidth(),
                        LayoutParams.WRAP_CONTENT);
            }
            //layoutParams.setMargins(flowLayout.getLeft(), 0 , flowLayout.getRight(), 0);
            mDetailedView.setLayoutParams(layoutParams);

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

            Rect rect = new Rect();
            mClickedView.getGlobalVisibleRect(rect);
            // Anchor the dialog to where the user clicked.
            WindowManager.LayoutParams wmlp = getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.START;
            wmlp.x = rect.centerX() / 2;
            wmlp.y = rect.centerY() - (rect.height() * 2);

            // Don't let the dialog look like we are stealing all focus from the user.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            // If the user clicks outside the dialog, we should dismiss it.
            setCanceledOnTouchOutside(true);
        }
    }

    /**
     * Called by {@link AstroDragListener} to read Cache value
     * However updation is done only in {@link #addChipAt(View, int)} & {@link #removeChipAt(int)}
     *
     * @return
     */
    public Map<View, ChipInterface> getChipMap() {
        return mChipMap;
    }

    /**
     * Returns the current chips' values.
     *
     * @return
     */
    public List<ChipInterface> getObjects() {
        List<ChipInterface> returnChips = new ArrayList<>();
        Set<View> keys = safeGetKeySet(mChipMap);
        for (View key : keys) {
            if (mChipMap.get(key) == null) continue;
            returnChips.add(mChipMap.get(key));
        }
        return returnChips;
    }

    /**
     * @return all child chip views
     */
    public List<ChipView> getChips() {
        List<ChipView> chipViews = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (!(view instanceof ChipView)) {
                continue;
            }
            chipViews.add((ChipView) view);
        }
        return chipViews;
    }

    /**
     * @return specific chip at the position
     */
    @Nullable public ChipView getChipViewAtPosition(int position) {
        if (getChildCount() == 0 || getChildCount() <= position) {
            return null;
        }
        return (ChipView) getChildAt(position);
    }

    /**
     * returns if the current layout is collapsed or not
     *
     * @return
     */
    public boolean isCollapsed() {
        return mIsCollapsed;
    }

    /**
     * Converts Parcelable to ChipInterface
     *
     * @param list
     * @return
     */
    protected ArrayList<ChipInterface> convertParcelableArrayToObjectArray(ArrayList<Parcelable>
            list) {
        return (ArrayList<ChipInterface>) (ArrayList) list;
    }

    /**
     * Returns parcelables converted from our ChipInterface
     *
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

    public void clearAutoCompleteFocus() {
        if (mAutoCompleteTextView.hasFocus()) {
            mAutoCompleteTextView.clearFocus();
        }
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
        for (int i = 0; i < list.size(); i++) {
            ChipInterface chipInterface = list.get(i);
            addChipAt(getObjectView(chipInterface, chipInterface.isAutoCompleted()), i);
        }

        // If layout is collapsed then try to request the focus
        if (!isCollapsed()) {
            mAutoCompleteTextView.setVisibility(VISIBLE);
            if (!mAutoCompleteTextView.hasFocus()) {
                mAutoCompleteTextView.requestFocus();
            }
        }
    }
}
