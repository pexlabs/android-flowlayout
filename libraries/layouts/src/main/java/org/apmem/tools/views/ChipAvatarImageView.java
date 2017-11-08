package org.apmem.tools.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.apmem.tools.layouts.R;

/**
 * ImageView used to display a picture either in a circle or in a rectangle with round edges
 */
@SuppressLint("AppCompatCustomView")
public class ChipAvatarImageView extends ImageView {
    private Path clipPath;
    private RectF clipPathSrcRect;
    private RectF clipPathDestRect;
    private Matrix pathScaleMatrix;
    private Matrix imageRotateMatrix;
    private RectF drawableRect;
    private RectF viewRect;
    private boolean hasScaled;
    private float cornerRadius = 4f;

    private Mode mode;

    public enum Mode {
        CIRCLE,
        ROUND_RECT
    }

    public ChipAvatarImageView(Context context) {
        super(context);
        setMode(Mode.CIRCLE);
        initialize();
    }

    public ChipAvatarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setModeFromAttributes(context, attrs);
        initialize();
    }

    public ChipAvatarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setModeFromAttributes(context, attrs);
        initialize();
    }

    @Override
    public void setImageResource(int resId) {
        initialize();
        super.setImageResource(resId);
    }

    private void initialize() {
        // Clipping path definition. This path will be laid out as a 32x32 path and then scaled to
        // the size of our canvas once in onDraw
        setClipPath();

        clipPathSrcRect = new RectF(0f, 0f, 32f, 32f);
        clipPathDestRect = new RectF();
        pathScaleMatrix = new Matrix();

        // This is used to rotate the source image by -15 degrees and scale it to fit. The work will
        // be done in onDraw
        setScaleType(ScaleType.MATRIX);
        imageRotateMatrix = new Matrix();
        drawableRect = new RectF();
        viewRect = new RectF();

        hasScaled = false;
    }

    private void setClipPath() {
        // Clipping path definition. This path will be laid out as a 32x32 path and then scaled to
        // the size of our canvas once in onDraw
        if (mode == Mode.CIRCLE) {
            clipPath = new Path();
            clipPath.addCircle(16f, 16f, 16f, Path.Direction.CW);
            clipPath.close();
        } else if (mode == Mode.ROUND_RECT) {
            clipPath = new Path();
            clipPath.addRoundRect(0, 0, 32f, 32f, cornerRadius, cornerRadius, Path.Direction.CW);
            clipPath.close();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!hasScaled) {
            // Scale our clipping path
            clipPathDestRect.set(0f, 0f, canvas.getWidth(), canvas.getHeight());
            pathScaleMatrix.setRectToRect(clipPathSrcRect, clipPathDestRect, Matrix.ScaleToFit.CENTER);
            clipPath.transform(pathScaleMatrix);

            // Scale our image matrix
            if (getDrawable() != null) {
                drawableRect.set(0, 0,
                        getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
                viewRect.set(0, 0, getWidth(), getHeight());
                imageRotateMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
            }
            setImageMatrix(imageRotateMatrix);

            hasScaled = true;
        }
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }

    private void setModeFromAttributes(Context context, AttributeSet attrs) {
        Mode mode;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.ChipAvatarImageView, 0, 0);
        try {
            mode = Mode.values()[
                    attributes.getInt(R.styleable.ChipAvatarImageView_image_mode, Mode.CIRCLE.ordinal())];
            cornerRadius = attributes.getDimension(R.styleable.ChipAvatarImageView_image_corner_radius, 4f);
        } finally {
            attributes.recycle();
        }
        setMode(mode);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        hasScaled = false;
        setClipPath();
        invalidate();
    }
}