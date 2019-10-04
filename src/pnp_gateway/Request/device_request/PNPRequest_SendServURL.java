/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnp_gateway.Request.device_request;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class PNPRequest_SendServURL extends PNPRequest {
    private COMPortManager comport_manager = new COMPortManager();
    private SerialPort comPort = comport_manager.getCOMPort();
    
    private PNPRequest root_request = new PNPRequest();
    
    private String operation = "";
    private String device_ID = "";
    private String service_URL = "";
    private String observation = "";
    private String description_file = "";   // from msg_body
    
    /*  exmample:
        {
            "operation":"SendServURL",
            "device_ID":"NGISDevice010",
            "service_URL":"140.115.110.69:8080/SensorThingsAPIPart2/v1.0"
        }
   */
    
    public PNPRequest_SendServURL(PNPRequest request, SerialPort comPort){
        this.comPort = comPort;
        this.root_request = request;
        
        this.device_ID = request.getDevice_ID();
        this.service_URL = request.getService_URL();
    }
    
    public void doSendServURL() throws JSONException {
        System.out.println("\nQuery SensorThing API service " + service_URL + " for device: " + device_ID);
        String GET_resp = "";
        try {
            // check web service
            // String GETThing_url = service_URL + "/Things?$filter=properties/UID%20eq%20%27" + device_ID + "%27&$select=id&$expand=Datastreams($select=id,name;$count=true),TaskingCapabilities($select=id,name;$count=true)&$count=true";
            String GETThing_url = service_URL + "/Things?$filter=properties/UID%20eq%20%27" + device_ID + "%27&$select=id&$expand=Datastreams($select=id,name;$count=true)&$count=true";
            
            GET_resp = new HTTPManager().sendGet(GETThing_url);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PNPRequest_UploadObs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PNPRequest_SendServURL.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JSONObject getThing_response_jsonObject = new JSONObject(GET_resp);
        int count = getThing_response_jsonObject.getInt("@iot.count");
        if (count == 0) {
            System.out.println("device: " + device_ID + " doesn't exist in service");
            JSONObject GetDesc = new JSONObject();
            GetDesc.put("operation", "GetDesc").put("device_ID", this.device_ID);
            String GetDesc_string = GetDesc.toString() + "<CR>";

            // ask description file*************************************
            byte[] GetDesc_buffer = GetDesc_string.getBytes();
            for(int a=0; a<GetDesc_buffer.length; a++){
                System.out.print((char)GetDesc_buffer[a]);
            }System.out.println();
            new XBeeManager(this.comPort).XBeeSendMsg(GetDesc_buffer);
            
            // wait SendDesc req
            String SendDesc = "";
            JSONObject SendDesc_JSON;
            do {
                System.out.println("listening GetDesc response...");
                SendDesc = new XBeeManager(this.comPort).XBeeReadMsg();
                SendDesc_JSON = new JSONObject(SendDesc);
                /* 
                    req --> operation: sendDesc, 
                            device_ID, 
                            msg_body: "<Description file>"
                */
            } while( !( (SendDesc_JSON.getString("device_ID")).equals(device_ID) ) );
            System.out.println("Recieved response: " + SendDesc);

            root_request.doRequest(SendDesc);
            // ask description file*************************************
        }
        
        else {
            /* TO-DO */
            /*
                GET_resp:
                {
                    "@iot.count": 1,
                    "value": [                        
                        {
                            "Datastreams": [
                                {
                                    "name": "sensor1",
                                    "@iot.id": 46
                                },
                                {
                                    "name": "sensor2",
                                    "@iot.id": 47
                                }
                            ],
                            "Datastreams@iot.count": 2,
                            "TaskingCapabilities": [
                                {
                                    "name": "Hue",
                                    "@iot.id": 44
                                },
                                {
                                    "name": "Wemo tasking capability",
                                    "@iot.id": 46
                                }
                            ],
                            "TaskingCapabilities@iot.count": 2,
                            "@iot.id": 123
                        }
                    ]
                }
            */
            
            // Lookup table preperation      
            JSONObject lookuptable_jsonObject = new JSONObject();
            JSONObject Datastream_id_list = null;
            
            // Thing
            JSONObject Thing_jsonobject = getThing_response_jsonObject.getJSONArray("value").getJSONObject(0);              
            // Thing_ID 
            int Thing_id = Thing_jsonobject.getInt("@iot.id");
            // Datastream
            JSONArray Datastreams_jsonArray = null;
            int datastream_number = Thing_jsonobject.getInt("Datastreams@iot.count");
            if (datastream_number != 0) {
                Datastreams_jsonArray = Thing_jsonobject.getJSONArray("Datastreams");
                Datastream_id_list = new JSONObject();
            }
            for (int ds_count = 0; ds_count<datastream_number; ds_count++) {
                JSONObject datastream_jsonObject = Datastreams_jsonArray.getJSONObject(ds_count);
                
                int DS_id = datastream_jsonObject.getInt("@iot.id");
                String name = datastream_jsonObject.getString("name");
                
                Datastream_id_list.put(name, DS_id);
                /*
                    "Datastream_id_list": {
                        "sensor1": 40,
                        "sensor2": 41
                    }
                */
            }           
            
            lookuptable_jsonObject.put("device_ID", this.device_ID);
            lookuptable_jsonObject.put("Things_ID", Thing_id);
            lookuptable_jsonObject.put("service_URL", this.service_URL);

            if (Datastream_id_list != null)
                lookuptable_jsonObject.put("Datastream_id_list", Datastream_id_list);

            LookupTableManager lookupTable = new LookupTableManager();
            lookupTable.updateLookupTable(lookuptable_jsonObject.toString(), device_ID);
            System.out.print("Updated: ");System.out.println(lookuptable_jsonObject.toString()+"\n");

            // send observations
            System.out.println("Uploading observations");

            JSONArray observation_jsonArray = new JSONArray(root_request.getObservation());
            int observation_jsonArray_length = observation_jsonArray.length();
            for (int observation_Num = 0; observation_Num<observation_jsonArray_length; observation_Num++) {
                // prepare POST-Observation message body
                JSONObject observation_jsonObject = observation_jsonArray.getJSONObject(observation_Num);
                String datastream_name = observation_jsonObject.getString("name");
                Double observation_value = observation_jsonObject.getDouble("observation");
                JSONObject observation_messageBody = new JSONObject().put("result", observation_value);

                // prepare POST-Observation URL
                int DSID = lookuptable_jsonObject.getJSONObject("Datastream_id_list").getInt(datastream_name);
                String service_URL = lookuptable_jsonObject.getString("service_URL");
                String target = "Datastreams(" + DSID + ")/Observations";

                    System.out.print("target: ");System.out.println(target);//
                    System.out.print("service_URL: ");System.out.println(service_URL);
                    System.out.print("DSID: ");System.out.println(DSID);
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