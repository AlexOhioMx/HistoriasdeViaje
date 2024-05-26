package com.example.historiasdeviaje.ui.theme

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.historiasdeviaje.R
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PublicarHistoriaActivity : AppCompatActivity() {
    private val REQUEST_CODE = 0
    private var uriFoto: Uri? = null

    lateinit var tituloHistoria: EditText
    lateinit var descripcionHistoria: EditText
    lateinit var imagenHistoria: ImageView
    lateinit var botonSubirFotos: Button
    lateinit var botonPublicar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar_historia)

        tituloHistoria = findViewById(R.id.titulo_historia)
        descripcionHistoria = findViewById(R.id.descripcion_historia)
        imagenHistoria = findViewById(R.id.imagen_historia)
        botonSubirFotos = findViewById(R.id.boton_subir_fotos)
        botonPublicar = findViewById(R.id.boton_publicar)

        botonSubirFotos.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_CODE)
        }

        botonPublicar.setOnClickListener {
            uriFoto?.let { uri ->
                // Obtener el input stream de la Uri
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val imagenBase64 = bitmapToBase64(bitmap)

                val titulo = tituloHistoria.text.toString()
                val descripcion = descripcionHistoria.text.toString()

                // Llamada a la función para insertar en la base de datos
                insertarPublicacion(titulo, descripcion, imagenBase64)

                // Redirigir a VerHistoriaActivity DESPUÉS de insertar
                val intent = Intent(this, VerHistoriaActivity::class.java).apply {
                    putExtra("TITULO", titulo)
                    putExtra("DESCRIPCION", descripcion)
                    putExtra("URI_FOTO", uriFoto.toString())
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Por favor, selecciona una imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            uriFoto = data?.data
            Glide.with(this).load(uriFoto).into(imagenHistoria)
        }
    }

    private fun insertarPublicacion(titulo: String, descripcion: String, imagenBase64: String) {
        InsertarPublicacionTask().execute(titulo, descripcion, imagenBase64)
    }

    inner class InsertarPublicacionTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String {
            try {
                // Conexión a tu base de datos MySQL (reemplaza con tu configuración)
                val url = URL("http://192.168.0.38:80/insertar_publicacion.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.doOutput = true

                // Datos en formato JSON
                val json = JSONObject().apply {
                    put("titulo", params[0])
                    put("descripcion", params[1])
                    put("imagen", params[2])
                }

                // Envío de datos al servidor
                val os = OutputStreamWriter(conn.outputStream)
                os.write(json.toString())
                os.flush()
                os.close()

                // Obtener respuesta del servidor
                return conn.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.e("InsertarPublicacionTask", "Error: ${e.message}")
                return "Error"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result == "Success") {
                Toast.makeText(this@PublicarHistoriaActivity, "Publicación exitosa", Toast.LENGTH_SHORT).show()
                // Aquí podrías redirigir a otra actividad si lo deseas
            } else {
                Toast.makeText(this@PublicarHistoriaActivity, "Error al publicar", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
