/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnp_gateway.Request.device_request;

import SensorThingsAPI_data_model.Datastream;
import SensorThingsAPI_data_model.TaskingCapability;
import SensorThingsAPI_data_model.Thing;
import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pnp_gateway.Request.PNPRequest;
import pnp_gateway.util.COMPortManager;
import pnp_gateway.util.HTTPManager;
import pnp_gateway.util.LookupTableManager;
import pnp_gateway.util.ServerConfig;
import pnp_gateway.util.XBeeManager;

/**
 *
 * @author user
 */
public class PNPRequest_SendDesc extends PNPRequest {
    private COMPortManager comport_manager = new COMPortManager();
    private SerialPort comPort = comport_manager.getCOMPort();
    
    private PNPRequest root_request = new PNPRequest();
    private String next = "";
    
    private String operation = "";
    private String device_ID = "";
    private String service_URL = "";
    private String observation = "";
    private String description_file = "";    // from msg_body
    
    /* exmample:
        {
            "operation":"SendDesc",
            "device_ID":"NGISDevice010",
            "msg_body":"
                {
                    \"name\":\"NGISDevice010\",
                    \"description\":\"NGIS project sensor\",
                    \"properties\":{\"UID\":\"NGISDevice010\"},
                    \"Locations\":[
                        {
                            \"name\":\"CSRSR\",
                            \"description\":\"CSRSR\",
                            \"encodingType\":\"application/vnd.geo+json\",
                            \"location\":{\"type\":\"Point\",\"coordinates\":[24.967757, 121.187112]}
                        }
                    ],
                    \"Datastreams\":[
                        {
                            \"name\":\"sensor1\",
                            \"description\":\"Temperature\",
                            \"observationType\":\"\",
                            \"unitOfMeasurement\":{\"name\":\"degree Celsius\",\"symbol\":\"degree Celsius\",\"definition\":\"\"},
                            \"Sensor\":{\"name\":\"tempSensor\",\"description\":\"Thermometer\",\"encodingType\":\"text/html\",\"metadata\":\"\"},
                            \"ObservedProperty\":{\"name\":\"air_temperature\",\"definition\":\"\",\"description\":\"Temperature of air in situ.\"}
                        },
                        {
                            \"name\":\"sensor2\",
                            \"description\":\"Humidity\",
                            \"observationType\":\"\",
                            \"unitOfMeasurement\":{\"name\":\"Percentage\",\"symbol\":\"%\",\"definition\":\"\"},
                            \"Sensor\":{\"name\":\"humiSensor\",\"description\":\"Hygrometer\",\"encodingType\":\"text/html\",\"metadata\":\"\"},
                            \"ObservedProperty\":{\"name\":\"humidity\",\"definition\":\"\",\"description\":\"Humidity of air in situ.\"}
                        }
                    ],
                    "TaskingCapabilities": [
                        {
                            "name": "buzzer",
                            "description": "",
                            "properties":{},
                            "taskingParameters": {
                                "field": [
                                    {
                                        "name": "buzz_time",
                                        "description": "ring buzzer several times",
                                        "use": "Mandatory",
                                        "definition": {
                                            "inputType": "Integer",
                                            "unitOfMeasurement": "times",
                                            "allowedValues": [
                                                {"Max": 10,"Min": 1}
                                            ] 
                                        }
                                    }
                                ]
                            },
                            "zigbeeProtocol": {
                                "addressingSH": "",
                                "addressingSL": "",
                                "messageDataType": "application/text",
                                "messageBody": "NGISTaskingDevice001:buzzer:{buzz_time}"
                            },
                            "Actuator": {
                                "name": "Actuator1",
                                "description": "Buzzer actuator",
                                "encodingType": "sensorml",
                                "metadata": ""
                            }
                        }
                    ]
                }
            "
        }
   */
    
    public PNPRequest_SendDesc(PNPRequest request, SerialPort comPort){
        this.comPort = comPort;
        this.root_request = request;
        
        this.device_ID = request.getDevice_ID();
        this.description_file = request.getDescription_file();
        this.service_URL = request.getService_URL();
    }
    
    public void doSendDesc() throws JSONException {
        // Description file JSON
        JSONObject description_file_jsonObject = new JSONObject(description_file);

        /* Thing information */        
        String Thing_name = description_file_jsonObject.getString("name");
        String Thing_description = description_file_jsonObject.getString("description");
        String Thing_properties = description_file_jsonObject.getJSONObject("properties").toString();
        
        String Thing_UID = description_file_jsonObject.getJSONObject("properties").getString("UID");
        String Thing_Locations = null;
        if (description_file_jsonObject.has("Locations")) {
            Thing_Locations = description_file_jsonObject.getJSONArray("Locations").toString();System.out.println("Thing_Locations: "+Thing_Locations);
        }
        /* ******************************************************************* */
        
        /* Datastream information */        
        JSONArray Datastreams_jsonArray = null;
        int datastream_number = 0;
        if (description_file_jsonObject.has("Datastreams")) {
            Datastreams_jsonArray = description_file_jsonObject.getJSONArray("Datastreams");
            datastream_number = Datastreams_jsonArray.length();
        }
        /* ******************************************************************* */
        
        /* TaskingCapability information */
        JSONArray TaskingCapabilities_jsonArray = null;
        int TaskingCapability_number = 0;
        if (description_file_jsonObject.has("TaskingCapabilities")) {
            TaskingCapabilities_jsonArray = description_file_jsonObject.getJSONArray("TaskingCapabilities");
            TaskingCapability_number = TaskingCapabilities_jsonArray.length();
                System.out.println("TaskingCapability_number: "+TaskingCapability_number);
        }
        /* ******************************************************************* */
        
        /* Lookup table preperation */        
        String Thing_id = null;                 // will get "Thing id" after Thing POST 
        String Datastream_id = null;            // will get "Datastream id" after Datastream POST 
        String TaskingCapability_id = null;    // will get " TaskingCapability id" after TaskingCapability POST 
        
        JSONObject Datastream_id_list = null;
        JSONObject TaskingCapability_parameter_list = null;
        /* ******************************************************************* */
        
        // Create Thing
        Thing Thing = new Thing(
                Thing_name,
                Thing_description,
                Thing_properties
        );
        
        if (Thing_Locations != null) {
            Thing.setLocationsFromString(Thing_Locations);
        }
        
        String post_Thing_String = Thing.getThingSting();
        
            System.out.print("Thing: ");
            System.out.println(post_Thing_String);
        /* ******************************************************************* */    
        
        // POST Thing
        int breakout = 0;
        try {
            do {
                String location = new HTTPManager().sendPost(service_URL, "Things", post_Thing_String);
                breakout++;
                
                if (location != null){
                    Thing_id = location.substring(location.indexOf("(")+1, location.indexOf(")"));
                    Thing.setID(Integer.valueOf(Thing_id));
            
                        System.out.print("Thing_id: ");
                        System.out.println(Thing_id);
                }
                
                // set limit times
                if (breakout == 5)  
                    break;
            } while (Thing_id == null);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PNPRequest_UploadObs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        /* ******************************************************************* */
        
        // Create Datastream
        if (Datastreams_jsonArray != null) {
            Datastream_id_list = new JSONObject();
            for (int ds_count = 0; ds_count<datastream_number; ds_count++) {
                JSONObject datastream_jsonObject = Datastreams_jsonArray.getJSONObject(ds_count);
                
                String datastream_name = datastream_jsonObject.getString("name");
                String datastream_description = datastream_jsonObject.getString("description");
                String datastream_observationType = datastream_jsonObject.getString("observationType");
                String datastream_uom = datastream_jsonObject.getJSONObject("unitOfMeasurement").toString();
                String datastream_Sensor = datastream_jsonObject.getJSONObject("Sensor").toString();
                String datastream_ObservedProperty = datastream_jsonObject.getJSONObject("ObservedProperty").toString();
                
                Datastream Datastream = new Datastream(
                    datastream_name,
                    datastream_description,
                    datastream_observationType,
                    datastream_uom,                    
                    datastream_Sensor,
                    datastream_ObservedProperty,
                    Thing.getThingSting()
                );
                
                Datastream.setDatastreamJSONObject();
                String post_Datastream_String = Datastream.getDatastreamString();
                
                    System.out.print("Datastream: ");
                    System.out.println(post_Datastream_String);
                    
                // POST Datastream
                breakout = 0;
                try {
                    do {
                        String location = new HTTPManager().sendPost(service_URL, "Things(" + Thing_id + ")/Datastreams", post_Datastream_String);
                        breakout++;

                        if (location != null){
                            Datastream_id = location.substring(location.indexOf("(")+1, location.indexOf(")"));
                            Datastream.setID(Integer.valueOf(Datastream_id));
                            
                                System.out.print("Datastream_id: ");
                                System.out.println(Datastream_id);
                        }

                        // set limit times
                        if (breakout == 5)  
                            break;
                    } while (Datastream_id == null);                        
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(PNPRequest_UploadObs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                
                // prepare DSID list
                Datastream_id_list.put(datastream_name, Integer.valueOf(Datastream_id));
            }
        }
        /* ******************************************************************* */
        
        // TO-DO
        // Create TaskingCapability
        if (TaskingCapabilities_jsonArray != null) {
            for (int tc_count = 0; tc_count<TaskingCapability_number; tc_count++) {
                // taskingcapability in description file
                JSONObject taskingcapability_jsonObject = TaskingCapabilities_jsonArray.getJSONObject(tc_count);                
                
                String taskingcapability_name = taskingcapability_jsonObject.getString("name");
                String taskingcapability_description = taskingcapability_jsonObject.getString("description");
                String taskingcapability_taskingParameters = taskingcapability_jsonObject.getJSONObject("taskingParameters").toString();
                String taskingcapability_actuator = taskingcapability_jsonObject.getJSONObject("Actuator").toString();
                
                String taskingcapability_properties = null;
                if (taskingcapability_jsonObject.has("properties")) {
                    taskingcapability_properties = taskingcapability_jsonObject.getJSONObject("properties").toString();
                }
                
                // Store taskingcapability to lookuptable
                JSONObject taskingcapability_to_lookuptable = new JSONObject();

                // translate protocol into HTTP                
                // zigbeeProtocol
                String HTTP_msgbody = "";
                JSONObject taskingcapability_zigbeeProtocol = null;
                if (taskingcapability_jsonObject.has("zigbeeProtocol")){
                    taskingcapability_zigbeeProtocol = taskingcapability_jsonObject.getJSONObject("zigbeeProtocol");                    
                    taskingcapability_to_lookuptable.put("zigbeeProtocol", taskingcapability_zigbeeProtocol);
                    
                    int parameters_number = taskingcapability_taskingParameters.length();
                    for (int parameters_count = 0; parameters_count<parameters_number; parameters_count++) {
                        String parameter_name = taskingcapability_jsonObject.getJSONObject("taskingParameters").getString("name");
                        HTTP_msgbody = HTTP_msgbody + "\\\"" + parameter_name + "\\\"" + ":{" + parameter_name + "}";
                        if (parameters_count<(parameters_number-1)){
                            HTTP_msgbody = HTTP_msgbody + ",";
                        }
                    }
                    
                    // assemble httpProtocol
                    HTTP_msgbody = "{" + HTTP_msgbody + ",\\\"TaskingCapability_name\\\":\\\"" + taskingcapability_name + "\\\",\\\"device_ID\\\":\\\"" + Thing_UID + "\\\"}";
                    /* 
                        HTTP_msgbody: {\"on\":{on},\"device_ID\":\"MY_DEVICE4\",\"TC_name\":\"LightBulb\"}
                    */

                    System.out.print("HTTP_msgbody: ");System.out.println(HTTP_msgbody);
                }
                
                TaskingCapability TaskingCapability = new TaskingCapability(
                    taskingcapability_name, 
                    taskingcapability_description, 
                    taskingcapability_taskingParameters,
                    taskingcapability_actuator
                );

                String post_TaskingCapability_String = TaskingCapability.getTaskingCapabilityString();
                
                    System.out.print("TaskingCapability: ");
                    System.out.println(post_TaskingCapability_String);
                    
                // prepare create TaskingCapability request
                ServerConfig s = new ServerConfig();
                String gatewayURL = s.getGatewayURL();
                JSONObject TaskingCapability_parameter  = new JSONObject();
                
//                TaskingCapability_parameter.put("name", taskingcapability_name)
//                
//                String taskingcapability_js = 
//                        "{\"name\":\"" + TCF_name + "\",\"description\":\"" + TCF_desciption + "\",\"parameters\":" + TCF_parameters + ",\"httpProtocol\":[{\"httpMethod\":\"POST\",\"absoluteResourcePath\":\""+ gatewayURL + "\",\"messageDataType\":\"application/json\",\"messageBody\":\"" + HTTP_msgbody + "\"}],\"Thing\":{\"@iot.id\":" + RespThing_ID + "},\"Actuator\":" + TCF_Actuator + "}";
//                    System.out.print("\ntaskingcapability_js: ");
//                    System.out.println(taskingcapability_js);
//                    
//                // POST TaskingCapability
//                breakout = 0;
//                try {
//                    do {
//                        String location = new HTTPManager().sendPost(service_URL, "TaskingCapability", post_TaskingCapability_String);
//                        breakout++;
//
//                        if (location != null){
//                            TaskingCapability_id = location.substring(location.indexOf("(")+1, location.indexOf(")"));
//                            TaskingCapability.setID(Integer.valueOf(TaskingCapability_id));
//                            
//                                System.out.print("TaskingCapability_id: ");
//                                System.out.println(TaskingCapability_id);
//                        }
//
//                        // set limit times
//                        if (breakout == 5)  
//                            break;
//                    } while (Thing_id == null);                        
//                } catch (IOException ex) {
//                    java.util.logging.Logger.getLogger(PNPRequest_UploadObs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                }
                
            }
        }
        
        // Upadate Lookup Table
        /*
            Lookup Table scheme:
                * device_ID (UID, int)
                * Things_ID (VID, int)    
                * service_URL (String)
                * DSID
                    * "<Datastreams_name>": Datastreams_ID (int)
                * TCs
                    * "<TaskingCapabilities_name>": TaskingCapability (JSON)

            example:
                {
                    "device_ID": "MY_DEVICE018",
                    "Things_ID": 22,
                    "service_URL": "140.115.110.69:8080/STA/v1.0",
                    "Datastream_id_list": {
                        "sensor1": 40,
                        "sensor2": 41
                    },
                    "TaskingCapability_parameter_list": {
                        "LightBulb": {
                            "TCID": 31,
                            "parameters": [
                                {
                                    "name": "on",
                                    "description": "turn on or off",
                                    "use": "Mandatory",
                                    "definition": {
                                        "inputType": "Boolean",
                                        "unitOfMeasurement": "Status",
                                        "allowedValues": [
                                            {
                                                "value": true,
                                                "description": "turn on"
                                            },
                                            {
                                                "value": false,
                                                "description": "turn off"
                                            }
                                        ]
                                    }
                                }
                            ],
                            "zigbeeProtocol": {
                                "addressingSH": "0013A200",
                                "addressingSL": "40BF8550",
                                "messageDataType": "application/text",
                                "messageBody": "light:{on}"
                            }
                        }
                    }
                }
        */
        
        JSONObject lookuptable_jsonObject = new JSONObject();
        lookuptable_jsonObject.put("device_ID", Thing_UID);
        lookuptable_jsonObject.put("Things_ID", Integer.valueOf(Thing_id));
        lookuptable_jsonObject.put("service_URL", this.service_URL);
        
        if (Datastream_id_list != null)
            lookuptable_jsonObject.put("Datastream_id_list", Datastream_id_list);
        if (TaskingCapability_parameter_list != null)
            lookuptable_jsonObject.put("TaskingCapability_parameter_list", TaskingCapability_parameter_list);
        
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