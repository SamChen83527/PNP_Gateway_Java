package pnp_gateway.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Alec Huang
 */
public class DBManager {
    private static final DBManager instance = new DBManager();
    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    private static final String DB_URL = "jdbc:mysql://localhost/sensordb"; //
    //  Database credentials
    private static final String USER = "root";
    private static final String PASSWORD = ""; //TODO: Enter your MySQL password here

    private DBManager(){
        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = getConnection();

            //reset the database
            try {
                Statement statement = conn.createStatement();
//                statement.execute("DROP TABLE IF EXISTS data;");
//                statement.execute("DROP TABLE IF EXISTS humi;");
//                statement.execute("DROP TABLE IF EXISTS course;");

//                statement.execute("CREATE TABLE data(did SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, sensorID VARCHAR(50), time VARCHAR(50), temperature VARCHAR(50), humidity VARCHAR(50), soilMoisture VARCHAR(50), luminosity VARCHAR(50), primary key(did));");
//                statement.execute("CREATE TABLE humi(hid SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, sensorID VARCHAR(50), time VARCHAR(50), value VARCHAR(50), primary key(hid));");
//                statement.execute("CREATE TABLE course(cid SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, name VARCHAR(50), primary key(cid));");
//                statement.execute("CREATE TABLE registration(sid SMALLINT UNSIGNED NOT NULL, cid SMALLINT UNSIGNED NOT NULL, foreign key(sid) references student(sid), foreign key(cid) references course(cid));");
                
                statement.close();
                conn.close(); //避免佔用記憶體，(雖然java會自動釋放不需要的記憶體)
            } catch (SQLException ex) {
                System.out.println("Create DB Tables Error:"+ ex.getMessage());
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static DBManager getInstance(){
        return instance;
    }
    
    public void insertData(String sensorID, String temp, String humi, String soilMois, String lumi){
        try {
            //TODO:
            //Insert a student record into the database with the student's name, age, school, and studentID as inputs.
            //Hint: Use the getConnection function to get a database connection, then use Statement for sending SQL string to the database. See the constructor for an example.
            //Note: Remember to close the statement and connection.

            Connection conn = getConnection();
            Statement statement = conn.createStatement();
            statement.execute("Insert into data values(default,'"+sensorID+"', now(),'"+temp+"', '"+humi+"', '"+soilMois+"', '"+lumi+"');");
            
            statement.close();
            conn.close();
            
            
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
//    public ArrayList<sensorData> getDataValueBySensorID(String sensorID){
//        ArrayList<sensorData> sensorList = new ArrayList<sensorData>();
//        sensorData sensorDataObj = null;
//        try {
//            Connection conn = getConnection();
//            Statement statement = conn.createStatement();
//            ResultSet rs;
//            
//            String sql = "select* from data where data.sensorID = '"+sensorID+"';";
//            rs = statement.executeQuery(sql);
//                                    
//            while(rs.next()){
//                String sensor_ID = rs.getString("data.sensorID");
//                String temp = rs.getString("data.temperature");
//                String humi = rs.getString("data.humidity");
//                String soilMois = rs.getString("data.soilMoisture");
//                String lumi = rs.getString("data.luminosity");
//                String time = rs.getString("data.time");
//                
//                sensorDataObj = new sensorData();
//                sensorDataObj.setSensorID(sensor_ID);
//                sensorDataObj.setTemprature(temp);
//                sensorDataObj.setHumidity(humi);
//                sensorDataObj.setSoilMoisture(soilMois);
//                sensorDataObj.setLuminosity(lumi);
//                sensorDataObj.setTime(time);
//                sensorList.add(sensorDataObj);
//            }
//            
//            rs.close();
//            statement.close();
//            conn.close();
//        }catch (SQLException ex) {
//            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return sensorList;
//    }
    
//    public ArrayList<sensorData> getDataValueBySensorIDandTimePeriod(String sensorID, String start, String end){
//        ArrayList<sensorData> sensorList = new ArrayList<sensorData>();
//        sensorData sensorDataObj = null;
//        try {
//            Connection conn = getConnection();
//            Statement statement = conn.createStatement();
//            ResultSet rs;
//            
//            String sql = "select* from data where time between  '"+start+"'  and  '"+end+"'  and data.sensorID = '"+sensorID+"';";
//            rs = statement.executeQuery(sql);
//                                    
//            while(rs.next()){
//                String sensor_ID = rs.getString("data.sensorID");
//                String temp = rs.getString("data.temperature");
//                String humi = rs.getString("data.humidity");
//                String soilMois = rs.getString("data.soilMoisture");
//                String lumi = rs.getString("data.luminosity");
//                String time = rs.getString("data.time");
//                
//                sensorDataObj = new sensorData();
//                sensorDataObj.setSensorID(sensor_ID);
//                sensorDataObj.setTemprature(temp);
//                sensorDataObj.setHumidity(humi);
//                sensorDataObj.setSoilMoisture(soilMois);
//                sensorDataObj.setLuminosity(lumi);
//                sensorDataObj.setTime(time);
//                sensorList.add(sensorDataObj);
//            }
//            
//            rs.close();
//            statement.close();
//            conn.close();
//        }catch (SQLException ex) {
//            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return sensorList;
//    }
    
//    public ArrayList<sensorData> getLatestDataValueBySensorID(String sensorID){
//        ArrayList<sensorData> sensorList = new ArrayList<sensorData>();
//        sensorData sensorDataObj = null;
//        try {
//            Connection conn = getConnection();
//            Statement statement = conn.createStatement();
//            ResultSet rs;
//            
//            String sql = "select* from data where data.sensorID = '" +sensorID+ "' order by data.time DESC limit 1;";
//            rs = statement.executeQuery(sql);
//                                    
//            while(rs.next()){
//                String sensor_ID = rs.getString("data.sensorID");
//                String temp = rs.getString("data.temperature");
//                String humi = rs.getString("data.humidity");
//                String soilMois = rs.getString("data.soilMoisture");
//                String lumi = rs.getString("data.luminosity");
//                String time = rs.getString("data.time");
//                
//                sensorDataObj = new sensorData();
//                sensorDataObj.setSensorID(sensor_ID);
//                sensorDataObj.setTemprature(temp);
//                sensorDataObj.setHumidity(humi);
//                sensorDataObj.setSoilMoisture(soilMois);
//                sensorDataObj.setLuminosity(lumi);
//                sensorDataObj.setTime(time);
//                sensorList.add(sensorDataObj);
//            }
//            
//            rs.close();
//            statement.close();
//            conn.close();
//        }catch (SQLException ex) {
//            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return sensorList;
//    }
    
    //This function will return a database Connection object
    private Connection getConnection(){
        try {
            return DriverManager.getConnection(DB_URL,USER,PASSWORD);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static void main(String[] args) {
//        String requestBody = "18,2016-04-06,15:00,2016-04-12,15:00";
//        String[] requestArray = requestBody.split(",");
//        String sensorid = requestArray[0];
//        String start_day = requestArray[1];
//        String start_time = requestArray[2];
//        String end_day = requestArray[3];
//        String end_time = requestArray[4];
//                
//        String start = start_day + " " + start_time;
//        String end = end_day + " " + end_time;
//        System.out.println(start);
//        System.out.println(end);
//        
//        JSONObject ObservationObj = new JSONObject();
//        JSONObject DataObj = new JSONObject();
//        String responseString = "";
//        
//        ArrayList<sensorData> sensorList = DBManager.getInstance().getDataValueBySensorIDandTimePeriod(sensorid, start, end); //get data list from MySQL.
////        ArrayList<sensorData> sensorList = DBManager.getInstance().getDataValueBySensorID(sensorid);
//        if (sensorList == null){
//        } else{
//            for(int iSensor=0; iSensor < sensorList.size(); iSensor++){
//                
//                DataObj = sensorList.get(iSensor).sensorData2JSONObject();
//                ObservationObj.append("observation", DataObj);
//                }
//        }
//        
//        responseString = ObservationObj.toString();
//        System.out.println(responseString);
        
        String request1 = "1,2016-09-05,00:00,2016-09-05,23:59";
        String request2 = "1";
        
        String[] requestArray1 = request1.split(",");
        String[] requestArray2 = request2.split(",");
        System.out.println(requestArray1.length);
        System.out.println(requestArray2.length);
        System.out.println(request2);
    }
}
