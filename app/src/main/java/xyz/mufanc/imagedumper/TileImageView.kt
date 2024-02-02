package xyz.mufanc.imagedumper

import android.content.Context
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView

class TileImageView(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    init {
        background = TileDrawable(
            AppCompatResources.getDrawable(context, R.drawable.bg_transparent)!!,
            Shader.TileMode.REPEAT
        )
    }
}
