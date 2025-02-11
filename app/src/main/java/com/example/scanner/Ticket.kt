package com.carlosmartinlaguna.scanner

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.scanner.BaseActivityClass
import com.example.scanner.SendGridEmailSender
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayInputStream
import java.io.InputStream

class Ticket : BaseActivityClass() {

    private lateinit var nombreProductoEditText: TextInputEditText
    private lateinit var nombreEmpresaEditText: TextInputEditText
    private lateinit var codigoIdentificadorEditText: TextInputEditText
    private lateinit var btnInfoIdentificador: ImageButton
    private lateinit var textTicket: TextView
    private lateinit var btnVolver: ImageButton
    private lateinit var backgroundFondo: ConstraintLayout
    private lateinit var emailEditText: TextInputEditText


    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket)

        ModoInversivo.setImmersiveMode(this)

        backgroundFondo = findViewById(R.id.fondo)
        textTicket = findViewById(R.id.textViewTicket)
        nombreProductoEditText = findViewById(R.id.textNombreProducto_editText)
        nombreEmpresaEditText = findViewById(R.id.textNombreEmpresa_editText)
        codigoIdentificadorEditText = findViewById(R.id.textCodigoIdentidicador_editText)
        btnInfoIdentificador = findViewById(R.id.btnInfoIdentificacion)
        btnVolver = findViewById(R.id.btnVolverTicket)
        emailEditText = findViewById(R.id.textEmail_editText)

        btnVolver.setOnClickListener {
            onBackPressed()
        }

        btnInfoIdentificador.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.identificador))

            // Configurar la imagen
            val imageView = ImageView(this)
            imageView.setImageResource(R.drawable.codigodebarra)
            builder.setView(imageView)


            // Configurar el botón "Aceptar" del diálogo
            builder.setPositiveButton(getString(R.string.aceptar)) { dialog, _ ->
                dialog.dismiss() // Cerrar el diálogo al pulsar el botón "Aceptar"
            }

            val dialog = builder.create()
            dialog.show()
        }


        val botonMandar: Button = findViewById(R.id.botonMandar)
        botonMandar.setOnClickListener {
            if (camposRellenos()) {
                guardarInformacion()
            } else {
                Toast.makeText(this,
                    getString(R.string.toastMensajeErrorTicketIncompleto), Toast.LENGTH_SHORT).show()
            }
        }

        val preferences = getSharedPreferences("PreferenciaDaltonico", Context.MODE_PRIVATE)

        when (preferences.getString("opcionSeleccionadaDaltonico", "")) {
            "Acromatía" -> {
                backgroundFondo.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.background_gradient_acromatia))
                textTicket.setTextColor(getColor(R.color.black))
                botonMandar.setBackgroundColor(getColor(R.color.black))
                botonMandar.setTextColor(getColor(R.color.white))
                btnInfoIdentificador.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_icono_info_negro))
                btnVolver.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_icono_flacha_back_negra))

            }
            "Normal" -> {
                backgroundFondo.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.background_gradient))
                botonMandar.setBackgroundColor(getColor(R.color.boton))
                botonMandar.setTextColor(getColor(R.color.white))
                textTicket.setTextColor(getColor(R.color.white))
                btnInfoIdentificador.setBackgroundColor(getColor(R.color.boton))
            }
            else -> {
                backgroundFondo.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.background_gradient_demas))
                botonMandar.setBackgroundColor(getColor(R.color.rojo))
                botonMandar.setTextColor(getColor(R.color.white))
                textTicket.setTextColor(getColor(R.color.white))
                btnInfoIdentificador.setBackgroundColor(getColor(R.color.rojo))
            }
        }

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
    }

    private fun camposRellenos(): Boolean {
        val nombreProducto = nombreProductoEditText.text.toString()
        val nombreEmpresa = nombreEmpresaEditText.text.toString()
        val codigoIdentificador = codigoIdentificadorEditText.text.toString()
        val email = emailEditText.text.toString()

        return nombreProducto.isNotEmpty() && nombreEmpresa.isNotEmpty() && codigoIdentificador.isNotEmpty() && email.isNotEmpty() && email.contains("@")
    }

    private fun guardarInformacion() {
        val nombreProducto = nombreProductoEditText.text.toString()
        val nombreEmpresa = nombreEmpresaEditText.text.toString()
        val codigoIdentificador = codigoIdentificadorEditText.text.toString()
        val email = emailEditText.text.toString()

        val informacion = "Nombre del Producto: $nombreProducto\n" +
                "Nombre de la Empresa: $nombreEmpresa\n" +
                "Identificador del Producto: $codigoIdentificador\n"+
                "Email: $email"

        // Crear un archivo de texto en Firebase Storage
        val storageReference = firebaseStorage.reference
        val archivoReferencia = storageReference.child("informacion_ticket_$nombreProducto"+"_$email.txt")

        val datos: ByteArray = informacion.toByteArray()
        val inputStream: InputStream = ByteArrayInputStream(datos)

        archivoReferencia.putStream(inputStream)
            .addOnSuccessListener {
                Toast.makeText(this, "Archivo creado exitosamente", Toast.LENGTH_SHORT).show()
                nombreProductoEditText.setText("")
                nombreEmpresaEditText.setText("")
                codigoIdentificadorEditText.setText("")
                enviarCorreoConSendGrid(email)

            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al crear el archivo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun enviarCorreoConSendGrid(toEmail: String) {
        val apiKey = "mlsn.0b6664c1dfc299bd1ab228e79180324d6c1d242676509511b4aec29a57fa8922" // Reemplaza con tu API Key de SendGrid
        val fromEmail = "carlosmlaguna01@gmail.com" // Correo desde el que se envía
        val subject = "Información de Ticket"
        val body = """
        Gracias por enviar su información. Hemos recibido los siguientes datos:
        
        Producto: ${nombreProductoEditText.text}
        Empresa: ${nombreEmpresaEditText.text}
        Identificador: ${codigoIdentificadorEditText.text}
        
        Si tiene alguna consulta, no dude en contactarnos.
    """.trimIndent()

        // Ejecutar el envío en un hilo separado
        Thread {
            try {
                val sendGridEmailSender = SendGridEmailSender(apiKey)
                sendGridEmailSender.sendEmail(fromEmail, toEmail, subject, body)
                runOnUiThread {
                    Toast.makeText(this, "Correo enviado exitosamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error al enviar el correo", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}

