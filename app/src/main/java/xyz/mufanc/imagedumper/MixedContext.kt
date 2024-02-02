package xyz.mufanc.imagedumper

import android.app.ActivityThread
import android.content.Context
import android.content.ContextWrapper
import org.joor.Reflect

class MixedContext private constructor(
    private val host: Context,
    private val module: Context
) : ContextWrapper(module) {

    override fun getApplicationContext(): Context {
        return MixedContext(host.applicationContext, module)
    }

    override fun getSystemService(name: String): Any {
        return when (name) {
            Context.WINDOW_SERVICE -> host.getSystemService(name)
            else -> super.getSystemService(name)
        }
    }

    companion object {

        val hostAppContext: Context by lazy { ActivityThread.currentActivityThread().application }

        val modulePackageContext: Context by lazy {
            hostAppContext.createPackageContext(BuildConfig.APPLICATION_ID, 0).apply {
                setTheme(R.style.Theme_ImageDumper)
                Reflect.on(classLoader).set("parent", MixedContext::class.java.classLoader)
            }
        }

        operator fun invoke(context: Context): MixedContext {
            return (context as? MixedContext) ?: MixedContext(context, modulePackageContext)
        }
    }
}
