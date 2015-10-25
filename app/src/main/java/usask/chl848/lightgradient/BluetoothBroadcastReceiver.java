package usask.chl848.lightgradient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver for distributing the events sent by bluetooth
 */
public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    private BluetoothClientData m_data;

    public BluetoothBroadcastReceiver(BluetoothClientData data) {
        super();
        m_data = data;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //PlutoLogger.Instance().write("BluetoothBroadcastReceiver::onReceive() - Bluetooth device found : " + device.getName());
            // If it's already paired, skip it, because it's been listed already
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                if (device.getName().contains("btserver")) {
                    m_data.setDevice(device);
                    m_data.setupClientThread();
                    m_data.stopDiscovery();
                }
            }
            // When discovery is finished, unregister listener
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
           // PlutoLogger.Instance().write("BluetoothBroadcastReceiver::onReceive() - Bluetooth discovery finished");
            m_data.stopDiscovery();
        }
    }
}
