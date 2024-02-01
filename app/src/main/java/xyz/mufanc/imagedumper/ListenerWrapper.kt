package xyz.mufanc.imagedumper

import android.util.Log
import android.view.View
import xyz.mufanc.imagedumper.ModuleMain.Companion.TAG

class ListenerWrapper(
    private val inner: View.OnLongClickListener? = null
): View.OnLongClickListener {

    override fun onLongClick(view: View?): Boolean {
        val bitmap = Utils.dumpBitmap(view)

        if (bitmap == null) {
            Log.w(TAG, "long click detected, but failed to dump bitmap.")
        }

        if (view != null) {
            try {
                PreviewDialog(view, bitmap).show()
                return true
            } catch (err: Throwable) {
                Log.e(TAG, "", err)
            }
        }

        return inner?.onLongClick(view) ?: false
    }
}
