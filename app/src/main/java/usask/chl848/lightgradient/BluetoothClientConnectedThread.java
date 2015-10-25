package usask.chl848.lightgradient;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * thread used to exchange data with bluetooth server
 */
public class BluetoothClientConnectedThread extends Thread {
    private BluetoothClientData m_data;
    private BluetoothSocket m_socket;

    private InputStream m_inStream;
    private OutputStream m_outStream;

    public BluetoothClientConnectedThread(BluetoothClientData data, BluetoothSocket socket) {
        try {
            m_data = data;
            m_socket = socket;
            m_inStream = m_socket.getInputStream();
            m_outStream = m_socket.getOutputStream();
            m_data.m_activity.m_drawView.setMessage(m_data.m_activity.getResources().getString(R.string.connected));
            m_data.setIsConnected(true);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        byte[] buffer = new byte[2048];
        int bytes;

        while (true) {
            try {
                // Read from the InputStream
                if( m_inStream != null && (bytes = m_inStream.read(buffer)) > 0 )
                {
                    byte[] buf_data = new byte[bytes];
                    for(int i=0; i<bytes; i++)
                    {
                        buf_data[i] = buffer[i];
                    }
                    String msg = new String(buf_data);
                    m_data.receiveBTMessage(msg);
                }
            } catch (IOException e) {
                cancel();
                m_data.m_activity.m_drawView.setMessage(m_data.m_activity.getResources().getString(R.string.notConnected));
                m_data.setIsConnected(false);
                m_data.m_activity.m_drawView.clearRemotePhoneInfo();
                break;
            }
        }
    }

    public void write(String msg) {
        try {
            if (m_outStream != null) {
                m_outStream.write(msg.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            if (m_inStream != null) {
                m_inStream.close();
                m_inStream = null;
            }
            if (m_outStream != null) {
                m_outStream.close();
                m_outStream = null;
            }
            if (m_socket != null) {
                Utility.cleanCloseFix(m_socket);
                m_socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}