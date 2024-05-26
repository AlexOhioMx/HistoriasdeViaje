package com.example.historiasdeviaje.ui.theme

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.historiasdeviaje.R
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PublicarHistoriaActivity : AppCompatActivity(), LocationListener {
    private lateinit var locationManager: LocationManager // Variable para la ubicación
    private val REQUEST_CODE = 0
    private var uriFoto: Uri? = null

    private val locationPermissionCode = 2 // Variable para los permisos de la ubicación

    lateinit var tituloHistoria: EditText
    lateinit var descripcionHistoria: EditText
    lateinit var imagenHistoria: ImageView
    lateinit var botonSubirFotos: Button
    lateinit var botonPublicar: Button
    lateinit var botonTomarFoto: Button

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                abrirCamara()
            } else {
                Toast.makeText(this, "Se necesita el permiso de la cámara", Toast.LENGTH_SHORT).show()
            }
        }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            if (data != null && data.extras != null) {
                val imageBitmap = data.extras!!.get("data") as Bitmap
                uriFoto = getImageUriFromBitmap(imageBitmap)
                Glide.with(this).load(uriFoto).into(imagenHistoria)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar_historia)

        tituloHistoria = findViewById(R.id.titulo_historia)
        descripcionHistoria = findViewById(R.id.descripcion_historia)
        imagenHistoria = findViewById(R.id.imagen_historia)
        botonSubirFotos = findViewById(R.id.boton_subir_fotos)
        botonPublicar = findViewById(R.id.boton_publicar)
        botonTomarFoto = findViewById(R.id.boton_tomar_foto)

        botonSubirFotos.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_CODE)
        }

        botonTomarFoto.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        botonPublicar.setOnClickListener {
            if (uriFoto != null) {
                val inputStream = contentResolver.openInputStream(uriFoto!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val imagenBase64 = bitmapToBase64(bitmap)

                val titulo = tituloHistoria.text.toString()
                val descripcion = descripcionHistoria.text.toString()

                insertarPublicacion(titulo, descripcion, imagenBase64)
            } else {
                Toast.makeText(this, "Por favor, selecciona una imagen", Toast.LENGTH_SHORT).show()
            }
        }

        // Solicitar la ubicación
        getLocation()
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            uriFoto = data?.data
            Glide.with(this).load(uriFoto).into(imagenHistoria)
        }
    }

    private fun insertarPublicacion(titulo: String, descripcion: String, imagenBase64: String) {
        InsertarPublicacionTask().execute(
            JSONObject().apply {
                put("titulo", titulo)
                put("descripcion", descripcion)
                put("imagen", imagenBase64)
                put("latitud", currentLatitude) // Agrega latitud
                put("longitud", currentLongitude) // Agrega longitud
            }.toString()
        )
    }

    inner class InsertarPublicacionTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String {
            var response = ""
            try {
                val url = URL("http://192.168.0.12:80/insertar_publicacion.php") // Reemplaza con tu IP
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.doOutput = true

                // Enviar el JSON al servidor
                val os = OutputStreamWriter(conn.outputStream)
                os.write(params[0]) // Enviar el JSON completo
                os.flush()
                os.close()

                // Lee la respuesta del servidor
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    response = conn.inputStream.bufferedReader().use { it.readText() }
                }
            } catch (e: Exception) {
                Log.e("InsertarPublicacionTask", "Error: ${e.message}")
                return "Error: ${e.message}" // Devuelve el mensaje de error completo
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.d("InsertarPublicacionTask", "Respuesta del servidor: $result")
            val jsonResponse = JSONObject(result)
            if (jsonResponse.optString("status") == "Success") {
                // Publicación exitosa, redirige a VerHistoriaActivity
                val intent = Intent(this@PublicarHistoriaActivity, VerHistoriaActivity::class.java).apply {
                    putExtra("titulo", tituloHistoria.text.toString())
                    putExtra("descripcion", descripcionHistoria.text.toString())
                    putExtra("imagenUri", uriFoto.toString())
                    putExtra("latitud", jsonResponse.optDouble("latitud"))
                    putExtra("longitud", jsonResponse.optDouble("longitud"))
                }
                startActivity(intent)
            } else {
                // Hubo un error, muestra el mensaje en un Toast
                val errorMessage = jsonResponse.optString("message", "Error desconocido")
                Toast.makeText(this@PublicarHistoriaActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    // Variables para almacenar la latitud y longitud actuales
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    // Método para obtener la ubicación
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                5f, this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                    5f, this)
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
    }
}
