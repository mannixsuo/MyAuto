package com.myauto.myauto.function

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager

class App {

    private lateinit var application: Application

    private lateinit var packageManager: PackageManager
    fun init(application: Application) {
        this.application = application
        this.packageManager = application.packageManager
    }

    fun lunchAppByPackage(pkgName: String): Boolean {
        val launchIntentForPackage = packageManager.getLaunchIntentForPackage(pkgName)
        launchIntentForPackage?.let {
            application.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return true
        }
        return false
    }

    fun lunchAppByName(appName: String): Boolean {
        val pkgName = getAppPkgName(appName)
        pkgName?.let {
            return lunchAppByPackage(pkgName)
        }
        return false
    }


    fun getAppPkgName(appName: String): String? {
        val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (appInfo in installedApplications) {
            if (packageManager.getApplicationLabel(appInfo).toString() == appName) {
                return appInfo.packageName
            }
        }
        return null
    }
}
