/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnp_gateway.Request.device_request;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.Iterator;
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
public class PNPRequest_UpdateStatus {
    private COMPortManager comport_manager = new COMPortManager();
    private SerialPort comPort = comport_manager.getCOMPort();
    
    private PNPRequest root_request = new PNPRequest();
    
    private String device_ID = "";
    private String service_URL = "";
    private String description_file = "";    // from msg_body
    
    /* exmample:
        {
            "operation": "UpdateStatus",
            "device_ID": "<device_ID>"
        }
    */
    
    public PNPRequest_UpdateStatus(PNPRequest request, SerialPort comPort) {
        this.comPort = comPort;
        this.root_request = request;
        
        this.device_ID = request.getDevice_ID();
    }
    
    public void doUpdateStatus() throws JSONException {
        System.out.println("\nQuery Lookup Table for device: " + device_ID);
        LookupTableManager lookupTable = new LookupTableManager();
        String query_lookupTable_result = lookupTable.queryLookupTable(device_ID);

        System.out.println(query_lookupTable_result);
        /*  lookupTable record:
            {
                "device_ID": "MY_DEVICE002",
                "Thing_ID": "2",
                "service_URL": "140.115.110.69:8080/STA/v1.0",
                "Datastream_id_list": {
                    "sensor1": 3,
                    "sensor2": 4
                },
                "TaskingCapability_parameter_list": {
                    "LightBulb": {                        
                        "TCID": 20,
                        "parameters": [
                            {
                                "name": "on",
                                "use": "Mandatory",
                                "description": "turn on or off",
                                "inputType":"Boolean",
                                "unitOfMeasurement":"status",
                                "constraint":{
                                    "value":[
                                        true,
                                        false
                                    ]
                                }
                            },
                            {
                                "name": "brightness",
                                "use": "optional",
                                "description": "change brightness",
                                "inputType":"Integer",
                                "unitOfMeasurement":"brightness degree",
                                "constraint":{
                                    "value":[
                                        {
                                            "Max":254,
                                            "Min":1
                                        }
                                    ]
                                }
                            } 
                        ],
                        "zigbeeProtocol": {
                            "messageBody": "Status:{on},Brightness:{brightness}",
                            "addressingSH": "0013A200",
                            "addressingSL": "40BF8550",
                            "messageDataType": "application/text",                            
                            "optionalParameterList":{
                                "status":[
                                    "Status:{status},",
                                    ",Status:{status}"
                                ],
                                "brightness":[
                                    ",Brightness:{brightness}",
                                    "Brightness:{brightness},"
                                ]
                            }
                        }
                    }
                }
            }
        */
        
        JSONObject lookupTable_jsonObject = null;
        if (query_lookupTable_result != null){
            lookupTable_jsonObject = new JSONObject(query_lookupTable_result);
        }
        
        if ((lookupTable_jsonObject == null)||(!lookupTable_jsonObject.has("TaskingCapability_parameter_list"))) {
            // device or taskingcapability doesn't exist in lookuptable
            if (query_lookupTable_result == null) {
                System.out.println("Device doesn't exist in lookup table");
            } else if (!lookupTable_jsonObject.has("TaskingCapability_parameter_list")) {
                System.out.println("Lack of Tasking Capability description in Lookup Table");
            }
            
            System.out.println("\nGet description file from device: " + device_ID);
                      
            // ask service URL and description file *****************************************
            JSONObject GetServURLandDesc = new JSONObject();
            GetServURLandDesc.put("operation", "GetServURLandDesc").put("device_ID", this.device_ID);
            String GetServURLandDesc_string = GetServURLandDesc.toString() + "<CR>";

            byte[] GetServURLandDesc_buffer = GetServURLandDesc_string.getBytes();
            new XBeeManager(this.comPort).XBeeSendMsg(GetServURLandDesc_buffer);
            // ask service URL and description file *****************************************
            
            // wait response
            String SendServURLandDesc = "";
            JSONObject SendServURLandDesc_JSON;
            do {
                System.out.println("listening GetServURLandDesc response...");
                SendServURLandDesc = new XBeeManager(this.comPort).XBeeReadMsg();
                SendServURLandDesc_JSON = new JSONObject(SendServURLandDesc);
                /* 
                    {
                        "operation": "SendServURLandDesc",
                        "device_ID": "<device_ID>",
                        "service_URL": "<service_URL>",
                        "msg_body ": "<description_file>"
                    }
                */
            } while ( !( (SendServURLandDesc_JSON.getString("device_ID")).equals(device_ID) ) );
            System.out.println("Recieved response: " + SendServURLandDesc);

            root_request.doRequest(SendServURLandDesc);
            // wait GetDesc req ****************************************
        }
                
        else {
            /* update gateway location */
            /*  lookupTable record:
                {
                    "device_ID": "MY_DEVICE002",
                    "Thing_ID": "2",
                    "service_URL": "140.115.110.69:8080/STA/v1.0",
                    "DSID": {
                        "sensor1": 3,
                        "sensor2": 4
                    },
                    "TaskingCapability_parameter_list": {
                        "LightBulb": {
                            "zigbeeProtocol": {
                                "messageBody": "Status:{on},Brightness:{brightness}",
                                "addressingSH": "0013A200",
                                "addressingSL": "40BF8550",
                                "messageDataType": "application/text",                            
                                "optionalParameterList":{
                                    "status":[
                                        "Status:{status},",
                                        ",Status:{status}"
                                    ],
                                    "brightness":[
                                        ",Brightness:{brightness}",
                                        "Brightness:{brightness},"
                                    ]
                                }
                            },
                            "TCID": 20,
                            "parameters": [
                                {
                                    "name": "on",
                                    "use": "Mandatory",
                                    "description": "turn on or off",
                                    "inputType":"Boolean",
                                    "unitOfMeasurement":"status",
                                    "constraint":{
                                        "value":[
                                            true,
                                            false
                                        ]
                                    }
                                },
                                {
                                    "name": "brightness",
                                    "use": "optional",
                                    "description": "change brightness",
                                    "inputType":"Integer",
                                    "unitOfMeasurement":"brightness degree",
                                    "constraint":{
                                        "value":[
                                            {
                                                "Max":254,
                                                "Min":1
                                            }
                                        ]
                                    }
                                } 
                            ]
                        }
                    }
                }
            */
            
            // Update Gateway URL
            this.service_URL = lookupTable_jsonObject.getString("service_URL");
            JSONObject PATCH_content = new JSONObject();
            int taskingcapability_id = 0;

            JSONObject taskingcapability_list_in_lookuptable = lookupTable_jsonObject.getJSONObject("TaskingCapability_parameter_list");
            System.out.println(taskingcapability_list_in_lookuptable);
            
            Iterator iter = taskingcapability_list_in_lookuptable.keys();
            while(iter.hasNext()){
                String taskingcapability_name = iter.next().toString(); // Taskingcapability parameter name in lookup table

                JSONObject taskingcapability_jsonobject_in_lookuptable = taskingcapability_list_in_lookuptable.getJSONObject(taskingcapability_name);
                taskingcapability_id = taskingcapability_jsonobject_in_lookuptable.getInt("TCID");
                JSONArray parameters_jsonarray = taskingcapability_jsonobject_in_lookuptable.getJSONObject("taskingParameters").getJSONArray("field");

                // translate protocol
                String parameter_template = "";
                String HTTP_msgbody = "";       // POST
                // String HTTP_QueryString = "";   // GET
                // find protocol
                JSONObject protocol = new JSONObject();
                Iterator iter_for_taskingcapability = taskingcapability_jsonobject_in_lookuptable.keys();
                while(iter_for_taskingcapability.hasNext()){
                    String protocol_name = iter_for_taskingcapability.next().toString();
                    // translate zigbeeProtocol to Gateway protocol: {"parameterID":{parameterID},"TC_name":"<parameter_name>","device_ID":"<device_ID>"}
                    if (protocol_name.equals("zigbeeProtocol")) {
                        protocol = taskingcapability_jsonobject_in_lookuptable.getJSONObject("zigbeeProtocol");
                        int parameters_num = parameters_jsonarray.length();
                        for (int parameter_count = 0; parameter_count<parameters_num; parameter_count++) {
                            String parameter_name = parameters_jsonarray.getJSONObject(parameter_count).getString("name");
                            parameter_template = parameter_template + "\\\"" + parameter_name + "\\\"" + ":{" + parameter_name + "}"; 
                            if (parameter_count<(parameters_num-1)){
                                parameter_template = parameter_template + ",";
                                // HTTP_QueryString = HTTP_QueryString + "&";
                            }
                            // \\\"parameter_name_1\\\":{parameter_name_1},\\\"parameter_name_2\\\":{parameter_name_2}
                        }
                        /* TO-DO: 注意斜線問題 */
                        HTTP_msgbody = "{" + parameter_template + ",\\\"TaskingCapability_name\\\":\\\"" + taskingcapability_name + "\\\",\\\"device_ID\\\":\\\"" + device_ID + "\\\"}"; 
                        /* 
                            HTTP_msgbody: 
                            {
                                \\\"parameter_name_1\\\":{<parameter_name_1>},
                                \\\"parameter_name_2\\\":<{parameter_name_2>},
                                \\\"TaskingCapability_name\\\":\\\"<taskingcapability_name>\\\",
                                \\\"device_ID\\\":\\\"<device_ID>\\\"
                            }

                            {"buzz_time":{buzz_time},"device_ID":"NGISTaskingDevice001","TaskingCapability_name":"buzz_time"}
                            {"msg":{msg},"device_ID":"NGISTaskingDevice003","TaskingCapability_name":"displaymsg"}
                        */
                        ServerConfig s = new ServerConfig();
                        String gatewayURL = s.getGatewayURL();
                        
                        if (protocol.has("optionalParameterList")) {                            
                            JSONObject optionalParameterList = protocol.getJSONObject("optionalParameterList");
                            PATCH_content
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
                            PATCH_content
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
                    }
                }
                
                System.out.print("HTTP_msgbody: ");System.out.println(HTTP_msgbody);
                System.out.print("PATCH_content: ");System.out.println(PATCH_content.toString());
            
                try {
                    new HTTPManager().sendPatch(service_URL, "TaskingCapabilities(" + taskingcapability_id + ")", PATCH_content.toString());
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(PNPRequest_UpdateStatus.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }
            System.out.println("\nUpdated Gateway URL\n");

            // Confirm *****************************************************
            String Confirm = "{\"operation\":\"Confirm\",\"device_ID\":\"" + device_ID + "\"}<CR>";
            byte[] Confirm_buffer = Confirm.getBytes();
            new XBeeManager(this.comPort).XBeeSendMsg(Confirm_buffer);
            // Confirm *****************************************************
        }
    }
}
