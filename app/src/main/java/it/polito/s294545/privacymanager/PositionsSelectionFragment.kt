package it.polito.s294545.privacymanager

import android.content.Context
import android.content.res.ColorStateList
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

private var savedPositions = mutableListOf<Address>()

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
        val v = inflater.inflate(R.layout.fragment_positions_selection, container, false)

        // Geocoder to manage geographical positions
        geocoder = Geocoder(requireContext())

        // Manage add position button
        addPositionButton = v.findViewById(R.id.add_positions_button)

        addPositionButton.setOnClickListener {
            handleNewPosition(v)
        }

        return v
    }

    // Manage new position
    private fun handleNewPosition(v: View) {
        addPositionButton.isClickable = false
        addPositionButton.setBackgroundColor(resources.getColor(R.color.dark_grey))

        val positionContainer = v.findViewById<LinearLayout>(R.id.positions_container)

        val positionBox = layoutInflater.inflate(R.layout.box_add_position, positionContainer, false)

        positionContainer.addView(positionBox)

        // Save inserted position as parameter
        val positionName = positionBox.findViewById<TextInputEditText>(R.id.edit_position)
        val confirmPosition = positionBox.findViewById<FloatingActionButton>(R.id.confirm_position_button)

        positionName.doOnTextChanged { text, start, before, count ->
            if (!text.isNullOrEmpty()) {
                confirmPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
                confirmPosition.isClickable = true
            }
            else {
                confirmPosition.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
                confirmPosition.isClickable = false
            }
        }

        confirmPosition.setOnClickListener {
            if (!positionName.text.isNullOrEmpty()) {
                val geoPositions = geocoder.getFromLocationName(positionName.text.toString(), 1)

                if (!geoPositions.isNullOrEmpty()) {
                    val address = geoPositions[0]

                    savedPositions.add(address)
                }

                parameterListener?.onParameterEntered("positions", savedPositions)

                confirmPosition.visibility = GONE
                addPositionButton.isClickable = true
                addPositionButton.setBackgroundColor(resources.getColor(R.color.primary))
            }
        }

        // Delete inserted position
        val deletePositionButton = positionBox.findViewById<FloatingActionButton>(R.id.delete_position_button)
        deletePositionButton.setOnClickListener {
            if (!positionName.text.isNullOrEmpty()) {
                val geoPositions = geocoder.getFromLocationName(positionName.text.toString(), 1)

                if (!geoPositions.isNullOrEmpty()) {
                    val address = geoPositions[0]

                    savedPositions.removeAll { it.getAddressLine(0) == address.getAddressLine(0) }
                }

                parameterListener?.onParameterEntered("positions", savedPositions)
            }

            addPositionButton.isClickable = true
            addPositionButton.setBackgroundColor(resources.getColor(R.color.primary))
            positionContainer.removeView(positionBox)
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