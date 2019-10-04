/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnp_gateway.util;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 *
 * @author user
 */
public class XBeeManager {
    private COMPortManager comport_manager = new COMPortManager();
    private SerialPort comPort = comport_manager.getCOMPort();
    
    public XBeeManager(SerialPort comPort){
        this.comPort = comPort;
    }
    
    public void XBeeSendMsg(byte[] buffer) {
        OutputStream outputStream = comPort.getOutputStream();
        for (int outputSize = 0; outputSize<buffer.length; outputSize++) {
            try {
                outputStream.write(buffer[outputSize]);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(XBeeManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } 
        }
    }
    
    public String XBeeReadMsg() {
        String resp = "";
        int indexCR;
        do {
            byte[] newRespData = new byte[comPort.bytesAvailable()];
            int numResRead = comPort.readBytes(newRespData, newRespData.length);

            for (int i = 0; i<numResRead; i++) {
                resp = resp + (char)newRespData[i];
            }
            indexCR = resp.indexOf("<CR>");
        } while(indexCR == -1);

        return resp;
    }
    
}
