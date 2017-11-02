/**
 * Extension of existing Flow layout for Astro specific functions
 */

package org.apmem.tools.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import org.apmem.tools.layouts.logic.CommonLogic;
import org.apmem.tools.layouts.logic.ConfigDefinition;
import org.apmem.tools.layouts.logic.LineDefinition;
import org.apmem.tools.layouts.logic.ViewDefinition;
import org.apmem.tools.listeners.AstroDragListener;
import org.apmem.tools.model.ChipInterface;
import org.apmem.tools.util.Utils;
import org.apmem.tools.util.ViewUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class FlowLayout extends ViewGroup {

    private static final String LOG_TAG = FlowLayout.class.getSimpleName();
    private static final int DEFAULT_PADDING = 8;
    private static final int MIN_WIDTH = 20;

    private final ConfigDefinition mConfig;
    private List<LineDefinition> mLines = new ArrayList<>();
    private List<ViewDefinition> mViews = new ArrayList<>();

    // TextWatcher to watch of text change events
    private TextWatcher mTextWatcher;
    // Our beloved AutoCompleteTextView
    protected MultiAutoCompleteTextView mAutoCompleteTextView;

    // hint of AutoCompleteTextView
    protected String mHintText = "";

    // color related attributes
    protected int mChipDetailedTextColor;
    protected int mChipDetailedDeleteIconColor;
    protected int mChipDetailedBackgroundColor;
    protected int mChipBackgroundColor;
    protected int mCountViewTextColor;
    protected boolean mShowChipDetailed = true;
    protected float mCountViewTextSize;
    protected int mChipBorderColor;

    public FlowLayout(Context context) {
        super(context);
        this.mConfig = new ConfigDefinition();
        readStyleParameters(context, null);
    }

    public FlowLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mConfig = new ConfigDefinition();
        readStyleParameters(context, attributeSet);
    }

    public FlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.mConfig = new ConfigDefinition();
        readStyleParameters(context, attributeSet);
    }

    private void readStyleParameters(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout);
        try {
            this.mConfig.setOrientation(a.getInteger(R.styleable.FlowLayout_android_orientation,
                    CommonLogic.HORIZONTAL));
            this.mConfig.setMaxLines(a.getInteger(R.styleable.FlowLayout_maxLines, 0));
            this.mConfig.setDebugDraw(a.getBoolean(R.styleable.FlowLayout_debugDraw, false));
            this.mConfig.setWeightDefault(a.getFloat(R.styleable.FlowLayout_weightDefault, 0.0f));
            this.mConfig.setGravity(a.getInteger(R.styleable.FlowLayout_android_gravity,
                    Gravity.NO_GRAVITY));

            int layoutDirection;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutDirection = a.getInteger(R.styleable.FlowLayout_layoutDirection,
                        LAYOUT_DIRECTION_LTR);
            } else {
                layoutDirection = a.getInteger(R.styleable.FlowLayout_layoutDirection,
                        super.getLayoutDirection());
            }
            //noinspection ResourceType
            this.setLayoutDirection(layoutDirection);

            // color initialization for chip view & detail view
            mChipDetailedTextColor = a.getColor(R.styleable.FlowLayout_chip_detailed_textColor,
                    getResources().getColor(R.color.white));
            mChipDetailedBackgroundColor = a.getColor(R.styleable.
                            FlowLayout_chip_detailed_backgroundColor,
                    getResources().getColor(R.color.astroViolet700));
            mChipDetailedDeleteIconColor = a.getColor(R.styleable.
                            FlowLayout_chip_detailed_deleteIconColor,
                    getResources().getColor(android.R.color.transparent));
            mChipBackgroundColor = a.getColor(R.styleable.FlowLayout_chip_backgroundColor,
                    getResources().getColor(R.color.astroBlack100));
            mCountViewTextColor = a.getColor(R.styleable.FlowLayout_count_view_text_color,
                    getResources().getColor(R.color.astroBlack200));
            mChipBorderColor = a.getColor(R.styleable.FlowLayout_count_view_border_color,
                    getResources().getColor(R.color.blue900));

            // show chip detailed
            mShowChipDetailed = a.getBoolean(R.styleable.FlowLayout_showChipDetailed, true);
            mCountViewTextSize = a.getDimension(R.styleable.FlowLayout_count_view_size,
                    getResources().getDimension(R.dimen.count_view_label_text_size));

        } finally {
            a.recycle();
        }

        // first initialize the AutoCompleteView
        initAutoCompleteView();
        addView(mAutoCompleteTextView);
        setGravity(Gravity.CENTER_VERTICAL);
    }

    public void addAutoCompleteView() {
        addView(mAutoCompleteTextView);
    }

    /**
     * Sets adapter to AutoCompleteTextView
     *
     * @param adapter
     */
    public <T extends BaseAdapter & Filterable> void setAutoCompleteViewAdapter(T adapter) {
        //mListAdapter = adapter;
        mAutoCompleteTextView.setAdapter(adapter);
        mAutoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    /**
     * Sets background of {@link org.apmem.tools.views.ChipView}
     *
     * @param backgroundColor
     */
    public void setChipBackgroundColor(int backgroundColor) {
        mChipBackgroundColor = backgroundColor;
    }

    /**
     * Flag to show chip detail or now, true by default
     *
     * @return
     */
    public boolean isShowChipDetailed() {
        return mShowChipDetailed;
    }

    /**
     * Sets background color of text of detailed view
     *
     * @param chipDetailedTextColor
     */
    public void setChipDetailedTextColor(int chipDetailedTextColor) {
        mChipDetailedTextColor = chipDetailedTextColor;
    }

    /**
     * Sets delete icon of detailed view
     *
     * @param chipDetailedDeleteIconColor
     */
    public void setChipDetailedDeleteIconColor(int chipDetailedDeleteIconColor) {
        mChipDetailedDeleteIconColor = chipDetailedDeleteIconColor;
    }

    /**
     * sets background color of detailed view
     *
     * @param chipDetailedBackgroundColor
     */
    public void setChipDetailedBackgroundColor(int chipDetailedBackgroundColor) {
        mChipDetailedBackgroundColor = chipDetailedBackgroundColor;
    }

    /**
     * Sets size of Count text view (used for +n feature)
     *
     * @param countViewTextSize
     */
    public void setCountViewTextSize(float countViewTextSize) {
        mCountViewTextSize = countViewTextSize;
    }

    /**
     * Sets text color of count text view
     *
     * @param countViewTextColor
     */
    public void setCountViewTextColor(int countViewTextColor) {
        mCountViewTextColor = countViewTextColor;
    }

    /**
     * Sets border color of chip view
     *
     * @param borderColor
     */
    public void setChipBorderColor(int borderColor) {
        mChipBorderColor = borderColor;
    }

    /**
     * Sets the hint of AutoCompleteView
     *
     * @param hintLabel
     */
    public void setHint(String hintLabel) {
        mHintText = hintLabel;
        mAutoCompleteTextView.setHint(mHintText);
    }

    /**
     * DropDownAnchor layout id
     *
     * @param id
     */
    public void setDropDownAnchor(int id) {
        mAutoCompleteTextView.setDropDownAnchor(id);
    }

    public void setTextWatcher(TextWatcher textWatcher) {
        mTextWatcher = textWatcher;
    }

    private void initAutoCompleteView() {
        // Create a new Instance
        mAutoCompleteTextView = new MultiAutoCompleteTextView(getContext());
        // Set min width to 20px. Resizing will not happen if we set with of
        // MultiAutoCompleteTextView to MATCH_PARENT. Also, we want MultiAutoCompleteTextView to be
        // the last member in the layout. So what we just set minimum width of MultiAutoCompleteTextView
        // to 20px, so whenever user types/copies/pastes it will expand. And flow layout will
        // decide where to put it.
        mAutoCompleteTextView.setMinWidth(MIN_WIDTH);
        // This is kind of hack to show cursor. We will handle all trim related functionalities
        mAutoCompleteTextView.setText(" ");

        final int padding = ViewUtil.dpToPx(DEFAULT_PADDING);
        mAutoCompleteTextView.setPadding(padding, padding, padding, padding);

        mAutoCompleteTextView.setGravity(Gravity.CENTER_VERTICAL);
        // set the hint
        mAutoCompleteTextView.setHint(mHintText);
        mAutoCompleteTextView.setBackgroundResource(android.R.color.transparent);

        // IME options & Input related initialization
        // prevent fullscreen on landscape
        mAutoCompleteTextView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mAutoCompleteTextView.setPrivateImeOptions("nm");
        // no suggestion
        mAutoCompleteTextView.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mAutoCompleteTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // When clicked on DropDown list add that value as a chip
        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAutoCompleteTextView.setText("");
                View chipView = getObjectView(mAutoCompleteTextView.getAdapter().getItem(position));
                int chipPosition = getChildCount() - 1;
                addChipAt(chipView, chipPosition);
            }
        });

        // make suggestions view full width
        mAutoCompleteTextView.setDropDownWidth(ViewUtil.getDeviceWidth());

        // When user clicks on DONE, add the value as chip if any
        mAutoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = v.getText().toString();
                    if (TextUtils.isEmpty(text)) {
                        return true;
                    }
                    if (!Utils.isValidEmailAddress(text)) {
                        return true;
                    }
                    mAutoCompleteTextView.setText("");
                    View chipView = getObjectView(text);
                    int chipPosition = getChildCount() - 1;
                    addChipAt(chipView, chipPosition);
                    return true;
                }
                return false;
            }
        });

        // if user pressed delete button, remove the chip
        mAutoCompleteTextView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    // Remove last chip.
                    if (getChildCount() > 1 && mAutoCompleteTextView.getText().toString().length() <= 1) {
                        removeChipAt(getChildCount() - 2);
                        return true;
                    }
                }
                return false;
            }
        });

        // Add text watcher, if at all user pressed <Space> add the text before that as a chip
        mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO optimize this later
                // Not sure why added chip is calling this method. Hence ignoring text change
                // events fired by chip.
                if (!(s.toString().contains("model.Chip"))) {
                    if (mTextWatcher != null) {
                        mTextWatcher.onTextChanged(s, start, count, after);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(s.toString().contains("model.Chip"))) {
                    if (mTextWatcher != null) {
                        mTextWatcher.onTextChanged(s, start, before, count);
                    }
                }

                if (s.length() >= 2 && s.charAt(s.length() - 1) == ' ' && s.charAt(s.length() - 2) != ' ') {
                    // add only if it is valid email address
                    if (Utils.isValidEmailAddress(s.toString().trim())) {
                        mAutoCompleteTextView.setText("");
                        View chipView = getObjectView(s.toString().trim());
                        int chipPosition = getChildCount() - 1;
                        addChipAt(chipView, chipPosition);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!(s.toString().contains("model.Chip"))) {
                    if (mTextWatcher != null) {
                        mTextWatcher.afterTextChanged(s);
                    }
                }
            }
        });

        mAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // if focus is lost then hide the cursor forcefully
                    mAutoCompleteTextView.setCursorVisible(false);
                    mAutoCompleteTextView.setActivated(false);
                    mAutoCompleteTextView.setPressed(false);
                    // lost the focus. check if there is a text
                    String text = mAutoCompleteTextView.getText().toString().trim();
                    if (!TextUtils.isEmpty(text)) {
                        if (Utils.isValidEmailAddress(text)) {
                            View chipView = getObjectView(text);
                            int chipPosition = getChildCount() - 1;
                            addChipAt(chipView, chipPosition);
                        }
                    }
                    // If no focus then collapse
                    if (mLines.size() > 1) {
                        collapse();
                    }
                } else {
                    // if focus is gained then show the cursor forcefully
                    mAutoCompleteTextView.setCursorVisible(true);
                    mAutoCompleteTextView.setActivated(true);
                    mAutoCompleteTextView.setPressed(true);
                    // If AutoCompleteView gets the focus then expand
                    expand();
                }
            }
        });

        // AutoCompleteView should also listen for Dragging events
        mAutoCompleteTextView.setOnDragListener(new AstroDragListener());
    }

    public abstract void collapse();

    public abstract void expand();

    public abstract View getObjectView(Object item);

    public abstract void removeChipAt(int position);

    public abstract void addChipAt(View view, int position);

    public abstract List<ChipInterface> getObjects();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int count = this.getChildCount();
        mViews.clear();
        mLines.clear();
        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft() + this.getPaddingRight(), lp.width),
                    getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom(), lp.height)
            );

            ViewDefinition view = new ViewDefinition(this.mConfig, child);
            view.setWidth(child.getMeasuredWidth());
            view.setHeight(child.getMeasuredHeight());
            view.setNewLine(lp.isNewLine());
            view.setGravity(lp.getGravity());
            view.setWeight(lp.getWeight());
            view.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            mViews.add(view);
        }

        this.mConfig.setMaxWidth(MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft());
        this.mConfig.setMaxHeight(MeasureSpec.getSize(heightMeasureSpec) - this.getPaddingTop() - this.getPaddingBottom());
        this.mConfig.setWidthMode(MeasureSpec.getMode(widthMeasureSpec));
        this.mConfig.setHeightMode(MeasureSpec.getMode(heightMeasureSpec));
        this.mConfig.setCheckCanFit(this.mConfig.getLengthMode() != View.MeasureSpec.UNSPECIFIED);

        CommonLogic.fillLines(mViews, mLines, mConfig);
        CommonLogic.calculateLinesAndChildPosition(mLines);

        int contentLength = 0;
        final int linesCount = mLines.size();
        for (int i = 0; i < linesCount; i++) {
            LineDefinition l = mLines.get(i);
            contentLength = Math.max(contentLength, l.getLineLength());
        }

        LineDefinition currentLine = mLines.get(mLines.size() - 1);
        int contentThickness = currentLine.getLineStartThickness() + currentLine.getLineThickness();
        int realControlLength = CommonLogic.findSize(this.mConfig.getLengthMode(), this.mConfig.getMaxLength(), contentLength);
        int realControlThickness = CommonLogic.findSize(this.mConfig.getThicknessMode(), this.mConfig.getMaxThickness(), contentThickness);

        CommonLogic.applyGravityToLines(mLines, realControlLength, realControlThickness, mConfig);

        for (int i = 0; i < linesCount; i++) {
            LineDefinition line = mLines.get(i);
            applyPositionsToViews(line);
        }

        /* need to take padding into account */
        int totalControlWidth = this.getPaddingLeft() + this.getPaddingRight();
        int totalControlHeight = this.getPaddingBottom() + this.getPaddingTop();
        if (this.mConfig.getOrientation() == CommonLogic.HORIZONTAL) {
            totalControlWidth += contentLength;
            totalControlHeight += contentThickness;
        } else {
            totalControlWidth += contentThickness;
            totalControlHeight += contentLength;
        }
        this.setMeasuredDimension(resolveSize(totalControlWidth, widthMeasureSpec), resolveSize(totalControlHeight, heightMeasureSpec));
    }

    private void applyPositionsToViews(LineDefinition line) {
        final List<ViewDefinition> childViews = line.getViews();
        final int childCount = childViews.size();
        for (int i = 0; i < childCount; i++) {
            final ViewDefinition child = childViews.get(i);
            final View view = child.getView();
            view.measure(
                    MeasureSpec.makeMeasureSpec(child.getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(child.getHeight(), MeasureSpec.EXACTLY)
            );
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int linesCount = this.mLines.size();
        for (int i = 0; i < linesCount; i++) {
            final LineDefinition line = this.mLines.get(i);

            final int count = line.getViews().size();
            for (int j = 0; j < count; j++) {
                ViewDefinition child = line.getViews().get(j);
                View view = child.getView();
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                view.layout(
                        this.getPaddingLeft() + line.getX() + child.getInlineX() + lp.leftMargin,
                        this.getPaddingTop() + line.getY() + child.getInlineY() + lp.topMargin,
                        this.getPaddingLeft() + line.getX() + child.getInlineX() + lp.leftMargin + child.getWidth(),
                        this.getPaddingTop() + line.getY() + child.getInlineY() + lp.topMargin + child.getHeight()
                );
            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean more = super.drawChild(canvas, child, drawingTime);
        this.drawDebugInfo(canvas, child);
        return more;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(this.getContext(), attributeSet);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    private void drawDebugInfo(Canvas canvas, View child) {
        if (!isDebugDraw()) {
            return;
        }

        Paint childPaint = this.createPaint(0xffffff00);
        Paint newLinePaint = this.createPaint(0xffff0000);

        LayoutParams lp = (LayoutParams) child.getLayoutParams();

        if (lp.rightMargin > 0) {
            float x = child.getRight();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x + lp.rightMargin, y, childPaint);
            canvas.drawLine(x + lp.rightMargin - 4.0f, y - 4.0f, x + lp.rightMargin, y, childPaint);
            canvas.drawLine(x + lp.rightMargin - 4.0f, y + 4.0f, x + lp.rightMargin, y, childPaint);
        }

        if (lp.leftMargin > 0) {
            float x = child.getLeft();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x - lp.leftMargin, y, childPaint);
            canvas.drawLine(x - lp.leftMargin + 4.0f, y - 4.0f, x - lp.leftMargin, y, childPaint);
            canvas.drawLine(x - lp.leftMargin + 4.0f, y + 4.0f, x - lp.leftMargin, y, childPaint);
        }

        if (lp.bottomMargin > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getBottom();
            canvas.drawLine(x, y, x, y + lp.bottomMargin, childPaint);
            canvas.drawLine(x - 4.0f, y + lp.bottomMargin - 4.0f, x, y + lp.bottomMargin, childPaint);
            canvas.drawLine(x + 4.0f, y + lp.bottomMargin - 4.0f, x, y + lp.bottomMargin, childPaint);
        }

        if (lp.topMargin > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getTop();
            canvas.drawLine(x, y, x, y - lp.topMargin, childPaint);
            canvas.drawLine(x - 4.0f, y - lp.topMargin + 4.0f, x, y - lp.topMargin, childPaint);
            canvas.drawLine(x + 4.0f, y - lp.topMargin + 4.0f, x, y - lp.topMargin, childPaint);
        }

        if (lp.isNewLine()) {
            if (this.mConfig.getOrientation() == CommonLogic.HORIZONTAL) {
                float x = child.getLeft();
                float y = child.getTop() + child.getHeight() / 2.0f;
                canvas.drawLine(x, y - 6.0f, x, y + 6.0f, newLinePaint);
            } else {
                float x = child.getLeft() + child.getWidth() / 2.0f;
                float y = child.getTop();
                canvas.drawLine(x - 6.0f, y, x + 6.0f, y, newLinePaint);
            }
        }
    }

    private Paint createPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(2.0f);
        return paint;
    }

    public int getOrientation() {
        return this.mConfig.getOrientation();
    }

    public void setOrientation(int orientation) {
        this.mConfig.setOrientation(orientation);
        this.requestLayout();
    }

    public boolean isDebugDraw() {
        return this.mConfig.isDebugDraw() || debugDraw();
    }

    public void setDebugDraw(boolean debugDraw) {
        this.mConfig.setDebugDraw(debugDraw);
        this.invalidate();
    }

    private boolean debugDraw() {
        try {
            // android add this method at 4.1
            Method m = ViewGroup.class.getDeclaredMethod("debugDraw", (Class[]) null);
            m.setAccessible(true);
            return (boolean) m.invoke(this, new Object[]{null});
        } catch (Exception e) {
            // if no such method (android not support this at lower api level), return false
            // ignore this, it's safe here
        }

        return false;
    }

    public float getWeightDefault() {
        return this.mConfig.getWeightDefault();
    }

    public void setWeightDefault(float weightDefault) {
        this.mConfig.setWeightDefault(weightDefault);
        this.requestLayout();
    }

    public int getGravity() {
        return this.mConfig.getGravity();
    }

    public void setGravity(int gravity) {
        this.mConfig.setGravity(gravity);
        this.requestLayout();
    }

    @Override
    public int getLayoutDirection() {
        if (this.mConfig == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return View.LAYOUT_DIRECTION_LTR;
            } else {
                return super.getLayoutDirection();
            }
        }

        //noinspection ResourceType
        return this.mConfig.getLayoutDirection();
    }

    @Override
    public void setLayoutDirection(int layoutDirection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            super.setLayoutDirection(layoutDirection);
        }

        //noinspection ResourceType
        if (this.mConfig.getLayoutDirection() != layoutDirection) {
            this.mConfig.setLayoutDirection(layoutDirection);
            requestLayout();
        }
    }

    public int getMaxLines() {
        return this.mConfig.getMaxLines();
    }

    public void setMaxLines(int maxLines) {
        this.mConfig.setMaxLines(maxLines);
        this.requestLayout();
    }

    public static class LayoutParams extends MarginLayoutParams {
        @ViewDebug.ExportedProperty(mapping = {
                @ViewDebug.IntToString(from = Gravity.NO_GRAVITY, to = "NONE"),
                @ViewDebug.IntToString(from = Gravity.TOP, to = "TOP"),
                @ViewDebug.IntToString(from = Gravity.BOTTOM, to = "BOTTOM"),
                @ViewDebug.IntToString(from = Gravity.LEFT, to = "LEFT"),
                @ViewDebug.IntToString(from = Gravity.RIGHT, to = "RIGHT"),
                @ViewDebug.IntToString(from = Gravity.CENTER_VERTICAL, to = "CENTER_VERTICAL"),
                @ViewDebug.IntToString(from = Gravity.FILL_VERTICAL, to = "FILL_VERTICAL"),
                @ViewDebug.IntToString(from = Gravity.CENTER_HORIZONTAL, to = "CENTER_HORIZONTAL"),
                @ViewDebug.IntToString(from = Gravity.FILL_HORIZONTAL, to = "FILL_HORIZONTAL"),
                @ViewDebug.IntToString(from = Gravity.CENTER, to = "CENTER"),
                @ViewDebug.IntToString(from = Gravity.FILL, to = "FILL")
        })

        private boolean newLine = false;
        private int gravity = Gravity.NO_GRAVITY;
        private float weight = -1.0f;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.readStyleParameters(context, attributeSet);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        private void readStyleParameters(Context context, AttributeSet attributeSet) {
            TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout_LayoutParams);
            try {
                this.newLine = a.getBoolean(R.styleable.FlowLayout_LayoutParams_layout_newLine, false);
                this.gravity = a.getInt(R.styleable.FlowLayout_LayoutParams_android_layout_gravity, Gravity.NO_GRAVITY);
                this.weight = a.getFloat(R.styleable.FlowLayout_LayoutParams_layout_weight, -1.0f);
            } finally {
                a.recycle();
            }
        }

        public int getGravity() {
            return gravity;
        }

        public void setGravity(int gravity) {
            this.gravity = gravity;
        }

        public float getWeight() {
            return weight;
        }

        public void setWeight(float weight) {
            this.weight = weight;
        }

        public boolean isNewLine() {
            return newLine;
        }

        public void setNewLine(boolean newLine) {
            this.newLine = newLine;
        }
    }

    public List<LineDefinition> getLines() {
        return mLines;
    }
}
