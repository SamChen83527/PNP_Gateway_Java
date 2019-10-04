/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnp_gateway.Request.device_request;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pnp_gateway.Request.PNPRequest;
import pnp_gateway.util.COMPortManager;
import pnp_gateway.util.HTTPManager;
import pnp_gateway.util.LookupTableManager;
import pnp_gateway.util.XBeeManager;

/**
 *
 * @author user
 */
public class PNPRequest_UploadObs extends PNPRequest {
    private COMPortManager comport_manager = new COMPortManager();
    private SerialPort comPort = comport_manager.getCOMPort();
    
    private PNPRequest root_request = new PNPRequest();
    
    private String device_ID = "";
    private String service_URL = "";
    private String observation = "";
    private String description_file = "";    // from msg_body
    
    /* exmample:
        {
            "operation": "UploadObs",
            "device_ID": "<device_ID>",
            "msg_body": [
                {
                    "name": "<datastream_1>",
                    "observation": <obs1>
                },
                {
                    "name": "<datastream_2>",
                    "observation": <obs2>
                }
            ]
        }
    */
    
    public PNPRequest_UploadObs(PNPRequest request, SerialPort comPort){
        this.comPort = comPort;
        this.root_request = request;
        
        this.device_ID = request.getDevice_ID();
        this.observation = request.getObservation();
    }
    
    public void doUploadObs() throws JSONException {
        System.out.println("\nQuery Lookup Table for device: " + device_ID);
        LookupTableManager lookupTable = new LookupTableManager();
        String query_lookupTable_result = lookupTable.queryLookupTable(device_ID);

        System.out.println(query_lookupTable_result);
        /* lookupTable record:
            {
                "device_ID":"MY_DEVICE001",
                "Thing_ID":"1",
                "DSID":{
                    "sensor1": 1
                    "sensor2": 2
                },
                "service_URL": "<service_URL>"
            }
        */
         
        /* Device doesn't exist in lookup table */
        if (query_lookupTable_result == null) {
            System.out.println("Get Service URL for device: " + device_ID);
            JSONObject GetServURL = new JSONObject();
            GetServURL.put("operation", "GetServURL").put("device_ID", this.device_ID);
            String GetServURL_string = GetServURL.toString() + "<CR>";

            // ask service URL *****************************************
            byte[] GetServURL_buffer = GetServURL_string.getBytes();
            for(int a=0; a<GetServURL_buffer.length; a++){
                System.out.print((char)GetServURL_buffer[a]);
            }System.out.println();
            new XBeeManager(this.comPort).XBeeSendMsg(GetServURL_buffer);
            
            // wait response
            String SendServURL = "";
            JSONObject SendServURL_JSON;
            do {
                System.out.println("listening GetServURL response...");
                SendServURL = new XBeeManager(this.comPort).XBeeReadMsg();
                SendServURL_JSON = new JSONObject(SendServURL);
                /* 
                    {
                        "operation": "SendServURL",
                        "device_ID": "<device_ID>",
                        "service_URL": "<service_URL>"
                    }
                */
            } while ( !( (SendServURL_JSON.getString("device_ID")).equals(device_ID) ) );
            System.out.println("Recieved response: " + SendServURL);
            
             // ask service URL *****************************************
            root_request.doRequest(SendServURL);
        }
        
        /* device exists in lookup table */
        else if (query_lookupTable_result != null) {
            // upload observations
            JSONArray observation_jsonArray = new JSONArray(observation);System.out.println("observation_jsonArray: "+observation_jsonArray.toString());
            /*
            observation_jsonArray:
                [
                    {
                        "name": "sensor1",
                        "observation": 25.6
                    },
                    {
                        "name": "sensor2",
                        "observation": 47.5
                    }
                ]
            */

            JSONObject lookupTable_jsonObject = new JSONObject(query_lookupTable_result);System.out.println("lookupTable_jsonObject: "+lookupTable_jsonObject.toString());
            /* lookupTable record:
                {
                    "device_ID":"MY_DEVICE001",
                    "Thing_ID":"1",
                    "DSID":{
                        "sensor1": 1
                        "sensor2": 2
                    },
                    "service_URL": "<service_URL>"
                }
            */

            // send observations
            System.out.println("Uploading observations");

            int observation_jsonArray_length = observation_jsonArray.length();
            for (int observation_Num = 0; observation_Num<observation_jsonArray_length; observation_Num++) {
                // prepare POST-Observation message body
                JSONObject observation_jsonObject = observation_jsonArray.getJSONObject(observation_Num);
                String datastream_name = observation_jsonObject.getString("name");
                Double observation_value = observation_jsonObject.getDouble("observation");
                JSONObject observation_messageBody = new JSONObject().put("result", observation_value);System.out.println("observation_messageBody: "+observation_messageBody.toString());

                // prepare POST-Observation URL
                int DSID = lookupTable_jsonObject.getJSONObject("Datastream_id_list").getInt(datastream_name);
                String service_URL = lookupTable_jsonObject.getString("service_URL");
                String target = "Datastreams(" + DSID + ")/Observations";
                    
                    System.out.print("service_URL: ");System.out.println(service_URL);
                    System.out.print("target: ");System.out.println(target);
                    System.out.print("Datastream_id_list: ");System.out.println(DSID);
                    System.out.print("Datastream name: ");System.out.println(datastream_name);
                    System.out.print("Observation messageBody: ");System.out.println(observation_messageBody.toString()+"\n");

                // send POST-Observation
                try {
                    new HTTPManager().sendPost(service_URL, target, observation_messageBody.toString());
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(PNPRequest_UploadObs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }

            // Confirm *****************************************************
            JSONObject Confirm = new JSONObject();
            Confirm.put("operation", "Confirm").put("device_ID", this.device_ID);
            String Confirm_string = Confirm.toString() + "<CR>";
            
            byte[] Confirm_buffer = Confirm_string.getBytes();
            new XBeeManager(this.comPort).XBeeSendMsg(Confirm_buffer);
            // Confirm *****************************************************
        }
    }
}