package org.openepics.names.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.UUID;


/**
 * Data transfer object representing Devices for JSON and XML serialization.
 *
 * @author Andraz Pozar  
 */
@XmlRootElement
public class DeviceNameElement {
    
    private UUID uuid;
    private String section;
    private String subSection;
    private String discipline;
    private String deviceType;
    private String instanceIndex;
    private String name;
    private String status;
    public DeviceNameElement() {}
    
    
    public DeviceNameElement(UUID uuid, String name, String status){
	    setUuid(uuid);
	    setName(name);
	    setStatus(status);
    }
    
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getSubSection() { return subSection; }
    public void setSubSection(String subSection) { this.subSection = subSection; }

    public String getDiscipline() { return discipline; }
    public void setDiscipline(String discipline) { this.discipline = discipline; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getInstanceIndex() { return instanceIndex; }
    public void setInstanceIndex(String instanceIndex) { this.instanceIndex = instanceIndex; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
}
