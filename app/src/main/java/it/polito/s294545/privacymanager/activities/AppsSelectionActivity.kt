package it.polito.s294545.privacymanager.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedApps
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedPkg

class AppsSelectionActivity : AppCompatActivity() {

    private var listPermissions: ArrayList<String>? = null

    private var appsIntent: Any? = null
    private var pkgsIntent: Any? = null
    private var conditionsIntent: String? = null
    private var actionIntent: String? = null

    private var listAppsInfo = mapOf<String, Pair<Drawable, String>>()

    private lateinit var saveButton: Button

    companion object {
        var savedApps = mutableListOf<String>()
        var savedPkg = mutableListOf<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps_selection)

        // Get toolbar
        val toolbarLayout = findViewById<ConstraintLayout>(R.id.toolbarLayout)
        val toolbar = toolbarLayout.findViewById<MaterialToolbar>(R.id.toolbar)

        // Set the navigation icon
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.icon_left_arrow)
        toolbar.setNavigationIconTint(resources.getColor(R.color.white))

        toolbar.setNavigationOnClickListener { manageBackNavigation() }

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

        saveButton = findViewById(R.id.save_button)

        // Initialize list of apps
        val permissionsIntent = intent.extras?.get("permissions")

        if (permissionsIntent != null) {
            listPermissions = permissionsIntent as ArrayList<String>
            getApps()
        }

        // Check if we are editing a rule
        if (retrievedRule != null && retrievedRule!!.apps != null) {
            savedApps.addAll(retrievedRule!!.apps as Collection<String>)
            savedPkg.addAll(retrievedRule!!.packageNames as Collection<String>)
            retrievedRule!!.apps = null
        }

        appsIntent = intent.extras?.get("apps")
        pkgsIntent = intent.extras?.get("pkgs")
        conditionsIntent = intent.extras?.getString("rule")
        actionIntent = intent.extras?.getString("action")

        if (appsIntent != null && pkgsIntent != null) {
            savedApps.addAll(appsIntent as ArrayList<String>)
            savedPkg.addAll(pkgsIntent as ArrayList<String>)

            saveButton.isClickable = true
            saveButton.isFocusable = true
            saveButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
        }

        // Managing recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.list_apps)
        val adapter = AppsSelectionAdapter(listAppsInfo, saveButton, resources)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val filterAppText = findViewById<TextInputEditText>(R.id.filter_app_text)

        filterAppText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed in this case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Apply the filter to the adapter when the text changes
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed in this case
            }
        })

        // Managing save button
        saveButton.setOnClickListener {
            if (savedApps.isNotEmpty()) {
                val intent = Intent(this, ParametersDefinitionActivity::class.java)

                intent.putExtra("permissions", listPermissions)
                intent.putExtra("apps", ArrayList(savedApps))
                intent.putExtra("pkgs", ArrayList(savedPkg))

                if (conditionsIntent != null) {
                    intent.putExtra("rule", conditionsIntent)
                }
                if (actionIntent != null) {
                    intent.putExtra("action", actionIntent)
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun getApps() {
        // Specify the permissions to check
        val permissionsToCheck = mutableListOf<String>()

        for (p in listPermissions!!) {
            when (p) {
                "notifications" -> permissionsToCheck.add("android.permission.POST_NOTIFICATIONS")
                "location" -> permissionsToCheck.addAll(listOf("android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"))
                "calendar" -> permissionsToCheck.add("android.permission.WRITE_CALENDAR")
                "camera" -> permissionsToCheck.add("android.permission.CAMERA")
                //"sms" -> permissionsToCheck.add("android.permission.SEND_SMS")
            }
        }

        // Get a reference to the PackageManager
        val packageManager = packageManager

        // Get a list of all installed apps
        val installedApps = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        // Key = app name, value = icon, package name
        val appsAndIcons = mutableMapOf<String, Pair<Drawable, String>>()

        // Iterate through the installed apps
        for (packageInfo in installedApps) {
            val requestedPermissions = packageInfo.requestedPermissions
            val packageName = packageInfo.packageName

            // Check if the app has the specific permission (excluding system apps and this app)
            if (requestedPermissions != null &&
                !(packageName.startsWith("com.android", true) ||
                        //packageName.startsWith("com.google.android") ||
                        packageName.startsWith("it.polito.s294545"))) {
                for (permission in requestedPermissions) {
                    if (permissionsToCheck.contains(permission)) {
                        val tmpName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                        val appName = tmpName.substring(0, 1).uppercase() + tmpName.substring(1)
                        val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)

                        appsAndIcons[appName] = Pair(appIcon, packageName)
                    }
                }
            }
        }

        val orderedAppsAndIcons = appsAndIcons.toSortedMap()

        listAppsInfo = orderedAppsAndIcons
    }

    override fun onDestroy() {
        super.onDestroy()

        listPermissions!!.clear()
        savedApps.clear()
        savedPkg.clear()
        appsIntent = null
        pkgsIntent = null
        conditionsIntent = null
        actionIntent = null
    }

    private fun manageBackNavigation() {
        val intent = Intent(this, ParametersDefinitionActivity::class.java)

        intent.putExtra("permissions", ArrayList(listPermissions!!))

        if (appsIntent != null) {
            intent.putExtra("apps", ArrayList(appsIntent as ArrayList<String>))
            intent.putExtra("pkgs", ArrayList(pkgsIntent as ArrayList<String>))
        }
        if (conditionsIntent != null) {
            intent.putExtra("rule", conditionsIntent)
        }
        if (actionIntent != null) {
            intent.putExtra("action", actionIntent)
        }

        listPermissions!!.clear()
        savedApps.clear()
        savedPkg.clear()
        appsIntent = null
        pkgsIntent = null
        conditionsIntent = null
        actionIntent = null

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}

// Define recycler view for apps
class AppsSelectionViewHolder(v: View) : RecyclerView.ViewHolder(v){
    val appIcon = v.findViewById<ImageView>(R.id.app_icon)
    val appTitle = v.findViewById<TextView>(R.id.app_title)
    val checkBox = v.findViewById<CheckBox>(R.id.checkBox)
}

class AppsSelectionAdapter(private val listAppsInfo: Map<String, Pair<Drawable, String>>,
                           private val saveButton: Button, private val resources: Resources): RecyclerView.Adapter<AppsSelectionViewHolder>(), Filterable {

    private var filteredList = listAppsInfo.entries.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsSelectionViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.box_app_selection, parent, false)

        return AppsSelectionViewHolder(v)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: AppsSelectionViewHolder, position: Int) {
        val appName = filteredList[position].key
        val pkg = filteredList[position].value.second
        val appIcon = filteredList[position].value.first

        holder.appTitle.text = appName
        holder.appIcon.setImageDrawable(appIcon)

        // Remove previous listener to prevent unwanted callbacks during recycling
        holder.checkBox.setOnCheckedChangeListener(null)

        // Set the checkbox state based on whether the app is in the savedApps list
        holder.checkBox.isChecked = AppsSelectionActivity.savedApps.contains(appName)

        // When checkbox is clicked, insert or remove the corresponding app in the saved list
        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            // Save new app
            if (isChecked && !AppsSelectionActivity.savedApps.contains(appName)) {
                if (AppsSelectionActivity.savedApps.isEmpty()) {
                    saveButton.isClickable = true
                    saveButton.isFocusable = true
                    saveButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
                }

                AppsSelectionActivity.savedApps.add(appName)
                AppsSelectionActivity.savedPkg.add(pkg)
            }
            // Remove inserted app
            else if (!isChecked && AppsSelectionActivity.savedApps.contains(appName)) {
                AppsSelectionActivity.savedApps.remove(appName)
                AppsSelectionActivity.savedPkg.remove(pkg)

                if (AppsSelectionActivity.savedApps.isEmpty()) {
                    saveButton.isClickable = false
                    saveButton.isFocusable = false
                    saveButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filtered = listAppsInfo.filter {
                    it.key.startsWith(constraint.toString(), ignoreCase = true)
                }
                results.values = filtered
                results.count = filtered.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.values != null) {
                    filteredList = (results.values as LinkedHashMap<String, Pair<Drawable, String>>).entries.toMutableList()
                    notifyDataSetChanged()
                }
            }
        }
    }
}