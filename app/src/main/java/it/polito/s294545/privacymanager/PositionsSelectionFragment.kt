package it.polito.s294545.privacymanager

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

private var savedPositions = mutableListOf<CustomAddress>()

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
    val deletePositionButton = v.findViewById<FloatingActionButton>(R.id.delete_position_button)
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
            holder.confirmPosition.visibility = GONE
        }
        else {
            // Make the addPositionButton unavailable while inserting new position, waiting for saving
            addPositionButton.isClickable = false
            addPositionButton.setBackgroundColor(resources.getColor(R.color.dark_grey))

        }

        // If starting to insert text, make available the confirm button
        // If text is blank, make the button unavailable
        holder.positionName.doOnTextChanged { text, start, before, count ->
            holder.confirmPosition.visibility = VISIBLE

            if (!text.isNullOrEmpty()) {
                holder.confirmPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
                holder.confirmPosition.isClickable = true
            }
            else {
                holder.confirmPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
                holder.confirmPosition.isClickable = false
            }
        }

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
                holder.confirmPosition.visibility = GONE
                addPositionButton.isClickable = true
                addPositionButton.setBackgroundColor(resources.getColor(R.color.primary))

                // We also disable the input text
                holder.positionName.isFocusable = false
                holder.positionName.inputType = InputType.TYPE_NULL
            }
        }

        // Delete inserted position
        holder.deletePositionButton.setOnClickListener {
            savedPositions.removeAt(holder.adapterPosition)
            parameterListener?.onParameterEntered("positions", savedPositions)
            notifyItemRemoved(position)

            addPositionButton.isClickable = true
            addPositionButton.setBackgroundColor(resources.getColor(R.color.primary))
        }
    }
}