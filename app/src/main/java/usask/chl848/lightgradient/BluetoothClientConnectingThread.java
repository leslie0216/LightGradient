package usask.chl848.lightgradient;

import android.bluetooth.BluetoothSocket;
import java.io.IOException;

/**
 * thread used to connect to bluetooth server
 */
public class BluetoothClientConnectingThread extends Thread {
    private BluetoothClientData m_data;
    private BluetoothSocket m_socket = null;

    public BluetoothClientConnectingThread(BluetoothClientData data) {
        m_data = data;
        initSocket();
    }

    private void initSocket() {
        try {
            if (m_data.m_device != null) {
                //m_socket = m_data.m_device.createInsecureRfcommSocketToServiceRecord(BluetoothClientData.BLUETOOTH_UUID);
                m_socket = m_data.m_device.createRfcommSocketToServiceRecord(BluetoothClientData.BLUETOOTH_UUID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (m_socket != null) {
                m_data.cancelDiscovery();

                try {
                    m_data.m_activity.m_drawView.setMessage(m_data.m_activity.getResources().getString(R.string.connecting));
                    m_socket.connect();
                } catch (IOException e) {
                    try {
                        //e.printStackTrace();
                        Utility.cleanCloseFix(m_socket);
                        //clearFileDescriptor(m_socket);
                        m_socket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    initSocket();
                    continue;
                }

                //Do work to manage the connection (in a separate thread)
                m_data.startConnectedThread(m_socket);
                break;
            } else {
                initSocket();
            }
        }
    }

    public void cancel() {
        try {
            if (m_socket != null) {
                Utility.cleanCloseFix(m_socket);
                m_socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
