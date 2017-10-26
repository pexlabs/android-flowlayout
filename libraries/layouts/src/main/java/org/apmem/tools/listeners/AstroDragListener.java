package org.apmem.tools.listeners;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;

import org.apmem.tools.layouts.FlowLayout;

/**
 * Created by kaustubh on 26/10/17.
 */

public class AstroDragListener implements View.OnDragListener {
    private Drawable mEnterShape;

    public AstroDragListener(Context context, int enterShapeDrawableId) {
        if(enterShapeDrawableId != -1) {
            mEnterShape = context.getResources().getDrawable(enterShapeDrawableId);
        }
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                // Determines if this View can accept the dragged data

                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                if (mEnterShape != null) {
                    v.setBackgroundDrawable(mEnterShape);
                }
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                v.setBackgroundResource(0);
                break;
            case DragEvent.ACTION_DROP:
                // Dropped, reassign View to ViewGroup
                Log.d("###MYDRAG", "DROPPED");
                final View view = (View) event.getLocalState();
                final FlowLayout owner = (FlowLayout) view.getParent();
                FlowLayout container = (FlowLayout) v.getParent();
                int i;
                boolean found = false;
                for(i=0; i<container.getChildCount()-1; i++) {
                    if(container.getChildAt(i) == v) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    return false;
                }

                owner.removeView(view);
                owner.invalidate();

                view.setVisibility(View.VISIBLE);
                container.addView(view, i+1);
                container.invalidate();
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                v.setBackgroundResource(0);
                /*if (!event.getResult()) {
                    final View view2 = (View) event.getLocalState();
                    view2.setVisibility(View.VISIBLE);
                }*/
            default:
                break;
        }
        return true;
    }

}
