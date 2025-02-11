package com.example.scanner

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivityClass : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Aplicar idioma global
        val idioma = getPreferredLanguage()
        setLocale(idioma)
    }

    private fun getPreferredLanguage(): String {
        val preferences = getSharedPreferences("PreferenciasIdioma", Context.MODE_PRIVATE)
        return preferences.getString("opcionSeleccionadaIdioma", "es") ?: "es"
    }

    // Método para aplicar el idioma globalmente
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
}