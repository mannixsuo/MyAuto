package com.myauto.myauto.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager

interface ScreenCaptureRequester {
    fun cancel()

    interface Callback {
        fun onRequestResult(result: Int, data: Intent?)
    }

    fun request()

    fun setOnActivityResultCallback(callBack: Callback)

    abstract class AbstractScreenCaptureRequester : ScreenCaptureRequester {
        private var mCallback: Callback? = null
        var mResult: Intent? = null
        override fun setOnActivityResultCallback(callBack: Callback) {
            this.mCallback = callBack
        }

        open fun onResult(resultCode: Int, data: Intent?) {
            mResult = data
            mCallback?.onRequestResult(resultCode, data)
        }

        override fun cancel() {
            mResult?.let { return }
            mCallback?.onRequestResult(Activity.RESULT_CANCELED, null)
        }
    }

    class ActivityScreenCaptureRequester(mediator: OnActivityResultDelegate.Mediator, activity: Activity) :
        AbstractScreenCaptureRequester(), ScreenCaptureRequester,
        OnActivityResultDelegate {
        private var mMediator: OnActivityResultDelegate.Mediator? = null
        private var mActivity: Activity? = null
        init {
            this.mMediator = mediator
            this.mActivity = activity
        }
        private val REQUEST_CODE_MEDIA_PROJECTION = 17777


        override fun request() {
            mActivity?.startActivityForResult(
                (mActivity?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
                    .createScreenCaptureIntent(), REQUEST_CODE_MEDIA_PROJECTION
            )
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            mResult = data
            mMediator?.removeDelegate(this)
            onResult(resultCode, data)
        }

    }
}