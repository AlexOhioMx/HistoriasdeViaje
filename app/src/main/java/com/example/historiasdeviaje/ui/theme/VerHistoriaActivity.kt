package com.example.historiasdeviaje.ui.theme

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.historiasdeviaje.R

class VerHistoriaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_historia)

        val tituloHistoria = findViewById<TextView>(R.id.titulo_historia)
        val descripcionHistoria = findViewById<TextView>(R.id.descripcion_historia)
        val imagenHistoria = findViewById<ImageView>(R.id.foto_historia)

        // Aquí obtienes los datos de la historia que fueron pasados desde PublicarHistoriaActivity
        tituloHistoria.text = intent.getStringExtra("TITULO")
        descripcionHistoria.text = intent.getStringExtra("DESCRIPCION")
        val uriFoto = Uri.parse(intent.getStringExtra("URI_FOTO"))
        Glide.with(this).load(uriFoto).into(imagenHistoria)
    }
}
