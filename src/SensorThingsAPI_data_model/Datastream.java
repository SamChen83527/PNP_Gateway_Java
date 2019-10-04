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
public class Datastream {
    private int id = Integer.MAX_VALUE;
    private String name = null;
    private String description = null;
    private String observationType = null;
    private String unitOfMeasurement = null;
    
    private Thing Thing = null;
    private String Sensor = null;
    private String ObservedProperty = null;
    
    // optional
    private String observedArea = null;
    private String phenomenonTime = null;
    private String resultTime = null;
    
//    private boolean setID;
//    private boolean setName;
//    private boolean setDescription;
//    private boolean setObservationType;
//    private boolean setUnitOfMeasurement;
//    private boolean setSensor;
//    private boolean setObservedProperty;
//    private boolean setThing;
    
    
    JSONObject postDatastreamRequest = new JSONObject();
    
    public Datastream(
            String name, 
            String description, 
            String observationType, 
            String unitOfMeasurement, 
            String Sensor, 
            String ObservedProperty,
            String Thing) {
        this.name = name;
        this.description = description;
        this.observationType = observationType;
        this.unitOfMeasurement = unitOfMeasurement;
        this.Sensor = Sensor;
        this.ObservedProperty = ObservedProperty;
        this.Thing = new Thing(new JSONObject(Thing));
        
        setDatastreamJSONObject();
    }
    
    public Datastream(JSONObject datastreamObj){
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
    
    // observationType
//    public void setObservationType(String observationType) {
//        this.observationType = observationType;
//        setObservationType = true;
//    }
    
    public String getObservationType() {
        return observationType;
    }
    
//    public boolean isSetObservationType() {
//        return setObservationType;
//    }
    
    // unitOfMeasurement
//    public void setUnitOfMeasurement(String unitOfMeasurement) {
//        this.unitOfMeasurement = unitOfMeasurement;
//        setUnitOfMeasurement = true;
//    }
    
    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }
    
//    public boolean isSetUnitOfMeasurement() {
//        return setUnitOfMeasurement;
//    }
    
    // Sensor
//    public void setSensort(String unitOfMeasurement) {
//        this.Sensor = Sensor;
//        setSensor = true;
//    }
    
    public String getSensor() {
        return Sensor;
    }
    
//    public boolean isSetSensor() {
//        return setSensor;
//    }
    
    // Sensor
//    public void setObservedProperty(String ObservedProperty) {
//        this.ObservedProperty = ObservedProperty;
//        setObservedProperty = true;
//    }
    
    public String getObservedProperty() {
        return ObservedProperty;
    }
    
//    public boolean isSetObservedProperty() {
//        return setObservedProperty;
//    }
    
    // Thing
    public void setThing(Thing thing) {
        this.Thing = thing;
    }
    
    public void setThingFromString(String Thing) {
        JSONObject thingObj = new JSONObject(Thing);
        Thing thing = new Thing(thingObj);
        this.Thing = thing;
    }
    
    public Thing getThing() {
        return Thing;
    }
    
//    public boolean isSetThing() {
//        return setThing;
//    }
    
    public void setDatastreamJSONObject() {
        postDatastreamRequest
            .put("name", this.name)
            .put("description", this.description)
            .put("observationType", observationType)
            .put("unitOfMeasurement", new JSONObject(this.unitOfMeasurement))
            .put("Sensor", new JSONObject(this.Sensor))
            .put("ObservedProperty", new JSONObject(this.ObservedProperty));
    }
    
    public void setDatastreamJSONObjectWithThingID() {
        postDatastreamRequest
            .put("name", this.name)
            .put("description", this.description)
            .put("observationType", observationType)
            .put("unitOfMeasurement", new JSONObject(this.unitOfMeasurement))
            .put("Sensor", new JSONObject(this.Sensor))
            .put("ObservedProperty", new JSONObject(this.ObservedProperty))
            .put("Thing", new JSONObject().put("@iot.id", Thing.getID()).toString());
    }
    
    public JSONObject getDatastreamJSONObject() {
        return postDatastreamRequest;
    }
    
    public String getDatastreamString() {
        return postDatastreamRequest.toString();
    }
}
