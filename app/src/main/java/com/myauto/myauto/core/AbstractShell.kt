package com.myauto.myauto.core

import android.content.Context
import android.preference.PreferenceManager

abstract class AbstractShell {

    val COMMAND_SU = "su"
    val COMMAND_SH = "sh"
    val COMMAND_LINE_END = "\n"
    val COMMAND_EXIT = "exit"
    private val none = -1
    lateinit var myScreenMetrics: ScreenMetrics

    lateinit var myContext: Context

    var myTouchDevice = none
        set(touchDevice) {
            if (myTouchDevice != none) {
                return
            }
            field = touchDevice
        }

    abstract fun initShell(initialCommand: String)
    abstract fun exec(command: String)
    abstract fun exit()

    fun touchX(x: Int) {

    }


    fun sendEvent(device: Int, type: Int, code: Int, value: Int) {
        exec("sendevent /dev/input/event$device $type $code $value")
    }

    class Result {
        var code = -1
        lateinit var error: String
        lateinit var result: String
        override fun toString(): String {
            return "ShellResult{code=$code, error='$error', result='$result'}"
        }
    }
}
