/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.names.webservice;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;


/**
 *
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
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
    
    
    

    public DeviceNameElement(UUID uuid, String section, String subSection,
            String discipline, String deviceType, String instanceIndex,
            String name) {
        super();
        this.uuid = uuid;
        this.section = section;
        this.subSection = subSection;
        this.discipline = discipline;
        this.deviceType = deviceType;
        this.instanceIndex = instanceIndex;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getSection() {
        return section;
    }

    public String getSubSection() {
        return subSection;
    }

    public String getDiscipline() {
        return discipline;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getInstanceIndex() {
        return instanceIndex;
    }

    public String getName() {
        return name;
    }
}
