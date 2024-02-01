package xyz.mufanc.imagedumper

import android.app.Activity
import android.app.ActivityThread
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.XposedHooker
import org.joor.Reflect
import sun.reflect.Reflection
import xyz.mufanc.autox.annotation.XposedEntry

@XposedEntry
@Suppress("Unused")
class ModuleMain(
    private val ixp: XposedInterface,
    private val mlp: XposedModuleInterface.ModuleLoadedParam
) : XposedModule(ixp, mlp) {

    companion object {
        const val TAG = "ImageDumper"
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        if (!param.isFirstPackage) return
        if (param.packageName != mlp.processName) return

        hook(ImageView::class.java.findMethod("initImageView"), ImageViewInitHook::class.java)
        hook(View::class.java.findMethod("setOnLongClickListener"), SetOnLongClickListenerHook::class.java)

        val apps: ArrayList<Application> = Reflect.on(ActivityThread.currentActivityThread()).get("mAllApplications")
        val monitor = ApplicationMonitor(apps)

        Log.i(TAG, "module loaded. (${mlp.processName})")
    }

    private class ApplicationMonitor(apps: ArrayList<Application>) : ArrayListProxy<Application>(apps) {

        override fun add(element: Application): Boolean {
            return super.add(element).apply {
                handleLoadApplication(element)
                detach()
            }
        }

        private fun handleLoadApplication(app: Application) {
            app.registerActivityLifecycleCallbacks(
                object : ActivityLifecycleCallbacks {
                    override fun onActivityCreated(activity: Activity, cache: Bundle?) {
                        FloatyWindow(activity)
                    }

                    override fun onActivityStarted(activity: Activity) = Unit
                    override fun onActivityResumed(activity: Activity) = Unit
                    override fun onActivityPaused(activity: Activity) = Unit
                    override fun onActivityStopped(activity: Activity) = Unit
                    override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
                    override fun onActivityDestroyed(activity: Activity) = Unit
                }
            )
        }
    }

    @XposedHooker
    class ImageViewInitHook : XposedInterface.Hooker {
        companion object {

            private val nodump: String by lazy {
                ModuleContext.packageContext.getString(R.string.tag_nodump)
            }

            @AfterInvocation
            @JvmStatic
            fun handle(callback: AfterHookCallback, ctx: ImageViewInitHook?) {
                val iv = (callback.thisObject as ImageView)

                if (iv.tag != nodump) {
                    iv.setOnLongClickListener(ListenerWrapper())
                } else {
                    Log.i(TAG, "skip: $iv")
                }
            }
        }
    }

    @XposedHooker
    class SetOnLongClickListenerHook : XposedInterface.Hooker {
        companion object {
            @AfterInvocation
            @JvmStatic
            fun handle(callback: AfterHookCallback, ctx: SetOnLongClickListenerHook?) {
                val caller = Reflection.getCallerClass()
                val mine = SetOnLongClickListenerHook::class.java

                if (caller == mine) return  // avoid loop

                val iv = (callback.thisObject as? ImageView) ?: return

                val info: Any = Reflect.on(iv).get("mListenerInfo") ?: return
                val listener: View.OnLongClickListener = Reflect.on(info).get("mOnLongClickListener")

                iv.setOnLongClickListener(ListenerWrapper(listener))

                Log.i(TAG, "reset listener for: $iv")
            }
        }
    }
}
