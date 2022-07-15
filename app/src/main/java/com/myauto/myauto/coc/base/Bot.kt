package com.myauto.myauto.coc.base

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.myauto.myauto.core.Shell
import com.myauto.myauto.function.App
import java.util.concurrent.TimeUnit

class Bot(application: Application) {
    private val shell = Shell()
    private var application: Application
    private val app = App()

    init {
        this.application = application
    }

    private val pkgName = "com.supercell.clashofclans"

    private val tag = "Bot"

    @RequiresApi(Build.VERSION_CODES.R)
    fun start() {
        Log.d(tag, "Bot Start")
        // --------- init -----------
        shell.initShell("su")
        app.init(application)
        // --------------
        shell.exec("pm list packages")
        TimeUnit.SECONDS.sleep(1)
        app.lunchAppByPackage(pkgName)
        TimeUnit.SECONDS.sleep(10)

    }

}