package com.myauto.myauto.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.OrientationEventListener
import androidx.annotation.RequiresApi
import java.util.concurrent.atomic.AtomicReference

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenCapture(context: Context, data: Intent, orientation: Int, screenDensity: Int, handler: Handler?) {
    private var mContext: Context
    private var mData: Intent


    private var mOrientation = -1
    private var mDetectedOrientation = 0
    private var mScreenDensity = 0
    private var mHandler: Handler?
    private var mProjectionManager: MediaProjectionManager
    private var mMediaProjection: MediaProjection
    private val ORIENTATION_AUTO = Configuration.ORIENTATION_UNDEFINED
    private val ORIENTATION_LANDSCAPE = Configuration.ORIENTATION_LANDSCAPE
    private val ORIENTATION_PORTRAIT = Configuration.ORIENTATION_PORTRAIT

    private val imageCache = AtomicReference<Image>()
    private val logTag = "ScreenCapture"
    private lateinit var mOrientationEventListener: OrientationEventListener
    private val screenMetrics: ScreenMetrics = ScreenMetrics()
    private var mImageAcquireLooper: Looper? = null
    private lateinit var mImageReader: ImageReader
    private lateinit var mVirtualDisplay: VirtualDisplay
    private var mException: Exception? = null
    private var mUnderUsingImage: Image? = null

    init {
        this.mContext = context
        this.mData = data
        this.mScreenDensity = screenDensity
        this.mHandler = handler
        this.mProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        this.mMediaProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mData.clone() as Intent)
        setOrientation(orientation)
        observeOrientation()
    }


    private fun observeOrientation() {
        mOrientationEventListener = object : OrientationEventListener(mContext) {
            override fun onOrientationChanged(o: Int) {
                val orientation = mContext.resources.configuration.orientation
                if (mOrientation == ORIENTATION_AUTO && mDetectedOrientation != orientation) {
                    mDetectedOrientation = orientation
                    try {
                        refreshVirtualDisplay(orientation)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable()
        }
    }

    private fun setOrientation(orientation: Int) {
        if (mOrientation == orientation) return
        mOrientation = orientation
        mDetectedOrientation = mContext.resources.configuration.orientation
        refreshVirtualDisplay(if (mOrientation == ORIENTATION_AUTO) mDetectedOrientation else mOrientation)
    }

    fun refreshVirtualDisplay(orientation: Int) {
        mImageAcquireLooper?.quit()
        mImageReader.close()
        mVirtualDisplay.release()
        mMediaProjection.stop()
        mMediaProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mData.clone() as Intent)
        val screenHeight: Int = screenMetrics.getOrientationAwareScreenHeight(orientation)
        val screenWidth: Int = screenMetrics.getOrientationAwareScreenWidth(orientation)
        initVirtualDisplay(screenWidth, screenHeight, mScreenDensity)
        startAcquireImageLoop()
    }

    private fun initVirtualDisplay(screenWidth: Int, screenHeight: Int, mScreenDensity: Int) {
        mImageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 3)
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
            logTag,
            screenWidth,
            screenHeight,
            mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader.surface,
            null,
            null
        )
    }

    private fun startAcquireImageLoop() {
        mHandler?.let {
            setImageListener(it)
            return
        }
        Thread {
            Log.d(logTag, "AcquireImageLoop: start")
            Looper.prepare()
            mImageAcquireLooper = Looper.myLooper()
            Looper.myLooper()?.let {
                setImageListener(Handler(it))
                Looper.loop()
            }
            Log.d(logTag, "AcquireImageLoop: stop")
        }.start()
    }

    private fun setImageListener(handler: Handler) {
        mImageReader.setOnImageAvailableListener({ reader: ImageReader ->
            try {
                val oldCacheImage: Image? = imageCache.getAndSet(null)
                oldCacheImage?.close()
                imageCache.set(reader.acquireLatestImage())
            } catch (e: java.lang.Exception) {
                mException = e
            }
        }, handler)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun release() {
        mImageAcquireLooper?.quit()
        mMediaProjection.stop()
        mVirtualDisplay.release()
        mImageReader.close()
        mUnderUsingImage?.close()
        val cachedImage: Image = imageCache.getAndSet(null)
        cachedImage.close()
        mOrientationEventListener.disable()
    }

    fun capture(): Image? {
        mException?.let {
            mException = null
            throw java.lang.RuntimeException(it)
        }
        val currentThread = Thread.currentThread()
        while (!currentThread.isInterrupted) {
            val cachedImage = imageCache.getAndSet(null)
            cachedImage?.let {
                mUnderUsingImage?.close()
                mUnderUsingImage = it
                return it
            }
        }
        return null
    }
}