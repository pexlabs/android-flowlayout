package org.apmem.tools.listeners;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.MultiAutoCompleteTextView;

import org.apmem.tools.layouts.AstroFlowLayout;
import org.apmem.tools.layouts.FlowLayout;
import org.apmem.tools.model.ChipInterface;
import org.apmem.tools.util.ViewUtil;

/**
 * Created by kaustubh on 26/10/17.
 */

public class AstroDragListener implements View.OnDragListener {

    private static final String LOG_TAG = "###AstroDragListener";

    private Drawable mDragEnterShapeDrawable;

    public AstroDragListener() {
    }

    public void setDragEnterShapeDrawable(Drawable enterShapeDrawable) {
        mDragEnterShapeDrawable = enterShapeDrawable;
    }

    @Override
    public boolean onDrag(View targetView, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;

            case DragEvent.ACTION_DRAG_ENTERED:
                // Set enter drawable if it is not null
                if (mDragEnterShapeDrawable != null) {
                    targetView.setBackgroundDrawable(mDragEnterShapeDrawable);
                }

                break;

            case DragEvent.ACTION_DRAG_EXITED:
                // Once dragging is exited then remove the background resource for that
                // particular view
                targetView.setBackgroundResource(0);
                break;

            case DragEvent.ACTION_DROP:
                // Drag is Dropped! now get source view, target view, source container &
                // target container one by one
                // get the source view
                final View sourceView = (View) event.getLocalState();

                // Source & target positions of views
                // Target position is position where user wants to drop a view
                int targetPosition = -1;
                // Source position is a position of a view which user is trying to drop
                int sourcePosition = -1;

                // Get the source container
                final AstroFlowLayout sourceContainer = (AstroFlowLayout) sourceView.getParent();

                // As we have set drag listener to {@link AstroFlowLayout} we must consider the case
                // of dropping a view on AstroFlowLayout
                AstroFlowLayout targetContainer;
                // If user dropped view on AstroFlowLayout
                if (targetView instanceof AstroFlowLayout) {
                    targetContainer = (AstroFlowLayout) targetView;
                    // Find the position of MultiAutoCompleteTextView in that parent
                    targetPosition = ViewUtil.getPositionOfAutoCompleteTextView(targetContainer);
                    // If there are more than 1 views, just drop before MultiAutoCompleteTextView
                    if (targetPosition > 0) {
                        targetPosition--;
                    }
                } else {
                    // This means, user has dropped on ChipView or MultiAutoCompleteTextView
                    // get the target container
                    targetContainer = (AstroFlowLayout) targetView.getParent();
                    // Now get the position of target view so that we can drop source view
                    // according to target position
                    targetPosition = ViewUtil.getViewPositionInParent(targetContainer, targetView);
                    if (targetPosition == -1) {
                        return true;
                    }
                }

                // Check if the container in which we are dropping a view is collapsed or not
                // if it is collapsed, then expand it
                if (targetContainer.isCollapsed()) {
                    targetContainer.forceExpand();
                }

                // now get the position of source view in source container
                sourcePosition = ViewUtil.getViewPositionInParent(sourceContainer, sourceView);
                if (sourcePosition == -1) {
                    return true;
                }

                if (targetView instanceof MultiAutoCompleteTextView) {
                    // now is target view is MultiAutoCompleteTextView, that means user tried to drop
                    // source view on MultiAutoCompleteTextView, hence we will add view before it
                    if (targetContainer.getChildCount() <= 1) {
                        // if there is only child i.e. MultiAutoCompleteTextView then we will add
                        // view at the start
                        targetPosition = 0;
                    } else {
                        // else we will simply add view before it
                        targetPosition = targetContainer.getChildCount() - 1;
                    }
                }

                // Now we have figured out which view will be dropped at which point.
                // Now deal with the objects
                ChipInterface chipInterface = sourceContainer.getChipMap().get(sourceView);

                // As we had set visibility of source view to GONE, its time to make it visible
                // just for safety
                sourceView.setVisibility(View.VISIBLE);
                // As drop action is complete, remove source view from source target container
                sourceContainer.removeChildView(sourceView);

                // And add source view to target container
                targetContainer.addChipAtPositionWithChip(sourceView, chipInterface, targetPosition);
                break;

            case DragEvent.ACTION_DRAG_ENDED:
                // at the ent of drag event, check if event was successful or not
                // if not then restore the visibility of the view to VISIBLE
                // event.getResult() == false when user drags a view out of screen, or on a view
                // which does not listen to drag events
                if (!event.getResult()) {
                    final View localView = (View) event.getLocalState();
                    final FlowLayout localViewParent = (FlowLayout) localView.getParent();
                    localView.setVisibility(View.VISIBLE);
                    localViewParent.invalidate();
                }
        }
        return true;
    }

}
