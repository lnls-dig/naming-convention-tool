package org.openepics.names.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.openepics.names.jaxb.DeviceNamesResource;

/**
 * This is the naming service client API that clients can use to access the service.
 * 
 * @author Sunil Sah <sunil.sah@cosylab.com>
 */
public class NamesClient {

    private static final Logger LOGGER = Logger.getLogger(NamesClient.class.getName());

    private static final String BASE_URL_PROPERTY_NAME = "names.servicesBaseURL";
    private static final String PROPERTIES_FILENAME = "names.properties";
    
    private Properties properties = new Properties();

	/**
	 * Constructs the instance the client and loads properties from <code>names.properties</code> file
	 * found on the class path. If the file is not found, the following default values are used:
	 * 
	 * <ul>
	 * <li><code>names.servicesBaseURL<code> = https://localhost:8080/names/rest</li>
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
	 * Connects to naming service and returns {@link DeviceNamesResource} to access the device names data.
	 * 
	 * @return the resource
	 */
	public DeviceNamesResource getNamesResource() {
        LOGGER.fine("Invoking getNamesResource");
        
        final String baseUrl = properties.getProperty(BASE_URL_PROPERTY_NAME);
        
        try {
        	return JAXRSClientFactory.create(baseUrl, DeviceNamesResource.class);
        } catch (Exception e) {
        	throw new RuntimeException("Could not retrieve data from naming service at " + baseUrl + ".", e);
        }
	}
}