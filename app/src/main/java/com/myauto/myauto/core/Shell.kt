package com.myauto.myauto.core

import android.util.Log
import androidx.annotation.RequiresApi
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.util.concurrent.locks.ReentrantLock

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

    private lateinit var mySucceedOutput: StringBuilder
    private lateinit var myErrorOutput: StringBuilder

    private val myCommandOutputLock = Object()

    private val myCommandLock = ReentrantLock()

    /**
     * init the shell with initial command
     */
    override fun initShell(initialCommand: String) {
        val runtime = Runtime.getRuntime()
        myProcess = runtime.exec(initialCommand)
        mySucceedReader = InputStreamReader(myProcess!!.inputStream)
        myCommandOutputStream = DataOutputStream(myProcess!!.outputStream)
        myErrorReader = InputStreamReader(myProcess!!.errorStream)
        mySucceedOutput = StringBuilder()
        myErrorOutput = StringBuilder()
        Thread {
            val buf = CharArray(1024)
            var read: Int;
            var string: String
            do {
                read = mySucceedReader.read(buf)
                if (read != -1) {
                    string = String(buf, 0, read)
                    Log.i("mySucceedReader:", string)
                    mySucceedOutput.append(string)
                }
            } while (read != -1)

            do {
                read = myErrorReader.read(buf)
                if (read!=-1){
                    string = String(buf, 0, read)
                    Log.i("myErrorReader:", string)
                    myErrorOutput.append(string)
                }
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


    fun execAndWaitFor(command: String): Result {
        try {
            // execute one command single time
            myCommandLock.lock()
            Log.i("Shell execAndWaitFor", command)
            mySucceedOutput = StringBuilder()
            myErrorOutput = StringBuilder()
            myCommandOutputStream.writeBytes(command)
            if (!command.endsWith(COMMAND_LINE_END)) {
                myCommandOutputStream.writeBytes(COMMAND_LINE_END)
            }
            myCommandOutputStream.flush()
            // wait output process to notify
            myCommandOutputLock.wait(5000)
            Log.i("Shell", "Shell finish")
            return Result(1, myErrorOutput.toString(), mySucceedOutput.toString())
        } finally {
            // unlock
            myCommandLock.unlock()
        }

    }


    @RequiresApi(26)
    override fun exit() {
        if (myProcess?.isAlive == true) {
            myProcess?.destroy()
            myProcess = null
        }
    }


}
