package xyz.mufanc.imagedumper

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ImageView
import org.joor.Reflect
import xyz.mufanc.imagedumper.ModuleMain.Companion.TAG
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.WeakHashMap
import kotlin.concurrent.thread

class ListenerGuard(
    iv: ImageView,
    listener: View.OnLongClickListener,
    queue: ReferenceQueue<View.OnLongClickListener>
) : PhantomReference<View.OnLongClickListener>(listener, queue) {

    private val iv = WeakReference(iv)

    @SuppressLint("ClickableViewAccessibility")
    companion object {

        private val queue = ReferenceQueue<View.OnLongClickListener>()
        private val registry = WeakHashMap<View, ListenerGuard>()

        fun attach(iv: ImageView) {
            val listener = ListenerWrapper()

            iv.setOnLongClickListener(listener)
            registry[iv] = ListenerGuard(iv, listener, queue)

            Log.d(TAG, "attached to: $iv")
        }

        init {
            thread {
                while(true) {
                    val guard = queue.remove() as ListenerGuard
                    val iv = guard.iv.get()

                    if (iv != null) {
                        val info: Any = Reflect.on(iv).get("mListenerInfo")
                        val listener: View.OnLongClickListener = Reflect.on(info).get("mOnLongClickListener")

                        iv.setOnLongClickListener(ListenerWrapper(listener))

                        Log.i(TAG, "reset listener for: $iv")
                    }
                }
            }
        }
    }
}
