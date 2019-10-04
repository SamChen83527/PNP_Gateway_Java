/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnp_gateway;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pnp_gateway.Request.PNPRequest;
import pnp_gateway.util.COMPortManager;
import pnp_gateway.util.LookupTableManager;
import pnp_gateway.util.XBeeManager;

/**
 *
 * @author user
 */
public class WorkerInterface extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    COMPortManager comport_manager = new COMPortManager();
    SerialPort comPort = comport_manager.getCOMPort();
    
    // This Happens Once and is Reused
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
    }
    
    protected void doGet( HttpServletRequest request,
                        HttpServletResponse response)
        throws ServletException, IOException {
        
        // <IP>/PNPGateway/service?message=opencomport
        String message = request.getParameter("message");
        request.setAttribute("message", message);
        
        String comport_open_message = message;
        String openConfirm = "comport closed";
        
        // check if the GET request is valid.
        if (comport_open_message.equals("opencomport")) {
            
            openConfirm = "comport opened";System.out.println("comport openedd\n");
            comPort.addDataListener(new SerialPortDataListener() {
                String data = "";

                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }
                
                // if the GET request is valid, the gateway with start looping this listener and serving the request from the devices.
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                       return;

                    //read data form the comport.
                    byte[] newData = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(newData, newData.length);

                    for (int i = 0; i<numRead; i++) {
                        data = data + (char)newData[i];
                    }

                    // Check if request is valid (<CR>), and then do something...
                    int indexCR = data.indexOf("<CR>");
                    if (indexCR != -1) {
                        System.out.println(data);
                        
                        try {
                            /*  Parse the data read from the comport:
                                example --> operation, 
                                            device_ID, 
                                            msg_body, 
                                            observation (UploadObs only) 
                            */
                            PNPRequest req = new PNPRequest(data, comPort);
                            
                            // clean buffer, wait for next request
                            System.out.println("clean buffer\n\n\n\n\n");
                            data = "";
                        } catch (JSONException e) {
                            e.printStackTrace();
                            data = "";
                            System.out.println(e);
                        }
                    }
                }
            });
        } 
        PrintWriter writer = response.getWriter();
        writer.println("doGet successful");
        writer.println("Your messagesssss is:");
        writer.println(openConfirm);
        
        // send task -------------------------------------------------------- //
        byte[] URLbuffer = openConfirm.getBytes();
        OutputStream URLoutputStream = comPort.getOutputStream();
        for (int outputSize = 0; outputSize<URLbuffer.length; outputSize++) {
            try {
                URLoutputStream.write(URLbuffer[outputSize]);
            } catch (IOException ex) {
                Logger.getLogger(COMPortManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        writer.close();
    }
    
    protected void doPost( HttpServletRequest request,
                         HttpServletResponse response)
        throws ServletException, IOException {
        // get requestBody -------------------------------------------------- //
        BufferedReader reader = request.getReader();
        String input = null;
        String requestBody = "";
        while((input = reader.readLine()) != null) {
            requestBody = requestBody + input;
        }
        
        System.out.println("pnp gateway recieved request: "+requestBody+"\n");
        
        /* To-Do: new Lookup Table scheme */
        // translate request
        /*
            {
                "device_ID": "TaskingDevice_092703",
                "TaskingCapability_name": "buzzer",
                "buzz_time": 10,
                "<para_name>": ...
                ...
                ...
            }
        */
        
        // Parse the Task from STA
        JSONObject req_jsonObject = new JSONObject(requestBody);
        String device_ID = req_jsonObject.getString("device_ID");                     // Thing UID-------------------
        String TaskingCapability_name = req_jsonObject.getString("TaskingCapability_name");    // TaskingCapability name------
        
        System.out.println("Device ID: "+ device_ID);
        System.out.println("TaskingCapability_name: "+TaskingCapability_name+"\n");
        
        // Query device record from Lookup Table
        LookupTableManager lookupTable = new LookupTableManager();
        String record = lookupTable.queryLookupTable(device_ID);
        JSONObject lookuptable_jsonObject = new JSONObject(record);  // Lookup Table record-----------
        JSONObject taskingcapability_jsonobject_in_lookuptable = 
                lookuptable_jsonObject.getJSONObject("TaskingCapability_parameter_list").getJSONObject(TaskingCapability_name); // TaskingCapability
                
        // translate task template to device task
        // task template /* light:{on} */
        String device_task = "";                
        // find protocol and task template
        JSONObject protocol = new JSONObject();
        String task_template = "";
        Iterator iter = taskingcapability_jsonobject_in_lookuptable.keys();
        while(iter.hasNext()){
            String protocol_name = iter.next().toString();                      System.out.println("protocol_name: "+protocol_name.toString());
            if (protocol_name.equals("zigbeeProtocol")) {                      
                protocol = taskingcapability_jsonobject_in_lookuptable.getJSONObject("zigbeeProtocol");
                                                                                System.out.println("protocol: "+protocol.toString());
                task_template = protocol.getString("messageBody");              System.out.println("Task_temp: "+task_template);
                device_task = task_template;
            }
            if (protocol_name.equals("loraProtocol")) {
                protocol = taskingcapability_jsonobject_in_lookuptable.getJSONObject("loraProtocol");     System.out.println("protocol: "+protocol.toString());
                task_template = protocol.getString("messageBody");              System.out.println("Task_temp: "+task_template);
                device_task = task_template;
            }
            // TO-DO: add more protocols
        }
        System.out.println("finished querying protocol ...\n");

        // parameters >> parameterID
        JSONArray parameters = taskingcapability_jsonobject_in_lookuptable.getJSONObject("taskingParameters").getJSONArray("field");    
                                                                                System.out.println("parameters: "+parameters.toString());
        int parameter_number = parameters.length();                             System.out.println("parameter_number: "+parameter_number);
        for (int i=0; i<parameter_number; i++){
            // parameterID
            String parameter_name = parameters.getJSONObject(i).getString("name"); System.out.println("parameter name: "+parameter_name);
            // inputType
            String inputType = parameters.getJSONObject(i).getJSONObject("definition").getString("inputType");
                                                                                System.out.println("inputType: "+inputType);
            String value = "";
            if (inputType.equals("Boolean")) {
                value = String.valueOf(req_jsonObject.getBoolean(parameter_name));
            }
            if (inputType.equals("Integer")) {
                value = String.valueOf(req_jsonObject.getInt(parameter_name));
            }
            if (inputType.equals("String")) {
                value = String.valueOf(req_jsonObject.getString(parameter_name));
            }

            String place_holder = "{" + parameter_name + "}";
            device_task = device_task.replace(place_holder, value);
        }
        
        System.out.println("device_task: "+device_task+"\n");
        
        // assigned destination /-----------------------------------------------
//        
//        // send task -------------------------------------------------------- //
//        byte[] URLbuffer = device_task.getBytes();
//        OutputStream URLoutputStream = comPort.getOutputStream();
//        for (int outputSize = 0; outputSize<URLbuffer.length; outputSize++) {
//            try {
//                URLoutputStream.write(URLbuffer[outputSize]);
//            } catch (IOException ex) {
//                Logger.getLogger(COMPortManager.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        
        // Send Task *****************************************************
        byte[] device_task_buffer = device_task.getBytes();
        new XBeeManager(this.comPort).XBeeSendMsg(device_task_buffer);
        // Send Task *****************************************************
        
        System.out.println("finished sending task ...\n\n\n");
        
        // create response----------------------------------------------------//        
        PrintWriter writer = response.getWriter();
        writer.println(requestBody);
        writer.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
