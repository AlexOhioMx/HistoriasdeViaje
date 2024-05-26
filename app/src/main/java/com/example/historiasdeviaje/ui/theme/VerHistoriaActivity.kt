package com.example.historiasdeviaje.ui.theme

import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.historiasdeviaje.R
import org.json.JSONArray
import org.json.JSONException
import java.net.URL

// Clase de datos para representar una historia
data class Historia(
    val historiaId: Int,
    val titulo: String,
    val descripcion: String,
    val imagenBase64: String
)

class VerHistoriaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoriaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_historia)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoriaAdapter(emptyList()) // Inicializa con una lista vacía
        recyclerView.adapter = adapter

        ObtenerHistoriasTask().execute()
    }

    inner class ObtenerHistoriasTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val urlString = "http://192.168.1.20:80/obtener_historia.php" // Reemplaza con tu IP
            return try {
                URL(urlString).readText()
            } catch (e: Exception) {
                Log.e("ObtenerHistoriasTask", "Error: ${e.message}")
                "" // Devuelve una cadena vacía en caso de error
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                if (!result.isNullOrEmpty()) {
                    val jsonArray = JSONArray(result)
                    val historias = ArrayList<Historia>()
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val historia = Historia(
                            jsonObject.getInt("HistoriaID"),
                            jsonObject.getString("Titulo"),
                            jsonObject.getString("Descripcion"),
                            jsonObject.getString("Imagen")
                        )
                        historias.add(historia)
                    }
                    adapter.updateData(historias)
                } else {
                    Toast.makeText(this@VerHistoriaActivity, "No hay historias disponibles", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                Log.e("ObtenerHistoriasTask", "Error al analizar JSON: ${e.message}")
                Toast.makeText(this@VerHistoriaActivity, "Error al obtener las historias", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Adaptador para el RecyclerView
class HistoriaAdapter(private var historias: List<Historia>) : RecyclerView.Adapter<HistoriaAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tituloTextView: TextView = itemView.findViewById(R.id.tituloTextView)
        val descripcionTextView: TextView = itemView.findViewById(R.id.descripcionTextView)
        val imagenImageView: ImageView = itemView.findViewById(R.id.imagenImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_historia, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historia = historias[position]
        holder.tituloTextView.text = historia.titulo
        holder.descripcionTextView.text = historia.descripcion

        // Carga la imagen desde el Base64
        val decodedString = Base64.decode(historia.imagenBase64, Base64.DEFAULT)
        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        Glide.with(holder.itemView.context).load(decodedByte).into(holder.imagenImageView)
    }

    override fun getItemCount(): Int {
        return historias.size
    }

    // Función para actualizar los datos del adaptador
    fun updateData(newHistorias: List<Historia>) {
        historias = newHistorias
        notifyDataSetChanged()
    }
}
