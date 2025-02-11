package com.carlosmartinlaguna.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.carlosmartinlaguna.scanner.databinding.ActivityBuscarProductosBinding
import com.example.scanner.BaseActivityClass
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import java.util.*

class BuscarProductos : BaseActivityClass() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityBuscarProductosBinding
    private val db = FirebaseFirestore.getInstance()
    private var versionAuxAplicacion = 0
    private var versionAuxBaseDatos = 0
    private lateinit var preferencesDaltonico: SharedPreferences
    private lateinit var preferencesIdioma: SharedPreferences

    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperar preferencias del usuario
        preferencesDaltonico = getSharedPreferences("PreferenciaDaltonico", Context.MODE_PRIVATE)
        // Recuperar preferencias del idioma
        preferencesIdioma = getSharedPreferences("PreferenciasIdioma", Context.MODE_PRIVATE)
        val idioma = preferencesIdioma.getString("opcionSeleccionadaIdioma", "es") ?: "es"
        setLocale(idioma)

        ModoInversivo.setImmersiveMode(this)
        binding = ActivityBuscarProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("preferenciasVersiones", MODE_PRIVATE)
        versionAuxAplicacion = sharedPreferences.getInt("versionAuxAplicacion", 0)
        versionAuxBaseDatos = sharedPreferences.getInt("versionAuxBaseDatos", 0)

        // Verificar versión actualizada aplicacion
        val versionCollectionAplicacion = db.collection("Version")
        versionCollectionAplicacion.document("Version")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val version = document.getLong("VersionAplicacion")?.toInt()
                    if (version != null && version > versionAuxAplicacion) {
                        versionAuxAplicacion = version
                        with(sharedPreferences.edit()) {
                            putInt("versionAuxAplicacion", versionAuxAplicacion)
                            apply()
                        }
                        showCustomDialogVersion()
                    }
                }
            }
        // Verificar versión actualizada base datos
        val versionCollectionBaseDatos = db.collection("Version")
        versionCollectionBaseDatos.document("Version")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val version = document.getLong("VersionBaseDatos")?.toInt()
                    if (version != null && version > versionAuxBaseDatos) {
                        versionAuxBaseDatos = version
                        with(sharedPreferences.edit()) {
                            putInt("versionAuxBaseDatos", versionAuxBaseDatos)
                            apply()
                        }
                        showCustomDialogVersionBaseDato()
                    }
                }
            }

        // Configurar el botón de información de productos genéricos
        binding.btnInfoProductosGenericos.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val customTitle = TextView(this)
            customTitle.text = getString(R.string.productosGenericosTitulo)
            customTitle.textSize = 20f
            customTitle.setPadding(40, 20, 40, 20)
            customTitle.gravity = Gravity.CENTER

            val mensaje = getString(R.string.productosGenericos)
            val textView = TextView(this)
            textView.text = mensaje
            textView.textSize = 20F
            textView.setPadding(40, 20, 40, 20)
            textView.gravity = Gravity.CENTER

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.addView(customTitle)
            layout.addView(textView)

            when (preferencesDaltonico.getString("opcionSeleccionadaDaltonico", "")) {
                "Acromatía" -> {
                    layout.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.background_gradient_acromatia
                        )
                    ) // Cambiar a color si es necesario
                    customTitle.setTextColor(ContextCompat.getColor(this, R.color.black))
                    textView.setTextColor(ContextCompat.getColor(this, R.color.black))
                }

                "Normal" -> {
                    layout.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.background_gradient
                        )
                    ) // Cambiar a color si es necesario
                    customTitle.setTextColor(ContextCompat.getColor(this, R.color.white))
                    textView.setTextColor(ContextCompat.getColor(this, R.color.white))
                }

                else -> {
                    layout.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.background_gradient_demas
                        )
                    ) // Cambiar a color si es necesario
                    customTitle.setTextColor(ContextCompat.getColor(this, R.color.white))
                    textView.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
            }

            builder.setView(layout)
            builder.setCancelable(true)
            val dialog = builder.create()
            dialog.show()

        }

        // Configurar el botón de escaneo
        binding.btnScanner.setOnClickListener { initScanner() }

        // Aplicar colores según las preferencias de daltonismo
        when (preferencesDaltonico.getString("opcionSeleccionadaDaltonico", "")) {
            "Acromatía" -> {
                binding.root.setBackgroundResource(R.drawable.background_gradient_acromatia)
                binding.btnScanner.setBackgroundColor(getColor(R.color.black))
                binding.btnInfoProductosGenericos.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_icono_info_negro
                    )
                )
                binding.menu.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_icono_menu_desp_negro
                    )
                )
            }

            "Normal" -> {
                binding.root.setBackgroundResource(R.drawable.background_gradient)
                binding.btnScanner.setBackgroundColor(getColor(R.color.boton))
            }

            else -> {
                binding.root.setBackgroundResource(R.drawable.background_gradient_demas)
                binding.btnScanner.setBackgroundColor(getColor(R.color.beige))
            }
        }

        // Configurar menú del botón de opciones
        val btnMenu: ImageButton = findViewById(R.id.menu)
        btnMenu.setOnClickListener { showPopupMenu(it) }
    }

    private fun showPopupMenu(anchorView: android.view.View) {
        val popupMenu = PopupMenu(ContextThemeWrapper(this, R.style.MyPopupMenuStyle), anchorView)
        popupMenu.inflate(R.menu.menu_principal)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.opcionDaltonico -> {
                    showDaltonicoDialog()
                    true
                }

                R.id.opcionIdioma -> {
                    showIdiomaDialog()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    // Mostrar el diálogo para seleccionar el modo daltonico
    private fun showDaltonicoDialog() {
        val opcionesDaltonico = resources.getStringArray(R.array.opciones_daltonico).toList()
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_layout, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        title.text = getString(R.string.tituloDialgoDaltonico)

        val listView = dialogView.findViewById<ListView>(R.id.dialogList)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, opcionesDaltonico)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, which, _ ->
            val selectedOption = opcionesDaltonico[which]
            val editor: SharedPreferences.Editor = preferencesDaltonico.edit()
            editor.putString("opcionSeleccionadaDaltonico", selectedOption)
            editor.apply()
            recreate()
            dialog.dismiss()
        }
        val preferencesDaltonico =
            getSharedPreferences("PreferenciaDaltonico", Context.MODE_PRIVATE)
        when (preferencesDaltonico.getString("opcionSeleccionadaDaltonico", "")) {
            "Acromatía" -> {
                // Fondo y colores para Acromatía
                dialogView.background =
                    ContextCompat.getDrawable(this, R.drawable.background_gradient_acromatia)
                title.setTextColor(ContextCompat.getColor(this, R.color.black)) // Título en negro
                listView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.transparent
                    )
                ) // ListView transparente
            }

            "Normal" -> {
                // Fondo y colores para Normal
                dialogView.background =
                    ContextCompat.getDrawable(this, R.drawable.background_gradient)
                title.setTextColor(ContextCompat.getColor(this, R.color.white)) // Título en blanco
                listView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.transparent
                    )
                ) // ListView transparente
            }

            else -> {
                // Fondo y colores para los demás casos (cuando es "Demás")
                dialogView.background =
                    ContextCompat.getDrawable(this, R.drawable.background_gradient_demas)
                title.setTextColor(ContextCompat.getColor(this, R.color.white)) // Título en blanco
                listView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.transparent
                    )
                ) // ListView transparente

                // Cambiar color del texto en el ListView en caso de "Demás"
                val textColor = ContextCompat.getColor(
                    this,
                    R.color.rojo
                ) // Aquí puedes poner el color que desees
                listView.setAdapter(object : ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    opcionesDaltonico
                ) {
                    override fun getView(
                        position: Int,
                        convertView: android.view.View?,
                        parent: android.view.ViewGroup
                    ): android.view.View {
                        val view = super.getView(position, convertView, parent)
                        val textView = view.findViewById<TextView>(android.R.id.text1)
                        textView.setTextColor(textColor) // Cambia el color del texto
                        return view
                    }
                })
            }
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun showIdiomaDialog() {
        val opcionesIdioma = arrayListOf("Español", "English", "Français", "Deutsch")
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_layout, null)
        builder.setView(dialogView)

        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        title.text = getString(R.string.selecciona_una_opcion)

        val listView = dialogView.findViewById<ListView>(R.id.dialogList)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, opcionesIdioma)
        listView.adapter = adapter

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        listView.setOnItemClickListener { _, _, which, _ ->
            val selectedOption = when (which) {
                0 -> "es"
                1 -> "en"
                2 -> "fr"
                3 -> "de"
                else -> "es"
            }

            val editor: SharedPreferences.Editor = preferencesIdioma.edit()
            editor.putString("opcionSeleccionadaIdioma", selectedOption)
            editor.apply()

            // Aplicar el cambio de idioma global
            setLocale(selectedOption)

            // Reiniciar la actividad actual para aplicar el idioma
            val intent = Intent(this, this::class.java)
            finish()  // Cerrar la actividad actual
            startActivity(intent)  // Abrir la actividad nuevamente
        }
    }




    private fun setLocale(languageCode: String) {
        val currentLanguage = resources.configuration.locales[0].language
        if (currentLanguage != languageCode) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }



    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt(getString(R.string.mensajeScanner))
        integrator.setCameraId(0)  // Usar la cámara trasera
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show()
            } else {
                searchProduct(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    private fun searchProduct(content: String) {
        db.collection("Productos").document(content)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombreEmpresa = document.getString("NombreEmpresa")
                    val nombreProducto = document.getString("Nombre")
                    val mensaje = getString(R.string.mensaje, nombreProducto, nombreEmpresa, getString(R.string.mensajeOk))

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.producto_encontrado))
                    builder.setMessage(mensaje)
                    val dialog = builder.create()
                    dialog.show()

                    val titleView = dialog.findViewById<TextView>(android.R.id.title)
                    val messageView = dialog.findViewById<TextView>(android.R.id.message)

                    when (preferencesDaltonico.getString("opcionSeleccionadaDaltonico", "")) {
                        "Acromatía" -> {
                            dialog.window?.setBackgroundDrawableResource(R.drawable.background_gradient_acromatia)
                            titleView?.setTextColor(ContextCompat.getColor(this, R.color.black))
                            titleView?.textSize = 20f
                            messageView?.setTextColor(ContextCompat.getColor(this, R.color.black))
                            messageView?.textSize = 18f
                        }

                        "Normal" -> {
                            dialog.window?.setBackgroundDrawableResource(R.drawable.background_gradient)
                            titleView?.setTextColor(ContextCompat.getColor(this, R.color.white))
                            titleView?.textSize = 20f
                            messageView?.setTextColor(ContextCompat.getColor(this, R.color.white))
                            messageView?.textSize = 18f
                        }

                        else -> {
                            dialog.window?.setBackgroundDrawableResource(R.drawable.background_gradient_demas)
                            titleView?.setTextColor(ContextCompat.getColor(this, R.color.white))
                            titleView?.textSize = 20f
                            messageView?.setTextColor(ContextCompat.getColor(this, R.color.white))
                            messageView?.textSize = 18f
                        }
                    }

                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.tituloDialogProductoNoEncontrado))
                    builder.setMessage(getString(R.string.mensajeProductoNoEncontrado))
                    builder.setPositiveButton(getString(R.string.crearTicket)) { dialog, _ ->
                        // Acción a realizar al pulsar el botón "Crear Ticket"
                        val intent = Intent(this, Ticket::class.java)
                        this.startActivity(intent)
                        dialog.dismiss() // Cerrar el diálogo
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al buscar el producto", Toast.LENGTH_LONG).show()
            }
    }

    private fun showCustomDialogVersion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.tituloNuevaVersion))
        builder.setMessage(getString(R.string.mensajeNuevaVersion))
        builder.setPositiveButton(getString(R.string.actualizar)) { _, _ ->
            // Acción de actualización
        }
        builder.setNegativeButton(getString(R.string.cancelar)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showCustomDialogVersionBaseDato() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.tituloNuevaVersionBD))
        builder.setMessage(getString(R.string.mensajeNuevaVersionBD))
        builder.show()
    }

    override fun onResume() {
        super.onResume()

        // Asegúrate de cargar las preferencias de idioma cada vez que la actividad regresa al primer plano
        val idioma = preferencesIdioma.getString("opcionSeleccionadaIdioma", "es") ?: "es"
        setLocale(idioma)  // Aplica el idioma guardado en las preferencias
    }


}



