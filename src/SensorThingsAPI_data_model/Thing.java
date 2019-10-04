/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SensorThingsAPI_data_model;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author user
 */
public class Thing {
    private int id = Integer.MAX_VALUE;
    private String name = null;
    private String description = null;
    private String properties = null;
    
    // Optional
    private String Locations = null;
    private ArrayList<Datastream> Datastreams = new ArrayList<>();  // 0..*
    private ArrayList<TaskingCapability> TaskingCapabilities  = new ArrayList<>();  // 0..*

//    private boolean setID;
//    private boolean setName;
//    private boolean setDescription;
//    private boolean setProperties;
//    private boolean setLocations;
//    private boolean setTaskingCapabilities;
    
    private JSONObject postThingRequest = new JSONObject();;
    
    public Thing(
            String name,
            String description,
            String properties) {
        this.name = name;
        this.description = description;
        this.properties =properties;
        setThingJSONObject();
    }
    
    public Thing(
            String name,
            String description,
            String properties,
            String Locations) {
        this.name = name;
        this.description = description;
        this.properties =properties;
        this.Locations = Locations;
        
        setThingJSONObject();
    }
    
    public Thing(JSONObject thingObj) {
        this.name = thingObj.getString("name");
        this.description = thingObj.getString("description");
        this.properties = thingObj.getJSONObject("properties").toString();
        
        setThingJSONObject();
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
    
    // properties
//    public void setProperties(String properties) {
//        this.properties = properties;
//        setProperties = true;
//    }
    
    public String getProperties() {
        return properties;
    }
    
//    public boolean isSetProperties() {
//        return setProperties;
//    }
    
    // Locations
//    public void setLocations(String locations) {
//        this.locations = locations;
//        setLocations = true;
//    }
    
    public String getLocations() {
        return Locations;
    }
    
//    public boolean isSetLocations() {
//        return setLocations;
//    }
    
    // Locations
    public void setLocationsFromString(String Locations) {
        this.Locations = Locations;
    }
    
    // Datastreams
    public void addDatastreams(Datastream datastream) {
        this.Datastreams.add(datastream);
    }
    
    public ArrayList<Datastream> getDatastreams() {
        return Datastreams;
    }
    
    public int getDatastreamNumber() {
        return Datastreams.size();
    }
    
    // TaskingCapabilities
    public void addTaskingCapability(TaskingCapability taskingCapabilities) {
        this.TaskingCapabilities.add(taskingCapabilities);
    }
     
    public ArrayList<TaskingCapability> getTaskingCapability() {
        return TaskingCapabilities;
    }
    
//    public boolean isSetTaskingCapability() {
//        return setTaskingCapabilities;
//    }
    
    public int getTaskingCapabilityNumber() {
        return TaskingCapabilities.size();
    }
    
    private void setThingJSONObject() {
        postThingRequest
                .put("name", this.name)
                .put("description", this.description)
                .put("properties", new JSONObject(this.properties));
        
        if (Locations != null) {
            postThingRequest.put("Locations", new JSONArray(this.Locations));
        }
        
        if (Datastreams.size() != 0) {
            postThingRequest.put("Datastreams", Datastreams.toArray());
        }
        
        if (TaskingCapabilities.size() != 0) {
            postThingRequest.put("TaskingCapabilities", TaskingCapabilities.toArray());
        }
    }
    
    public JSONObject getThingJSONObject() {
        setThingJSONObject();
        return postThingRequest;
    }
    
    public String getThingSting() {
        setThingJSONObject();
        return postThingRequest.toString();
    }
}