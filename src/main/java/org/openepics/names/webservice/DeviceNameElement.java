/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.names.webservice;

import javax.xml.bind.annotation.XmlRootElement;



/**
 *
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 */
@XmlRootElement
public class DeviceNameElement {
    private String uuid;
    private String section;
    private String subSection;
    private String discipline;
    private String deviceType;
    private String instanceIndex;
    private String name;

    DeviceNameElement() {
    }

    public String getUuid() {
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

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setSubSection(String subSection) {
        this.subSection = subSection;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setInstanceIndex(String instanceIndex) {
        this.instanceIndex = instanceIndex;
    }

    public void setName(String name) {
        this.name = name;
    }

    
}
