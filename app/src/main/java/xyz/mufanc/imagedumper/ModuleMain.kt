package xyz.mufanc.imagedumper

import android.annotation.SuppressLint
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

    @SuppressLint("SoonBlockedPrivateApi", "DiscouragedPrivateApi")
    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        if (!param.isFirstPackage) return
        if (param.packageName != mlp.processName) return

        hook(ImageView::class.java.findMethod("initImageView"), ImageViewInitHook::class.java)
        hook(View::class.java.findMethod("setOnLongClickListener"), SetOnLongClickListenerHook::class.java)

        Log.i(TAG, "module loaded. (${mlp.processName})")
    }

    @XposedHooker
    class ImageViewInitHook : XposedInterface.Hooker {
        companion object {
            @AfterInvocation
            @JvmStatic
            fun handle(callback: AfterHookCallback, ctx: ImageViewInitHook?) {
                val iv = (callback.thisObject as ImageView)
                iv.setOnLongClickListener(ListenerWrapper())
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
