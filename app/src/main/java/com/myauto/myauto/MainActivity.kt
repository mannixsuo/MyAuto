package com.myauto.myauto

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.myauto.myauto.core.Image
import com.myauto.myauto.core.Shell
import com.myauto.myauto.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

//import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val shell = Shell()
    lateinit var command: String
    lateinit var originImageUri: Uri
    lateinit var templateImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        OpenCVLoader.initDebug()
//        shell.initShell("su")

        val originImage = findViewById<ImageView>(R.id.origin)
        originImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, 100)
        }

        val templateImage = findViewById<ImageView>(R.id.template)
        templateImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, 200)
        }
        val matchButton = findViewById<Button>(R.id.button3)
        matchButton.setOnClickListener {
            val origin: InputStream? = contentResolver.openInputStream(originImageUri)
            val template: InputStream? = contentResolver.openInputStream(templateImageUri)
            if (origin != null) {
                if (template != null) {
                    val bitmap = Image().matchTemplate(template, origin)

                    val view: ImageView = findViewById(R.id.origin)
                    val bitmapFile = File(it.context.filesDir.absolutePath + "/bitmap.jpg")
                    Log.i("bitmapFile", bitmapFile.absolutePath)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, FileOutputStream(bitmapFile))
                    view.setImageBitmap(bitmap)
                }
            }
        }

    }

    fun home(v: View) {
//        shell.execAndWaitFor("su")
        shell.home()
        shell.tap(720, 2711)
    }

    fun getPicture(v: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, 100)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            val uri: Uri? = data?.data
            if (uri != null) {
                originImageUri = uri
            }
            try {
                // 读取图像灰度化
                uri?.let {
                    val src = Mat()
                    val dest = Mat()
                    val ips: InputStream? = contentResolver.openInputStream(it)
                    val option: BitmapFactory.Options = BitmapFactory.Options()
                    option.inSampleSize = 1
                    val bitmap = BitmapFactory.decodeStream(ips)
                    Utils.bitmapToMat(bitmap, src)
                    Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2GRAY);
                    Utils.matToBitmap(dest, bitmap)
                    val view: ImageView = findViewById(R.id.origin)
                    view.setImageBitmap(bitmap);
                }
            } catch (e: Exception) {
                Log.i("err", e.toString())
            }
        }
        if (resultCode == RESULT_OK && requestCode == 200) {
            val uri: Uri? = data?.data
            if (uri != null) {
                templateImageUri = uri
            }
            try {
                // 读取图像灰度化
                uri?.let {
                    val src = Mat()
                    val dest = Mat()
                    val ips: InputStream? = contentResolver.openInputStream(it)
                    val option: BitmapFactory.Options = BitmapFactory.Options()
                    option.inSampleSize = 1
                    val bitmap = BitmapFactory.decodeStream(ips)
                    Utils.bitmapToMat(bitmap, src)
                    Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2GRAY);
                    Utils.matToBitmap(dest, bitmap)
                    val view: ImageView = findViewById(R.id.template)
                    view.setImageBitmap(bitmap);
                }
            } catch (e: Exception) {
                Log.i("err", e.toString())
            }
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
