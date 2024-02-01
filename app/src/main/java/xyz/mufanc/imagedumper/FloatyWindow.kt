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
import xyz.mufanc.imagedumper.ModuleMain.Companion.TAG

@SuppressLint("ViewConstructor")
class FloatyWindow(activity: Activity) : FrameLayout(activity), View.OnClickListener, View.OnTouchListener {

    private val mWindowManager = activity.getSystemService(WindowManager::class.java)

    private val mContext = ModuleContext(activity)

    private val mWindowParams = defaultLayoutParams(activity)

    private val mController = MoveController(this)

    override fun onTouch(view: View, ev: MotionEvent): Boolean {
        mController.feed(ev)
        return false
    }

    override fun onClick(view: View?) {
        Log.i(TAG, "Todo: dump images")
    }

    init {
        LayoutInflater.from(mContext).inflate(R.layout.floaty_window, this)

        setOnClickListener(this)
        setOnTouchListener(this)

        mWindowManager.addView(this, mWindowParams)
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

    class MoveController(
        private val floaty: FloatyWindow
    ) {

        private var x = 0f
        private var y = 0f

        fun feed(ev: MotionEvent) {
            if (ev.action == MotionEvent.ACTION_MOVE) {
                val dx = ev.rawX - x
                val dy = ev.rawY - y

                floaty.mWindowParams.apply {
                    // Gravity.END or Gravity.BOTTOM
                    x -= dx.toInt()
                    y -= dy.toInt()
                }

                floaty.mWindowManager.updateViewLayout(floaty, floaty.mWindowParams)
            }

            x = ev.rawX
            y = ev.rawY
        }
    }
}
