package it.polito.s294545.privacymanager.ruleDefinitionFragments

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.s294545.privacymanager.utilities.ParameterListener
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.activities.retrievedRule

//val listBluetooth = listOf("Test BT 1", "Test BT 2", "Test BT 3")
var listBluetooth = mutableListOf<String>()

var savedBT = mutableListOf<String>()

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BluetoothSelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BluetoothSelectionFragment : Fragment() {
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
        val v = inflater.inflate(R.layout.fragment_bluetooth_selection, container, false)

        // Check if we are editing a rule
        if (retrievedRule != null) {
            // Check if in the saved rule have been defined some bt
            if (retrievedRule!!.bt != null && !retrievedRule!!.bt.isNullOrEmpty()) {
                savedBT.addAll(retrievedRule!!.bt!!)
                parameterListener?.onParameterEntered("bt", savedBT.toList())
                retrievedRule!!.bt = null
            }
        }

        // Scan bt devices
        scanBT()

        // Managing recycler view
        val recyclerView = v.findViewById<RecyclerView>(R.id.list_bluetooth)
        recyclerView.adapter = BluetoothSelectionAdapter(listBluetooth, parameterListener)
        recyclerView.layoutManager = LinearLayoutManager(context)

        return v
    }

    @SuppressLint("MissingPermission")
    private fun scanBT() {
        // Get the BluetoothManager
        val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        // Get the BluetoothAdapter
        val bluetoothAdapter = bluetoothManager.adapter

        // Get the set of paired devices
        val pairedDevices = bluetoothAdapter.bondedDevices

        // Loop through the paired devices
        for (device in pairedDevices) {
            listBluetooth.add(device.name)
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
         * @return A new instance of fragment BluetoothSelectionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BluetoothSelectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

// Define recycler view for bluetooth
class BluetoothSelectionViewHolder(v: View) : RecyclerView.ViewHolder(v){
    val bluetoothDevice = v.findViewById<TextView>(R.id.bluetooth_device)
    val checkBox = v.findViewById<CheckBox>(R.id.checkBox)
}

class BluetoothSelectionAdapter(private val listBluetooth: List<String>, private val parameterListener: ParameterListener?): RecyclerView.Adapter<BluetoothSelectionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothSelectionViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.box_bluetooth_selection, parent, false)

        return BluetoothSelectionViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listBluetooth.size
    }

    override fun onBindViewHolder(holder: BluetoothSelectionViewHolder, position: Int) {
        val deviceName = listBluetooth[position]

        holder.bluetoothDevice.text = deviceName

        if (savedBT.contains(deviceName)) {
            holder.checkBox.isChecked = true
        }

        // When checkbox is clicked, insert or remove the corresponding bt device in the saved list
        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            // Save new device
            if (isChecked && !savedBT.contains(deviceName)) {
                savedBT.add(deviceName)
            }
            // Remove inserted device
            else if (!isChecked && savedBT.contains(deviceName)) {
                savedBT.remove(deviceName)
            }

            parameterListener?.onParameterEntered("bt", savedBT.toList())
        }
    }
}