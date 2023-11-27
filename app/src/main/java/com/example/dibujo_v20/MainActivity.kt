
package com.example.dibujo_v20

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var dibujarView: DibujoView

    private lateinit var pincel: ImageButton
    private lateinit var botonAzul: ImageButton
    private lateinit var botonVerde: ImageButton
    private lateinit var botonRojo: ImageButton
    private lateinit var botonVioleta: ImageButton
    private lateinit var botonNaranja: ImageButton
    private lateinit var botonDeshacer: ImageButton
    private lateinit var selectorColor: ImageButton
    private lateinit var botonGaleria: ImageButton
    private lateinit var botonGuardar: ImageButton

    private var abrirGaleria: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { resultado ->
        findViewById<ImageView>(R.id.imagen_de_galeria).setImageURI(resultado.data?.data)
    }

    private var solicitarPermiso: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
            permisosAPedir -> permisosAPedir.entries.forEach {
        var nombrePermiso = it.key
        var permisoOtorgado = it.value

        if (permisoOtorgado && (nombrePermiso == Manifest.permission.READ_EXTERNAL_STORAGE)) {

            Toast.makeText(this, "Permiso otorgado: $nombrePermiso", Toast.LENGTH_LONG).show()
            var seleccionIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            abrirGaleria.launch(seleccionIntent)

        } else if (permisoOtorgado && (nombrePermiso == Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Toast.makeText(this, "Permiso otorgado: $nombrePermiso", Toast.LENGTH_LONG).show()
            CoroutineScope(Dispatchers.IO).launch {
                guardarImagen(obtenerBitmap(findViewById(R.id.constraint_layout_3)))
            }
        } else {
            if (nombrePermiso == Manifest.permission.READ_EXTERNAL_STORAGE || nombrePermiso == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                Toast.makeText(this, "Permiso denegado: $nombrePermiso", Toast.LENGTH_LONG).show()
            }
        }
    }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pincel = findViewById(R.id.pincel)
        dibujarView = findViewById(R.id.dibujo)
        dibujarView.cambiarGrosor(20F)

        botonAzul = findViewById(R.id.boton_azul)
        botonRojo = findViewById(R.id.boton_rojo)
        botonNaranja = findViewById(R.id.boton_naranja)
        botonVerde = findViewById(R.id.boton_verde)
        botonVioleta = findViewById(R.id.boton_violeta)
        botonDeshacer = findViewById<ImageButton>(R.id.deshacer)
        selectorColor = findViewById(R.id.selectorColor)
        botonGaleria = findViewById(R.id.galeria)
        botonGuardar = findViewById(R.id.guardar)

        pincel.setOnClickListener {
            pintarConPincel()
        }



        botonVioleta.setOnClickListener(this)
        botonAzul.setOnClickListener(this)
        botonVerde.setOnClickListener(this)
        botonRojo.setOnClickListener(this)
        botonNaranja.setOnClickListener(this)
        botonDeshacer.setOnClickListener(this)
        selectorColor.setOnClickListener(this)
        botonGaleria.setOnClickListener(this)
        botonGuardar.setOnClickListener(this)


    }

    private fun pintarConPincel() {
        var textoPincel = Dialog(this@MainActivity)
        textoPincel.setContentView(R.layout.ventana_pincel)
        var seekbarProgreso = textoPincel.findViewById<SeekBar>(R.id.seekBarDialog)
        var mostrarProgresoSeekBar = textoPincel.findViewById<TextView>(R.id.textViewProgressDialog)
        seekbarProgreso.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                dibujarView.cambiarGrosor(seekBar.progress.toFloat())
                mostrarProgresoSeekBar.text = seekBar.progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        textoPincel.show()
    }


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.boton_violeta -> {
                dibujarView.cambiarColorPincel("#f30bf4")
            }
            R.id.boton_azul -> {
                dibujarView.cambiarColorPincel("#490bf4")
            }
            R.id.boton_naranja -> {
                dibujarView.cambiarColorPincel("#f2a50d")
            }
            R.id.boton_rojo -> {
                dibujarView.cambiarColorPincel("#f63209")
            }
            R.id.boton_verde -> {
                dibujarView.cambiarColorPincel("#0ff04a")
            }
            R.id.deshacer -> {
                dibujarView.deshacerCamino()
            }
            R.id.selectorColor -> {
                mostrarColorPicker()
            }
            R.id.galeria -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    solicitarPermisoAlmacenamiento()
                } else {
                    var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    abrirGaleria.launch(intent)
                }
            }
            R.id.guardar -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    solicitarPermisoAlmacenamiento()
                } else {
                    var layout = findViewById<ConstraintLayout>(R.id.constraint_layout_3)
                    CoroutineScope(Dispatchers.IO).launch {
                        guardarImagen(obtenerBitmap(layout))
                    }
                }
            }

        }
    }

    private fun mostrarColorPicker() {
        var ventana = AmbilWarnaDialog(this, Color.GREEN, object :
            AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {

            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                dibujarView.cambiarColorPincel(color)
            }

        })
        ventana.show()
    }

    private fun solicitarPermisoAlmacenamiento() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            var builder = AlertDialog.Builder(this)
            builder.setTitle("Permiso de Almacenamiento").setMessage("Se necesita permiso de acceso al almacenamiento").setPositiveButton("Permitir") {
                ventana, _ -> solicitarPermiso.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                ventana.dismiss()
            }
            builder.create().show()

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            var builder = AlertDialog.Builder(this)
            builder.setTitle("Permiso de Almacenamiento").setMessage("Se necesita permiso de acceso al almacenamiento").setPositiveButton("Permitir") {
                    ventana, _ -> solicitarPermiso.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                ventana.dismiss()
            }
            builder.create().show()

        } else {
            solicitarPermiso.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }


    private fun obtenerBitmap(view: View): Bitmap {
        var bm = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bm)
        view.draw(canvas)
        return bm
    }

    private suspend fun guardarImagen(bm :Bitmap) {
        var origen = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        var dir = File("$origen/img_guardadas")
        dir.mkdir()
        var generador = java.util.Random()
        var n = 10000
        n = generador.nextInt(n)
        var output = File(dir, "IMA-$n.jpg")
        if (output.exists()) {
            output.delete()
        } else {
            try {
                var outputStream = FileOutputStream(output)
                bm.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e:Exception) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "${output.absolutePath} guardada", Toast.LENGTH_LONG).show()
            }
        }

    }

}