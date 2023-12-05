package it.polito.s294545.privacymanager.ruleDefinitionFragments

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.s294545.privacymanager.utilities.ParameterListener
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.activities.retrievedRule

var listApps = listOf("Test app 1", "Test app 2", "Test app 3", "Test app 4", "Test app 5", "Test app 6", "Test app 7", "Test app 8", "Test app 9", "Test app 10")
var listIcons = listOf<Drawable>()
var listPackageName = listOf<String>()

var savedApps = mutableListOf<String>()
var savedPkg = mutableListOf<String>()

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AppsSelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppsSelectionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var parameterListener : ParameterListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_apps_selection, container, false)

        // Check if we are editing a rule
        if (retrievedRule != null && retrievedRule!!.apps != null) {
            savedApps.addAll(retrievedRule!!.apps as Collection<String>)
            savedPkg.addAll(retrievedRule!!.packageNames as Collection<String>)
            parameterListener?.onParameterEntered("apps", savedApps.toList())
            parameterListener?.onParameterEntered("packageNames", savedPkg.toList())
            retrievedRule!!.apps = null
        }

        // Managing recycler view
        val recyclerView = view.findViewById<RecyclerView>(R.id.list_apps)
        recyclerView.adapter = AppsSelectionAdapter(listApps, parameterListener)
        recyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is ParameterListener) {
            parameterListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()

        parameterListener = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AppsSelectionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AppsSelectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

// Define recycler view for apps
class AppsSelectionViewHolder(v: View) : RecyclerView.ViewHolder(v){
    val appIcon = v.findViewById<ImageView>(R.id.app_icon)
    val appTitle = v.findViewById<TextView>(R.id.app_title)
    val checkBox = v.findViewById<CheckBox>(R.id.checkBox)
}

class AppsSelectionAdapter(private val listApps: List<String>, private val parameterListener: ParameterListener?): RecyclerView.Adapter<AppsSelectionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsSelectionViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.box_app_selection, parent, false)

        return AppsSelectionViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listApps.size
    }

    override fun onBindViewHolder(holder: AppsSelectionViewHolder, position: Int) {
        val appName = listApps[position]
        val pkg = listPackageName[position]
        val appIcon = listIcons[position]

        holder.appTitle.text = appName
        holder.appIcon.setImageDrawable(appIcon)

        // Remove previous listener to prevent unwanted callbacks during recycling
        holder.checkBox.setOnCheckedChangeListener(null)

        // Set the checkbox state based on whether the app is in the savedApps list
        holder.checkBox.isChecked = savedApps.contains(appName)

        // When checkbox is clicked, insert or remove the corresponding app in the saved list
        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            // Save new app
            if (isChecked && !savedApps.contains(appName)) {
                savedApps.add(appName)
                savedPkg.add(pkg)
            }
            // Remove inserted app
            else if (!isChecked && savedApps.contains(appName)) {
                savedApps.remove(appName)
                savedPkg.remove(pkg)
            }

            parameterListener?.onParameterEntered("apps", savedApps.toList())
            parameterListener?.onParameterEntered("packageNames", savedPkg.toList())
        }
    }
}