package usask.chl848.lightgradient;

import android.bluetooth.BluetoothSocket;
import android.net.LocalSocket;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Utility functions
 */
public class Utility {
    public static void cleanCloseFix(BluetoothSocket btSocket) throws IOException
    {
        synchronized(btSocket)
        {
            Field socketField = null;
            LocalSocket mSocket = null;
            try
            {
                socketField = btSocket.getClass().getDeclaredField("mSocket");
                socketField.setAccessible(true);

                mSocket = (LocalSocket)socketField.get(btSocket);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                //PlutoLogger.Instance().write("Utility::cleanCloseFix() - Exception getting mSocket in cleanCloseFix(): " + e.toString());
            }

            if(mSocket != null)
            {
                mSocket.shutdownInput();
                mSocket.shutdownOutput();
                mSocket.close();

                mSocket = null;

                try { socketField.set(btSocket, mSocket); }
                catch(Exception e)
                {
                    e.printStackTrace();
                    //PlutoLogger.Instance().write("Utility::cleanCloseFix() - Exception setting mSocket = null in cleanCloseFix(): " + e.toString());
                }
            }


            Field pfdField = null;
            ParcelFileDescriptor mPfd = null;
            try
            {
                pfdField = btSocket.getClass().getDeclaredField("mPfd");
                pfdField.setAccessible(true);

                mPfd = (ParcelFileDescriptor)pfdField.get(btSocket);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                //PlutoLogger.Instance().write("Utility::cleanCloseFix() - Exception getting mPfd in cleanCloseFix(): " + e.toString());
            }

            if(mPfd != null)
            {
                mPfd.close();

                mPfd = null;

                try { pfdField.set(btSocket, mPfd); }
                catch(Exception e)
                {
                    e.printStackTrace();
                    //PlutoLogger.Instance().write("Utility::cleanCloseFix() - Exception setting mPfd = null in cleanCloseFix(): " + e.toString());
                }
            }

        } //synchronized
    }
}
