package xyz.mufanc.imagedumper

import android.app.ActivityThread
import android.content.Context
import android.content.ContextWrapper
import org.joor.Reflect

class ModuleContext private constructor(
    private val host: Context,
    private val module: Context
) : ContextWrapper(module) {

    constructor(context: Context) : this(context, packageContext)

    override fun getApplicationContext(): Context {
        return ModuleContext(host.applicationContext, module)
    }

    override fun getSystemService(name: String): Any {
        return when (name) {
            Context.WINDOW_SERVICE -> host.getSystemService(name)
            else -> super.getSystemService(name)
        }
    }

    companion object {

        private val app by lazy { ActivityThread.currentActivityThread().application }

        val packageContext: Context by lazy {
            app.createPackageContext(BuildConfig.APPLICATION_ID, 0).apply {
                setTheme(R.style.Theme_ImageDumper)
                Reflect.on(classLoader).set("parent", ModuleContext::class.java.classLoader)
            }
        }
    }
}
