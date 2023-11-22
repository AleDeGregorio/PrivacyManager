package it.polito.s294545.privacymanager

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SavedRuleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_rule)

        // Get toolbar
        val toolbarLayout = findViewById<ConstraintLayout>(R.id.toolbarLayout)
        val toolbar = toolbarLayout.findViewById<MaterialToolbar>(R.id.toolbar)

        // Set the navigation icon
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.icon_left_arrow)
        toolbar.setNavigationIconTint(resources.getColor(R.color.white))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Set rule text in card
        val ruleText = findViewById<TextView>(R.id.rule_text)
        ruleText.text = Html.fromHtml(
            "Le <i>app</i> <b>WhatsApp</b> e <b>Maps</b> con <i>autorizzazioni</i> <b>Localizzazione</b> e <b>Fotocamera</b> " +
            "i <i>giorni</i> <b>Lun-Ven 08:00-17:00</b> e <b>Sabato (tutto il giorno)</b> e con le condizioni di <i>rete</i> <b>Connessione dati</b>, " +
            "<i>bluetooth</i> <b>Smartwatch</b> e <i>batteria</i> <<b>50%</b> subiscono l'<i>azione</i> <b>Chiudi applicazione</b>."
        )

        // Manage start rule
        val startRuleButton = findViewById<FloatingActionButton>(R.id.start_rule_button)
        startRuleButton.setOnClickListener { v -> showPopupStartRule(v) }
    }
}

@SuppressLint("ClickableViewAccessibility")
fun showPopupStartRule(view: View) {
    val (popupView, popupWindow) = managePopup(view, R.layout.popup_start_rule)

    // Initialize the elements of our window, install the handler
    val buttonStartRule = popupView.findViewById<Button>(R.id.start_rule_button)
    buttonStartRule.setOnClickListener {
        // Start rule
    }

    val buttonCancel = popupView.findViewById<Button>(R.id.cancel_button)
    buttonCancel.setOnClickListener {
        popupWindow.dismiss()
    }


    // Handler for clicking on the inactive zone of the window
    popupView.setOnTouchListener { v, event -> // Close the window when clicked
        popupWindow.dismiss()
        true
    }
}