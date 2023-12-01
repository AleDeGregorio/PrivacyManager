package it.polito.s294545.privacymanager.ruleDefinitionFragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import it.polito.s294545.privacymanager.utilities.ParameterListener
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.activities.retrievedRule

private var savedAction = "obscure_notification"

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ActionWithNotificationSelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActionWithNotificationSelectionFragment : Fragment() {
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
        val v = inflater.inflate(R.layout.fragment_action_with_notification_selection, container, false)

        // Setting radio buttons color programmatically
        val obscureNotificationRadio = v.findViewById<RadioButton>(R.id.obscure_notification)
        obscureNotificationRadio.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
            R.color.secondary
        ))

        val blockNotificationRadio = v.findViewById<RadioButton>(R.id.block_notification)
        blockNotificationRadio.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
            R.color.secondary
        ))

        // Check if we are editing a rule
        if (retrievedRule != null) {
            savedAction = retrievedRule!!.action!!

            if (savedAction == "block_notification") {
                obscureNotificationRadio.isChecked = false
                blockNotificationRadio.isChecked = true
            }

            parameterListener?.onParameterEntered("action", savedAction)
        }

        // Pass action info as parameter
        obscureNotificationRadio.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                savedAction = "obscure_notification"
                parameterListener?.onParameterEntered("action", savedAction)
            }
        }

        blockNotificationRadio.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                savedAction = "block_notification"
                parameterListener?.onParameterEntered("action", savedAction)
            }
        }

        return v
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
         * @return A new instance of fragment ActionSelectionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActionWithNotificationSelectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}