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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pnp_gateway.Request.PNPRequest;
import pnp_gateway.WorkerInterface;
import pnp_gateway.util.COMPortManager;
import pnp_gateway.util.HTTPManager;
import pnp_gateway.util.LookupTableManager;
import pnp_gateway.util.ServerConfig;
import pnp_gateway.util.XBeeManager;

/**
 *
 * @author user
 */
public class PNPRequest_SendServURLandDesc {
    private COMPortManager comport_manager = new COMPortManager();
    private SerialPort comPort = comport_manager.getCOMPort();
    
    private PNPRequest root_request = new PNPRequest();
    
    private String operation = "";
    private String device_ID = "";
    private String service_URL = "";
    private String observation = "";
    private String description_file = "";    // from msg_body
    
    /* exmample:
        {
            "operation":"SendServURL",
            "device_ID":"NGISDevice010",
            "service_URL":"140.115.110.69:8080/SensorThingsAPIPart2/v1.0"
        }
   */
    public PNPRequest_SendServURLandDesc(PNPRequest request, SerialPort comPort){
        this.comPort = comPort;
        this.root_request = request;
        
        this.device_ID = request.getDevice_ID();
        this.service_URL = request.getService_URL();
        this.description_file = request.getDescription_file();
    }
    
    public void doSendServURLandDesc() throws JSONException {
        // check web service
        String GET_resp = "";
        try {
            // http://140.115.110.69:8080/SensorThingsService/v1.0/Things?$filter=properties/UID%20eq%20%27MY_DEVICE018%27&$select=id&$expand=Datastreams($select=id,name)&$expand=TaskingCapabilities($select=id,name)
            String GETThing_url = service_URL + "/Things?$filter=properties/UID%20eq%20%27" + this.device_ID + "%27&$select=id&$expand=Datastreams($select=id,name;$count=true),TaskingCapabilities($select=id,name;$count=true)&$count=true";
            
            GET_resp = new HTTPManager().sendGet(GETThing_url);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PNPRequest_UploadObs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PNPRequest_SendServURL.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JSONObject getThing_response_jsonObject = new JSONObject(GET_resp);
        
        // Description file JSON
        JSONObject description_file_jsonObject = new JSONObject(this.description_file);

        /* Thing information */
        String Thing_name = description_file_jsonObject.getString("name");
        String Thing_description = description_file_jsonObject.getString("description");
        String Thing_properties = description_file_jsonObject.getJSONObject("properties").toString();

        String Thing_UID = description_file_jsonObject.getJSONObject("properties").getString("UID");
        String Thing_Locations = null;
        if (description_file_jsonObject.has("Locations")) {
            Thing_Locations = description_file_jsonObject.getJSONArray("Locations").toString();System.out.println("Thing_Locations: "+Thing_Locations);
        }

        /* Datastream information */        
        JSONArray Datastreams_jsonArray = null;
        int datastream_number = 0;
        if (description_file_jsonObject.has("Datastreams")) {
            Datastreams_jsonArray = description_file_jsonObject.getJSONArray("Datastreams");
            datastream_number = Datastreams_jsonArray.length();
        }

        /* TaskingCapability information */
        JSONArray TaskingCapabilities_jsonArray = null;
        int TaskingCapability_number = 0;
        if (description_file_jsonObject.has("TaskingCapabilities")) {
            TaskingCapabilities_jsonArray = description_file_jsonObject.getJSONArray("TaskingCapabilities");
            TaskingCapability_number = TaskingCapabilities_jsonArray.length();
        }

        /* Lookup table preperation */        
        String Thing_id = null;                 // will get "Thing id" after Thing POST 
        String Datastream_id = null;            // will get "Datastream id" after Datastream POST 
        String TaskingCapability_id = null;     // will get " TaskingCapability id" after TaskingCapability POST 

        JSONObject Datastream_id_list = null;
        JSONObject TaskingCapability_parameter_list = null;
        
        int count = getThing_response_jsonObject.getInt("@iot.count");        
        // device doesn't exist in service
        if (count == 0) {
            System.out.println("device: " + device_ID + " doesn't exist in service");
            
            // Create Thing
            Thing Thing = new Thing (
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
            
            // Create Datastream
            if (Datastreams_jsonArray != null) {
                System.out.println("Prepare Datastreams");
                Datastream_id_list = new JSONObject();
                for (int ds_count = 0; ds_count<datastream_number; ds_count++) {
                    JSONObject datastream_jsonObject = Datastreams_jsonArray.getJSONObject(ds_count);

                    String datastream_name = datastream_jsonObject.getString("name");
                    String datastream_description = datastream_jsonObject.getString("description");
                    String datastream_observationType = datastream_jsonObject.getString("observationType");
                    String datastream_uom = datastream_jsonObject.getJSONObject("unitOfMeasurement").toString();
                    String datastream_Sensor = datastream_jsonObject.getJSONObject("Sensor").toString();
                    String datastream_ObservedProperty = datastream_jsonObject.getJSONObject("ObservedProperty").toString();

                    Datastream Datastream = new Datastream (
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
                    int POST_Datastream_breakout = 0;
                    try {
                        do {
                            String location = new HTTPManager().sendPost(service_URL, "Things(" + Thing_id + ")/Datastreams", post_Datastream_String);
                            POST_Datastream_breakout++;

                            if (location != null){
                                Datastream_id = location.substring(location.indexOf("(")+1, location.indexOf(")"));
                                Datastream.setID(Integer.valueOf(Datastream_id));

                                    System.out.print("Datastream_id: ");
                                    System.out.println(Datastream_id);
                            }

                            // set limit times
                            if (POST_Datastream_breakout == 5)  
                                break;
                        } while (Datastream_id == null);                        
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(PNPRequest_UploadObs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }

                    // prepare DSID list
                    Datastream_id_list.put(datastream_name, Integer.valueOf(Datastream_id));
                }
            }
            
            // Create TaskingCapability
            if (TaskingCapabilities_jsonArray != null) {
                System.out.println("Prepare TaskingCapabilities");
                for (int tc_count = 0; tc_count<TaskingCapability_number; tc_count++) {
                    // taskingcapability in description file
                    JSONObject taskingcapability_jsonObject = TaskingCapabilities_jsonArray.getJSONObject(tc_count);

                    String taskingcapability_name = taskingcapability_jsonObject.getString("name");                                                                 System.out.println("taskingcapability_name: "+taskingcapability_name);
                    String taskingcapability_description = taskingcapability_jsonObject.getString("description");                                                   System.out.println("taskingcapability_description: "+taskingcapability_description);
                    String taskingcapability_taskingParameters = taskingcapability_jsonObject.getJSONObject("taskingParameters").toString();                        System.out.println("taskingcapability_taskingParameters: "+taskingcapability_taskingParameters);
                    String taskingcapability_actuator = taskingcapability_jsonObject.getJSONObject("Actuator").toString();                                          System.out.println("taskingcapability_actuator: "+taskingcapability_actuator);

                    String taskingcapability_properties = null;
                    if (taskingcapability_jsonObject.has("properties")) {
                        taskingcapability_properties = taskingcapability_jsonObject.getJSONObject("properties").toString();
                    }

                    // Store taskingcapability to lookuptable
                    JSONObject taskingcapability_to_lookuptable = new JSONObject();

                    // translate protocol into HTTP
                    JSONObject taskingcapability_httpProtocol_jsonObject = null;
                    String taskingcapability_httpProtocol = null;                    
                    ServerConfig s = new ServerConfig();
                    String gatewayURL = s.getGatewayURL();
                    // ZigBee protocol
                    String HTTP_msgbody = "";
                    JSONObject taskingcapability_zigbeeProtocol = null;
                    if (taskingcapability_jsonObject.has("zigbeeProtocol")){
                        taskingcapability_zigbeeProtocol = taskingcapability_jsonObject.getJSONObject("zigbeeProtocol");                    
                        taskingcapability_to_lookuptable.put("zigbeeProtocol", taskingcapability_zigbeeProtocol);

                        int parameters_number = taskingcapability_jsonObject.getJSONObject("taskingParameters").getJSONArray("field").length();
                        for (int parameters_count = 0; parameters_count<parameters_number; parameters_count++) {
                            String parameter_name = taskingcapability_jsonObject.getJSONObject("taskingParameters").getJSONArray("field").getJSONObject(parameters_count).getString("name");System.out.println("parameter_name: "+parameter_name);
                            HTTP_msgbody = HTTP_msgbody + "\"" + parameter_name + "\"" + ":{" + parameter_name + "}";
                            if (parameters_count<(parameters_number-1)){
                                HTTP_msgbody = HTTP_msgbody + ",";
                            }
                        }

                        // assemble httpProtocol
                        HTTP_msgbody = "{" + HTTP_msgbody + ",\"TaskingCapability_name\":\"" + taskingcapability_name + "\",\"device_ID\":\"" + Thing_UID + "\"}";
                        /* 
                            HTTP_msgbody: {\"on\":{on},\"device_ID\":\"MY_DEVICE4\",\"TC_name\":\"LightBulb\"}
                        */

                        System.out.print("HTTP_msgbody: ");System.out.println(HTTP_msgbody);
                        
                        taskingcapability_httpProtocol_jsonObject = new JSONObject()
                                .put("httpMethod", "POST")
                                .put("absoluteResourcePath", gatewayURL)
                                .put("contentType", "application/json")
                                .put("messageBody", HTTP_msgbody);
                                                
                        if (taskingcapability_jsonObject.getJSONObject("zigbeeProtocol").has("optionalParameterList")) {
                            taskingcapability_httpProtocol_jsonObject
                                    .put(
                                            "optionalParameterList", 
                                            taskingcapability_jsonObject.getJSONObject("zigbeeProtocol").getJSONObject("optionalParameterList").toString()
                                    );
                        }
                        taskingcapability_httpProtocol = taskingcapability_httpProtocol_jsonObject.toString();System.out.println("taskingcapability_httpProtocol: "+taskingcapability_httpProtocol);
                    }

                    TaskingCapability TaskingCapability = new TaskingCapability (
                        taskingcapability_name, 
                        taskingcapability_description, 
                        taskingcapability_taskingParameters,
                        taskingcapability_actuator,
                        taskingcapability_properties,
                        taskingcapability_httpProtocol.toString()
                    );

                    TaskingCapability.setTaskingCapabilityJSONObject();
                    String post_TaskingCapability_String = TaskingCapability.getTaskingCapabilityString();

                        System.out.print("TaskingCapability: ");
                        System.out.println(post_TaskingCapability_String);
                  
                    int POST_TaskingCapability_breakout = 0;
                    try {
                        do {
                            String location = new HTTPManager().sendPost(service_URL, "Things(" + Thing_id + ")/TaskingCapabilities", post_TaskingCapability_String);
                            POST_TaskingCapability_breakout++;
    
                            if (location != null){
                                TaskingCapability_id = location.substring(location.indexOf("(")+1, location.indexOf(")"));
                                TaskingCapability.setID(Integer.valueOf(TaskingCapability_id));
                                
                                    System.out.print("TaskingCapability_id: ");
                                    System.out.println(TaskingCapability_id);
                            }
    
                            // set limit times
                            if (POST_TaskingCapability_breakout == 5)
                                break;
                        } while (TaskingCapability_id == null);                        
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(PNPRequest_UploadObs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                    
                    // prepare TaskingCapability parameter list
                    TaskingCapability_parameter_list = new JSONObject()
                            .put(
                                    taskingcapability_name, 
                                    taskingcapability_to_lookuptable
                                        .put(
                                                "TCID", 
                                                TaskingCapability.getID()
                                        )
                                        .put(
                                                "taskingParameters", new JSONObject(taskingcapability_taskingParameters)
                                        )
                            );
                }
            }
            
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
            
            // Confirm *****************************************************
            JSONObject Confirm = new JSONObject();
            Confirm.put("operation", "Confirm").put("device_ID", this.device_ID);
            String Confirm_string = Confirm.toString() + "<CR>";

            byte[] Confirm_buffer = Confirm_string.getBytes();
            new XBeeManager(this.comPort).XBeeSendMsg(Confirm_buffer);
            // Confirm *****************************************************
        }
        
        // device doesn't exist in gateway but exists in service
        else {
            System.out.println("device: " + device_ID + " exists in service");
            /*
                {
                    "@iot.count": 1,
                    "value": [
                        {
                            "Datastreams": [
                                {
                                    "name": "sensor1",
                                    "@iot.id": 62
                                },
                                {
                                    "name": "sensor2",
                                    "@iot.id": 63
                                }
                            ],
                            "Datastreams@iot.count": 2,
            
                            "TaskingCapabilities": [
                                {
                                    "name": "Hue",
                                    "@iot.id": 133
                                }
                            ],
                            "TaskingCapabilities@iot.count": 0,
            
                            "@iot.id": 131
                        }
                    ]
                }
            */
            
            // Update Gateway URL
            int thing_id_int = getThing_response_jsonObject.getJSONArray("value").getJSONObject(0).getInt("@iot.id");
            int datastreams_number = getThing_response_jsonObject.getJSONArray("value").getJSONObject(0).getInt("Datastreams@iot.count");
            JSONArray datastreams_jsonarray = new JSONArray();
            if (datastreams_number != 0) {
                for (int datastreams_count=0; datastreams_count<datastreams_number; datastreams_count++) {
                    Datastream_id_list.put(
                        datastreams_jsonarray.getJSONObject(datastreams_count).getString("name"), 
                        datastreams_jsonarray.getJSONObject(datastreams_count).getInt("@iot.id")
                    );
                }
            }
            
            JSONArray taskingcapabilities_in_service = getThing_response_jsonObject.getJSONArray("value").getJSONObject(0).getJSONArray("TaskingCapabilities");
            int taskingcapabilities_in_service_number = getThing_response_jsonObject.getJSONArray("value").getJSONObject(0).getInt("TaskingCapabilities@iot.count");
            
            for(int taskingcapability_count = 0; taskingcapability_count < taskingcapabilities_in_service_number; taskingcapability_count++){
            // for each taskingcapability in service
            
                int taskingcapability_id_in_service =taskingcapabilities_in_service.getJSONObject(taskingcapability_count).getInt("@iot.id");
                String taskingcapability_name_in_service = taskingcapabilities_in_service.getJSONObject(taskingcapability_count).getString("name");
                                
                // match to description file
                for (int tc_from_descriptionfile=0; tc_from_descriptionfile<taskingcapabilities_in_service_number; tc_from_descriptionfile++) {
                    String HTTP_msgbody = "";
                    JSONObject TaskingCapability = TaskingCapabilities_jsonArray.getJSONObject(tc_from_descriptionfile); // Tasking Capability from description file
                    String TaskingCapability_name = TaskingCapability.getString("name");
                    
                    // for lookuptable
                    JSONObject this_taskingcapability = new JSONObject();
                    
                    // if match, then PATCH
                    if (TaskingCapability_name.equals(taskingcapability_name_in_service)) {
                        JSONArray taskingParameters = TaskingCapability.getJSONObject("taskingParameters").getJSONArray("field");
                        
                        this_taskingcapability.put("taskingParameters", TaskingCapability.getJSONObject("taskingParameters"));
                        this_taskingcapability.put("TCID", taskingcapability_id_in_service);
                        
                        JSONObject TaskingCapability_zigbeeProtocol = null;
                        if (TaskingCapability.has("zigbeeProtocol")){
                            TaskingCapability_zigbeeProtocol = TaskingCapability.getJSONObject("zigbeeProtocol");
                            
                            this_taskingcapability.put("zigbeeProtocol", TaskingCapability_zigbeeProtocol);
                            
                            int parameters_number = taskingParameters.length();
                            for (int tc_parameter_count = 0; tc_parameter_count<parameters_number; tc_parameter_count++) {
                                String parameter_name = taskingParameters.getJSONObject(tc_parameter_count).getString("name");
                                HTTP_msgbody = HTTP_msgbody + "\\\"" + parameter_name + "\\\"" + ":{" + parameter_name + "}";
                                if (tc_parameter_count<(parameters_number-1)){
                                    HTTP_msgbody = HTTP_msgbody + ",";
                                }
                            }
                            HTTP_msgbody = "{" + HTTP_msgbody + ",\\\"TaskingCapability_name\\\":\\\"" + TaskingCapability_name + "\\\",\\\"device_ID\\\":\\\"" + device_ID + "\\\"}";
                            /* 
                                HTTP_msgbody: {\"on\":{on},\"device_ID\":\"MY_DEVICE4\",\"TaskingCapability_name\":\"LightBulb\"}
                            */
                            System.out.print("HTTP_msgbody: ");System.out.println(HTTP_msgbody);
                            
                            
                            JSONObject PATCH_content_jsonObject = new JSONObject();                            
                            ServerConfig s = new ServerConfig();
                            String gatewayURL = s.getGatewayURL();

                            if (TaskingCapability_zigbeeProtocol.has("optionalParameterList")) {
                                JSONObject optionalParameterList = TaskingCapability_zigbeeProtocol.getJSONObject("optionalParameterList");
                                PATCH_content_jsonObject
                                        .put(
                                                "httpProtocol",
                                                new JSONObject()
                                                    .put("httpMethod", "POST")
                                                    .put("absoluteResourcePath", gatewayURL)
                                                    .put("contentType", "application/json")
                                                    .put("messageBody", HTTP_msgbody)
                                                    .put("optionalParameterList", optionalParameterList)
                                        );
                                // PATCH_content = "{\"httpProtocol\":[{\"httpMethod\":\"POST\",\"absoluteResourcePath\": \"" + gatewayURL + "\",\"contentType\":\"application/json\",\"messageBody\":\""+ HTTP_msgbody + "\",\"optionalParameterList\":\""+optionalParameterList+"\"}]}";
                            } else {
                                PATCH_content_jsonObject
                                        .put(
                                                "httpProtocol",
                                                new JSONObject()
                                                    .put("httpMethod", "POST")
                                                    .put("absoluteResourcePath", gatewayURL)
                                                    .put("contentType", "application/json")
                                                    .put("messageBody", HTTP_msgbody)
                                        );
                                // PATCH_content = "{\"httpProtocol\":[{\"httpMethod\":\"POST\",\"absoluteResourcePath\": \"" + gatewayURL + "\",\"contentType\":\"application/json\",\"messageBody\":\""+ HTTP_msgbody + "\"}]}";
                            }
                            
                            System.out.println("PATCH: "+PATCH_content_jsonObject.toString());
                            // PATCH
                            try {
                                new HTTPManager().sendPatch (service_URL, "TaskingCapabilities(" + Integer.toString(taskingcapability_id_in_service) + ")", PATCH_content_jsonObject.toString());
                            } catch (IOException ex) {
                                java.util.logging.Logger.getLogger(PNPRequest_UploadObs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                            }
                        }
                    }
                    TaskingCapability_parameter_list = new JSONObject().put(TaskingCapability_name, this_taskingcapability);
                }
                
                // prepare for Lookup Table
                JSONObject lookuptable_jsonObject = new JSONObject();
                lookuptable_jsonObject.put("device_ID", this.device_ID);
                lookuptable_jsonObject.put("Things_ID", thing_id_int);
                lookuptable_jsonObject.put("service_URL", this.service_URL);
                System.out.println("lookuptable_jsonObject 1: " + lookuptable_jsonObject.toString());
                
                // prepare DSID list                
                if (Datastream_id_list != null)
                    lookuptable_jsonObject.put("Datastream_id_list", Datastream_id_list);
                if (TaskingCapability_parameter_list != null)
                    lookuptable_jsonObject.put("TaskingCapability_parameter_list", TaskingCapability_parameter_list);
                System.out.println("lookuptable_jsonObject 2: " + lookuptable_jsonObject.toString());
                
                LookupTableManager lookupTable = new LookupTableManager();
                lookupTable.updateLookupTable(lookuptable_jsonObject.toString(), device_ID);
                System.out.print("Updated: ");System.out.println(lookuptable_jsonObject.toString()+"\n");
                
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
}