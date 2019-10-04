/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SensorThingsAPI_data_model;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author user
 */
public class TaskingCapability {
    private int id = Integer.MAX_VALUE;
    private String name = null;    
    private String description = null;
    private String properties = null;    
    private String taskingParameters = null;    
    private String Actuator = null; 
    private String httpProtocol = null;
    
//    private boolean setID;
//    private boolean setName;
//    private boolean setDescription;
//    private boolean setTaskingParameters;
//    private boolean setThing;    
    
    private Thing Thing = null;
    JSONObject postTaskingCapabilityRequest = new JSONObject();
    JSONArray taskingCapabilityJSONArray = null;

    public TaskingCapability(
            String name, 
            String description, 
            String taskingParameters,
            String Actuator) {
        this(name, description, taskingParameters, Actuator, null, null);
    }
    
    public TaskingCapability(
            String name,
            String description,            
            String taskingParameters,
            String Actuator,
            
            String properties,
            String httpProtocol){
        this.name = name;
        this.description = description;        
        this.taskingParameters = taskingParameters;
        this.Actuator = Actuator;
        
        // optional
        if (properties != null) {
            this.properties = properties;
        }
        if (httpProtocol != null) {
            this.httpProtocol = httpProtocol;
        }        
        System.out.println("name: "+this.name);
        System.out.println("description: "+this.description);
        System.out.println("taskingParameters: "+this.taskingParameters);
        System.out.println("Actuator: "+this.Actuator);
        System.out.println("properties: "+this.properties);
        System.out.println("httpProtocol: "+this.httpProtocol);
    }
    
    public TaskingCapability(JSONObject taskingCapabilityObj){
        
    }
    
    // id
    public void setID(int id) {
        this.id = id;
//        setID = true;
    }
    
    public int getID() {
        return id;
    }
    
//    public boolean isSetID() {
//        return setID;
//    }
    
    // name
//    public void setName(String name) {
//        this.name = name;
//        setName = true;
//    }
    
    public String getName() {
        return name;
    }
    
//    public boolean isSetName() {
//        return setName;
//    }
    
    // description
//    public void setDescription(String description) {
//        this.description = description;
//        setDescription = true;
//    }
    
    public String getDescription() {
        return description;
    }
    
//    public boolean isSetDescription() {
//        return setDescription;
//    }
    
    // parameters
//    public void setaskingParameters(JSONArray parameters) {
//        this.parameters = parameters;
//        setParameters = true;
//    }
    
    public String getTaskingParameters() {
        return taskingParameters;
    }
    
//    public boolean isSetParameters() {
//        return setTaskingParameters;
//    }
    
    // protocol
    
    // Thing
    public void setThing(Thing thing) {
        this.Thing = thing;
//        setThing = true;
    }
    
    public Thing getThing() {
        return Thing;
    }
    
//    public boolean isSetThing() {
//        return setThing;
//    }
    
    public void setTaskingCapabilityJSONObject() {
        postTaskingCapabilityRequest
                .put("name", this.name)
                .put("description", this.description)
                .put("taskingParameters", new JSONObject(taskingParameters))
                .put("Actuator", new JSONObject(this.Actuator));
        
        if (properties!=null){
            postTaskingCapabilityRequest.put("properties", new JSONObject(this.properties));
        }
        
        if (httpProtocol!=null){
            postTaskingCapabilityRequest.put("httpProtocol", new JSONObject(httpProtocol));
        }
        
        System.out.println("setTaskingCapabilityJSONArray");
    }
    
    public void setTaskingCapabilityJSONObjectWithThingID() {
        postTaskingCapabilityRequest
                .put("name", this.name)
                .put("description", this.description)        
                .put("taskingParameters", new JSONObject(this.taskingParameters))
                .put("Actuator", new JSONObject(this.Actuator))
                .put("Thing", new JSONObject().put("@iot.id", Thing.getID()).toString());
        
        if (properties!=null){
            postTaskingCapabilityRequest.put("properties", new JSONObject(this.properties));
        }
        
        if (httpProtocol!=null){
            postTaskingCapabilityRequest.put("httpProtocol", new JSONObject(httpProtocol));
        }
    }   
    
    public JSONObject getTaskingCapabilityJSONObject() {
        return postTaskingCapabilityRequest;
    }
    
    public String getTaskingCapabilityString() {
        return postTaskingCapabilityRequest.toString();
    }
}
