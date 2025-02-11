package com.example.scanner

import android.util.Log
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import okhttp3.OkHttpClient
import okhttp3.Request as OkHttpRequest
import okhttp3.Response
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody

class SendGridEmailSender(private val apiKey: String) {

    private val sendGridUrl = "https://api.sendgrid.com/v3/mail/send"  // URL base de SendGrid API

    fun sendEmail(fromEmail: String, toEmail: String, subject: String, body: String) {
        val from = Email(fromEmail)
        val to = Email(toEmail)
        val content = Content("text/plain", body)
        val mail = Mail(from, subject, to, content)

        // Crear una instancia de OkHttpClient
        val okHttpClient = OkHttpClient()

        try {
            // Construir el cuerpo de la solicitud en JSON
            val requestBody = RequestBody.create(
                "application/json".toMediaType(),
                mail.build()
            )

            // Crear la solicitud con OkHttp
            val okHttpRequest = OkHttpRequest.Builder()
                .url(sendGridUrl)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            // Ejecutar la solicitud usando OkHttp
            val response: Response = okHttpClient.newCall(okHttpRequest).execute()

            // Log de la respuesta
            Log.d("SendGrid", "Status Code: ${response.code}")
            Log.d("SendGrid", "Response Body: ${response.body?.string()}")
            Log.d("SendGrid", "Headers: ${response.headers}")
        } catch (e: Exception) {
            Log.e("SendGrid", "Error: ${e.message}", e)
        }
    }
}
