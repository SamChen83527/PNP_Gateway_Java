/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnp_gateway.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author user
 */
public class ServerConfig {
    
    private String GATEWAY_URL = null;
    private String COMPORT_NAME = null;
    private String TAKSINGSERVICE_URL = null;
    
    public ServerConfig() {
        getServerProperties();
    }
    
    private void getServerProperties(){
        try {
            InputStream fXmlFile = ServerConfig.class.getResourceAsStream("serverConfig.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("configs");
            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                    Node node = nList.item(temp);
                    if (node.getNodeType() == Node.ELEMENT_NODE)
                    {
                        //GET SERVER CONFIG
                        Element eElement = (Element) node;
                        this.COMPORT_NAME = eElement.getElementsByTagName("comport_name").item(0).getTextContent();
                        this.GATEWAY_URL = eElement.getElementsByTagName("gateway_url").item(0).getTextContent();
                        this.TAKSINGSERVICE_URL = eElement.getElementsByTagName("tasking_service").item(0).getTextContent();
                    }
            }

        } catch (IOException | ParserConfigurationException | DOMException | SAXException e) {
                e.printStackTrace();
        }
    }
    
    public String getComPortName() {
        return this.COMPORT_NAME;
    }
    
    public String getGatewayURL() {
        return this.GATEWAY_URL;
    }
    
    public String getTaskingServiceURL() {
        return this.TAKSINGSERVICE_URL;
    }
    
}