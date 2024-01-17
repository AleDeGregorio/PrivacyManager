package it.polito.s294545.privacymanager.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.developer.gbuttons.GoogleSignInButton
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.utilities.PreferencesManager

class SignInActivity : AppCompatActivity() {

    private lateinit var loginLauncher: ActivityResultLauncher<Intent>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        PreferencesManager.deleteAll(this)

        loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                val response = IdpResponse.fromResultIntent(data)
                val user = FirebaseAuth.getInstance().currentUser

                if (user != null) {
                    if (response?.providerType.equals("google.com")) {
                        checkGoogleAccount(user)
                    }
                }
                else {
                    launchSignInFlow()
                }
            }
        }

        val signInButton = findViewById<GoogleSignInButton>(R.id.sign_in_button)

        signInButton.setOnClickListener { launchSignInFlow() }
    }

    private fun launchSignInFlow() {
        val providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build())

        val authUiLayout = AuthMethodPickerLayout
            .Builder(R.layout.activity_sign_in)
            .setGoogleButtonId(R.id.sign_in_button)
            .build()

        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setAuthMethodPickerLayout(authUiLayout)
            .setLogo(R.drawable.icon_logo)
            .setTheme(R.style.Theme_PrivacyManager)
            .build()

        loginLauncher.launch(intent)
    }

    private fun checkGoogleAccount(user: FirebaseUser) {

        val userRef = db.collection("users").document(user.uid)

        val intent = Intent(this, AskPermissionsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        userRef.get().addOnCompleteListener {task ->
            if (task.isSuccessful) {
                // User already signed in, go to main application
                if (task.result.exists()) {
                    PreferencesManager.saveUserLogged(this)
                    PreferencesManager.saveUserID(this, user.uid)
                    startActivity(intent)
                }
                // User not signed in, save data in DB and in shared preferences and then go to main application
                else {
                    val userData = hashMapOf(
                        "timestampSignIn" to FieldValue.serverTimestamp(),
                        "tutorialsOpened" to 0
                    )

                    userRef.set(userData)
                        .addOnSuccessListener {
                            PreferencesManager.saveUserLogged(this)
                            PreferencesManager.saveUserID(this, user.uid)
                            startActivity(intent)
                        }
                }
            }
            // Error during data retrieving
            else {
                Toast.makeText(this, "Si è verificato un errore: ${task.exception}", Toast.LENGTH_LONG).show()
            }
        }
    }
}