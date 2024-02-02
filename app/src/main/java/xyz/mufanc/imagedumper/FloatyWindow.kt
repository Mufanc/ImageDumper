package xyz.mufanc.imagedumper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.children
import xyz.mufanc.imagedumper.ModuleMain.Companion.TAG

@SuppressLint("ViewConstructor")
class FloatyWindow(activity: Activity) : FrameLayout(activity), View.OnClickListener, View.OnTouchListener {

    private val windowManager = activity.getSystemService(WindowManager::class.java)

    private val context = MixedContext(activity)

    private val windowParams = defaultLayoutParams(activity)

    private val controller = MoveController(this)

    private val activityWindow = activity.window

    override fun onTouch(view: View, ev: MotionEvent): Boolean {
        controller.feed(ev)
        return false
    }

    override fun onClick(view: View?) {
        val images = ViewVisitor(activityWindow.decorView as ViewGroup)
            .findImageView()
            .mapNotNull { Utils.dumpBitmap(it) }
            .toList()

        ImageSelectorDialog(context, images).show()
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.floaty_window, this)

        setOnClickListener(this)
        setOnTouchListener(this)

        windowManager.addView(this, windowParams)

        Log.i(TAG, "floaty window created.")
    }

    companion object {
        private fun defaultLayoutParams(context: Context): WindowManager.LayoutParams {
            val displayMetrics = context.resources.displayMetrics

            val dpMargin = 20
            val pxMargin = (dpMargin * displayMetrics.density).toInt()

            return WindowManager.LayoutParams().apply {
                x = pxMargin
                y = pxMargin
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                format = PixelFormat.RGBA_8888
                gravity = Gravity.END or Gravity.BOTTOM
            }
        }
    }

    private class ViewVisitor(
        private val root: ViewGroup
    ) {
        companion object {
            private val nodump: String by lazy {
                MixedContext.modulePackageContext.getString(R.string.tag_nodump)
            }
        }

        fun findImageView(): Sequence<ImageView> {
            var images: Sequence<ImageView> = emptySequence()

            root.children.forEach { view ->
                if (view is ImageView && view.tag != nodump) {
                    images += sequenceOf(view)
                } else if (view is ViewGroup) {
                    images += ViewVisitor(view).findImageView()
                }
            }

            return images
        }
    }

    private class MoveController(
        private val floaty: FloatyWindow
    ) {

        private var x = 0f
        private var y = 0f

        fun feed(ev: MotionEvent) {
            if (ev.action == MotionEvent.ACTION_MOVE) {
                val dx = ev.rawX - x
                val dy = ev.rawY - y

                floaty.windowParams.apply {
                    // Gravity.END or Gravity.BOTTOM
                    x -= dx.toInt()
                    y -= dy.toInt()
                }

                floaty.windowManager.updateViewLayout(floaty, floaty.windowParams)
            }

            x = ev.rawX
            y = ev.rawY
        }
    }
}
