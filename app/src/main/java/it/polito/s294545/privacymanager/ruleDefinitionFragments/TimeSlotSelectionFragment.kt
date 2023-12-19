package it.polito.s294545.privacymanager.ruleDefinitionFragments

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import it.polito.s294545.privacymanager.utilities.ParameterListener
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.activities.retrievedRule
import it.polito.s294545.privacymanager.customDataClasses.TimeSlot
import java.util.Calendar

var savedSlot = TimeSlot()

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TimeSlotSelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimeSlotSelectionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var context: Context
    private var parameterListener : ParameterListener? = null

    // Time picker variables initialization
    private lateinit var fromTimePicker: TimePickerDialog
    private lateinit var fromTimeSlot: String
    private lateinit var selectFromTimeSlot: TextView
    private lateinit var toTimePicker: TimePickerDialog
    private lateinit var toTimeSlot: String
    private lateinit var selectToTimeSlot: TextView

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
        val v = inflater.inflate(R.layout.fragment_time_slot_selection, container, false)

        // Check if we are editing a rule
        if (retrievedRule != null) {
            // Check if in the saved rule has been defined a time slot
            if (retrievedRule!!.timeSlot != null && !retrievedRule!!.timeSlot?.days.isNullOrEmpty()) {
                savedSlot = retrievedRule!!.timeSlot!!
                parameterListener?.onParameterEntered("time_slot", savedSlot)
            }
        }

        handleTimeSlot(v)

        return v
    }

    // Manage new time slot
    private fun handleTimeSlot(v: View) {
        // ----- Manage time slot day buttons -----

        // Monday
        val mondayButton = v.findViewById<ExtendedFloatingActionButton>(R.id.monday)
        initializeDay("monday", mondayButton)
        mondayButton.setOnClickListener { toggleDaySelection(it, "monday") }

        // Tuesday
        val tuesdayButton = v.findViewById<ExtendedFloatingActionButton>(R.id.tuesday)
        initializeDay("tuesday", tuesdayButton)
        tuesdayButton.setOnClickListener { toggleDaySelection(it, "tuesday") }

        // Wednesday
        val wednesdayButton = v.findViewById<ExtendedFloatingActionButton>(R.id.wednesday)
        initializeDay("wednesday", wednesdayButton)
        wednesdayButton.setOnClickListener { toggleDaySelection(it, "wednesday") }

        // Thursday
        val thursdayButton = v.findViewById<ExtendedFloatingActionButton>(R.id.thursday)
        initializeDay("thursday", thursdayButton)
        thursdayButton.setOnClickListener { toggleDaySelection(it, "thursday") }

        // Friday
        val fridayButton = v.findViewById<ExtendedFloatingActionButton>(R.id.friday)
        initializeDay("friday", fridayButton)
        fridayButton.setOnClickListener { toggleDaySelection(it, "friday") }

        // Saturday
        val saturdayButton = v.findViewById<ExtendedFloatingActionButton>(R.id.saturday)
        initializeDay("saturday", saturdayButton)
        saturdayButton.setOnClickListener { toggleDaySelection(it, "saturday") }

        // Sunday
        val sundayButton = v.findViewById<ExtendedFloatingActionButton>(R.id.sunday)
        initializeDay("sunday", sundayButton)
        sundayButton.setOnClickListener { toggleDaySelection(it, "sunday") }

        // ----- End time slot day management -----

        // ----- Manage time slot time -----

        // Set from time picker
        selectFromTimeSlot = v.findViewById(R.id.select_from_time_slot)
        selectFromTimeSlot.text = savedSlot.time.first
        var fromHour : Int? = null
        var fromMinutes : Int? = null

        selectFromTimeSlot.setOnClickListener {
            val cldr = Calendar.getInstance()
            var hour = cldr[Calendar.HOUR_OF_DAY]
            var minutes = cldr[Calendar.MINUTE]

            if (this::fromTimeSlot.isInitialized) {
                hour = fromTimeSlot.split(":")[0].toInt()
                minutes = fromTimeSlot.split(":")[1].toInt()

                fromHour = hour
                fromMinutes = minutes
            }

            // Time picker dialog
            fromTimePicker = TimePickerDialog(context,
                { _, hourOfDay, minute ->
                    var hourFormatted = hourOfDay.toString()
                    var minuteFormatted = minute.toString()
                    if (hourOfDay < 10) {
                        hourFormatted = "0$hourFormatted"
                    }

                    if (minute < 10) {
                        minuteFormatted = "0$minute"
                    }
                    selectFromTimeSlot.text = String.format("%s:%s", hourFormatted, minuteFormatted)

                    // Save inserted time as parameter
                    fromHour = hourOfDay
                    fromMinutes = minute
                    savedSlot.time = Pair(selectFromTimeSlot.text.toString(), savedSlot.time.second)
                    parameterListener?.onParameterEntered("time_slot", savedSlot)
                }, hour, minutes, true)

            fromTimePicker.show()
        }

        // Set to time picker
        selectToTimeSlot = v.findViewById(R.id.select_to_time_slot)
        selectToTimeSlot.text = savedSlot.time.second
        var toHour : Int? = null
        var toMinutes : Int? = null

        selectToTimeSlot.setOnClickListener {
            val cldr = Calendar.getInstance()
            var hour = cldr[Calendar.HOUR_OF_DAY]
            var minutes = cldr[Calendar.MINUTE]

            if (this::toTimeSlot.isInitialized) {
                hour = toTimeSlot.split(":")[0].toInt()
                minutes = toTimeSlot.split(":")[1].toInt()

                toHour = hour
                toMinutes = minutes
            }

            // Time picker dialog
            toTimePicker = TimePickerDialog(context,
                { _, hourOfDay, minute ->
                    var hourFormatted = hourOfDay.toString()
                    var minuteFormatted = minute.toString()
                    if (hourOfDay < 10) {
                        hourFormatted = "0$hourFormatted"
                    }

                    if (minute < 10) {
                        minuteFormatted = "0$minute"
                    }
                    selectToTimeSlot.text = String.format("%s:%s", hourFormatted, minuteFormatted)

                    // Save inserted time as parameter
                    toHour = hourOfDay
                    toMinutes = minute
                    savedSlot.time = Pair(savedSlot.time.first, selectToTimeSlot.text.toString())
                    parameterListener?.onParameterEntered("time_slot", savedSlot)
                }, hour, minutes, true)

            toTimePicker.show()
        }

        // ----- End time slot time management -----

        // Reset time
        val reset = v.findViewById<ExtendedFloatingActionButton>(R.id.reset_button)

        reset.setOnClickListener {
            selectFromTimeSlot.text = ""
            selectToTimeSlot.text = ""

            savedSlot.time = Pair("", "")
            parameterListener?.onParameterEntered("time_slot", savedSlot)
        }
    }

    // Manage day selection/deselection
    private fun toggleDaySelection(day: View, dayText: String) {
        if (!day.isSelected) {
            day.isSelected = true
            day.setBackgroundColor(resources.getColor(R.color.primary))

            savedSlot.days.add(dayText)
            parameterListener?.onParameterEntered("time_slot", savedSlot)
        }
        else {
            day.isSelected = false
            day.setBackgroundColor(resources.getColor(R.color.grey))

            savedSlot.days.remove(dayText)
            parameterListener?.onParameterEntered("time_slot", savedSlot)
        }
    }

    private fun initializeDay(day: String, dayButton: ExtendedFloatingActionButton) {
        if (savedSlot.days.contains(day)) {
            dayButton.isSelected = true
            dayButton.setBackgroundColor(resources.getColor(R.color.primary))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireActivity()
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
         * @return A new instance of fragment PermissionsSelectionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TimeSlotSelectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}