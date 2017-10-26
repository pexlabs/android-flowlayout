package org.apmem.tools.example;

import android.content.ClipData;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apmem.tools.layouts.FlowLayout;
import org.apmem.tools.layouts.logic.LineDefinition;
import org.apmem.tools.layouts.logic.ViewDefinition;
import org.apmem.tools.listeners.AstroDragListener;
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

/**
 * Created by kaustubh on 26/10/17.
 */

public class AstroFlowLayout extends FlowLayout {

    private static final String LOG_TAG = AstroFlowLayout.class.getSimpleName();

    private Set<View> mAllViews = new HashSet<>();
    private Set<View> mHiddenViews = new HashSet<>();
    private Map<View, ChipInterface> mChipMap = new HashMap<>();
    private ColorStateList mChipDetailedTextColor;
    private ColorStateList mChipDetailedDeleteIconColor;
    private ColorStateList mChipDetailedBackgroundColor;
    private boolean mShowChipDetailed = true;
    private ColorStateList mChipBackgroundColor;

    public AstroFlowLayout(Context context) {
        this(context, null);
    }

    public AstroFlowLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AstroFlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        if(attributeSet != null) {
            TypedArray array = context.getTheme().obtainStyledAttributes(attributeSet,
                    R.styleable.FlowLayout,0 ,0);
            try {
                mChipBackgroundColor = array.getColorStateList(R.styleable.FlowLayout_chip_backgroundColor);
                // show chip detailed
                mShowChipDetailed = array.getBoolean(R.styleable.FlowLayout_showChipDetailed, true);
                // chip detailed text color
                mChipDetailedTextColor = array.getColorStateList(R.styleable.FlowLayout_chip_detailed_textColor);
                mChipDetailedBackgroundColor = array.getColorStateList(R.styleable.FlowLayout_chip_detailed_backgroundColor);
                mChipDetailedDeleteIconColor = array.getColorStateList(R.styleable.FlowLayout_chip_detailed_deleteIconColor);
            } finally {
                array.recycle();
            }
        }
    }

    public void setChipBackgroundColor(ColorStateList backgroundColor) {
        mChipBackgroundColor = backgroundColor;
    }

    public boolean isShowChipDetailed() {
        return mShowChipDetailed;
    }

    public void setChipDetailedTextColor(ColorStateList chipDetailedTextColor) {
        mChipDetailedTextColor = chipDetailedTextColor;
    }

    public void setChipDetailedDeleteIconColor(ColorStateList chipDetailedDeleteIconColor) {
        mChipDetailedDeleteIconColor = chipDetailedDeleteIconColor;
    }

    public void setChipDetailedBackgroundColor(ColorStateList chipDetailedBackgroundColor) {
        mChipDetailedBackgroundColor = chipDetailedBackgroundColor;
    }

    @Override
    public void collapse() {
        List<View> views = new ArrayList<>();
        for(int i=0; i< getChildCount()-1; i++) {
            mHiddenViews.add(getChildAt(i));
        }

        LineDefinition lineDefinition = getLines().get(0);
        for(ViewDefinition viewDefinition : lineDefinition.getViews()) {
            views.add(viewDefinition.getView());
            mHiddenViews.remove(viewDefinition.getView());
        }

        removeAllViews();
        for(View view : views) {
            addView(view);
        }
        addView(getCountView("+"+mHiddenViews.size()));
    }

    @Override
    public void expand() {
        if(mHiddenViews.size() < 1) {
            return;
        }
        removeViewAt(getChildCount()-1);
        for(View view : mHiddenViews) {
            addView(view);
        }
        addAutoCompleteView();
        mHiddenViews.clear();
    }

    private View getCountView(String text) {
        TextView t = new TextView(getContext());
        t.setPadding(5, 5, 5, 5);
        t.setText(text);
        t.setTextColor(Color.WHITE);
        return t;
    }

    @Override
    public View getObjectView(Object item) {
        String text = "";
        if(item instanceof String) {
            text = (String) item;
        }
        //return getTextView(text, true);
        ChipView chipView = new ChipView(getContext());
        chipView.setLabel(text, 0, ViewUtil.dpToPx(16));
        chipView.setPadding(2, 2, 2, 2);
        chipView.setHasAvatarIcon(true);
        chipView.setChipBorderColor(4, Color.BLUE);
        chipView.setOnChipClicked(new ChipClickListener());
        chipView.setOnDragListener(new AstroDragListener(getContext(), -1));
        chipView.setOnTouchListener(new AstroTouchListener());
        return chipView;
    }

    private class ChipClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            int[] coord = new int[2];
            v.getLocationInWindow(coord);

            final DetailedChipView detailedChipView = getDetailedChipView(mChipMap.get(v));
            setDetailedChipViewPosition(v, detailedChipView, coord);

            detailedChipView.setOnDeleteClicked(new View.OnClickListener() {
                @Override
                public void onClick(View child) {
                    FlowLayout flowLayout = (FlowLayout) v.getParent();
                    int i=0;
                    boolean found = false;
                    for(i=0; i<flowLayout.getChildCount()-1; i++) {
                        if(flowLayout.getChildAt(i) == v) {
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        return;
                    }
                    removeChip(i);
                    detailedChipView.fadeOut();
                }
            });
        }
    }

    private DetailedChipView getDetailedChipView(ChipInterface chip) {
        return new DetailedChipView.Builder(getContext())
                .chip(chip)
                .textColor(mChipDetailedTextColor)
                .backgroundColor(mChipDetailedBackgroundColor)
                .deleteIconColor(mChipDetailedDeleteIconColor)
                .build();
    }

    private void setDetailedChipViewPosition(View view, DetailedChipView detailedChipView,
                                             int[] coord) {
        // window width
        ViewGroup rootView = (ViewGroup) view.getRootView();
        int windowWidth = ViewUtil.getWindowWidth(getContext());

        // chip size
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewUtil.dpToPx(300),
                ViewUtil.dpToPx(100));

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        // align left window
        if(coord[0] <= 0) {
            layoutParams.leftMargin = 0;
            layoutParams.topMargin = coord[1] - ViewUtil.dpToPx(13);
            detailedChipView.alignLeft();
        }
        // align right
        else if(coord[0] + ViewUtil.dpToPx(300) > windowWidth + ViewUtil.dpToPx(13)) {
            layoutParams.leftMargin = windowWidth - ViewUtil.dpToPx(300);
            layoutParams.topMargin = coord[1] - ViewUtil.dpToPx(13);
            detailedChipView.alignRight();
        }
        // same position as chip
        else {
            layoutParams.leftMargin = coord[0] - ViewUtil.dpToPx(13);
            layoutParams.topMargin = coord[1] - ViewUtil.dpToPx(13);
        }

        // show view
        rootView.addView(detailedChipView, layoutParams);
        detailedChipView.fadeIn();
    }


    public void addView(View view) {
        mAllViews.add(view);
        view.setOnDragListener(new AstroDragListener(getContext(), -1));
        view.setOnTouchListener(new AstroTouchListener());
        super.addView(view);
    }


    private final class AstroTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(mHiddenViews.size() > 0) {
                expand();
                return true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                ClipData data = ClipData.newPlainText(String.valueOf(view.getId()), "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                view.startDrag(data, shadowBuilder, view, 0);
                /*view.setVisibility(View.GONE);
                final FlowLayout owner = (FlowLayout) view.getParent();
                owner.invalidate();*/
            }
            return true;
        }
    }
}
