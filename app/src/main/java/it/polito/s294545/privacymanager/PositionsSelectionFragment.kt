package it.polito.s294545.privacymanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

    private lateinit var addPositionButton : ExtendedFloatingActionButton

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

        // Manage add position button
        addPositionButton = v.findViewById(R.id.add_positions_button)

        addPositionButton.setOnClickListener {
            handleNewPosition(v)
        }

        return v
    }

    // Manage new position
    private fun handleNewPosition(v: View) {
        val positionContainer = v.findViewById<LinearLayout>(R.id.positions_container)

        val positionBox = layoutInflater.inflate(R.layout.box_add_position, positionContainer, false)

        positionContainer.addView(positionBox)

        // Delete inserted position
        val deletePositionButton = positionBox.findViewById<FloatingActionButton>(R.id.delete_position_button)
        deletePositionButton.setOnClickListener {
            // Logic to remove inserted position...

            positionContainer.removeView(positionBox)
        }
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