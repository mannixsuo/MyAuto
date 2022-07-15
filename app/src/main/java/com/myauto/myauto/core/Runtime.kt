package com.myauto.myauto.core

import android.content.Context
import android.os.Build

class Runtime(builder: Builder) {
    private var images: Images? = null
    private var context: Context? = null

    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            images = Images(builder.context, this, builder.mScreenCaptureRequester)
        }
    }

    class Builder {
        private val mScreenCaptureRequester: ScreenCaptureRequester? = null
        private var context: Context? = null

    }
}