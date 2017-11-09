/**
 * DetailedChipView.java
 *
 * This file has been pulled from MaterialChipsLayout library
 */

package org.apmem.tools.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apmem.tools.layouts.R;
import org.apmem.tools.model.ChipInterface;
import org.apmem.tools.util.ColorUtil;
import org.apmem.tools.util.LetterTileProvider;


public class DetailedChipView extends RelativeLayout {

    private static final String TAG = DetailedChipView.class.getSimpleName();
    // context
    private Context mContext;
    // xml elements
    private LinearLayout mContentLayout;
    private ChipAvatarImageView mAvatarIconImageView;
    private TextView mNameTextView;
    private TextView mInfoTextView;
    private ImageView mDeleteButton;
    // letter tile provider
    private static LetterTileProvider mLetterTileProvider;
    // attributes
    private int mBackgroundColor;

    public DetailedChipView(Context context) {
        super(context);
        mContext = context;
        init(null);
    }

    public DetailedChipView(Context context, AttributeSet attrs) {
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
        // inflate layout
        View rootView = inflate(getContext(), R.layout.detailed_chip_view, this);

        mContentLayout = (LinearLayout) rootView.findViewById(R.id.content);
        mAvatarIconImageView = (ChipAvatarImageView) rootView.findViewById(R.id.avatar_icon);
        mNameTextView = (TextView) rootView.findViewById(R.id.name);
        mNameTextView.setSelected(true);
        mInfoTextView = (TextView) rootView.findViewById(R.id.info);
        mInfoTextView.setSelected(true);
        mDeleteButton = (ImageView) rootView.findViewById(R.id.delete_button);

        // letter tile provider
        mLetterTileProvider = new LetterTileProvider(mContext);

        // hide on first
        setVisibility(GONE);
        // hide on touch outside
        hideOnTouchOutside();
    }

    /**
     * Hide the view on touch outside of it
     */
    private void hideOnTouchOutside() {
        // set focusable
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
    }

    /**
     * Fade in
     */
    public void fadeIn() {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        startAnimation(anim);
        setVisibility(VISIBLE);
        // focus on the view
        requestFocus();
    }

    /**
     * Fade out
     */
    public void fadeOut() {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(200);
        startAnimation(anim);
        setVisibility(GONE);
        // fix onclick issue
        clearFocus();
        setClickable(false);
    }

    public void setAvatarIcon(Drawable icon) {
        mAvatarIconImageView.setImageDrawable(icon);
    }

    public void setAvatarIcon(Bitmap icon) {
        mAvatarIconImageView.setImageBitmap(icon);
    }

    public void setAvatarIcon(Uri icon) {
        mAvatarIconImageView.setImageURI(icon);
    }

    public void setName(String name) {
        mNameTextView.setText(name);
    }

    public void setInfo(String info) {
        if (info != null) {
            mInfoTextView.setVisibility(VISIBLE);
            mInfoTextView.setText(info);
        } else {
            mInfoTextView.setVisibility(GONE);
        }
    }

    public void setTextColor(int color) {
        mNameTextView.setTextColor(color);
        mInfoTextView.setTextColor(ColorUtil.alpha(color, 150));
    }

    public void setBackGroundcolor(int color) {
        mBackgroundColor = color;
        mContentLayout.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setDeleteIconColor(int color) {
        mDeleteButton.getDrawable().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public void setOnDeleteClicked(OnClickListener onClickListener) {
        mDeleteButton.setOnClickListener(onClickListener);
    }

    public void alignLeft() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mContentLayout.getLayoutParams();
        params.leftMargin = 0;
        mContentLayout.setLayoutParams(params);
    }

    public void alignRight() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mContentLayout.getLayoutParams();
        params.rightMargin = 0;
        mContentLayout.setLayoutParams(params);
    }

    public static class Builder {
        private Context context;
        private Uri avatarUri;
        private Drawable avatarDrawable;
        private String name;
        private String info;
        private int textColor;
        private int backgroundColor;
        private int deleteIconColor;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder avatar(Uri avatarUri) {
            this.avatarUri = avatarUri;
            return this;
        }

        public Builder avatar(Drawable avatarDrawable) {
            this.avatarDrawable = avatarDrawable;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder info(String info) {
            this.info = info;
            return this;
        }

        public Builder chip(ChipInterface chip) {
            this.avatarUri = chip.getAvatarUri();
            this.avatarDrawable = chip.getAvatarDrawable();
            this.name = chip.getLabel();
            this.info = chip.getInfo();
            return this;
        }

        public Builder textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder backgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder deleteIconColor(int deleteIconColor) {
            this.deleteIconColor = deleteIconColor;
            return this;
        }

        public DetailedChipView build() {
            return DetailedChipView.newInstance(this);
        }
    }

    private static DetailedChipView newInstance(Builder builder) {
        DetailedChipView detailedChipView = new DetailedChipView(builder.context);
        // avatar
        if (builder.avatarUri != null)
            detailedChipView.setAvatarIcon(builder.avatarUri);
        else if (builder.avatarDrawable != null)
            detailedChipView.setAvatarIcon(builder.avatarDrawable);
        else
            detailedChipView.setAvatarIcon(mLetterTileProvider.getLetterTile(builder.name));

        detailedChipView.setBackGroundcolor(builder.backgroundColor);

        detailedChipView.setTextColor(builder.textColor);

        detailedChipView.setDeleteIconColor(builder.deleteIconColor);

        detailedChipView.setName(builder.name);
        detailedChipView.setInfo(builder.info);
        return detailedChipView;
    }
}