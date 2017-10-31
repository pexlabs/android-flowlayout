package org.apmem.tools.layouts.logic;

import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.MultiAutoCompleteTextView;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class LineDefinition {
    private List<ViewDefinition> views = new ArrayList<>();
    private final ConfigDefinition config;
    private int lineLength;
    private int lineThickness;
    private int lineStartThickness;
    private int lineStartLength;

    public LineDefinition(ConfigDefinition config) {
        this.config = config;
        this.lineStartThickness = 0;
        this.lineStartLength = 0;
    }

    public void addView(ViewDefinition child) {
        this.addView(this.views.size(), child);
    }

    public void addView(int i, ViewDefinition child) {
        this.views.add(i, child);

        this.lineLength = this.lineLength + child.getLength() + child.getSpacingLength();
        this.lineThickness = Math.max(this.lineThickness, child.getThickness() + child.getSpacingThickness());
    }

    public boolean canFit(ViewDefinition child) {
        if (child.getView() instanceof MultiAutoCompleteTextView) {
            int remaining = config.getMaxLength() - lineLength + child.getSpacingLength();
            MultiAutoCompleteTextView view = (MultiAutoCompleteTextView) child.getView();
            Rect bounds = new Rect();
            Paint textPaint = view.getPaint();
            String text = view.getText().toString();
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            int width = bounds.width();
            FlowLayout.LayoutParams params = (FlowLayout.LayoutParams) view.getLayoutParams();
            params.width = remaining;
            view.setLayoutParams(params);
            return width <= config.getMaxLength() / 5;
        }
        return lineLength + child.getLength() + child.getSpacingLength() <= config.getMaxLength();
    }

    public int getLineStartThickness() {
        return lineStartThickness;
    }

    public void setLineStartThickness(int lineStartThickness) {
        this.lineStartThickness = lineStartThickness;
    }

    public int getLineThickness() {
        return lineThickness;
    }

    public int getLineLength() {
        return lineLength;
    }

    public int getLineStartLength() {
        return lineStartLength;
    }

    public void setLineStartLength(int lineStartLength) {
        this.lineStartLength = lineStartLength;
    }

    public List<ViewDefinition> getViews() {
        return views;
    }

    public void setViews(List<ViewDefinition> view) {
        this.views = view;
    }

    public void setThickness(int thickness) {
        this.lineThickness = thickness;
    }

    public void setLength(int length) {
        this.lineLength = length;
    }

    public int getX() {
        return this.config.getOrientation() == CommonLogic.HORIZONTAL ? this.lineStartLength : this.lineStartThickness;
    }
    public int getY() {
        return this.config.getOrientation() == CommonLogic.HORIZONTAL ? this.lineStartThickness : this.lineStartLength;
    }
}