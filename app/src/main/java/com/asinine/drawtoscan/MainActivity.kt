package com.asinine.drawtoscan

import android.Manifest
import android.content.DialogInterface
import android.graphics.*
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.googlecode.tesseract.android.TessBaseAPI
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MainActivity : AppCompatActivity(), DrawingView.PathTouchListener {
    lateinit var dv: DrawingView
    lateinit var dataPath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dv = findViewById(R.id.drawingView)
        dv.setPathTouchListener(this)
        askPermission()
    }

    fun askPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            moveTrainedData()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener {
                Snackbar.make(dv, it.name, Snackbar.LENGTH_SHORT).show()

            }
            .check()
    }

    override fun pathTouched(path: Path?) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.RED
        paint.strokeWidth = 5f
        val bitmap = Bitmap.createBitmap(
            window.decorView.width,
            window.decorView.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.YELLOW)
        canvas.drawPath(path!!, paint)
        showDialog(extractText(bitmap))
    }

    @Throws(Exception::class)
    private fun extractText(bitmap: Bitmap): String? {
        val tessBaseApi = TessBaseAPI()
        tessBaseApi.init(dataPath, "eng")
        tessBaseApi.setImage(bitmap)
        val extractedText = tessBaseApi.utF8Text
        tessBaseApi.end()
        return extractedText
    }

    fun moveTrainedData() {
        dataPath = getExternalFilesDir(null).toString() + "/tesseract/"
        val dir = File(dataPath + "tessdata/")
        if (!dir.exists()) dir.mkdirs()
        val inStream: InputStream = resources.openRawResource(R.raw.eng)
        val out = FileOutputStream(dataPath + "tessdata/eng.traineddata")
        val buff = ByteArray(1024)
        var read = 0

        try {
            while (inStream.read(buff).also { read = it } > 0) {
                out.write(buff, 0, read)
            }
        } finally {
            inStream.close()
            out.close()
            Snackbar.make(dv, "Trained data Initialized successfully", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun showDialog(ocrText: String?) {
        val dialog = AlertDialog.Builder(this).setTitle("Recognized character")
            .setView(EditText(this).apply {
                setText(ocrText)
            })
            .setPositiveButton("ok", DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
                dv.invalidate()
            })
        dialog.show()
    }

}