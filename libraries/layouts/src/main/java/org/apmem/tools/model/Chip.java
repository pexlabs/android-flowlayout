package org.apmem.tools.model;


/**
 * Chip.java
 * <p>
 * This file has been pulled from another library {MaterialChipsLayout} as it is.
 */


import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

public class Chip implements ChipInterface, Serializable, Parcelable {

    private Object id;
    private Uri avatarUri;
    private Drawable avatarDrawable;
    private String label;
    private String info;
    private String displayName;

    public Chip(@NonNull Object id, @Nullable Uri avatarUri, @NonNull String label, @Nullable String info) {
        this.id = id;
        this.avatarUri = avatarUri;
        this.label = label;
        this.info = info;
    }

    public Chip(@NonNull Object id, @Nullable Drawable avatarDrawable, @NonNull String label, @Nullable String info) {
        this.id = id;
        this.avatarDrawable = avatarDrawable;
        this.label = label;
        this.info = info;
    }

    public Chip(@Nullable Uri avatarUri, @NonNull String label, @Nullable String info) {
        this.avatarUri = avatarUri;
        this.label = label;
        this.info = info;
    }

    public Chip(@Nullable Drawable avatarDrawable, @NonNull String label, @Nullable String info) {
        this.avatarDrawable = avatarDrawable;
        this.label = label;
        this.info = info;
    }

    public Chip(@NonNull Object id, @NonNull String label, @Nullable String info) {
        this.id = id;
        this.label = label;
        this.info = info;
    }

    public Chip(@NonNull Object id, @NonNull String label, @Nullable String info, @NonNull String displayName) {
        this.id = id;
        this.label = label;
        this.info = info;
        this.displayName = displayName;
    }

    public Chip(@NonNull String label, @Nullable String info) {
        this.label = label;
        this.info = info;
    }

    protected Chip(Parcel in) {
        avatarUri = in.readParcelable(Uri.class.getClassLoader());
        label = in.readString();
        info = in.readString();
        displayName = in.readString();
    }

    public static final Creator<Chip> CREATOR = new Creator<Chip>() {
        @Override
        public Chip createFromParcel(Parcel in) {
            return new Chip(in);
        }

        @Override
        public Chip[] newArray(int size) {
            return new Chip[size];
        }
    };

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Uri getAvatarUri() {
        return avatarUri;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return avatarDrawable;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(avatarUri, flags);
        dest.writeString(label);
        dest.writeString(info);
        dest.writeString(displayName);
    }
}