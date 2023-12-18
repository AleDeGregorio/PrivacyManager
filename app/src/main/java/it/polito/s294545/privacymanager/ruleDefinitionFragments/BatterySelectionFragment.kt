package it.polito.s294545.privacymanager.ruleDefinitionFragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import it.polito.s294545.privacymanager.utilities.ParameterListener
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.activities.retrievedRule

var savedBattery : Int? = null

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BatterySelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BatterySelectionFragment : Fragment() {
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
        val v = inflater.inflate(R.layout.fragment_battery_selection, container, false)

        val battery = v.findViewById<TextInputEditText>(R.id.edit_battery)
        val checkBox = v.findViewById<CheckBox>(R.id.checkBox)

        // Check if we are editing a rule
        if (retrievedRule != null) {
            // Check if in the saved rule has been defined battery
            if (retrievedRule!!.battery != null) {
                savedBattery = retrievedRule!!.battery!!
                checkBox.isChecked = true
                battery.text = Editable.Factory.getInstance().newEditable(savedBattery.toString())
                parameterListener?.onParameterEntered("battery", savedBattery)
            }
        }

        // Pass battery info as parameter
        battery.doOnTextChanged { text, start, before, count ->
            if (checkBox.isChecked && !battery.text.isNullOrEmpty()) {
                savedBattery = battery.text.toString().toInt()
            }
            else if (!checkBox.isChecked) {
                savedBattery = null
            }

            parameterListener?.onParameterEntered("battery", savedBattery)
        }

        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && !battery.text.isNullOrEmpty()) {
                savedBattery = battery.text.toString().toInt()
                parameterListener?.onParameterEntered("battery", savedBattery)
            }
            else if (!isChecked) {
                savedBattery = null
                parameterListener?.onParameterEntered("battery", savedBattery)
            }
            else if (isChecked && battery.text.isNullOrEmpty()) {
                checkBox.isChecked = false
            }
        }

        val batterySlider = v.findViewById<SeekBar>(R.id.battery_slider)
        val batteryInfo = v.findViewById<TextView>(R.id.battery_info)

        batterySlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the text view with the selected value
                batteryInfo.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Called when the user starts moving the slider
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Called when the user stops moving the slider
            }
        })

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
         * @return A new instance of fragment BatterySelectionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BatterySelectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}