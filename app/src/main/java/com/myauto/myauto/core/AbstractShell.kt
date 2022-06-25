package com.myauto.myauto.core

import android.content.Context
import android.view.KeyEvent

abstract class AbstractShell {

    val COMMAND_SU = "su"
    val COMMAND_SH = "sh"
    val COMMAND_LINE_END = "\n"
    val COMMAND_EXIT = "exit"

    val ABS_MT_POSITION_X = 53
    val ABS_MT_POSITION_Y = 54

    val EV_ABS = 3
    val EV_SYN = 0
    private val none = -1
    private var myScreenMetrics: ScreenMetrics? = null

    lateinit var myContext: Context

    var myTouchDevice = 1

    abstract fun initShell(initialCommand: String)
    abstract fun exec(command: String)
    abstract fun exit()

    fun touchXY(x: Int, y: Int) {
        touchX(x)
        touchY(y)
    }

    fun tap(x: Int, y: Int) {
        exec("input tap ${scaleX(x)} ${scaleY(y)}")
    }

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int) {
        exec("input swipe ${scaleX(x1)} ${scaleY(y1)} ${scaleX(x2)} ${scaleY(y2)}")
    }

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, time: Int) {
        exec("input swipe ${scaleX(x1)} ${scaleY(y1)} ${scaleX(x2)} ${scaleY(y2)} $time")
    }

    fun home() {
        keyCode(KeyEvent.KEYCODE_HOME)
    }

    fun back() {
        keyCode(KeyEvent.KEYCODE_BACK)
    }

    fun up() {
        keyCode(KeyEvent.KEYCODE_DPAD_UP)
    }

    fun down() {
        keyCode(KeyEvent.KEYCODE_DPAD_DOWN)
    }

    fun left() {
        keyCode(KeyEvent.KEYCODE_DPAD_LEFT)
    }

    fun right() {
        keyCode(KeyEvent.KEYCODE_DPAD_RIGHT)
    }

    fun ok() {
        keyCode(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    fun volumeUp() {
        keyCode(KeyEvent.KEYCODE_VOLUME_UP)
    }

    fun volumeDown() {
        keyCode(KeyEvent.KEYCODE_VOLUME_DOWN)
    }

    fun menu() {
        keyCode(KeyEvent.KEYCODE_VOLUME_DOWN)
    }

    fun camera() {
        keyCode(KeyEvent.KEYCODE_CAMERA)
    }

    fun input(text: String) {
        exec("input text $text")
    }

    fun screenCap(path: String) {
        exec("screencap -p $path")
    }

    fun sleep(time: Long) {
        exec("sleep $time")
    }

    fun usleep(l: Long) {
        exec("usleep $l")
    }


    private fun keyCode(code: Int) {
        exec("input keyevent $code")
    }


    private fun sendEvent(device: Int, type: Int, code: Int, value: Int) {
        exec("sendevent /dev/input/event$device $type $code $value")
    }

    private fun scaleX(x: Int): Int {
        return myScreenMetrics?.scaleX(x) ?: x
    }

    private fun scaleY(y: Int): Int {
        return myScreenMetrics?.scaleY(y) ?: y
    }

    private fun touchX(x: Int) {
        sendEvent(myTouchDevice, EV_ABS, ABS_MT_POSITION_X, scaleX(x))
    }

    private fun touchY(y: Int) {
        sendEvent(myTouchDevice, EV_ABS, ABS_MT_POSITION_Y, scaleX(y))
    }


    class Result(code: Int, error: String, result: String) {
        var code = -1
        lateinit var error: String
        lateinit var result: String
        override fun toString(): String {
            return "ShellResult{code=$code, error='$error', result='$result'}"
        }
    }
}
