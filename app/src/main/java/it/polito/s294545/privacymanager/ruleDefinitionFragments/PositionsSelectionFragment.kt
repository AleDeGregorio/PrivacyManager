package it.polito.s294545.privacymanager.ruleDefinitionFragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.polito.s294545.privacymanager.customDataClasses.CustomAddress
import it.polito.s294545.privacymanager.utilities.ParameterListener
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.activities.managePopup
import it.polito.s294545.privacymanager.activities.retrievedRule
import it.polito.s294545.privacymanager.utilities.PreferencesManager
import it.polito.s294545.privacymanager.utilities.context

var savedPositions = mutableListOf<CustomAddress>()
var ctx: Context? = null

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PositionsSelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PositionsSelectionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var parameterListener : ParameterListener? = null

    private lateinit var addPositionButton : ExtendedFloatingActionButton
    private lateinit var geocoder: Geocoder

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
        return inflater.inflate(R.layout.fragment_positions_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = context

        // Check if we are editing a rule
        if (retrievedRule != null) {
            // Check if in the saved rule have been defined some positions
            if (retrievedRule!!.positions != null && !retrievedRule!!.positions.isNullOrEmpty()) {
                savedPositions.addAll(retrievedRule!!.positions!!)
                parameterListener?.onParameterEntered("positions", savedPositions)
            }
        }

        // Geocoder to manage geographical positions
        geocoder = Geocoder(requireContext())

        // Manage add position button
        addPositionButton = view.findViewById(R.id.add_positions_button)

        // Managing recycler view
        val recyclerView = view.findViewById<RecyclerView>(R.id.list_positions)
        val adapter = PositionsSelectionAdapter(savedPositions, parameterListener, geocoder, addPositionButton, resources)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        addPositionButton.setOnClickListener {
            savedPositions.add(CustomAddress())
            adapter.notifyItemInserted(savedPositions.size - 1)
            recyclerView.smoothScrollToPosition(savedPositions.size - 1)
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
         * @return A new instance of fragment PositionsSelectionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PositionsSelectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

// Define recycler view for positions
class PositionsSelectionViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val positionName = v.findViewById<TextInputEditText>(R.id.edit_position)
    val confirmPosition = v.findViewById<FloatingActionButton>(R.id.confirm_position_button)
    val currentPosition = v.findViewById<FloatingActionButton>(R.id.current_position_button)
    val editPosition = v.findViewById<FloatingActionButton>(R.id.edit_position_button)
    val deletePositionButton = v.findViewById<FloatingActionButton>(R.id.delete_position_button)

    val positionTextInput = v.findViewById<TextInputLayout>(R.id.text_input_position)
    val defaultColor = positionTextInput.defaultHintTextColor
}

class PositionsSelectionAdapter(
    private val listPositions: MutableList<CustomAddress>,
    private val parameterListener: ParameterListener?,
    private val geocoder: Geocoder,
    private val addPositionButton: ExtendedFloatingActionButton,
    private val resources: Resources)
    : RecyclerView.Adapter<PositionsSelectionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionsSelectionViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.box_add_position, parent, false)

        return PositionsSelectionViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listPositions.size
    }

    override fun onBindViewHolder(holder: PositionsSelectionViewHolder, position: Int) {
        // Check if a position has already been saved
        if (savedPositions[position].address != null) {
            holder.positionName.setText(savedPositions[position].address)

            holder.positionName.isEnabled = false
            holder.positionTextInput.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primary))
            holder.editPosition.visibility = VISIBLE
            holder.confirmPosition.visibility = INVISIBLE
            holder.currentPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
            holder.currentPosition.isClickable = false
            holder.currentPosition.isFocusable = false
        }
        else {
            // Make the addPositionButton unavailable while inserting new position, waiting for saving
            addPositionButton.isClickable = false
            addPositionButton.setBackgroundColor(resources.getColor(R.color.dark_grey))
            holder.positionName.text = Editable.Factory.getInstance().newEditable("")
            holder.positionName.isFocusable = true
            holder.positionTextInput.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primary))
        }

        // If starting to insert text, make available the confirm button
        // If text is blank, make the button unavailable
        holder.positionName.doOnTextChanged { text, start, before, count ->
            holder.confirmPosition.visibility = VISIBLE

            if (!text.isNullOrEmpty()) {
                holder.confirmPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(
                    R.color.primary
                ))
                holder.confirmPosition.isClickable = true
            }
            else {
                holder.confirmPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(
                    R.color.dark_grey
                ))
                holder.confirmPosition.isClickable = false
            }
        }

        holder.currentPosition.setOnClickListener { v -> showPopupCurrentPosition(v, position, holder) }

        // Save inserted position
        holder.confirmPosition.setOnClickListener {
            if (!holder.positionName.text.isNullOrEmpty()) {
                // Get the geographical position from the inserted address
                val geoPositions = geocoder.getFromLocationName(holder.positionName.text.toString(), 1)

                if (!geoPositions.isNullOrEmpty()) {
                    val address = geoPositions[0]

                    val customAddress = CustomAddress()
                    customAddress.address = address.getAddressLine(0)
                    customAddress.latitude = address.latitude
                    customAddress.longitude = address.longitude

                    // Insert the position in the list changing the default item set at the beginning
                    savedPositions[position] = customAddress
                }

                // Save parameter in activity
                parameterListener?.onParameterEntered("positions", savedPositions)

                // After new position has been saved, we don't need the confirm button anymore
                holder.confirmPosition.visibility = INVISIBLE
                addPositionButton.isClickable = true
                addPositionButton.setBackgroundColor(resources.getColor(R.color.primary))

                // It is also shown the edit position button
                holder.editPosition.visibility = VISIBLE

                // The text input is now not editable
                holder.positionName.isEnabled = false
                holder.positionTextInput.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primary))

                holder.currentPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
                holder.currentPosition.isClickable = false
                holder.currentPosition.isFocusable = false
            }
        }

        // Edit inserted position
        holder.editPosition.setOnClickListener {
            // Make the text input available again
            holder.positionName.isEnabled = true

            holder.editPosition.visibility = GONE
            holder.confirmPosition.visibility = VISIBLE
            holder.confirmPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(
                R.color.primary
            ))

            addPositionButton.isClickable = false
            addPositionButton.setBackgroundColor(resources.getColor(R.color.dark_grey))

            holder.currentPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
            holder.currentPosition.isClickable = true
            holder.currentPosition.isFocusable = true
        }

        // Delete inserted position
        holder.deletePositionButton.setOnClickListener {
            holder.positionName.isEnabled = true
            holder.editPosition.visibility = GONE

            savedPositions.removeAt(holder.adapterPosition)
            parameterListener?.onParameterEntered("positions", savedPositions)
            notifyItemRemoved(position)

            addPositionButton.isClickable = true
            addPositionButton.setBackgroundColor(resources.getColor(R.color.primary))

            holder.positionTextInput.defaultHintTextColor = holder.defaultColor

            holder.currentPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
            holder.currentPosition.isClickable = true
            holder.currentPosition.isFocusable = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun showPopupCurrentPosition(view: View, position: Int, holder: PositionsSelectionViewHolder) {
        val (popupView, popupWindow) = managePopup(view, R.layout.popup_current_location)

        // Initialize the elements of our window, install the handler
        val positionName = popupView.findViewById<TextInputEditText>(R.id.current_position_name)
        val buttonConfirm = popupView.findViewById<Button>(R.id.confirm_current_position_button)
        val buttonCancel = popupView.findViewById<Button>(R.id.cancel_current_position_button)

        // Current position name
        positionName.doOnTextChanged { text, start, before, count ->

            if (!text.isNullOrEmpty()) {
                buttonConfirm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
                buttonConfirm.isClickable = true
            }
            else {
                buttonConfirm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
                buttonConfirm.isClickable = false
            }
        }

        // Save current position
        buttonConfirm.setOnClickListener {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx!!)

            // Request the current location of the user
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val customAddress = CustomAddress()
                        customAddress.address = positionName.text.toString().trim()
                        customAddress.latitude = location.latitude
                        customAddress.longitude = location.longitude

                        // Insert the position in the list changing the default item set at the beginning
                        savedPositions[position] = customAddress

                        // Save parameter in activity
                        parameterListener?.onParameterEntered("positions", savedPositions)

                        // After new position has been saved, we don't need the confirm button anymore
                        holder.confirmPosition.visibility = INVISIBLE
                        addPositionButton.isClickable = true
                        addPositionButton.setBackgroundColor(resources.getColor(R.color.primary))

                        // It is also shown the edit position button
                        holder.editPosition.visibility = VISIBLE

                        holder.positionName.text = Editable.Factory.getInstance().newEditable(positionName.text.toString().trim())

                        // The text input is now not editable
                        holder.positionName.isEnabled = false
                        holder.positionTextInput.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primary))

                        holder.currentPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
                        holder.currentPosition.isClickable = false
                        holder.currentPosition.isFocusable = false

                        if (customAddress.address != "") {
                            popupWindow.dismiss()
                        }
                    }
                }
        }

        // Dismiss window
        buttonCancel.setOnClickListener {
            popupWindow.dismiss()
        }


        // Handler for clicking on the inactive zone of the window
        popupView.setOnTouchListener { v, event -> // Close the window when clicked
            popupWindow.dismiss()
            true
        }
    }
}