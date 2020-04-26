package com.r12.material_button

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.getLayoutDirection
import androidx.core.widget.TextViewCompat
import com.google.android.material.button.MaterialButton
import kotlin.math.min

@Suppress("MemberVisibilityCanBePrivate")
class MaterialCompatButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    private var isInitMode: Boolean = true
    private val iconRect: Rect = Rect()
    var startDrawable: Drawable? = null
        get() = if (isIconStart()) icon else field
        set(value) {
            if (isIconStart()) {
                icon = value
            } else {
                field = wrapDrawable(value)
                updateDrawables()
            }
        }
    var topDrawable: Drawable? = null
        set(value) {
            field = wrapDrawable(value)
            updateDrawables()
        }
    var endDrawable: Drawable? = null
        get() = if (!isIconStart()) icon else field
        set(value) {
            if (!isIconStart()) {
                icon = value
            } else {
                field = wrapDrawable(value)
                updateDrawables()
            }
        }
    var bottomDrawable: Drawable? = null
        set(value) {
            field = wrapDrawable(value)
            updateDrawables()
        }

    init {
        attrs?.let { attrs ->
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialCompatButton)
            typedArray.getDrawable(R.styleable.MaterialCompatButton_android_drawableStart)?.let {
                startDrawable = it
            }
            topDrawable = typedArray.getDrawable(R.styleable.MaterialCompatButton_android_drawableTop)
            typedArray.getDrawable(R.styleable.MaterialCompatButton_android_drawableEnd)?.let {
                endDrawable = it
            }
            bottomDrawable = typedArray.getDrawable(R.styleable.MaterialCompatButton_android_drawableBottom)
            typedArray.recycle()
        }
        isInitMode = false
        updateDrawables()
    }

    private fun wrapDrawable(drawable: Drawable?): Drawable? {
        val wrappedDrawable: Drawable = drawable?.let { DrawableCompat.wrap(it).mutate() } ?: return null
        DrawableCompat.setTintList(wrappedDrawable, iconTint)
        iconTintMode?.let {
            DrawableCompat.setTintMode(wrappedDrawable, iconTintMode)
        }
        return wrappedDrawable
    }

    fun setDrawableStart(@DrawableRes resId: Int) {
        startDrawable = ContextCompat.getDrawable(context, resId)
    }

    fun setDrawableTop(@DrawableRes resId: Int) {
        topDrawable = ContextCompat.getDrawable(context, resId)
    }

    fun setDrawableEnd(@DrawableRes resId: Int) {
        endDrawable = ContextCompat.getDrawable(context, resId)
    }

    fun setDrawableBottom(@DrawableRes resId: Int) {
        bottomDrawable = ContextCompat.getDrawable(context, resId)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        updateDrawables()
    }

    override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
        super.onTextChanged(charSequence, i, i1, i2)
        updateDrawables()
    }

    private fun updateDrawables() {
        if (isInitMode || layout == null) return

        icon?.let { iconRect.set(it.bounds) }
        if (isIconStart()) {
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, icon, topDrawable, endDrawable, bottomDrawable)
            if (iconGravity == ICON_GRAVITY_TEXT_START) {
                endDrawable?.let { adjustIconX(it) }
            }
        } else {
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, startDrawable, topDrawable, icon, bottomDrawable)
            if (iconGravity == ICON_GRAVITY_TEXT_END) {
                startDrawable?.let { adjustIconX(it) }
            }
        }
        icon?.bounds = iconRect
    }

    private fun isIconStart(): Boolean = iconGravity == ICON_GRAVITY_START || iconGravity == ICON_GRAVITY_TEXT_START

    private fun adjustIconX(oppositeDrawable: Drawable) {
        val textPaint: Paint = paint
        var buttonText = text.toString()
        transformationMethod?.let {
            buttonText = it.getTransformation(buttonText, this).toString()
        }

        val textWidth = min(textPaint.measureText(buttonText).toInt(), layout.ellipsizedWidth)
        val iconSize = if (iconSize == 0) icon?.intrinsicWidth ?: 0 else iconSize

        var startX = (measuredWidth -
                textWidth -
                ViewCompat.getPaddingStart(this) -
                ViewCompat.getPaddingEnd(this) -
                (iconPadding * 2) -
                oppositeDrawable.intrinsicWidth -
                iconSize) / 2

        // Only flip the bound value if either isLayoutRTL() or iconGravity is textEnd, but not both
        if (isLayoutRTL() != (iconGravity == ICON_GRAVITY_TEXT_END)) startX = -startX

        iconRect.left = startX
        iconRect.right = startX + iconSize
    }

    private fun isLayoutRTL(): Boolean = getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL

}