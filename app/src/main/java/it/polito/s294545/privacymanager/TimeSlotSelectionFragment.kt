package it.polito.s294545.privacymanager

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

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

    private lateinit var addTimeSlotButton : ExtendedFloatingActionButton

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

        // Manage add time slot button
        addTimeSlotButton = v.findViewById(R.id.add_time_slot_button)

        addTimeSlotButton.setOnClickListener {
            handleNewTimeSlot(v)

            addTimeSlotButton.setBackgroundColor(resources.getColor(R.color.dark_grey))
            addTimeSlotButton.isClickable = false
        }

        return v
    }

    // Manage new time slot
    private fun handleNewTimeSlot(v: View) {
        val timeSlotContainer = v.findViewById<LinearLayout>(R.id.time_slot_container)

        val timeSlotBox = layoutInflater.inflate(R.layout.box_add_time_slot, timeSlotContainer, false)

        timeSlotContainer.addView(timeSlotBox)

        // ----- Manage new time slot day buttons -----

        // Monday
        val mondayButton = timeSlotBox.findViewById<ExtendedFloatingActionButton>(R.id.monday)
        mondayButton.setOnClickListener { toggleDaySelection(it) }

        // Tuesday
        val tuesdayButton = timeSlotBox.findViewById<ExtendedFloatingActionButton>(R.id.tuesday)
        tuesdayButton.setOnClickListener { toggleDaySelection(it) }

        // Wednesday
        val wednesdayButton = timeSlotBox.findViewById<ExtendedFloatingActionButton>(R.id.wednesday)
        wednesdayButton.setOnClickListener { toggleDaySelection(it) }

        // Thursday
        val thursdayButton = timeSlotBox.findViewById<ExtendedFloatingActionButton>(R.id.thursday)
        thursdayButton.setOnClickListener { toggleDaySelection(it) }

        // Friday
        val fridayButton = timeSlotBox.findViewById<ExtendedFloatingActionButton>(R.id.friday)
        fridayButton.setOnClickListener { toggleDaySelection(it) }

        // Saturday
        val saturdayButton = timeSlotBox.findViewById<ExtendedFloatingActionButton>(R.id.saturday)
        saturdayButton.setOnClickListener { toggleDaySelection(it) }

        // Sunday
        val sundayButton = timeSlotBox.findViewById<ExtendedFloatingActionButton>(R.id.sunday)
        sundayButton.setOnClickListener { toggleDaySelection(it) }

        // ----- End new time slot day management -----

        // ----- Manage time slot time -----

        // Set from time picker
        selectFromTimeSlot = timeSlotBox.findViewById(R.id.select_from_time_slot)

        selectFromTimeSlot.setOnClickListener {
            val cldr = Calendar.getInstance()
            var hour = cldr[Calendar.HOUR_OF_DAY]
            var minutes = cldr[Calendar.MINUTE]

            if (this::fromTimeSlot.isInitialized) {
                hour = fromTimeSlot.split(":")[0].toInt()
                minutes = fromTimeSlot.split(":")[1].toInt()
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
                }, hour, minutes, true)

            fromTimePicker.show()
        }

        // Set to time picker
        selectToTimeSlot = timeSlotBox.findViewById(R.id.select_to_time_slot)

        selectToTimeSlot.setOnClickListener {
            val cldr = Calendar.getInstance()
            var hour = cldr[Calendar.HOUR_OF_DAY]
            var minutes = cldr[Calendar.MINUTE]

            if (this::toTimeSlot.isInitialized) {
                hour = toTimeSlot.split(":")[0].toInt()
                minutes = toTimeSlot.split(":")[1].toInt()
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
                }, hour, minutes, true)

            toTimePicker.show()
        }

        // ----- End time slot time management -----

        // Confirm inserted time slot
        val confirmTimeSlotButton = timeSlotBox.findViewById<ExtendedFloatingActionButton>(R.id.confirm_time_slot_button)
        confirmTimeSlotButton.setOnClickListener {
            // Logic to save inserted time slot...

            addTimeSlotButton.setBackgroundColor(resources.getColor(R.color.primary))
            addTimeSlotButton.isClickable = true

            confirmTimeSlotButton.setBackgroundColor(resources.getColor(R.color.dark_grey))
            confirmTimeSlotButton.isClickable = false
        }

        // Delete inserted time slot
        val deleteTimeSlotButton = timeSlotBox.findViewById<ExtendedFloatingActionButton>(R.id.delete_time_slot_button)
        deleteTimeSlotButton.setOnClickListener {
            // Logic to remove inserted time slot...

            timeSlotContainer.removeView(timeSlotBox)

            addTimeSlotButton.setBackgroundColor(resources.getColor(R.color.primary))
            addTimeSlotButton.isClickable = true
        }
    }

    // Manage day selection/deselection
    private fun toggleDaySelection(day: View) {
        if (!day.isSelected) {
            day.isSelected = true
            day.setBackgroundColor(resources.getColor(R.color.secondary))
        }
        else {
            day.isSelected = false
            day.setBackgroundColor(resources.getColor(R.color.grey))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireActivity()
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