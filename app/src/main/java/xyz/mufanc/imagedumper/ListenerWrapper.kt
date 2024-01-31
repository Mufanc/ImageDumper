package xyz.mufanc.imagedumper

import android.util.Log
import android.view.View
import xyz.mufanc.imagedumper.ModuleMain.Companion.TAG

class ListenerWrapper(
    private val inner: View.OnLongClickListener? = null
): View.OnLongClickListener {

    override fun onLongClick(view: View?): Boolean {
        val bitmap = BitmapHelper.dumpBitmap(view)

        if (view != null && bitmap != null) {
            try {
                PreviewDialog(view, bitmap).show()
            } catch (err: Throwable) {
                Log.e(TAG, "", err)
            }

            return true
        } else {

            Log.w(TAG, "long click detected, but failed to dump bitmap.")
        }

        return inner?.onLongClick(view) ?: false
    }
}
