package it.polito.s294545.privacymanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

        // Manage add network button
        addNetworkButton = v.findViewById(R.id.add_network_button)

        addNetworkButton.setOnClickListener {
            handleNewNetwork(v)
        }

        return v
    }

    // Manage new network
    private fun handleNewNetwork(v: View) {
        val networkContainer = v.findViewById<LinearLayout>(R.id.network_container)

        val networkBox = layoutInflater.inflate(R.layout.box_add_network, networkContainer, false)

        networkContainer.addView(networkBox)

        // Delete inserted position
        val deletePositionButton = networkBox.findViewById<FloatingActionButton>(R.id.delete_network_button)
        deletePositionButton.setOnClickListener {
            // Logic to remove inserted position...

            networkContainer.removeView(networkBox)
        }
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