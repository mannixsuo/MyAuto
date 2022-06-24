package com.myauto.myauto.core

import android.os.Handler
import android.preference.PreferenceManager
import jackpal.androidterm.ShellTermSession
import jackpal.androidterm.emulatorview.TermSession
import jackpal.androidterm.util.TermSettings
import java.io.BufferedReader
import java.io.DataOutputStream

class Shell : AbstractShell() {
    private lateinit var myProcess: Process
    private lateinit var myCommandOutputStream: DataOutputStream
    private lateinit var mySucceedReader: BufferedReader
    private lateinit var myErrorReader: BufferedReader
    private var mySucceedOutput = StringBuilder()
    private var myErrorOutput = StringBuilder()


    private lateinit var myTermSession: TermSession
    override fun initShell(initialCommand: String) {
        TODO("Not yet implemented")
    }


    fun init(initialCommand: String) {
        val uiHandler: Handler = Handler(myContext.mainLooper)
        uiHandler.post {
            val settings: TermSettings =
                TermSettings(myContext.resources, PreferenceManager.getDefaultSharedPreferences(myContext))
            myTermSession = MyShellTermSession(settings, initialCommand)
            myTermSession.initializeEmulator(1024, 40)
        }
    }

    override fun exec(command: String) {

    }

    override fun exit() {
        TODO("Not yet implemented")
    }

    interface Callback {
        fun onOutput(str: String?)
        fun onNewLine(line: String?)
        fun onInitialized()
        fun onInterrupted(e: InterruptedException?)
    }

    class MyShellTermSession(settings: TermSettings, initialCommand: String) :
        ShellTermSession(settings, initialCommand) {
        @Volatile
        private var myInitialized = false

        @Volatile
        private var myWaitingExit = false
        private val myStringBuffer = StringBuffer()
        private val myCommandOutputs = ArrayList<String>()
        private val mRoot = false
        private val myInitialLock = Object()
        private val myExtLock = Object()
        private lateinit var myCallBack: Callback

        fun onNewLine(line: String) {
            if (!myInitialized) {
                if (!mRoot && line.endsWith(" $ sh")) {
                    notifyInitialized()
                }
            } else {
                myCommandOutputs.add(line)
            }
            if (myCallBack != null) {
                myCallBack.onNewLine(line)
            }
            if (myWaitingExit && line.endsWith(" exit")) {
                notifyExit()
            }
        }

        private fun notifyInitialized() {
            myInitialized = true
            synchronized(myInitialLock) {
                myInitialLock.notifyAll()
            }
            if (myCallBack != null) {
                myCallBack.onInitialized()
            }
        }

        private fun notifyExit() {
            synchronized(myExtLock) {
                myWaitingExit = false
                myExtLock.notify()
            }
        }
    }


}
