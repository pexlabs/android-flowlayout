/**
 * ChipView.java
 *
 * This file has been pulled from MaterialChipsLayout library
 */

package org.apmem.tools.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apmem.tools.layouts.R;
import org.apmem.tools.model.ChipInterface;
import org.apmem.tools.util.LetterTileProvider;
import org.apmem.tools.util.ViewUtil;


public class ChipView extends RelativeLayout {

    private static final String TAG = ChipView.class.toString();
    // context
    private Context mContext;
    // xml elements
    private LinearLayout mContentLayout;
    private ChipAvatarImageView mAvatarIconImageView;
    private TextView mLabelTextView;
    private ImageView mDeleteButton;
    // attributes
    private static final int NONE = -1;
    private String mLabel;
    private ColorStateList mLabelColor;
    private boolean mHasAvatarIcon = false;
    private Drawable mAvatarIconDrawable;
    private Uri mAvatarIconUri;
    private boolean mDeletable = false;
    private Drawable mDeleteIcon;
    private ColorStateList mDeleteIconColor;
    private ColorStateList mBackgroundColor;
    private ColorStateList mBorderColor;
    private int mBorderSize;
    // Padding between the label if the avatar if needed, default is 0
    private int mLabelAvatarPaddingExtra;
    private int mLabelTextSize;
    private int mTextAvatarSpacing;
    // letter tile provider
    private LetterTileProvider mLetterTileProvider;
    // chip
    private ChipInterface mChip;
    // Maximum width of this chip
    private int mMaxWidth;
    // Flag to tell if the chip was populated by typing or by autocomplete
    private boolean mIsAutoCompleted;

    private boolean mAvatarAtEnd = false;
    private int mAvatarWidth;
    private int mAvatarHeight;
    private boolean mClickable;

    private OnLongClickListener mOnLongClickListener;

    public enum TextStyle {
        STRIKE_THROUGH,
        UNDERLINE
    }

    public ChipView(Context context) {
        super(context);
        mContext = context;
        init(null);
    }

    public ChipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    /**
     * Inflate the view according to attributes
     *
     * @param attrs the attributes
     */
    private void init(AttributeSet attrs) {
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

        // letter tile provider
        mLetterTileProvider = new LetterTileProvider(mContext);

        // attributes
        if(attrs != null) {
            TypedArray a = mContext.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ChipView,
                    0, 0);
            try {
                // label
                mLabel = a.getString(R.styleable.ChipView_label);
                mLabelColor = a.getColorStateList(R.styleable.ChipView_labelColor);
                mLabelAvatarPaddingExtra = a.getDimensionPixelSize(
                        R.styleable.ChipView_labelAvatarPaddingExtra, 0);
                mLabelTextSize = a.getDimensionPixelSize(R.styleable.ChipView_labelTextSize,
                        ViewUtil.dpToPx(14));
                // avatar icon
                mHasAvatarIcon = a.getBoolean(R.styleable.ChipView_hasAvatarIcon, false);
                int avatarIconId = a.getResourceId(R.styleable.ChipView_avatarIcon, NONE);
                if (avatarIconId != NONE) {
                    mAvatarIconDrawable = ContextCompat.getDrawable(mContext,avatarIconId);
                }
                if (mAvatarIconDrawable != null) mHasAvatarIcon = true;
                // delete icon
                mDeletable = a.getBoolean(R.styleable.ChipView_deletable, false);
                mDeleteIconColor = a.getColorStateList(R.styleable.ChipView_deleteIconColor);
                int deleteIconId = a.getResourceId(R.styleable.ChipView_deleteIcon, NONE);
                if (deleteIconId != NONE) {
                    mDeleteIcon = ContextCompat.getDrawable(mContext, deleteIconId);
                }
                // background color
                mBackgroundColor = a.getColorStateList(R.styleable.ChipView_backgroundColor);
                // border color

                mBorderColor = a.getColorStateList(R.styleable.ChipView_borderColor);
                // border size
                mBorderSize = a.getDimensionPixelSize(R.styleable.ChipView_borderSize,
                        ViewUtil.dpToPx(2));
                // Space between the text and the avatar
                mTextAvatarSpacing = a.getDimensionPixelSize(R.styleable.ChipView_textAvatarSpacing,
                        getResources().getDimensionPixelSize(R.dimen.text_avatar_spacing_default));
                mAvatarAtEnd = a.getBoolean(R.styleable.ChipView_avatarAtEnd, false);
                mAvatarWidth = a.getDimensionPixelSize(R.styleable.ChipView_avatarWidth, -1);
                mAvatarHeight = a.getDimensionPixelSize(R.styleable.ChipView_avatarHeight, -1);
                mClickable = a.getBoolean(R.styleable.ChipView_clickable, true);
            }
            finally {
                a.recycle();
            }
        }

        // inflate layout
        View rootView;

        if (mAvatarAtEnd) {
            rootView = inflate(getContext(), R.layout.chip_view_avatar_at_end, this);
        } else {
            rootView = inflate(getContext(), R.layout.chip_view, this);
        }

        mContentLayout = (LinearLayout) rootView.findViewById(R.id.content);
        mContentLayout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnLongClickListener != null) {
                    mOnLongClickListener.onLongClick(v);
                }
                return true;
            }
        });

        mAvatarIconImageView = (ChipAvatarImageView) rootView.findViewById(R.id.icon);
        mLabelTextView = (TextView) rootView.findViewById(R.id.label);
        mLabelTextView.setSingleLine();
        mDeleteButton = (ImageView) rootView.findViewById(R.id.delete_button);

        // inflate
        inflateWithAttributes();
    }

    /**
     * Sets style to a label text
     * @param style
     */
    public void setLabelStyle(TextStyle style) {
        switch (style) {
            case UNDERLINE:
                // Remove STRIKE_THRU_TEXT_FLAG
                mLabelTextView.setPaintFlags(mLabelTextView.getPaintFlags()
                        & (~ Paint.STRIKE_THRU_TEXT_FLAG));

                // Add UNDERLINE_TEXT_FLAG
                mLabelTextView.setPaintFlags(mLabelTextView.getPaintFlags()
                        | Paint.UNDERLINE_TEXT_FLAG);
                break;

            case STRIKE_THROUGH:
                // Remove UNDERLINE_TEXT_FLAG
                mLabelTextView.setPaintFlags(mLabelTextView.getPaintFlags()
                        & (~ Paint.UNDERLINE_TEXT_FLAG));

                // Add STRIKE_THRU_TEXT_FLAG
                mLabelTextView.setPaintFlags(mLabelTextView.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG);
                break;
        }
    }

    /**
     * Inflate the view
     */
    private void inflateWithAttributes() {
        // avatar
        setHasAvatarIcon(mHasAvatarIcon);

        if (mHasAvatarIcon && mAvatarAtEnd) {
            // Don't want conflicting images at the end
            mDeletable = false;
        }

        // delete button
        setDeletable(mDeletable);

        // label
        setLabel(mLabel, mLabelAvatarPaddingExtra, mLabelTextSize, mTextAvatarSpacing);
        if (mLabelColor != null) {
            setLabelColor(mLabelColor);
        }

        // background color
        if (mBackgroundColor != null) {
            setChipBackgroundColor(mBackgroundColor);
        }

        // border color
        if (mBorderColor != null) {
            setChipBorderColor(mBorderSize, mBorderColor);
        }

        if (!mClickable) {
            // Only disable or else leave default
            mContentLayout.setEnabled(mClickable);
            mAvatarIconImageView.setEnabled(mClickable);
            mLabelTextView.setEnabled(mClickable);
        }

        boolean refresh = false;
        if (mAvatarHeight >= 0) {
            mAvatarIconImageView.getLayoutParams().height = mAvatarHeight;
            refresh = true;
        }

        if (mAvatarWidth >= 0) {
            mAvatarIconImageView.getLayoutParams().width = mAvatarWidth;
            refresh = true;
        }

        if (refresh) {
            mAvatarIconImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mAvatarIconImageView.setAdjustViewBounds(true);
            requestLayout();
        }
    }

    public void setEnabled(boolean enabled) {
        mContentLayout.setEnabled(enabled);
        mAvatarIconImageView.setEnabled(enabled);
        mLabelTextView.setEnabled(enabled);
    }

    public void inflate(ChipInterface chip) {
        mChip = chip;
        // label
        mLabel = mChip.getLabel();
        // icon
        mAvatarIconUri = mChip.getAvatarUri();
        mAvatarIconDrawable = mChip.getAvatarDrawable();

        // inflate
        inflateWithAttributes();
    }

    /**
     * Get label
     *
     * @return the label
     */
    public String getLabel() {
        return mLabel;
    }

    /**
     * Set label
     *
     * @param label the label to set
     */
    public void setLabel(String label, int labelAvatarPaddingExtra, int labelTextSizePixels,
                         int labelAvatarSpacing) {
        mLabel = label;
        mLabelTextView.setText(label);
        if (labelTextSizePixels > 0) {
            mLabelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSizePixels);
        }

        if (mHasAvatarIcon) {
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) mLabelTextView.getLayoutParams();
            if (!mAvatarAtEnd) {
                layoutParams.leftMargin = labelAvatarSpacing;
                mLabelTextView.setLayoutParams(layoutParams);
                mLabelTextView.setPadding(labelAvatarPaddingExtra, mLabelTextView.getPaddingTop(),
                    mLabelTextView.getPaddingRight(), mLabelTextView.getPaddingBottom());
            } else {
                layoutParams.rightMargin = labelAvatarSpacing;
                mLabelTextView.setLayoutParams(layoutParams);
                mLabelTextView.setPadding(mLabelTextView.getPaddingLeft(), mLabelTextView.getPaddingTop(),
                    labelAvatarPaddingExtra, mLabelTextView.getPaddingBottom());
            }
        }
    }

    /**
     * Don't make this ChipView go beyond the max width
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (mMaxWidth > 0 && mMaxWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }

    /**
     * Set label color
     *
     * @param color the color to set
     */
    public void setLabelColor(ColorStateList color) {
        mLabelColor = color;
        mLabelTextView.setTextColor(color);
    }

    /**
     * Set label color
     *
     * @param color the color to set
     */
    public void setLabelColor(int color) {
        mLabelColor = ColorStateList.valueOf(color);
        mLabelTextView.setTextColor(color);
    }

    /**
     * Tint the existing avatar image
     * @param color
     * @param mode
     */
    public void tintAvatarIcon(int color, PorterDuff.Mode mode) {
        if (mAvatarIconImageView != null) {
            mAvatarIconImageView.setColorFilter(color, mode);
        }
    }

    /**
     * Returns if the current ChipView was created by autocomplete (by selecting dropdown) or not
     * @return
     */
    public boolean isAutoCompleted() {
        return mIsAutoCompleted;
    }

    /**
     * Sets if current ChipView is created by autocomplete (by selecting dropdown) or not
     * @param autoCompleted
     */
    public void setAutoCompleted(boolean autoCompleted) {
        mIsAutoCompleted = autoCompleted;
    }

    /**
     * Show or hide avatar icon.
     * alee - we don't want any padding between the avatar image and the text
     *
     * @param hasAvatarIcon true to show, false to hide
     */
    public void setHasAvatarIcon(boolean hasAvatarIcon) {
        mHasAvatarIcon = hasAvatarIcon;
        if(!mHasAvatarIcon) {
            // hide icon
            mAvatarIconImageView.setVisibility(GONE);
        } else {
            // show icon
            mAvatarIconImageView.setVisibility(VISIBLE);
            // set icon
            if(mAvatarIconUri != null)
                mAvatarIconImageView.setImageURI(mAvatarIconUri);
            else if(mAvatarIconDrawable != null)
                mAvatarIconImageView.setImageDrawable(mAvatarIconDrawable);
            else
                mAvatarIconImageView.setImageBitmap(mLetterTileProvider.getLetterTile(getLabel()));
        }
    }

    /**
     * Set avatar icon
     *
     * @param avatarIcon the icon to set
     */
    public void setAvatarIcon(Drawable avatarIcon) {
        mAvatarIconDrawable = avatarIcon;
        mHasAvatarIcon = true;
        inflateWithAttributes();
    }

    /**
     * Set avatar icon
     *
     * @param avatarUri the uri of the icon to set
     */
    public void setAvatarIcon(Uri avatarUri) {
        mAvatarIconUri = avatarUri;
        mHasAvatarIcon = true;
        inflateWithAttributes();
    }

    /**
     * Show or hide delete button
     * alee - we don't want any padding between the avatar image and the text
     *
     * @param deletable true to show, false to hide
     */
    public void setDeletable(boolean deletable) {
        mDeletable = deletable;
        if(!mDeletable) {
            // hide delete icon
            mDeleteButton.setVisibility(GONE);
        } else {
            // show icon
            mDeleteButton.setVisibility(VISIBLE);
            // set icon
            if(mDeleteIcon != null)
                mDeleteButton.setImageDrawable(mDeleteIcon);
            if(mDeleteIconColor != null)
                mDeleteButton.getDrawable().mutate().setColorFilter(
                        mDeleteIconColor.getDefaultColor(), PorterDuff.Mode.SRC_ATOP);
        }
    }

    /**
     * Set delete icon color
     *
     * @param color the color to set
     */
    public void setDeleteIconColor(ColorStateList color) {
        mDeleteIconColor = color;
        mDeletable = true;
        inflateWithAttributes();
    }

    /**
     * Set delete icon color
     *
     * @param color the color to set
     */
    public void setDeleteIconColor(int color) {
        mDeleteIconColor = ColorStateList.valueOf(color);
        mDeletable = true;
        inflateWithAttributes();
    }

    /**
     * Set delete icon
     *
     * @param deleteIcon the icon to set
     */
    public void setDeleteIcon(Drawable deleteIcon) {
        mDeleteIcon = deleteIcon;
        mDeletable = true;
        inflateWithAttributes();
    }

    /**
     * Set background color
     *
     * @param color the color to set
     */
    public void setChipBackgroundColor(ColorStateList color) {
        mBackgroundColor = color;
        setChipBackgroundColor(color.getDefaultColor());
    }

    /**
     * Set background color
     *
     * @param color the color to set
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setChipBackgroundColor(int color) {
        mBackgroundColor = ColorStateList.valueOf(color);
        RippleDrawable newDrawable = (RippleDrawable) mContentLayout.getBackground().mutate();
        GradientDrawable drawable = (GradientDrawable) newDrawable.findDrawableByLayerId(
                R.id.ripper_inner_item);
        drawable.setColor(color);
    }

    /**
     * Set border color
     *
     * @param sizePixels the border size
     * @param color the color to set
     */
    public void setChipBorderColor(int sizePixels, ColorStateList color) {
        mBorderColor = color;
        setChipBorderColor(sizePixels, color.getDefaultColor());
    }

    /**
     * Set border color and size
     *
     * @param sizePixels the border size
     * @param color the color to set
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setChipBorderColor(int sizePixels, int color) {
        mBorderColor = ColorStateList.valueOf(color);
        RippleDrawable newDrawable = (RippleDrawable) mContentLayout.getBackground().mutate();
        GradientDrawable drawable = (GradientDrawable) newDrawable.findDrawableByLayerId(
                R.id.ripper_inner_item);
        drawable.setStroke(sizePixels, mBorderColor);
    }

    /**
     * Set the chip object
     *
     * @param chip the chip
     */
    public void setChip(ChipInterface chip) {
        mChip = chip;
    }

    /**
     * Set OnClickListener on the delete button
     *
     * @param onClickListener the OnClickListener
     */
    public void setOnDeleteClicked(OnClickListener onClickListener) {
        mDeleteButton.setOnClickListener(onClickListener);
    }

    /**
     * Set OnclickListener on the entire chip
     *
     * @param onClickListener the OnClickListener
     */
    public void setOnChipClicked(OnClickListener onClickListener) {
        mContentLayout.setOnClickListener(onClickListener);
    }

    public void setOnChipLongClicked(OnLongClickListener longClicked) {
        mOnLongClickListener = longClicked;
    }

    /**
     * Builder class
     */
    public static class Builder {
        private Context context;
        private String label;
        private ColorStateList labelColor;
        private boolean hasAvatarIcon = false;
        private Uri avatarIconUri;
        private Drawable avatarIconDrawable;
        private boolean deletable = false;
        private Drawable deleteIcon;
        private ColorStateList deleteIconColor;
        private ColorStateList backgroundColor;
        private ChipInterface chip;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder labelColor(ColorStateList labelColor) {
            this.labelColor = labelColor;
            return this;
        }

        public Builder hasAvatarIcon(boolean hasAvatarIcon) {
            this.hasAvatarIcon = hasAvatarIcon;
            return this;
        }

        public Builder avatarIcon(Uri avatarUri) {
            this.avatarIconUri = avatarUri;
            return this;
        }

        public Builder avatarIcon(Drawable avatarIcon) {
            this.avatarIconDrawable = avatarIcon;
            return this;
        }

        public Builder deletable(boolean deletable) {
            this.deletable = deletable;
            return this;
        }

        public Builder deleteIcon(Drawable deleteIcon) {
            this.deleteIcon = deleteIcon;
            return this;
        }

        public Builder deleteIconColor(ColorStateList deleteIconColor) {
            this.deleteIconColor = deleteIconColor;
            return this;
        }

        public Builder backgroundColor(ColorStateList backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder chip(ChipInterface chip) {
            this.chip = chip;
            this.label = chip.getLabel();
            this.avatarIconDrawable = chip.getAvatarDrawable();
            this.avatarIconUri = chip.getAvatarUri();
            return this;
        }

        public ChipView build() {
            return newInstance(this);
        }
    }

    private static ChipView newInstance(Builder builder) {
        ChipView chipView = new ChipView(builder.context);
        chipView.mLabel = builder.label;
        chipView.mLabelColor = builder.labelColor;
        chipView.mHasAvatarIcon = builder.hasAvatarIcon;
        chipView.mAvatarIconUri = builder.avatarIconUri;
        chipView.mAvatarIconDrawable = builder.avatarIconDrawable;
        chipView.mDeletable = builder.deletable;
        chipView.mDeleteIcon = builder.deleteIcon;
        chipView.mDeleteIconColor = builder.deleteIconColor;
        chipView.mBackgroundColor = builder.backgroundColor;
        chipView.mChip = builder.chip;
        chipView.inflateWithAttributes();

        return chipView;
    }
}
