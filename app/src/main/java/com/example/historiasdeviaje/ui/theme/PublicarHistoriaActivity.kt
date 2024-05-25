package com.example.historiasdeviaje.ui.theme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.historiasdeviaje.R

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
            val intent = Intent(this, VerHistoriaActivity::class.java).apply {
                putExtra("TITULO", tituloHistoria.text.toString())
                putExtra("DESCRIPCION", descripcionHistoria.text.toString())
                putExtra("URI_FOTO", uriFoto.toString())
            }
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            uriFoto = data?.data
            Glide.with(this).load(uriFoto).into(imagenHistoria)
        }
    }
}
