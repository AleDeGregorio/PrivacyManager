package it.polito.s294545.privacymanager.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.customDataClasses.Rule
import it.polito.s294545.privacymanager.utilities.MonitorManager
import it.polito.s294545.privacymanager.utilities.PreferencesManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

//val listRules = listOf("Test rule 1", "Test rule 2", "Test rule 3")
private var savedRules = mutableListOf<Rule>()
private var activeRules = mutableListOf<Rule>()

private lateinit var context : Context
private lateinit var noRule : TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.applicationContext
        setContentView(R.layout.activity_main)

        val retrievedRules = PreferencesManager.getAllPrivacyRules(this)
        noRule = findViewById(R.id.no_rule)

        if (!retrievedRules.isNullOrEmpty()) {
            for (r in retrievedRules.keys) {
                val decodedRule = Json.decodeFromString<Rule>(retrievedRules[r].toString())

                if (decodedRule.active) {
                    activeRules.add(decodedRule)
                }
                else {
                    savedRules.add(decodedRule)
                }
            }
        }
        else {
            noRule.visibility = VISIBLE
        }

        // Managing saved rules recycler view
        if (savedRules.isEmpty() && activeRules.isNotEmpty()) {
            val containerSavedRules = findViewById<LinearLayout>(R.id.list_rules_container)
            containerSavedRules.visibility = GONE
        }

        val savedRulesRecyclerView = findViewById<RecyclerView>(R.id.list_rules)
        savedRulesRecyclerView.adapter = SavedRulesAdapter(savedRules, this)
        savedRulesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Managing active rules recycler view
        if (activeRules.isNotEmpty()) {
            val containerActiveRules = findViewById<LinearLayout>(R.id.list_active_rules_container)
            containerActiveRules.visibility = VISIBLE

            val intent = Intent(this, MonitorManager::class.java)
            intent.putExtra("activeRules", Json.encodeToString(activeRules))
            ContextCompat.startForegroundService(this, intent)
        }
        // If no active rules, stop monitoring service
        else {
            val intent = Intent(this, MonitorManager::class.java)
            stopService(intent)
        }

        val activeRulesRecyclerView = findViewById<RecyclerView>(R.id.list_active_rules)
        activeRulesRecyclerView.adapter = ActiveRulesAdapter(activeRules, this, resources)
        activeRulesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Manage insert new rule button
        val newRuleButton = findViewById<FloatingActionButton>(R.id.new_rule)
        newRuleButton.setOnClickListener { v -> showPopupNewRule(v) }
    }

    override fun onDestroy() {
        super.onDestroy()

        savedRules.clear()
        activeRules.clear()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPopupNewRule(view: View) {
        val (popupView, popupWindow) = managePopup(view, R.layout.popup_new_rule)

        // Initialize the elements of our window, install the handler
        val buttonCreate = popupView.findViewById<Button>(R.id.create_button)
        buttonCreate.setOnClickListener {
            // Create new rule
            val intent = Intent(this, PermissionsSelectionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
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

// Define recycler view for saved rules
class SavedRulesViewHolder(v: View) : RecyclerView.ViewHolder(v){
    val buttonRule = v.findViewById<Button>(R.id.button_rule)
    val editRuleButton = v.findViewById<FloatingActionButton>(R.id.edit_rule)
    val deleteRuleButton = v.findViewById<FloatingActionButton>(R.id.delete_rule)
}

class SavedRulesAdapter(private val listRules: MutableList<Rule>, context: Context): RecyclerView.Adapter<SavedRulesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedRulesViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.box_saved_rule, parent, false)

        return SavedRulesViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listRules.size
    }

    override fun onBindViewHolder(holder: SavedRulesViewHolder, position: Int) {
        val rule = listRules[position]
        val ruleName = rule.name

        holder.buttonRule.text = ruleName

        // Manage saved rule
        holder.buttonRule.setOnClickListener {
            val intent = Intent(context, SavedRuleActivity::class.java)
            intent.putExtra("rule", Json.encodeToString(rule))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        // Manage edit rule
        holder.editRuleButton.setOnClickListener {
            val intent = Intent(context, PermissionsSelectionActivity::class.java)
            intent.putExtra("rule", Json.encodeToString(rule))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        // Manage delete inserted rule button
        holder.deleteRuleButton.setOnClickListener { v -> showPopupDeleteRule(v, ruleName!!, holder) }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPopupDeleteRule(view: View, ruleName: String, holder: SavedRulesViewHolder) {
        val (popupView, popupWindow) = managePopup(view, R.layout.popup_delete_rule)

        val deleteInfo = popupView.findViewById<TextView>(R.id.delete_info)
        deleteInfo.text = "Desideri cancellare \"$ruleName\"?"

        // Initialize the elements of our window, install the handler
        val buttonConfirm = popupView.findViewById<Button>(R.id.confirm_delete_button)
        buttonConfirm.setOnClickListener {
            // Delete rule
            popupWindow.dismiss()
            savedRules.removeAt(holder.adapterPosition)
            PreferencesManager.deletePrivacyRule(context, ruleName)

            // Show "no saved rule" message if list is empty
            if (savedRules.isEmpty()) {
                noRule.visibility = VISIBLE
            }

            notifyItemRemoved(holder.adapterPosition)
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
}

// Define recycler view for active rules
class ActiveRulesViewHolder(v: View) : RecyclerView.ViewHolder(v){
    val buttonRule = v.findViewById<Button>(R.id.button_rule)
    val editRuleButton = v.findViewById<FloatingActionButton>(R.id.edit_rule)
    val deleteRuleButton = v.findViewById<FloatingActionButton>(R.id.delete_rule)
}

class ActiveRulesAdapter(private val listRules: MutableList<Rule>, context: Context, private val resources: Resources): RecyclerView.Adapter<ActiveRulesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveRulesViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.box_saved_rule, parent, false)

        return ActiveRulesViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listRules.size
    }

    override fun onBindViewHolder(holder: ActiveRulesViewHolder, position: Int) {
        val rule = listRules[position]
        val ruleName = rule.name

        holder.buttonRule.text = ruleName
        holder.buttonRule.setBackgroundColor(resources.getColor(R.color.primary))

        holder.editRuleButton.visibility = GONE
        holder.deleteRuleButton.visibility = GONE

        // Manage saved rule
        holder.buttonRule.setOnClickListener {
            val intent = Intent(context, SavedRuleActivity::class.java)
            intent.putExtra("rule", Json.encodeToString(rule))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}