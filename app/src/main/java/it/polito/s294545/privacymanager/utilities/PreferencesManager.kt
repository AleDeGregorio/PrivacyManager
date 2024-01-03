package it.polito.s294545.privacymanager.utilities

import android.content.Context

object PreferencesManager {

    private const val RULES_PREFERENCES = "SavedRules"
    private const val TUTORIALS_PREFERENCES = "Tutorials"
    private const val USER_LOGGED = "Login"

    fun deleteAll(context: Context) {
        val sharedPreferencesRules = context.getSharedPreferences(RULES_PREFERENCES, Context.MODE_PRIVATE)

        var editor = sharedPreferencesRules.edit()
        editor.clear()
        editor.apply()

        val sharedPreferencesTutorials = context.getSharedPreferences(TUTORIALS_PREFERENCES, Context.MODE_PRIVATE)

        editor = sharedPreferencesTutorials.edit()
        editor.clear()
        editor.apply()

        val sharedPreferencesLogin = context.getSharedPreferences(USER_LOGGED, Context.MODE_PRIVATE)

        editor = sharedPreferencesLogin.edit()
        editor.clear()
        editor.apply()
    }

    fun saveUserLogged(context: Context) {
        val sharedPreferences = context.getSharedPreferences(USER_LOGGED, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("userLogged", true)
        editor.apply()
    }

    fun getUserLogged(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(USER_LOGGED, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("userLogged", false)
    }

    fun saveAskPermissionsTutorialShown(context: Context) {
        val sharedPreferences = context.getSharedPreferences(TUTORIALS_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("askPermissionsTutorialShown", true)
        editor.apply()
    }

    fun getAskPermissionsTutorialShown(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(TUTORIALS_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("askPermissionsTutorialShown", false)
    }

    fun saveHomepageTutorialShown(context: Context) {
        val sharedPreferences = context.getSharedPreferences(TUTORIALS_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("homepageTutorialShown", true)
        editor.apply()
    }

    fun getHomepageTutorialShown(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(TUTORIALS_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("homepageTutorialShown", false)
    }

    fun saveRuleCreationTutorialShown(context: Context) {
        val sharedPreferences = context.getSharedPreferences(TUTORIALS_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("ruleCreationTutorialShown", true)
        editor.apply()
    }

    fun getRuleCreationTutorialShown(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(TUTORIALS_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("ruleCreationTutorialShown", false)
    }


    fun savePrivacyRule(context: Context, name: String, jsonPrivacyRule: String) {
        val sharedPreferences = context.getSharedPreferences(RULES_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(name, jsonPrivacyRule)
        editor.apply()
    }

    fun getPrivacyRule(context: Context, name: String): String {
        val sharedPreferences = context.getSharedPreferences(RULES_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.getString(name, "") ?: ""
    }

    fun getAllPrivacyRules(context: Context) : Map<String, Any?>? {
        val sharedPreferences = context.getSharedPreferences(RULES_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.all
    }

    fun deletePrivacyRule(context: Context, name: String) {
        val sharedPreferences = context.getSharedPreferences(RULES_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(name)
        editor.apply()
    }

    fun ruleNameAlreadyExists(context: Context, name: String) : Boolean {
        val sharedPreferences = context.getSharedPreferences(RULES_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.contains(name)
    }
}