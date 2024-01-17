package it.polito.s294545.privacymanager.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.customDataClasses.Rule
import it.polito.s294545.privacymanager.ruleDefinitionFragments.listAppsInfo
import it.polito.s294545.privacymanager.utilities.PreferencesManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import uk.co.deanwild.materialshowcaseview.ShowcaseTooltip
import java.time.Duration
import java.time.LocalDateTime

class ParametersDefinitionActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private val TUTORIAL_ID = "Tutorial rule creation"

    private var permissionsIntent: Any? = null
    private var appsIntent: Any? = null
    private var pkgsIntent: Any? = null
    private var conditionsIntent: Any? = null
    private var actionIntent: String? = null
    private var nameIntent: String? = null

    private lateinit var savedPermissions: ArrayList<String>
    private lateinit var savedApps: ArrayList<String>
    private lateinit var savedPkgs: ArrayList<String>
    private lateinit var savedConditions: Rule
    private lateinit var savedAction: String

    private var name: String? = null

    private lateinit var infoTextView: TextView
    private lateinit var permissionsCard: MaterialCardView
    private lateinit var permissionsButton: ExtendedFloatingActionButton
    private lateinit var appsCard: MaterialCardView
    private lateinit var appsButton: ExtendedFloatingActionButton
    private lateinit var conditionsCard: MaterialCardView
    private lateinit var conditionsButton: ExtendedFloatingActionButton
    private lateinit var actionCard: MaterialCardView
    private lateinit var actionButton: ExtendedFloatingActionButton
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameters_definition)

        // Get toolbar
        val toolbarLayout = findViewById<ConstraintLayout>(R.id.toolbarLayout)
        val toolbar = toolbarLayout.findViewById<MaterialToolbar>(R.id.toolbar)

        // Set the navigation icon
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.icon_cross)
        toolbar.setNavigationIconTint(resources.getColor(R.color.white))

        toolbar.setNavigationOnClickListener { manageBackNavigation() }

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

        // Manage cancel button
        val cancelButton = findViewById<Button>(R.id.cancel_button)
        cancelButton.setOnClickListener { manageBackNavigation() }

        // Manage user's back pressure
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                manageBackNavigation()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        infoTextView = findViewById(R.id.infoTextView)

        permissionsCard = findViewById(R.id.permissions_card)
        appsCard = findViewById(R.id.apps_card)
        conditionsCard = findViewById(R.id.conditions_card)
        actionCard = findViewById(R.id.action_card)

        permissionsButton = findViewById(R.id.permissions_button)
        appsButton = findViewById(R.id.apps_button)
        conditionsButton = findViewById(R.id.conditions_button)
        actionButton = findViewById(R.id.action_button)

        saveButton = findViewById(R.id.save_button)

        // ----- Manage all intents -----
        // Name
        nameIntent = intent.extras?.getString("name")

        if (nameIntent != null) {
            name = nameIntent as String
        }

        // Permissions
        permissionsIntent = intent.extras?.get("permissions")

        if (permissionsIntent != null) {
            savedPermissions = permissionsIntent as ArrayList<String>

            permissionsButton.setBackgroundColor(resources.getColor(R.color.primary))

            appsButton.setBackgroundColor(resources.getColor(R.color.cancel))
            appsButton.isClickable = true
            appsButton.isFocusable = true
            appsButton.setTextColor(resources.getColor(R.color.white))
            appsButton.setIconTintResource(R.color.white)
        }

        // Apps
        appsIntent = intent.extras?.get("apps")
        pkgsIntent = intent.extras?.get("pkgs")

        if (appsIntent != null && pkgsIntent != null) {
            savedApps = appsIntent as ArrayList<String>
            savedPkgs = pkgsIntent as ArrayList<String>

            appsButton.setBackgroundColor(resources.getColor(R.color.primary))

            conditionsButton.setBackgroundColor(resources.getColor(R.color.cancel))
            conditionsButton.isClickable = true
            conditionsButton.isFocusable = true
            conditionsButton.setTextColor(resources.getColor(R.color.white))
            conditionsButton.setIconTintResource(R.color.white)

            actionButton.setBackgroundColor(resources.getColor(R.color.cancel))
            actionButton.isClickable = true
            actionButton.isFocusable = true
            actionButton.setTextColor(resources.getColor(R.color.white))
            actionButton.setIconTintResource(R.color.white)
        }

        // Conditions
        conditionsIntent = intent.extras?.get("rule")

        if (conditionsIntent != null) {
            savedConditions = Json.decodeFromString(conditionsIntent.toString())

            if (savedConditions.positions.isNullOrEmpty() && (savedConditions.timeSlot == null || savedConditions.timeSlot!!.days.isEmpty()) &&
                savedConditions.networks.isNullOrEmpty() && savedConditions.bt.isNullOrEmpty() && savedConditions.battery == null) {
                conditionsButton.setBackgroundColor(resources.getColor(R.color.cancel))
            }
            else {
                conditionsButton.setBackgroundColor(resources.getColor(R.color.primary))
            }
        }

        // Action
        actionIntent = intent.extras?.getString("action")

        if (actionIntent != null) {
            savedAction = actionIntent as String
            actionButton.setBackgroundColor(resources.getColor(R.color.primary))

            if (appsIntent != null) {
                saveButton.setBackgroundColor(resources.getColor(R.color.primary))
                saveButton.isClickable = true
                saveButton.isFocusable = true
            }
        }

        // ----- Manage parameters buttons -----
        // Permissions
        permissionsButton.setOnClickListener {
            val intent = Intent(this, PermissionsSelectionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            if (nameIntent != null) {
                intent.putExtra("name", name)
            }
            if (permissionsIntent != null) {
                intent.putExtra("permissions", ArrayList(savedPermissions))
            }
            if (appsIntent != null) {
                intent.putExtra("apps", savedApps)
                intent.putExtra("pkgs", savedPkgs)
            }
            if (conditionsIntent != null) {
                intent.putExtra("rule", conditionsIntent.toString())
            }
            if (actionIntent != null) {
                intent.putExtra("action", savedAction)
            }

            startActivity(intent)
            finish()
        }

        // Apps
        if (permissionsIntent != null) {
            appsButton.setOnClickListener {
                val intent = Intent(this, AppsSelectionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtra("permissions", ArrayList(savedPermissions))

                if (nameIntent != null) {
                    intent.putExtra("name", name)
                }
                if (appsIntent != null) {
                    intent.putExtra("apps", savedApps)
                    intent.putExtra("pkgs", savedPkgs)
                }
                if (conditionsIntent != null) {
                    intent.putExtra("rule", conditionsIntent.toString())
                }
                if (actionIntent != null) {
                    intent.putExtra("action", savedAction)
                }

                startActivity(intent)
                finish()
            }
        }

        // Conditions
        if (appsIntent != null) {
            conditionsButton.setOnClickListener {
                val intent = Intent(this, RuleDefinitionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                if (nameIntent != null) {
                    intent.putExtra("name", name)
                }
                intent.putExtra("permissions", ArrayList(savedPermissions))
                intent.putExtra("apps", savedApps)
                intent.putExtra("pkgs", savedPkgs)

                if (conditionsIntent != null) {
                    intent.putExtra("rule", conditionsIntent.toString())
                }
                if (actionIntent != null) {
                    intent.putExtra("action", savedAction)
                }

                startActivity(intent)
                finish()
            }
        }

        // Action
        if (appsIntent != null) {
            actionButton.setOnClickListener {
                val intent = Intent(this, ActionSelectionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                if (nameIntent != null) {
                    intent.putExtra("name", name)
                }
                intent.putExtra("permissions", ArrayList(savedPermissions))
                intent.putExtra("apps", savedApps)
                intent.putExtra("pkgs", savedPkgs)

                if (conditionsIntent != null) {
                    intent.putExtra("rule", conditionsIntent.toString())
                }
                if (actionIntent != null) {
                    intent.putExtra("action", savedAction)
                }

                startActivity(intent)
                finish()
            }
        }

        // ----- Save the rule -----
        if (actionIntent != null) {

            // It is a new rule that has to be saved
            if (nameIntent == null) {
                saveButton.setOnClickListener { v -> showPopupSaveRule(v) }
            }
            // It is an edited rule
            else {
                saveButton.setOnClickListener { v -> showPopupActiveRule(v) }
            }
        }

        if (!PreferencesManager.getRuleCreationTutorialShown(this)) {
            showTutorial()
            PreferencesManager.saveRuleCreationTutorialShown(this)
        }
    }

    private fun showTutorial() {
        MaterialShowcaseView.resetSingleUse(this, TUTORIAL_ID)

        val config = ShowcaseConfig()
        config.delay = 500

        val sequence = MaterialShowcaseSequence(this, TUTORIAL_ID)

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(infoTextView)
                .withoutShape()
                .setContentText(getText("Qui puoi definire la tua regola\n\n", "Adesso ti verranno spiegati i vari parametri che puoi definire"))
                .setDismissText("Tocca per continuare")
                .setDismissOnTouch(true)
                .setSkipText("Salta")
                .setSkipStyle(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC))
                .renderOverNavigationBar()
                .build()
        )

        val toolTipPermissions = ShowcaseTooltip.build(this)
            .corner(30)
            .text("La parte fondamentale di una regola è rappresentata dalle autorizzazioni. Scegliendo le autorizzazioni che vuoi monitorare verrai avvertito quando viene effettuato un acesso alla funzionalità corrispondente")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(permissionsCard)
                .setToolTip(toolTipPermissions)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .setSkipText("Salta")
                .setSkipStyle(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC))
                .renderOverNavigationBar()
                .build()
        )

        val toolTipApps = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Una (o più) delle autorizzazioni che hai selezionato possono essere utilizzate da diverse app. Scegli dunque le app che vuoi controllare")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(appsCard)
                .setToolTip(toolTipApps)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build()
        )

        val toolTipConditions = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Puoi anche scegliere se attivare il monitoraggio definito nella tua regola sempre o a seconda di alcune condizioni. Le condizioni che puoi definire comprendono i giorni e l'orario, la posizione in cui ti trovi, il tipo di connessione utilizzato, i dispositivi bluetooth collegati e il livello di batteria")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(conditionsCard)
                .setToolTip(toolTipConditions)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build()
        )

        val toolTipAction = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Puoi infine selezionare come intervenire nel caso in cui si verifichi una violazione alla regola definita")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(actionCard)
                .setToolTip(toolTipAction)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build()
        )

        val toolTipSave = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Quando hai finito potrai cliccare su questo bottone per dare un nome alla tua regola e salvarla")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(saveButton)
                .setToolTip(toolTipSave)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(15)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun showPopupSaveRule(view: View) {
        val (popupView, popupWindow) = managePopup(view, R.layout.popup_save_rule)

        // Initialize the elements of our window, install the handler
        val ruleName = popupView.findViewById<TextInputEditText>(R.id.edit_rule_name)
        val buttonConfirm = popupView.findViewById<Button>(R.id.confirm_save_button)
        val buttonCancel = popupView.findViewById<Button>(R.id.cancel_save_button)
        val errorName = popupView.findViewById<TextView>(R.id.error_name)

        // Rule name
        ruleName.doOnTextChanged { text, start, before, count ->
            errorName.visibility = GONE

            if (!text.isNullOrEmpty()) {
                buttonConfirm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
                buttonConfirm.isClickable = true
            }
            else {
                buttonConfirm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
                buttonConfirm.isClickable = false
            }
        }

        // Save rule
        buttonConfirm.setOnClickListener {v ->
            if (!ruleName.text.isNullOrEmpty()) {
                name = ruleName.text.toString().trim()

                if (PreferencesManager.ruleNameAlreadyExists(this, name!!)) {
                    errorName.visibility = VISIBLE
                }
                else {
                    popupWindow.dismiss()
                    showPopupActiveRule(v)
                }
            }
        }

        // Dismiss window
        buttonCancel.setOnClickListener {
            popupWindow.dismiss()
        }


        // Handler for clicking on the inactive zone of the window
        popupView.setOnTouchListener { v, event -> // Close the window when clicked
            popupWindow.dismiss()
            true
        }
    }

    private fun showPopupActiveRule(view: View) {
        val (popupView, popupWindow) = managePopup(view, R.layout.popup_start_rule)

        // Initialize the elements of our window, install the handler
        val title = popupView.findViewById<TextView>(R.id.title)
        val buttonStartRule = popupView.findViewById<Button>(R.id.start_rule_button)
        val buttonCancel = popupView.findViewById<Button>(R.id.cancel_button)

        title.text = "Attivare subito la regola?"
        buttonStartRule.text = "Sì"
        buttonCancel.text = "No"

        buttonStartRule.setOnClickListener {
            saveRule(true)
        }

        buttonCancel.setOnClickListener {
            saveRule(false)
        }
    }

    private fun saveRule(startRule: Boolean) {
        // Save the inserted rule in a Rule object
        val rule = Rule()

        rule.name = name
        rule.permissions = savedPermissions
        rule.apps = savedApps
        rule.packageNames = savedPkgs

        val conditionsChosen = arrayListOf<String>()

        if (conditionsIntent != null) {
            rule.timeSlot = savedConditions.timeSlot
            rule.positions = savedConditions.positions?.filter { it.latitude != null }
            rule.networks = savedConditions.networks
            rule.bt = savedConditions.bt
            rule.battery = savedConditions.battery

            if (rule.timeSlot != null && rule.timeSlot!!.days.isNotEmpty()) {
                conditionsChosen.add("timeSlot")
            }
            if (!rule.positions.isNullOrEmpty()) {
                conditionsChosen.add("position")
            }
            if (!rule.networks.isNullOrEmpty()) {
                conditionsChosen.add("network")
            }
            if (!rule.bt.isNullOrEmpty()) {
                conditionsChosen.add("bluetooth")
            }
            if (rule.battery != null) {
                conditionsChosen.add("battery")
            }
        }

        rule.action = savedAction

        rule.active = startRule

        if (startRule) {
            PreferencesManager.saveStartRule(this, rule.name!!)
        }

        // Convert rule object to JSON string
        val ruleJSON = Json.encodeToString(Rule.serializer(), rule)

        // Save privacy rule in shared preferences
        PreferencesManager.savePrivacyRule(this, rule.name!!, ruleJSON)

        val userID = PreferencesManager.getUserID(this)
        val ruleRef = db.collection("users").document(userID).collection("statistics")

        ruleRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val ruleData = hashMapOf(
                    "ruleName" to rule.name,
                    "permissionsChosen" to rule.permissions,
                    "appsChosen" to rule.apps,
                    "conditionsChosen" to conditionsChosen,
                    "actionChosen" to rule.action,

                    "activations" to if (startRule) 1 else 0,
                    "timeOfAction" to 0,
                    "violations" to 0,
                    "timestampViolations" to emptyList<Long>()
                )

                ruleRef.add(ruleData).addOnSuccessListener { ruleReference ->
                    PreferencesManager.saveRuleID(this, rule.name!!, ruleReference.id)
                }
            }
            // Error during data retrieving
            else {
                Toast.makeText(this, "Si è verificato un errore: ${task.exception}", Toast.LENGTH_LONG).show()
            }
        }

        clearAll()

        // Navigate back to homepage
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        clearAll()
    }

    private fun clearAll() {
        permissionsIntent = null
        appsIntent = null
        pkgsIntent = null
        conditionsIntent = null
        actionIntent = null
        nameIntent = null
        name = null
    }

    private fun manageBackNavigation() {
        clearAll()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}