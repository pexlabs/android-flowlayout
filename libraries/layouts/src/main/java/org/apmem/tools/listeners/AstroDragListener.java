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

    private Drawable mEnterShape;

    public AstroDragListener() {
    }

    public void setEnterShapeDrawable(Drawable enterShapeDrawable) {
        mEnterShape = enterShapeDrawable;
    }

    @Override
    public boolean onDrag(View targetView, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;

            case DragEvent.ACTION_DRAG_ENTERED:
                // Set enter drawable if it is not null
                if (mEnterShape != null) {
                    targetView.setBackgroundDrawable(mEnterShape);
                }

                AstroFlowLayout targetContainer = (AstroFlowLayout) targetView.getParent();
                // Check if the container in which we are dropping a view is collapsed or not
                // if it is collapsed, then expand it
                if (targetContainer.isCollapsed()) {
                    Log.d(LOG_TAG, "is container collapsed " + targetContainer.isCollapsed());
                    targetContainer.forceExpand();
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

                // get the source container
                final AstroFlowLayout sourceContainer = (AstroFlowLayout) sourceView.getParent();

                // get the target container
                targetContainer = (AstroFlowLayout) targetView.getParent();

                // now get the position of source view in source container
                int sourcePosition = ViewUtil.getViewPositionInParent(sourceContainer, sourceView);

                // get position of target view in target container
                // this is nothing but our target DROP position
                int targetPosition = ViewUtil.getViewPositionInParent(targetContainer, targetView);

                if (targetView instanceof MultiAutoCompleteTextView) {
                    // now is target view is MultiAutoCompleteTextView, that means user tried to drop
                    // source view on MultiAutoCompleteTextView, hence we will add view before it
                    if (targetContainer.getChildCount() <= 1) {
                        // if there is only child i.e. MultiAutoCompleteTextView then we will add
                        // view at the start
                        targetPosition = 0;
                    } else {
                        // else we will simply add view before it
                        targetPosition = targetContainer.getChildCount() - 2;
                    }
                } else {
                    // don't decrement value if view is dropped on first i.e. 0th position
                    if (targetPosition > 0) {
                        targetPosition--;
                    }
                }

                // Now we have figured out which view will be dropped at which point.
                // Now deal with the objects
                ChipInterface chipInterface = sourceContainer.getChipMap().get(sourceView);

                sourceView.setVisibility(View.VISIBLE);
                sourceContainer.getChipMap().remove(sourceView);
                sourceContainer.removeChipAt(sourcePosition);

                // add view to target container
                targetContainer.addChipAtPositionWithChip(sourceView, chipInterface, targetPosition);
                break;

            case DragEvent.ACTION_DRAG_ENDED:
                // at the ent of drag event, check if event was successful or not
                // if not then restore the visibility of the view to VISIBLE
                // event.getResult() == false when user drags a view out of screen, or on a view
                // which does not listens to drag events
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
