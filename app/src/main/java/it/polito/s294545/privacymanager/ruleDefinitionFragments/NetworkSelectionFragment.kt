package it.polito.s294545.privacymanager.ruleDefinitionFragments

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import it.polito.s294545.privacymanager.utilities.ParameterListener
import it.polito.s294545.privacymanager.R

private var savedNetworks = mutableListOf<String>()
private var savedMobile = mutableListOf<String>()

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NetworkSelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NetworkSelectionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var parameterListener : ParameterListener? = null

    private lateinit var addNetworkButton : ExtendedFloatingActionButton

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
        return inflater.inflate(R.layout.fragment_network_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Save mobile network as parameter
        val mobileCheckBox = view.findViewById<CheckBox>(R.id.checkBox)
        mobileCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && !savedMobile.contains("mobile_data")) {
                savedMobile.add("mobile_data")
            }
            else if (!isChecked && savedMobile.contains("mobile_data")) {
                savedMobile.remove("mobile_data")
            }

            parameterListener?.onParameterEntered("networks", savedNetworks + savedMobile)
        }

        // Manage add position button
        addNetworkButton = view.findViewById(R.id.add_network_button)

        // Managing recycler view
        val recyclerView = view.findViewById<RecyclerView>(R.id.list_networks)
        val adapter = NetworksSelectionAdapter(savedNetworks, parameterListener, addNetworkButton, resources)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        addNetworkButton.setOnClickListener {
            savedNetworks.add("")
            adapter.notifyItemInserted(savedNetworks.size - 1)
            recyclerView.smoothScrollToPosition(savedNetworks.size - 1)
        }
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
         * @return A new instance of fragment NetworkSelectionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NetworkSelectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

// Define recycler view for positions
class NetworksSelectionViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val networkName = v.findViewById<TextInputEditText>(R.id.edit_network)
    val confirmNetwork = v.findViewById<FloatingActionButton>(R.id.confirm_network_button)
    val deleteNetworkButton = v.findViewById<FloatingActionButton>(R.id.delete_network_button)
}

class NetworksSelectionAdapter(
    private val listNetworks: MutableList<String>,
    private val parameterListener: ParameterListener?,
    private val addNetworkButton: ExtendedFloatingActionButton,
    private val resources: Resources
)
    : RecyclerView.Adapter<NetworksSelectionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworksSelectionViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.box_add_network, parent, false)

        return NetworksSelectionViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listNetworks.size
    }

    override fun onBindViewHolder(holder: NetworksSelectionViewHolder, position: Int) {
        // Check if a network has already been saved
        if (savedNetworks[position] != "") {
            holder.networkName.setText(savedNetworks[position])
            holder.confirmNetwork.visibility = GONE
        }
        else {
            // Make the addNetworkButton unavailable while inserting new network, waiting for saving
            addNetworkButton.isClickable = false
            addNetworkButton.setBackgroundColor(resources.getColor(R.color.dark_grey))
        }

        // If starting to insert text, make available the confirm button
        // If text is blank, make the button unavailable
        holder.networkName.doOnTextChanged { text, start, before, count ->
            holder.confirmNetwork.visibility = View.VISIBLE

            if (!text.isNullOrEmpty()) {
                holder.confirmNetwork.backgroundTintList = ColorStateList.valueOf(resources.getColor(
                    R.color.primary
                ))
                holder.confirmNetwork.isClickable = true
            }
            else {
                holder.confirmNetwork.backgroundTintList = ColorStateList.valueOf(resources.getColor(
                    R.color.dark_grey
                ))
                holder.confirmNetwork.isClickable = false
            }
        }

        // Save inserted network
        holder.confirmNetwork.setOnClickListener {
            if (!holder.networkName.text.isNullOrEmpty() && !savedNetworks.contains(holder.networkName.text.toString())) {
                savedNetworks[position] = holder.networkName.text.toString()

                // Save parameter in activity
                parameterListener?.onParameterEntered("networks", savedNetworks + savedMobile)

                // After new network has been saved, we don't need the confirm button anymore
                holder.confirmNetwork.visibility = GONE
                addNetworkButton.isClickable = true
                addNetworkButton.setBackgroundColor(resources.getColor(R.color.primary))

                // We also disable the input text
                holder.networkName.isFocusable = false
                holder.networkName.inputType = InputType.TYPE_NULL
            }
        }

        // Delete inserted position
        holder.deleteNetworkButton.setOnClickListener {
            Log.d("myapp", "${holder.adapterPosition}")
            savedNetworks.removeAt(holder.adapterPosition)
            Log.d("myapp", "$position")
            parameterListener?.onParameterEntered("networks", savedNetworks + savedMobile)
            notifyItemRemoved(holder.adapterPosition)

            addNetworkButton.isClickable = true
            addNetworkButton.setBackgroundColor(resources.getColor(R.color.primary))
        }
    }
}