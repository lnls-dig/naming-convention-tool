package org.openepics.names.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openepics.names.jaxb.DeviceNameElement;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * This is the naming service client API that clients can use to access the service.
 * @author Sunil Sah
 * @author karinrathsman
 *
 */
public class NamesClient {

    private static final Logger LOGGER = Logger.getLogger(NamesClient.class.getName());

    private static final String BASE_URL_PROPERTY_NAME = "names.servicesBaseURL";
    private static final String PROPERTIES_FILENAME = "names.properties";
    private static final String DEVICE_NAMES_PATH = "deviceNames";
    private static final String SLASH = "/";
    
    
    private Properties properties = new Properties();

	/**
	 * Constructs the instance the client and loads properties from <code>names.properties</code> file
	 * found on the class path. If the file is not found, the following default values are used:
	 * 
	 * <ul>
	 * <li><code>names.servicesBaseURL</code> = https://localhost:8080/names/rest</li>
	 * </ul>
	 * 
	 * Values can also be specified by setting the system properties, which override the default and file definitions.
	 * The system properties must be set before this constructor is invoked.
	 */
	public NamesClient() {
	
		try (final InputStream stream = NamesClient.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME)) {
	        properties.load(stream);
	    } catch (IOException | NullPointerException e) {
	        LOGGER.log(Level.INFO, "Loading properties from file " + PROPERTIES_FILENAME + " failed.");
	        properties.setProperty(BASE_URL_PROPERTY_NAME, "https://localhost:8080/names/rest");
	    }
		properties.putAll(System.getProperties());
	}

    /**
     * Connects to naming service and returns bulk data on all device names.
     * 
     * @return the list of all device names
     */
    public List<DeviceNameElement> getAllDeviceNames() {
        LOGGER.fine("Invoking getAllDeviceNames");
        
        final String url = getBaseUrl() + SLASH + DEVICE_NAMES_PATH;

        try {
            Response response = getResponse(url);
            List<DeviceNameElement> list = response.readEntity(new GenericType<List<DeviceNameElement>>(){});
            response.close();
            
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve data from naming service at " + url + ".", e);
        }
    }

    /**
     * Connects to naming service and returns data of a single device names.
     * 
     * @param reqUuid the uuid of the device name
     * @return the device name data element
     */
    public DeviceNameElement getDeviceName(String reqUuid) {
        LOGGER.fine("Invoking getAllDeviceNames");
        
        final String url = getBaseUrl() + SLASH + DEVICE_NAMES_PATH + SLASH + reqUuid;

        try {
            Response response = getResponse(url);
            DeviceNameElement deviceName = response.readEntity(DeviceNameElement.class);
            response.close();
            
            return deviceName;
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve data from naming service at " + url + ".", e);
        }
    }
    
    private String getBaseUrl() {
        return properties.getProperty(BASE_URL_PROPERTY_NAME);
    }
    
    private Response getResponse(String url) {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(url);
        return target.request("application/json").get();
    }
}