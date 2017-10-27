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
import android.util.AttributeSet;
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

    // Holds data of all the added views, help in expand/collapse function
    private Set<View> mAllViews = new HashSet<>();

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
        // First create temp list to hold all the views of first row of the flow layout
        List<View> views = new ArrayList<>();

        // First all all the views to hidden views
        for (int i = 0; i < getChildCount() - 1; i++) {
            mHiddenViews.add(getChildAt(i));
        }

        // Get the views of first line
        LineDefinition lineDefinition = getLines().get(0);
        for (ViewDefinition viewDefinition : lineDefinition.getViews()) {
            // now add this views to our temp list
            views.add(viewDefinition.getView());
            // And simultaneously removed from hidden views, as views of first row should not be
            // hidden
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
        countView.setPadding(5, 5, 5, 5);
        countView.setText(text);
        countView.setTextColor(Color.WHITE);
        countView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                forceExpand();
            }
        });
        countView.setTextSize(mCountViewTextSize);
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

        // Get the information of the view from cached map
        ChipInterface removedChip = mChipMap.get(view);

        // Remove the view at that particular position
        removeViewAt(position);

        // Call onChipRemoved for removed chip
        if (mChipListener != null) {
            mChipListener.onChipRemoved(removedChip);
        }

        // remove the chip from cache
        mChipMap.remove(getChildAt(position));

        // invalidate the view
        invalidate();
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
        ChipView chipView = new ChipView(getContext());
        chipView.setLabel(chipInterface.getLabel(), 0, ViewUtil.dpToPx(16));
        chipView.setPadding(2, 2, 2, 2);
        chipView.setHasAvatarIcon(true);
        chipView.setChipBorderColor(4, Color.BLUE);
        chipView.setOnClickListener(new ChipClickListener());
        chipView.setOnDragListener(mAstroDragListener);
        chipView.setLongClickable(true);
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

            if (mChipMap.get(v) == null) {
                return;
            }

            // Show detailed view as a dialog
            final DetailedChipView detailedChipView = getDetailedChipView(mChipMap.get(v));
            new ChipsDetailsDialog(v, detailedChipView, mChipMap.get(v)).show();
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

        // link or span on which user clicked
        // data about the clicked span / link
        private ChipInterface mChip;
        // the views of detailed chip layout
        private View mClickedView;
        private DetailedChipView mDetailedView;

        /**
         * Constructor for ChipsDetailsDialog which is also known as ChipDetailsView
         * This dialog is aligned w.r.t. clicked token considering x & y coordinates
         *
         * @param chip : the details of the token
         */
        public ChipsDetailsDialog(View clickedView, DetailedChipView detailedView,
                                  ChipInterface chip) {
            super(clickedView.getContext());
            mChip = chip;
            mClickedView = clickedView;
            mDetailedView = detailedView;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Don't want the title on this dialog
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (mDetailedView.getLayoutParams() == null) {
                mDetailedView.setLayoutParams(new RelativeLayout.LayoutParams(ViewUtil.dpToPx(300),
                        ViewUtil.dpToPx(100)));
            }
            mDetailedView.setVisibility(VISIBLE);
            setContentView(mDetailedView);

            // remove the chip once clicked on delete image
            mDetailedView.setOnDeleteClicked(new View.OnClickListener() {
                public void onClick(View v) {
                    FlowLayout flowLayout = (FlowLayout) mClickedView.getParent();
                    int i = 0;
                    boolean found = false;
                    for (i = 0; i < flowLayout.getChildCount() - 1; i++) {
                        if (flowLayout.getChildAt(i) == mClickedView) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return;
                    }
                    removeChipAt(i);
                    dismiss();
                }
            });

            // Anchor the dialog to where the user clicked.
            WindowManager.LayoutParams wmlp = getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.START;
            wmlp.x = (int) mClickedView.getX();
            wmlp.y = (int) mClickedView.getY() + ViewUtil.dpToPx(50);

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
}
