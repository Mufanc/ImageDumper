package xyz.mufanc.imagedumper

import java.lang.reflect.Method

fun Class<*>.findMethod(name: String): Method {
    return declaredMethods.find { it.name == name }
        ?: throw NoSuchMethodException()
}
