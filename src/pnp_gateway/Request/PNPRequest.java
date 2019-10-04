/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnp_gateway.Request;

import pnp_gateway.Request.device_request.PNPRequest_UploadObs;
import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONObject;
import org.json.JSONException;
import pnp_gateway.Request.device_request.PNPRequest_SendDesc;
import pnp_gateway.Request.device_request.PNPRequest_SendServURL;
import pnp_gateway.Request.device_request.PNPRequest_UpdateStatus;
import pnp_gateway.Request.device_request.PNPRequest_SendServURLandDesc;
import pnp_gateway.util.COMPortManager;

/**
 *
 * @author user
 */
public class PNPRequest extends javax.servlet.http.HttpServlet {
    private COMPortManager comport_manager = new COMPortManager();
    private SerialPort comPort = comport_manager.getCOMPort();

    private String operation = "";
    private String device_ID = "";
    private String service_URL = "";
    private String observation = "";
    private String description_file = "";

    public PNPRequest(){
    }

    public PNPRequest(String request, SerialPort comPort) {
        this.comPort = comPort;
        doRequest(request);
    }

    public void doRequest(String request) {
        /*  example --> operation, 
                        device_ID, 
                        msg_body, 
                        observation (UploadObs only) 
        */

        System.out.println("[Request]:");

            JSONObject request_jsonObject = new JSONObject(request);
            this.operation = request_jsonObject.getString("operation");
                            System.out.println("operation: " + operation);
            this.device_ID = request_jsonObject.getString("device_ID");
                            System.out.println("device_ID: " + device_ID);

            if (operation.equals("UploadObs")) {
                this.observation = request_jsonObject.getJSONArray("msg_body").toString();
                            System.out.println("message_body: " + observation);
                
                new PNPRequest_UploadObs(this, comPort).doUploadObs();
            }
            
            else if (operation.equals("UpdateStatus")) {
                new PNPRequest_UpdateStatus(this, comPort).doUpdateStatus();
            }
            
            else if (operation.equals("SendServURL")) {
                this.service_URL = request_jsonObject.getString("service_URL");
                            System.out.println("service_URL: " + service_URL);
                            
                new PNPRequest_SendServURL(this, comPort).doSendServURL();
            }
            
            else if (operation.equals("SendDesc")) {
                this.description_file = request_jsonObject.getString("msg_body");
                            System.out.println("message_body: " + description_file);
                
                new PNPRequest_SendDesc(this, comPort).doSendDesc();
            } 
            
            else if (operation.equals("SendServURLandDesc")) {
                this.service_URL = request_jsonObject.getString("service_URL");
                this.description_file = request_jsonObject.getString("msg_body");
                            System.out.println("service_URL: " + service_URL);
                            System.out.println("message_body: " + description_file);
                new PNPRequest_SendServURLandDesc(this, comPort).doSendServURLandDesc();
            }
    }

    public String getDevice_ID() {
        return device_ID;
    }

    public String getService_URL() {
        return service_URL;
    }

    public String getObservation() {
        return observation;
    }

    public String getDescription_file() {
        return description_file;
    }
    
}
