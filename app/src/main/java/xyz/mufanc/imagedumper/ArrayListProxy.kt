package xyz.mufanc.imagedumper

import android.annotation.SuppressLint
import java.util.WeakHashMap

abstract class ArrayListProxy<T>(
    private val inner: ArrayList<T>
) : ArrayList<T>() {

    fun detach() {
        detach(inner)
    }

    init {
        attach(inner, javaClass)
    }

    companion object {

        @SuppressLint("SoonBlockedPrivateApi")
        private val field = Object::class.java.getDeclaredField("shadow\$_klass_").apply { isAccessible = true }

        private val records = WeakHashMap<ArrayList<*>, Class<*>>()

        private fun attach(list: ArrayList<*>, klass: Class<out ArrayListProxy<*>>) {
            records[list] = list.javaClass
            field.set(list, klass)
        }

        private fun detach(list: ArrayList<*>) {
            val original = records.remove(list)
            if (original != null) {
                field.set(list, original)
            }
        }
    }
}
