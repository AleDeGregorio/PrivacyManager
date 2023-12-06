package it.polito.s294545.privacymanager.ruleDefinitionFragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import it.polito.s294545.privacymanager.utilities.ParameterListener
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.activities.retrievedRule

var savedActionNoNotification = "signal_app"

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ActionNoNotificationSelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActionNoNotificationSelectionFragment : Fragment() {
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
        val v = inflater.inflate(R.layout.fragment_action_no_notification_selection, container, false)

        // Setting radio buttons color programmatically
        val signalAppRadio = v.findViewById<RadioButton>(R.id.signal_app)
        signalAppRadio.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
            R.color.secondary
        ))

        val closeAppRadio = v.findViewById<RadioButton>(R.id.close_app)
        closeAppRadio.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
            R.color.secondary
        ))

        // Check if we are editing a rule
        if (retrievedRule != null) {
            savedActionNoNotification = retrievedRule!!.action!!

            if (savedActionNoNotification == "close_app") {
                signalAppRadio.isChecked = false
                closeAppRadio.isChecked = true
            }

            parameterListener?.onParameterEntered("action", savedActionNoNotification)
        }

        // Pass action info as parameter
        signalAppRadio.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                savedActionNoNotification = "signal_app"
                parameterListener?.onParameterEntered("action", savedActionNoNotification)
            }
        }

        closeAppRadio.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                savedActionNoNotification = "close_app"
                parameterListener?.onParameterEntered("action", savedActionNoNotification)
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
         * @return A new instance of fragment ActionNoNotificationSelectionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActionNoNotificationSelectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}