package it.polito.s294545.privacymanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

val listApps = listOf("Test app 1", "Test app 2", "Test app 3", "Test app 4", "Test app 5", "Test app 6", "Test app 7", "Test app 8", "Test app 9", "Test app 10")

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

        // Managing recycler view
        val recyclerView = view.findViewById<RecyclerView>(R.id.list_apps)
        recyclerView.adapter = AppsSelectionAdapter(listApps)
        recyclerView.layoutManager = LinearLayoutManager(context)
        return view
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

class AppsSelectionAdapter(private val listApps: List<String>): RecyclerView.Adapter<AppsSelectionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsSelectionViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.box_app_selection, parent, false)

        return AppsSelectionViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listApps.size
    }

    override fun onBindViewHolder(holder: AppsSelectionViewHolder, position: Int) {
        val appName = listApps[position]

        holder.appTitle.text = appName
    }
}