package com.myauto.myauto.core

import android.app.Activity
import android.content.res.Configuration
import android.util.DisplayMetrics

class ScreenMetrics {
    var deviceScreenWidth = 0
    var deviceScreenHeight = 0
    var initialized = false
    var deviceScreenDensity = 0
    var myDesignWidth = 0
    var myDesignHeight = 0

    fun initIfNeeded(activity: Activity) {
        if (initialized) {
            return
        }
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        deviceScreenHeight = metrics.heightPixels
        deviceScreenWidth = metrics.widthPixels
        deviceScreenDensity = metrics.densityDpi
        initialized = true
    }

    fun setScreenMetrics(width: Int, height: Int) {
        myDesignHeight = height
        myDesignWidth = width
    }

    fun getOrientationAwareScreenHeight(orientation: Int): Int {
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            deviceScreenWidth
        } else {
            deviceScreenHeight
        }
    }

    fun scaleX(x: Int): Int {
        return scaleX(x, myDesignWidth)
    }

    fun rescaleX(x: Int): Int {
        return rescaleX(x, myDesignWidth)
    }

    fun scaleY(y: Int): Int {
        return scaleY(y, myDesignHeight)
    }

    fun rescaleY(y: Int): Int {
        return rescaleY(y, myDesignHeight)
    }

    private fun scaleX(x: Int, width: Int): Int {
        return if (width == 0 || !initialized) x else x * deviceScreenWidth / width
    }

    private fun rescaleX(x: Int, width: Int): Int {
        return if (width == 0 || !initialized) x else x * width / deviceScreenWidth
    }

    private fun scaleY(y: Int, height: Int): Int {
        return if (height == 0 || !initialized) y else y * deviceScreenHeight / height
    }

    private fun rescaleY(y: Int, height: Int): Int {
        return if (height == 0 || !initialized) y else y * height / deviceScreenHeight
    }
}
