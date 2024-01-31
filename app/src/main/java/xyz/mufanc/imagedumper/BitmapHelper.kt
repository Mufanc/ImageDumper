package xyz.mufanc.imagedumper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import java.io.ByteArrayOutputStream

object BitmapHelper {

    fun dumpBitmap(view: View?): Bitmap? {
        if (view == null) return null
        if (!view.hasWindowFocus()) return null
        if (view !is ImageView) return null

        val drawable = view.drawable
        val bitmap = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            view.draw(canvas)

            bitmap
        }

        return bitmap
    }

    fun encode(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }
}
