package it.polito.s294545.privacymanager.utilities

import android.content.Context

object PreferencesManager {

    private const val PREFERENCES_NAME = "SavedRules"

    fun savePrivacyRule(context: Context, name: String, jsonPrivacyRule: String) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(name, jsonPrivacyRule)
        editor.apply()
    }

    fun getPrivacyRule(context: Context, name: String): String {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(name, "") ?: ""
    }
}