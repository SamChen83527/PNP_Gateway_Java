package pnp_gateway.util;


import com.fazecast.jSerialComm.SerialPort;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * This class responds for the com port instance.
 */

/**
 *
 * @author user
 */
public class COMPortManager {
    
    private SerialPort comPort = SerialPort.getCommPort(getCOMPortName());
    
    public COMPortManager() {
    }
    
    public String getCOMPortName() {
        ServerConfig s = new ServerConfig();
        String comPortName = s.getComPortName();
        
        return comPortName;
    }    
    
    public SerialPort getCOMPort() {
        comPort.openPort();
        return comPort;
    }
        
//    // output data, output COM port
//    public void writeData(String outputData, SerialPort comPort){
//        // open COM port with string name
////        SerialPort comPort = SerialPort.getCommPort(getCOMPortName());
////        comPort.openPort();
//
//        byte[] buffer = outputData.getBytes();
//        OutputStream outputStream = comPort.getOutputStream();
//        for(int outputSize = 0; outputSize<buffer.length; outputSize++){
//            try {
//                System.out.println("GetServURL...");
//                outputStream.write(buffer[outputSize]);
//            } catch (IOException ex) {
//                Logger.getLogger(COMPortManager.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        comPort.closePort();
//    }
}
