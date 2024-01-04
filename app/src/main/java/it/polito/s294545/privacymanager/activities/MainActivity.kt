package it.polito.s294545.privacymanager.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.customDataClasses.Rule
import it.polito.s294545.privacymanager.utilities.MonitorManager
import it.polito.s294545.privacymanager.utilities.PreferencesManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import uk.co.deanwild.materialshowcaseview.ShowcaseTooltip
import java.time.Duration
import java.time.LocalDateTime

//val listRules = listOf("Test rule 1", "Test rule 2", "Test rule 3")
private var savedRules = mutableListOf<Rule>()
private var activeRules = mutableListOf<Rule>()

private lateinit var context : Context
private lateinit var noRule : TextView
private lateinit var noActiveRule : TextView

private val db = FirebaseFirestore.getInstance()

class MainActivity : AppCompatActivity() {

    private val TUTORIAL_ID = "Tutorial homepage"

    private lateinit var title: TextView
    private lateinit var listSavedRules: LinearLayout
    private lateinit var listActiveRules: LinearLayout
    private lateinit var savedRulesRecyclerView: RecyclerView
    private lateinit var activeRulesRecyclerView: RecyclerView
    private lateinit var newRuleButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.applicationContext
        setContentView(R.layout.activity_main)

        title = findViewById(R.id.saved_rules)
        listSavedRules = findViewById(R.id.list_rules_container)
        listActiveRules = findViewById(R.id.list_active_rules_container)

        // Get toolbar
        val toolbarLayout = findViewById<ConstraintLayout>(R.id.toolbarLayout)
        val toolbar = toolbarLayout.findViewById<MaterialToolbar>(R.id.toolbar)

        // Set the navigation icon
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.icon_settings)
        toolbar.setNavigationIconTint(resources.getColor(R.color.white))

        // Set the help icon
        toolbar.inflateMenu(R.menu.toolbar_menu)
        toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.icon_help)

        toolbar.setOnMenuItemClickListener {
            val userID = PreferencesManager.getUserID(this)

            val userRef = db.collection("users").document(userID)

            userRef
                .update("tutorialsOpened", FieldValue.increment(1))
                .addOnSuccessListener { showTutorial() }

            true
        }

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, AskPermissionsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            intent.putExtra("fromHome", true)

            startActivity(intent)
            finish()
        }

        // Manage user's back pressure
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        val retrievedRules = PreferencesManager.getAllPrivacyRules(this)
        noRule = findViewById(R.id.no_rule)
        noActiveRule = findViewById(R.id.no_active_rule)

        if (!retrievedRules.isNullOrEmpty()) {
            for (r in retrievedRules.keys) {
                val decodedRule = Json.decodeFromString<Rule>(retrievedRules[r].toString())

                if (decodedRule.active && !activeRules.contains(decodedRule)) {
                    activeRules.add(decodedRule)
                }
                else {
                    savedRules.add(decodedRule)
                }
            }
        }

        // Check if there are any rule
        if (savedRules.isEmpty()) {
            noRule.visibility = VISIBLE
        }
        if (activeRules.isEmpty()) {
            noActiveRule.visibility = VISIBLE
        }

        savedRulesRecyclerView = findViewById(R.id.list_rules)
        savedRulesRecyclerView.adapter = SavedRulesAdapter(savedRules, this)
        savedRulesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Managing active rules recycler view
        if (activeRules.isNotEmpty()) {
            val containerActiveRules = listActiveRules
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

        activeRulesRecyclerView = findViewById(R.id.list_active_rules)
        activeRulesRecyclerView.adapter = ActiveRulesAdapter(activeRules, this, resources)
        activeRulesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Manage insert new rule button
        newRuleButton = findViewById(R.id.new_rule)
        newRuleButton.setOnClickListener { v -> showPopupNewRule(v) }

        if (!PreferencesManager.getHomepageTutorialShown(this)) {
            showTutorial()
            PreferencesManager.saveHomepageTutorialShown(this)
        }
    }

    private fun showTutorial() {
        MaterialShowcaseView.resetSingleUse(this, TUTORIAL_ID)

        val config = ShowcaseConfig()
        config.delay = 500

        val sequence = MaterialShowcaseSequence(this, TUTORIAL_ID)

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(title)
                .withoutShape()
                .setContentText(getText("Questa è l'homepage dell'applicazione\n\n", "Adesso ti verranno spiegate le varie sezioni"))
                .setDismissText("Tocca per continuare")
                .setDismissOnTouch(true)
                .setSkipText("Salta")
                .setSkipStyle(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC))
                .renderOverNavigationBar()
                .build()
        )

        val toolTipSavedRules = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Questa è una lista dove ti verranno mostrate tutte le regole che hai salvato. Da qui puoi vedere i dettagli della tua regola, attivarla, modificarla oppure eliminarla")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(listSavedRules)
                .setToolTip(toolTipSavedRules)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .setSkipText("Salta")
                .setSkipStyle(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC))
                .renderOverNavigationBar()
                .build()
        )

        val toolTipActiveRules = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Qui, invece, ti verranno mostrate tutte le regole che hai attualmente attivato. Anche da qui puoi vedere i dettagli della regola e disattivarla")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(listActiveRules)
                .setToolTip(toolTipActiveRules)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(40)
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build()
        )

        val toolTipNewRule = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Puoi cliccare qui per creare una nuova regola")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(newRuleButton)
                .setToolTip(toolTipNewRule)
                .withCircleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build()
        )

        sequence.start()
    }

    // Fix context text opacity problem in tutorial
    private fun getText(title: String, content: String): SpannableString {
        val spannableString = SpannableString(title + content)
        // 1.5f for title i.e. default text size * 1.5f
        spannableString.setSpan(RelativeSizeSpan(1.5f), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        // 0.85f for content i.e. default text size * 0.85f, smaller than the title
        spannableString.setSpan(RelativeSizeSpan(1.0f), title.length + 1, (title + content).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
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
            val intent = Intent(this, ParametersDefinitionActivity::class.java)
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
    val startRuleButton = v.findViewById<FloatingActionButton>(R.id.start_rule)
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

        // Manage start rule
        holder.startRuleButton.setOnClickListener { v -> showPopupActiveRule(v, rule) }

        // Manage edit rule
        holder.editRuleButton.setOnClickListener {
            val intent = Intent(context, ParametersDefinitionActivity::class.java)

            intent.putExtra("permissions", ArrayList(rule.permissions!!))
            intent.putExtra("apps", ArrayList(rule.apps!!))
            intent.putExtra("pkgs", ArrayList(rule.packageNames!!))

            val conditions = Rule()

            conditions.timeSlot = rule.timeSlot
            conditions.positions = rule.positions
            conditions.networks = rule.networks
            conditions.bt = rule.bt
            conditions.battery = rule.battery

            val conditionsJSON = Json.encodeToString(Rule.serializer(), rule)
            intent.putExtra("rule", conditionsJSON)

            intent.putExtra("action", rule.action)

            intent.putExtra("name", rule.name)

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
    val stopRuleButton = v.findViewById<FloatingActionButton>(R.id.edit_rule)
    val editRuleButton = v.findViewById<FloatingActionButton>(R.id.start_rule)
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

        holder.editRuleButton.visibility = INVISIBLE
        holder.deleteRuleButton.visibility = INVISIBLE

        holder.stopRuleButton.setImageDrawable(resources.getDrawable(R.drawable.icon_stop))

        // Manage stop active rule
        holder.stopRuleButton.setOnClickListener { v -> showPopupActiveRule(v, rule) }

        // Manage saved rule
        holder.buttonRule.setOnClickListener {
            val intent = Intent(context, SavedRuleActivity::class.java)
            intent.putExtra("rule", Json.encodeToString(rule))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

fun showPopupActiveRule(view: View, rule: Rule) {
    val (popupView, popupWindow) = managePopup(view, R.layout.popup_start_rule)

    // Initialize the elements of our window, install the handler
    val title = popupView.findViewById<TextView>(R.id.title)
    val buttonStartStopRule = popupView.findViewById<Button>(R.id.start_rule_button)
    val buttonCancel = popupView.findViewById<Button>(R.id.cancel_button)

    if (rule.active) {
        title.text = "Disattivare la regola?"
        buttonStartStopRule.text = "Disattiva"
    }

    buttonStartStopRule.setOnClickListener {
        // Change rule state
        rule.active = !rule.active

        // Update rule in shared preferences
        // Convert rule object to JSON string
        val ruleJSON = Json.encodeToString(Rule.serializer(), rule)

        // Save privacy rule in shared preferences
        PreferencesManager.savePrivacyRule(context, rule.name!!, ruleJSON)

        savedRules.clear()
        activeRules.clear()

        // Rule has been activated
        if (rule.active) {
            PreferencesManager.saveStartRule(context, rule.name!!)

            val userID = PreferencesManager.getUserID(context)
            val ruleRef = db.collection("users").document(userID).collection("statistics").document(rule.name!!)

            ruleRef.update("activations", FieldValue.increment(1))
        }
        // Rule has been stopped
        else {
            val startTime = PreferencesManager.getStartRule(context, rule.name!!)
            PreferencesManager.deleteStartRule(context, rule.name!!)

            val activeDuration = Duration.between(startTime, LocalDateTime.now())

            val userID = PreferencesManager.getUserID(context)
            val ruleRef = db.collection("users").document(userID).collection("statistics").document(rule.name!!)

            ruleRef.update("timeOfAction", FieldValue.increment(activeDuration.seconds))
        }

        // Reload homepage
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    buttonCancel.setOnClickListener {
        popupWindow.dismiss()
    }


    // Handler for clicking on the inactive zone of the window
    popupView.setOnTouchListener { v, event -> // Close the window when clicked
        popupWindow.dismiss()
        true
    }
}