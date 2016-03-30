package org.openepics.names.services.views;

import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.ui.common.ViewFactory;

/**
 * View for instances of device records in table 
 * @author karinrathsman
 *
 */
public class DeviceRecordView {
	private DeviceRevision deviceRevision;
	private NamePartView deviceType;
	private NamePartView subsection;
	private NamePartView  section;
	private NamePartView  superSection;
	private NamePartView  discipline;
	private NamePartView  deviceGroup;
	private ViewFactory viewFactory;
	
	public DeviceRecordView(ViewFactory viewFactory, DeviceRevision deviceRevision, NamePartView subsection, NamePartView deviceType) {
		this.viewFactory=viewFactory;		
		this.deviceRevision=deviceRevision;
		this.deviceType=deviceType;
		this.subsection=subsection;
//		update();
	}

	private void update(){		
		deviceType=null;
		subsection=null;
		section=null;
		superSection=null;
		deviceType=null;
		deviceGroup=null;
		discipline=null;
	}

	
    /**
     * @return The view of the subsection containing the device.
     */
    public NamePartView getSubsection() {
//        if (subsection == null) {
//            subsection = viewFactory.getView(deviceRevision.getSection());
//        }
        return subsection;
    }

    /**
     * @return The view of the device type containing the device.
     */
    public NamePartView getDeviceType() {
//        if (deviceType == null) {
//            deviceType = viewFactory.getView(deviceRevision.getDeviceType());
//        }
        return deviceType;
    }
	
	public NamePartView getSection() {
		if(section==null){
			section= getSubsection().getParent();
		}
		return section;
	}

	/**
	 * @return the superSection
	 */
	public NamePartView getSuperSection() {
		if(superSection==null){
			superSection=getSection().getParent();
		}
		return superSection;
	}
	/**
	 * @return the discipline
	 */
	public NamePartView getDiscipline() {
		if(discipline==null){
			discipline=getDeviceGroup().getParent();
		}
		return discipline;
	}

	/**
	 * @return the deviceGroup
	 */
	public NamePartView getDeviceGroup() {
		if(deviceGroup==null){
			deviceGroup= getDeviceType().getParent();
		}
		return deviceGroup;
	}    
    
	public DeviceRevision getDeviceRevision(){
		return deviceRevision;
	}
	
	public String getInstanceIndex(){
		return deviceRevision.getInstanceIndex();
	}
	
	public String getConventionName() {
		return deviceRevision.getConventionName();
	}
	
	public String getDescription(){
		return deviceRevision.getAdditionalInfo();
	}

	public Long getId(){
		return getDevice().getId();
	}
	
	public String getStyle(){
		return isDeleted()? "Deleted":"Approved";
	}
	
	public  Device getDevice(){
		return deviceRevision.getDevice(); 
	}
	
	public boolean isDeleted(){
		return deviceRevision.isDeleted();
	}
				
	@Override
	public boolean equals(Object other){
		return other instanceof DeviceRecordView ? ((DeviceRecordView) other).getId().equals(this.getId()) : false;
	}
}
