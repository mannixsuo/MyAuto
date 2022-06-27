package com.myauto.myauto.core

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.features2d.DescriptorMatcher
import org.opencv.features2d.SIFT
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

}
