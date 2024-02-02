package xyz.mufanc.imagedumper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import java.io.ByteArrayOutputStream

object Utils {

    fun dumpBitmap(view: View?): Bitmap? {
        if (view == null) return null
        if (view !is ImageView) return null
        if (view.width == 0 || view.height == 0) return null

        val drawable = view.drawable
        val bitmap = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            if (view.visibility != View.VISIBLE) return null

            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            view.draw(canvas)

            bitmap
        }

        return bitmap
    }

    fun encodeImage(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    fun performClick(view: View?) {
        var current = view
        while (current != null && !current.performClick()) {
            current = current.parent as? View
        }
    }

    fun performLongClick(view: View?) {
        var current = view
        while (current != null && !current.performLongClick()) {
            current = current.parent as? View
        }
    }
}
