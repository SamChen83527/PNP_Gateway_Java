/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnp_gateway.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class LookupTableManager {
    
    private String reading = "";
    
    public void LookupTableManager(){}
    /*
        Lookup Table scheme:
            * device_ID (UID, int)
            * Things_ID (VID, int)    
            * service_URL (String)
            * DSID
                * "<Datastreams_name>": Datastreams_ID (int)
            * TCID
                * "<TaskingCapabilities_name>": TaskingCapabilities_ID (int)
            * Protocols
                * Protocol
    
        example:
            {
                "device_ID": "MY_DEVICE018",
                "Things_ID": 22,
                "service_URL": "140.115.110.69:8080/STA/v1.0",
                "DSID": {
                    "sensor1": 40,
                    "sensor2": 41
                },
                "TCID": {
                    "LightBulb": 11
                },
                "Protocols": [
                    {
                        "Type": "zigbeeProtocol",
                        "addressingSH": "0013A200",
                        "addressingSL": "40BF8550",
                        "messageDataType": "application/text",
                        "messageBody": "light:{on}"
                    }
                ]
            }
    */
    
    public String queryLookupTable(String device_ID){
        //read Lookup Table
        
        File record = new File(device_ID + ".txt");
        if(record.isFile()){
            System.out.println("Device found.");
            try {
                FileReader fr;
                fr = new FileReader(device_ID + ".txt");
                try (BufferedReader br = new BufferedReader(fr)) {
                    while (br.ready()) {
                        reading = reading + br.readLine();
                    }
                }
                fr.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LookupTableManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LookupTableManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Device doesn't exist in lookup table.");
            reading = null;
        }
        
        return reading;
    }
    
    public void updateLookupTable(String data, String UID) {
        FileOutputStream fos;
        try {            
            fos = new FileOutputStream(UID + ".txt");
            DataOutputStream dos = new DataOutputStream(fos);
            
            dos.writeBytes(data);
                
            fos.close();
            dos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LookupTableManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LookupTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
