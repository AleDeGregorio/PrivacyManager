package it.polito.s294545.privacymanager

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

private var savedNetworks = mutableListOf<String>()

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
        val v = inflater.inflate(R.layout.fragment_network_selection, container, false)

        // Save mobile network as parameter
        val mobileCheckBox = v.findViewById<CheckBox>(R.id.checkBox)
        mobileCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && !savedNetworks.contains("mobile_data")) {
                savedNetworks.add("mobile_data")
            }
            else if (!isChecked && savedNetworks.contains("mobile_data")) {
                savedNetworks.remove("mobile_data")
            }

            parameterListener?.onParameterEntered("networks", savedNetworks)
        }

        // Manage add network button
        addNetworkButton = v.findViewById(R.id.add_network_button)

        addNetworkButton.setOnClickListener {
            handleNewNetwork(v)
        }

        return v
    }

    // Manage new network
    private fun handleNewNetwork(v: View) {
        addNetworkButton.isClickable = false
        addNetworkButton.setBackgroundColor(resources.getColor(R.color.dark_grey))

        val networkContainer = v.findViewById<LinearLayout>(R.id.network_container)

        val networkBox = layoutInflater.inflate(R.layout.box_add_network, networkContainer, false)

        networkContainer.addView(networkBox)

        // Save inserted network as parameter
        val networkName = networkBox.findViewById<TextInputEditText>(R.id.edit_network)
        val confirmNetwork = networkBox.findViewById<FloatingActionButton>(R.id.confirm_network_button)

        networkName.doOnTextChanged { text, start, before, count ->
            if (!text.isNullOrEmpty()) {
                confirmNetwork.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
                confirmNetwork.isClickable = true
            }
            else {
                confirmNetwork.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
                confirmNetwork.isClickable = false
            }
        }

        confirmNetwork.setOnClickListener {
            if (!networkName.text.isNullOrEmpty() && !savedNetworks.contains(networkName.text.toString())) {
                savedNetworks.add(networkName.text.toString())
                parameterListener?.onParameterEntered("networks", savedNetworks)

                confirmNetwork.visibility = GONE
                addNetworkButton.isClickable = true
                addNetworkButton.setBackgroundColor(resources.getColor(R.color.primary))
            }
        }

        // Delete inserted network
        val deleteNetworkButton = networkBox.findViewById<FloatingActionButton>(R.id.delete_network_button)
        deleteNetworkButton.setOnClickListener {
            if (!networkName.text.isNullOrEmpty() && savedNetworks.contains(networkName.text.toString())) {
                savedNetworks.remove(networkName.text.toString())
                parameterListener?.onParameterEntered("networks", savedNetworks)
            }

            addNetworkButton.isClickable = true
            addNetworkButton.setBackgroundColor(resources.getColor(R.color.primary))
            networkContainer.removeView(networkBox)
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