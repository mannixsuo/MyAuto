package com.myauto.myauto.core

import android.util.Log
import androidx.annotation.RequiresApi
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Shell used for execute adb command
 */
class Shell : AbstractShell() {
    /**
     * shell command execution process
     */
    private var myProcess: Process? = null

    private lateinit var myCommandOutputStream: DataOutputStream
    private lateinit var mySucceedReader: InputStreamReader
    private lateinit var myErrorReader: InputStreamReader
    private var mySucceedOutput = StringBuilder()
    private var myErrorOutput = StringBuilder()
    private val myCommandOutputLock = Object()

    /**
     * init the shell with initial command
     */
    override fun initShell(initialCommand: String) {
        val runtime = Runtime.getRuntime()
        myProcess = runtime.exec(initialCommand)
        mySucceedReader = InputStreamReader(myProcess!!.inputStream)
        myCommandOutputStream = DataOutputStream(myProcess!!.outputStream)
        myErrorReader = InputStreamReader(myProcess!!.errorStream)
        Thread {
            val buf = CharArray(1024)
            var read: Int;
            do {
                read = mySucceedReader.read(buf)
                Log.i("mySucceedReader:", String(buf, 0, read))
            } while (read != -1)
        }.start()

        Thread {
            val buf = CharArray(1024)
            var read: Int;
            do {
                read = myErrorReader.read(buf)
                Log.i("myErrorReader:", String(buf, 0, read))
            } while (read != -1)
        }.start()
    }

    override fun exec(command: String) {
        Log.i("Shell", command)
        myCommandOutputStream.writeBytes(command)
        if (!command.endsWith(COMMAND_LINE_END)) {
            myCommandOutputStream.writeBytes(COMMAND_LINE_END)
        }
        myCommandOutputStream.flush()
    }

    // TODO no response after execute
    fun execAndWaitFor(command: String): Result {
        Log.i("Shell execAndWaitFor", command)
        myCommandOutputStream.writeBytes(command)
        if (!command.endsWith(COMMAND_LINE_END)) {
            myCommandOutputStream.writeBytes(COMMAND_LINE_END)
        }
        myCommandOutputStream.flush()
        val successMsg = StringBuilder()
        val errorMsg = StringBuilder()
//        for (s in mySucceedReader.readLines()) {
//            Log.i("mySucceedReader", s)
//            successMsg.append(s)
//        }
//        for (s in myErrorReader.readLines()) {
//            Log.i("myErrorReader:", s)
//            errorMsg.append(s)
//        }
        Log.i("Shell", "Shell finish")
        return Result(1, errorMsg.toString(), successMsg.toString())
    }


    @RequiresApi(26)
    override fun exit() {
        if (myProcess?.isAlive == true) {
            myProcess?.destroy()
            myProcess = null
        }
    }


}
