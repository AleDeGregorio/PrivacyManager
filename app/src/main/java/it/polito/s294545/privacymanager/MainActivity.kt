package it.polito.s294545.privacymanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Manage select saved rule
        val ruleButton = findViewById<Button>(R.id.button_rule)
        ruleButton.setOnClickListener {
            val intent = Intent(this, SavedRuleActivity::class.java)
            startActivity(intent)
        }

        // Manage insert new rule button
        val newRuleButton = findViewById<FloatingActionButton>(R.id.new_rule)
        newRuleButton.setOnClickListener { v -> showPopupNewRule(v) }

        // Manage delete inserted rule button
        val deleteRuleButton = findViewById<FloatingActionButton>(R.id.delete_rule)
        deleteRuleButton.setOnClickListener { v -> showPopupDeleteRule(v) }
    }
}

fun managePopup(view: View, layout: Int) : Pair<View, PopupWindow> {
    // Create a View object yourself through inflater
    val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val popupView: View = inflater.inflate(layout, null)

    // Specify the length and width through constants
    val width = LinearLayout.LayoutParams.MATCH_PARENT
    val height = LinearLayout.LayoutParams.MATCH_PARENT

    // Make Inactive Items Outside Of PopupWindow
    val focusable = true

    // Create a window with our parameters
    val popupWindow = PopupWindow(popupView, width, height, focusable)

    // Set the location of the window on the screen
    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

    return Pair(popupView, popupWindow)
}

@SuppressLint("ClickableViewAccessibility")
fun showPopupNewRule(view: View) {
    val (popupView, popupWindow) = managePopup(view, R.layout.popup_new_rule)

    // Initialize the elements of our window, install the handler
    val buttonCreate = popupView.findViewById<Button>(R.id.create_button)
    buttonCreate.setOnClickListener {
        // Create new rule
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

@SuppressLint("ClickableViewAccessibility")
fun showPopupDeleteRule(view: View) {
    val (popupView, popupWindow) = managePopup(view, R.layout.popup_delete_rule)

    // Initialize the elements of our window, install the handler
    val buttonConfirm = popupView.findViewById<Button>(R.id.confirm_delete_button)
    buttonConfirm.setOnClickListener {
        // Delete rule
    }

    val buttonCancel = popupView.findViewById<Button>(R.id.cancel_delete_button)
    buttonCancel.setOnClickListener {
        popupWindow.dismiss()
    }


    // Handler for clicking on the inactive zone of the window
    popupView.setOnTouchListener { v, event -> // Close the window when clicked
        popupWindow.dismiss()
        true
    }
}