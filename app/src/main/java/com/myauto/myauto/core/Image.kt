package com.myauto.myauto.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.features2d.*
import org.opencv.imgproc.Imgproc
import java.io.InputStream
import java.util.*

class Image {

    fun findImage(templateImageUri: InputStream, originImageUri: InputStream) {

        val templateImage = Mat()
        Utils.bitmapToMat(BitmapFactory.decodeStream(templateImageUri), templateImage)

        val originImage = Mat()
        Utils.bitmapToMat(BitmapFactory.decodeStream(originImageUri), templateImage)

        val resTemplate = Mat()
        val resOrigin = Mat()


        val sift = SIFT.create()

        val templateKeyPoints = MatOfKeyPoint()
        val originalKeyPoints = MatOfKeyPoint()

        // detect image key points
        sift.detect(templateImage, templateKeyPoints)
        sift.detect(originImage, originalKeyPoints)

        sift.compute(templateImage, templateKeyPoints, resTemplate)
        sift.compute(originImage, originalKeyPoints, resOrigin)

        val matches = LinkedList<MatOfDMatch>()
        val descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED)
        // find best match
        if (resOrigin.empty() || resTemplate.empty()) {
            Log.e("Image", "resOrigin.empty() || resTemplate.empty()")
        }
        descriptorMatcher.knnMatch(resTemplate, resOrigin, matches, 2)
        val goodMatchList = LinkedList<DMatch>()
        matches.forEach {
            val dMatchArray = it.toArray()
            val m1 = dMatchArray[0]
            val m2 = dMatchArray[1]
            if (m1.distance <= m2.distance * 0.7) {
                goodMatchList.addLast(m1)
            }
        }
        // if count >4 then assume templateImage is in originImage
        if (goodMatchList.size > 4) {
            val templateKeyPointList = templateKeyPoints.toList()
            val originalKeyPointList = originalKeyPoints.toList()
            val objectPoints = LinkedList<Point>()
            val scenePoints = LinkedList<Point>()
            goodMatchList.forEach {
                objectPoints.addLast(templateKeyPointList[it.queryIdx].pt)
                scenePoints.addLast(originalKeyPointList[it.trainIdx].pt)
            }

            val objectMatOfPoint2f = MatOfPoint2f()
            objectMatOfPoint2f.fromList(objectPoints)

            val sceneMatOfPoint2f = MatOfPoint2f()
            sceneMatOfPoint2f.fromList(scenePoints)

            val homography = Calib3d.findHomography(objectMatOfPoint2f, sceneMatOfPoint2f, Calib3d.RANSAC, 3.0)

            val templateCorners = Mat(4, 1, CvType.CV_32FC2)
            val templateTransformResult = Mat(4, 1, CvType.CV_32FC2)

            templateCorners.put(0, 0, IntArray(2))
            templateCorners.put(1, 0, intArrayOf(templateImage.cols(), 0))
            templateCorners.put(2, 0, intArrayOf(templateImage.cols(), templateImage.rows()))
            templateCorners.put(3, 0, intArrayOf(0, templateImage.rows()))
            Core.perspectiveTransform(templateCorners, templateTransformResult, homography)
            val pointA = templateTransformResult.get(0, 0)
            val pointB = templateTransformResult.get(1, 0)
            val pointC = templateTransformResult.get(2, 0)
            val pointD = templateTransformResult.get(3, 0)

            Log.i("pointA", pointA.toString())
            Log.i("pointB", pointB.toString())
            Log.i("pointC", pointC.toString())
            Log.i("pointD", pointD.toString())

        } else {
            Log.i("", "match fail")
        }

    }

    fun findImageBF(templateImageUri: InputStream, originImageUri: InputStream): Bitmap {
        val templateImage = Mat(200, 200, CvType.CV_8U)
        Utils.bitmapToMat(BitmapFactory.decodeStream(templateImageUri), templateImage)

        val originImage = Mat(200, 200, CvType.CV_8U)
        Utils.bitmapToMat(BitmapFactory.decodeStream(originImageUri), originImage)

        val resTemplate = Mat()
        val resOrigin = Mat()

        val resTemplateDescriptor = Mat()
        val resOriginDescriptor = Mat()
        val templateKeyPoints = MatOfKeyPoint()
        val originalKeyPoints = MatOfKeyPoint()
        val orb = ORB.create()

        orb.detectAndCompute(templateImage, Mat(), templateKeyPoints, resTemplateDescriptor)
        orb.detectAndCompute(originImage, Mat(), originalKeyPoints, resOriginDescriptor)

        val bfMatcher = BFMatcher(Core.NORM_HAMMING, true)
        val matches = MatOfDMatch()
        bfMatcher.match(resTemplateDescriptor, resOriginDescriptor, matches)
        val arrayOfDMatchs = matches.toArray()
        arrayOfDMatchs.sortBy(DMatch::distance)

        Log.i("arrayOfDMatchs", arrayOfDMatchs.toString())
        val out = Mat()
        Features2d.drawMatches(templateImage, templateKeyPoints, originImage, originalKeyPoints, matches, out)

        val createBitmap = Bitmap.createBitmap(out.cols(), out.rows(), Bitmap.Config.ARGB_8888)

        Utils.matToBitmap(out, createBitmap)
        return createBitmap
    }

    fun findImageSift(templateImageUri: InputStream, originImageUri: InputStream): Bitmap {
        val templateImage = Mat(1000, 1000, CvType.CV_8U)
        Utils.bitmapToMat(BitmapFactory.decodeStream(templateImageUri), templateImage)

        val originImage = Mat(1000, 1000, CvType.CV_8U)
        Utils.bitmapToMat(BitmapFactory.decodeStream(originImageUri), originImage)

        val resTemplate = Mat()
        val resOrigin = Mat()
        Imgproc.cvtColor(templateImage, resTemplate, Imgproc.COLOR_BGR2GRAY)

        Imgproc.cvtColor(originImage, resOrigin, Imgproc.COLOR_BGR2GRAY)


        val sift = SIFT.create()
        val templateKeyPoints = MatOfKeyPoint()
        val originalKeyPoints = MatOfKeyPoint()
        val templateDescriptor = Mat()
        val originDescriptor = Mat()
        sift.detectAndCompute(resTemplate, Mat(), templateKeyPoints, templateDescriptor)
        sift.detectAndCompute(resOrigin, Mat(), originalKeyPoints, originDescriptor)
        val matches = ArrayList<MatOfDMatch>()
        FlannBasedMatcher().knnMatch(templateDescriptor, originDescriptor, matches, 2)
        val goodMatchList = LinkedList<MatOfDMatch>()
        matches.forEach {
            val dMatchArray = it.toArray()
            val m1 = dMatchArray[0]
            val m2 = dMatchArray[1]
            if (m1.distance <= m2.distance * 0.4) {
                goodMatchList.addLast(it)
            }
        }

        goodMatchList.sortBy {
            val array = it.toArray()
            val m1 = array[0]
            val m2 = array[1]
            m1.distance - m2.distance
        }

        val out = Mat()
        Features2d.drawMatchesKnn(templateImage, templateKeyPoints, originImage, originalKeyPoints, goodMatchList, out)

        val createBitmap = Bitmap.createBitmap(out.cols(), out.rows(), Bitmap.Config.ARGB_8888)

        Utils.matToBitmap(out, createBitmap)
        return createBitmap
    }

    fun matchTemplate(template: InputStream, src: InputStream): Bitmap {
        val templateMat = Mat(1000, 1000, CvType.CV_8U)
        val srcMat = Mat(1000, 1000, CvType.CV_8U)

        val templateBitmap = BitmapFactory.decodeStream(template)
        val srcBitmap = BitmapFactory.decodeStream(src)
        Utils.bitmapToMat(templateBitmap, templateMat)
        Utils.bitmapToMat(srcBitmap, srcMat)

        val templateGrayMat = Mat(1000, 1000, CvType.CV_8U)
        val srcGrayMat = Mat(1000, 1000, CvType.CV_8U)


        Imgproc.cvtColor(templateMat, templateGrayMat, Imgproc.COLOR_BGR2GRAY)

        Imgproc.cvtColor(srcMat, srcGrayMat, Imgproc.COLOR_BGR2GRAY)

        val w = templateMat.cols()
        val h = templateMat.rows()
        val resultMat = Mat(1000, 1000, CvType.CV_8U)
        Imgproc.matchTemplate(templateGrayMat, srcGrayMat, resultMat, Imgproc.TM_CCOEFF_NORMED)
        val minMaxLoc = Core.minMaxLoc(resultMat)
        val topLeft = minMaxLoc.maxLoc
        val bottomRight = Point(topLeft.x + w, topLeft.y + h)
        Imgproc.rectangle(srcMat, topLeft, bottomRight, Scalar(0.0, 255.0, 2.0), 2)
        val resultBitmap = Bitmap.createBitmap(srcBitmap)
        Utils.matToBitmap(srcMat, resultBitmap)
        return resultBitmap
    }

}
