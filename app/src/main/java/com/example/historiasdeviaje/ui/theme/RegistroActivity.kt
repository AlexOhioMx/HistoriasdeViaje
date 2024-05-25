package com.example.historiasdeviaje.ui.theme
import com.example.historiasdeviaje.R

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val nombreUsuario = findViewById<EditText>(R.id.nombre_usuario)
        val nombre = findViewById<EditText>(R.id.nombre)
        val contrasena = findViewById<EditText>(R.id.contrasena)
        val botonRegistrar = findViewById<Button>(R.id.boton_registrar)

        botonRegistrar.setOnClickListener {
            val dataToSend = JSONObject()
            dataToSend.put("NombreUsuario", nombreUsuario.text.toString())
            dataToSend.put("Nombre", nombre.text.toString())
            dataToSend.put("Contrasena", contrasena.text.toString())

            SendDataTask().execute(dataToSend.toString())
        }
    }

    inner class SendDataTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String {
            var response = ""
            try {
                val url = URL("http://192.168.1.20:80/registro_usuario.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json;charset=utf-8")
                conn.doOutput = true

                val os = OutputStreamWriter(conn.outputStream)
                os.write(params[0])
                os.flush()
                os.close()

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    response = conn.inputStream.bufferedReader().use { it.readText() }  // defaults to UTF-8
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return response
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            Log.d("RegistroActivity", "Respuesta del servidor: $result")  // Agrega un mensaje de registro
            val jsonResponse = JSONObject(result)
            if (jsonResponse["success"] == true) {
                // Si el registro fue exitoso, redirige al usuario a la actividad de inicio de sesión
                val intent = Intent(this@RegistroActivity, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Maneja el error
                Log.d("RegistroActivity", "Error al registrar usuario")  // Agrega un mensaje de registro
            }
        }
    }
}
