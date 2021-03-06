/**
 * Extension of existing Flow layout for Astro specific functions
 */

package org.apmem.tools.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
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
import org.apmem.tools.views.CopyPasteOptions;

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

    private OnFocusChangeListener mFocusChangeListener;

    // Our beloved AutoCompleteTextView
    protected MultiAutoCompleteTextView mAutoCompleteTextView;

    // Drag listener which listens to chips life like added/removed
    protected AstroDragListener mAstroDragListener;
    
    // hint of AutoCompleteTextView
    protected String mHintText = "";

    protected boolean mIsClicked = false;

    protected int mMaxWidth = 0;

    // color related attributes
    protected int mChipDetailedTextColor;
    protected int mChipDetailedDeleteIconColor;
    protected int mChipDetailedBackgroundColor;
    protected int mChipBackgroundColor;
    protected int mCountViewTextColor;
    protected boolean mShowChipDetailed = true;
    protected float mCountViewTextSize;
    protected int mChipBorderColor;
    protected int mTextSize;
    protected int mTextColor;
    protected boolean mCollapsible;
    protected Drawable mAddMoreImageResource;
    private ImageView mAddMoreImageView;
    private float mAddMoreImageViewMarginLeft;
    private float mAddMoreImageViewMarginRight;
    private float mAddMoreImageViewMarginTop;
    private float mAddMoreImageViewMarginBottom;
    protected float mChipViewHeight;
    protected float mChipViewMargin;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        mAstroDragListener = new AstroDragListener();
        mConfig = new ConfigDefinition();
        readStyleParameters(context, attributeSet);
    }

    private void readStyleParameters(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout);
        try {
            mConfig.setOrientation(a.getInteger(R.styleable.FlowLayout_android_orientation,
                    CommonLogic.HORIZONTAL));
            mConfig.setMaxLines(a.getInteger(R.styleable.FlowLayout_maxLines, 0));
            mConfig.setDebugDraw(a.getBoolean(R.styleable.FlowLayout_debugDraw, false));
            mConfig.setWeightDefault(a.getFloat(R.styleable.FlowLayout_weightDefault, 0.0f));
            mConfig.setGravity(a.getInteger(R.styleable.FlowLayout_android_gravity,
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
                    getResources().getColor(R.color.astroBlack));
            mChipDetailedDeleteIconColor = a.getColor(R.styleable.
                            FlowLayout_chip_detailed_deleteIconColor,
                    getResources().getColor(android.R.color.transparent));
            mChipBackgroundColor = a.getColor(R.styleable.FlowLayout_chip_backgroundColor,
                    getResources().getColor(R.color.astroBlack100));
            mCountViewTextColor = a.getColor(R.styleable.FlowLayout_count_view_text_color,
                    getResources().getColor(R.color.astroBlack700));
            mChipBorderColor = a.getColor(R.styleable.FlowLayout_count_view_border_color,
                    getResources().getColor(R.color.chips_background));
            mTextColor = a.getColor(R.styleable.FlowLayout_autocompletview_text_color,
                    getResources().getColor(R.color.astroBlack));

            // Size related initialization
            mTextSize = a.getDimensionPixelSize(R.styleable.FlowLayout_autocompletview_text_size,
                    ViewUtil.dpToPx(14));

            // show chip detailed
            mShowChipDetailed = a.getBoolean(R.styleable.FlowLayout_showChipDetailed, true);
            mCountViewTextSize = a.getDimension(R.styleable.FlowLayout_count_view_size,
                    getResources().getDimension(R.dimen.count_view_label_text_size));
            mCollapsible = a.getBoolean(R.styleable.FlowLayout_collapsible, false);
            int iconId = a.getResourceId(R.styleable.FlowLayout_add_more_image, -1);
            if (iconId != -1) {
                mAddMoreImageResource = ContextCompat.getDrawable(context, iconId);
            }
            mAddMoreImageViewMarginRight = a.getDimension(
                    R.styleable.FlowLayout_add_more_image_marginRight, 0f);
            mAddMoreImageViewMarginLeft = a.getDimension(
                    R.styleable.FlowLayout_add_more_image_marginLeft, 0f);
            mAddMoreImageViewMarginTop = a.getDimension(
                    R.styleable.FlowLayout_add_more_image_marginTop, 0f);
            mAddMoreImageViewMarginBottom = a.getDimension(
                    R.styleable.FlowLayout_add_more_image_marginBottom, 0f);

            mChipViewHeight = a.getDimension(R.styleable.FlowLayout_chipview_height, 0f);
            mChipViewMargin = a.getDimension(R.styleable.FlowLayout_chipview_margin, 0f);

        } finally {
            a.recycle();
        }

        // first initialize the AutoCompleteView
        initAutoCompleteView();
        mAddMoreImageView = new ImageView(context);
        mAddMoreImageView.setImageDrawable(mAddMoreImageResource);
        mAddMoreImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addAutoCompleteView();
                removeAddMoreImageView();
            }
        });
        addAutoCompleteView();
        setGravity(Gravity.CENTER_VERTICAL);
    }

    protected void addAddMoreImageButton() {
        if (mAddMoreImageView.getParent() != null) {
            removeView(mAddMoreImageView);
        }
        List<ChipInterface> objects = getObjects();
        if (objects.size() == 0) {
            return;
        }
        LayoutParams params = (LayoutParams) mAddMoreImageView.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        params.leftMargin = (int) mAddMoreImageViewMarginLeft;
        params.rightMargin = (int) mAddMoreImageViewMarginRight;
        params.topMargin = (int) mAddMoreImageViewMarginTop;
        params.bottomMargin = (int) mAddMoreImageViewMarginBottom;
        mAddMoreImageView.setLayoutParams(params);
        addView(mAddMoreImageView);
        // AddMoreImageView should also listen for Dragging events
        mAddMoreImageView.setOnDragListener(new AstroDragListener());
        mAutoCompleteTextView.setVisibility(GONE);
    }

    protected void removeAddMoreImageView() {
        removeView(mAddMoreImageView);
        mAutoCompleteTextView.setVisibility(VISIBLE);
        mAutoCompleteTextView.requestFocus();
    }

    public void addAutoCompleteView() {
        // If AutoCompleteTextView is already added then don't add it
        if (mAutoCompleteTextView != null && mAutoCompleteTextView.getParent() != null) {
            removeView(mAutoCompleteTextView);
        }
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
     * Astro needs to be able to disable this view.  The assumption is that, when disabled
     * it will not be enabled again.
     * @param enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mAutoCompleteTextView.setEnabled(enabled);
        if (!enabled) {
            // To be safe, remove various listeners
            mAutoCompleteTextView.setOnFocusChangeListener(null);
            mAutoCompleteTextView.removeTextChangedListener(mTextWatcher);
        }
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
     * Sets Text color for AutoCompleteView
     * @param textColor
     */
    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mAutoCompleteTextView.setTextColor(textColor);
    }

    /**
     * Sets Text size for AutoCompleteView
     * @param textSize
     */
    public void setTextSize(int textSize) {
        mTextSize = textSize;
        mAutoCompleteTextView.setTextSize(textSize);
    }

    /**
     * Returns mAutoCompleteTextView
     * @return
     */
    public MultiAutoCompleteTextView getAutoCompleteTextView() {
        return mAutoCompleteTextView;
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
        mAutoCompleteTextView.setTextColor(mTextColor);
        mAutoCompleteTextView.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
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
                // Ask for a view from children class
                View chipView = getObjectView(mAutoCompleteTextView.getAdapter().getItem(position),
                        true);
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
                    // If typed/entered text is not valid email address, return
                    if (!Utils.isValidEmailAddress(text.trim())) {
                        return true;
                    }
                    mAutoCompleteTextView.setText("");
                    View chipView = getObjectView(text.trim(), false);
                    int chipPosition = getChildCount() - 1;
                    addChipAt(chipView, chipPosition);
                    return true;
                }
                return false;
            }
        });

        // If user presses delete button, on keyboard, we will remove the chip at the last position
        // and set its value to the autocomplete view
        // This allows pressing of us to edit the address
        mAutoCompleteTextView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {

                    // Avoid deleting first space so that cursor will always be shown
                    if (getChildCount() == 1) {
                        if (mAutoCompleteTextView.length() == 0) {
                            mAutoCompleteTextView.setText(" ");
                            return true;
                        }
                        return false;
                    }
                    // Remove last chip.
                    if (getChildCount() > 1 && mAutoCompleteTextView.getText().toString().length() <= 1) {
                        removeChipAt(getChildCount() - 2);
                        // And show it in an edit text
                        mAutoCompleteTextView.setText(" ");
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
                if (mIsClicked) {
                    if (!(s.length() >= 2 && s.charAt(s.length() - 1) == ' ')) {
                        return;
                    }
                } else if (!(s.length() >= 2 && s.charAt(s.length() - 1) == ' ' &&
                            s.charAt(s.length() - 2) == ' ')) {
                        return;
                }
                if (Utils.isValidEmailAddress(s.toString().trim())) {
                    mAutoCompleteTextView.setText(" ");
                    View chipView = getObjectView(s.toString().trim(), false);
                    int chipPosition = getChildCount() - 1;
                    addChipAt(chipView, chipPosition);
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
                    addAddMoreImageButton();
                    // lost the focus. check if there is a text
                    String text = mAutoCompleteTextView.getText().toString().trim();
                    if (!TextUtils.isEmpty(text)) {
                        if (Utils.isValidEmailAddress(text)) {
                            View chipView = getObjectView(text, false);
                            int chipPosition = getChildCount() - 1;
                            addChipAt(chipView, chipPosition);
                        }
                    }
                } else {
                    removeAddMoreImageView();
                    // if focus is gained then show the cursor forcefully
                    mAutoCompleteTextView.setCursorVisible(true);
                    mAutoCompleteTextView.setActivated(true);
                    mAutoCompleteTextView.setPressed(true);
                }
                if (mFocusChangeListener != null) {
                    mFocusChangeListener.onFocusChange(v, hasFocus);
                }
            }
        });

        // AutoCompleteView should also listen for Dragging events
        mAutoCompleteTextView.setOnDragListener(new AstroDragListener());


        OnLongClickListener longClickListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new CopyPasteOptions(FlowLayout.this, mAutoCompleteTextView).show();
                return true;
            }
        };

        mAutoCompleteTextView.setLongClickable(false);
        mAutoCompleteTextView.setTextIsSelectable(false);
        mAutoCompleteTextView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        mAutoCompleteTextView.setOnLongClickListener(longClickListener);
        mAutoCompleteTextView.setClickable(false);
        setOnLongClickListener(longClickListener);
    }

    public void setClicked(boolean value) {
        mIsClicked = value;
    }

    public abstract void collapse();

    public abstract void expand();

    public abstract View getObjectView(Object item, boolean isAutoCompleted);

    public abstract void removeChipAt(int position);

    public abstract void addChipAt(View view, int position);

    public abstract List<ChipInterface> getObjects();

    public abstract ChipInterface getChipAt(int position);

    public void setOnFocusChangeListener(OnFocusChangeListener focusChangeListener) {
        mFocusChangeListener = focusChangeListener;
    }

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

            ViewDefinition view = new ViewDefinition(mConfig, child);
            view.setWidth(child.getMeasuredWidth());
            view.setHeight(child.getMeasuredHeight());
            view.setNewLine(lp.isNewLine());
            view.setGravity(lp.getGravity());
            view.setWeight(lp.getWeight());
            view.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            mViews.add(view);
        }

        mMaxWidth = MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft();
        mConfig.setMaxWidth(mMaxWidth);
        mConfig.setMaxHeight(MeasureSpec.getSize(heightMeasureSpec) - this.getPaddingTop() - this.getPaddingBottom());
        mConfig.setWidthMode(MeasureSpec.getMode(widthMeasureSpec));
        mConfig.setHeightMode(MeasureSpec.getMode(heightMeasureSpec));
        mConfig.setCheckCanFit(mConfig.getLengthMode() != View.MeasureSpec.UNSPECIFIED);

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
        int realControlLength = CommonLogic.findSize(mConfig.getLengthMode(), mConfig.getMaxLength(), contentLength);
        int realControlThickness = CommonLogic.findSize(mConfig.getThicknessMode(), mConfig.getMaxThickness(), contentThickness);

        CommonLogic.applyGravityToLines(mLines, realControlLength, realControlThickness, mConfig);

        for (int i = 0; i < linesCount; i++) {
            LineDefinition line = mLines.get(i);
            applyPositionsToViews(line);
        }

        /* need to take padding into account */
        int totalControlWidth = this.getPaddingLeft() + this.getPaddingRight();
        int totalControlHeight = this.getPaddingBottom() + this.getPaddingTop();
        if (mConfig.getOrientation() == CommonLogic.HORIZONTAL) {
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
            if (mConfig.getOrientation() == CommonLogic.HORIZONTAL) {
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
        return mConfig.getOrientation();
    }

    public void setOrientation(int orientation) {
        mConfig.setOrientation(orientation);
        this.requestLayout();
    }

    public boolean isDebugDraw() {
        return mConfig.isDebugDraw() || debugDraw();
    }

    public void setDebugDraw(boolean debugDraw) {
        mConfig.setDebugDraw(debugDraw);
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
        return mConfig.getWeightDefault();
    }

    public void setWeightDefault(float weightDefault) {
        mConfig.setWeightDefault(weightDefault);
        this.requestLayout();
    }

    public int getGravity() {
        return mConfig.getGravity();
    }

    public void setGravity(int gravity) {
        mConfig.setGravity(gravity);
        this.requestLayout();
    }

    @Override
    public int getLayoutDirection() {
        if (mConfig == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return View.LAYOUT_DIRECTION_LTR;
            } else {
                return super.getLayoutDirection();
            }
        }

        //noinspection ResourceType
        return mConfig.getLayoutDirection();
    }

    @Override
    public void setLayoutDirection(int layoutDirection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            super.setLayoutDirection(layoutDirection);
        }

        //noinspection ResourceType
        if (mConfig.getLayoutDirection() != layoutDirection) {
            mConfig.setLayoutDirection(layoutDirection);
            requestLayout();
        }
    }

    public int getMaxLines() {
        return mConfig.getMaxLines();
    }

    public void setMaxLines(int maxLines) {
        mConfig.setMaxLines(maxLines);
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
