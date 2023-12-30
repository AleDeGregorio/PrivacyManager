package it.polito.s294545.privacymanager.utilities

import android.content.Context
import it.polito.s294545.privacymanager.customDataClasses.Rule

object PreferencesManager {

    private const val PREFERENCES_NAME = "SavedRules"

    fun saveTutorialShown(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("tutorialShown", true)
        editor.apply()
    }

    fun getTutorialShown(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("tutorialShown", false)
    }

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

    fun getAllPrivacyRules(context: Context) : MutableMap<String, *>? {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.all
    }

    fun deletePrivacyRule(context: Context, name: String) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(name)
        editor.apply()
    }

    fun ruleNameAlreadyExists(context: Context, name: String) : Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.contains(name)
    }
}