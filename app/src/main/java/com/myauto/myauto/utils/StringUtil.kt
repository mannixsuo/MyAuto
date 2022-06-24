package com.myauto.myauto.utils

class StringUtil {

}

fun joinString(sp: String, vararg sts: Any): String {
    val result = StringBuilder()
    var first = true
    for (s in sts) {
        if (first) {
            first = false
        } else {
            result.append(sp)
        }
        result.append(s)
    }
    return result.toString()
}
