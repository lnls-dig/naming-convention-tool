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
	private NamePartView deviceTypeView;
	private NamePartView subsectionView;
	private DeviceElement section;
	private DeviceElement subsection;
	private DeviceElement superSection;
	private DeviceElement discipline;
	private DeviceElement deviceGroup;
	private DeviceElement deviceType;
	private ViewFactory viewFactory;
	
	public DeviceRecordView(ViewFactory viewFactory, DeviceRevision deviceRevision) {
		this.viewFactory=viewFactory;		
		this.deviceRevision=deviceRevision;
		update();
	}

    /**
     * @return The view of the subsection containing the device.
     */
    public NamePartView getSubsectionView() {
        if (subsectionView == null) {
            subsectionView = viewFactory.getView(deviceRevision.getSection());
        }
        return subsectionView;
    }

    /**
     * @return The view of the device type containing the device.
     */
    public NamePartView getDeviceTypeView() {
        if (deviceTypeView == null) {
            deviceTypeView = viewFactory.getView(deviceRevision.getDeviceType());
        }
        return deviceTypeView;
    }
	
	public void update(){		
		deviceTypeView=null;
		subsectionView=null;
		subsection=null;
		section=null;
		superSection=null;
		deviceType=null;
		deviceGroup=null;
		discipline=null;
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
		return deviceRevision.getDevice().getId();
	}
	
	public String getStyle(){
		return deviceRevision.isDeleted()? "Deleted":"Approved";
	}
	
	public  Device getDevice(){
		return deviceRevision.getDevice(); 
	}
	
	public DeviceElement getSubsection() {
		if(subsection==null){
			subsection=new DeviceElement(getSubsectionView());
		}
		return subsection;
	}	
		
	public DeviceElement getSection() {
		if(section==null){
			section=new DeviceElement(getSubsectionView().getParent());
		}
		return section;
	}

	/**
	 * @return the superSection
	 */
	public DeviceElement getSuperSection() {
		if(superSection==null){
			superSection=new DeviceElement(getSubsectionView().getParent().getParent());
		}
		return superSection;
	}
	/**
	 * @return the discipline
	 */
	public DeviceElement getDiscipline() {
		if(discipline==null){
			discipline=new DeviceElement(getDeviceTypeView().getParent().getParent());
		}
		return discipline;
	}

	/**
	 * @return the deviceGroup
	 */
	public DeviceElement getDeviceGroup() {
		if(deviceGroup==null){
			deviceGroup=new DeviceElement(getDeviceTypeView().getParent());
		}
		return deviceGroup;
	}

	/**
	 * @return the deviceType
	 */
	public DeviceElement getDeviceType() {
		if(deviceType==null){
			deviceType=new DeviceElement(getDeviceTypeView());
		}

		return deviceType;
	}
		
	public boolean isDeleted(){
		return deviceRevision.isDeleted();
	}
		
	public class DeviceElement{
		private String name;
		private String mnemonic;
		private String description;
		private NamePart namePart;
		
		public DeviceElement(NamePartView view){
			name = view.getName();
			mnemonic = view.getMnemonic();
			description=view.getDescription();
			namePart=view.getNamePart();
		}
		
		public String getName() {
			return name;
		}

		public String getMnemonic() {
			return mnemonic;
		}

		public String getDescription() {
			return description;
		}

		public NamePart getNamePart(){
			return namePart;
		}
		
	}
	
	
	@Override
	public boolean equals(Object other){
		return other instanceof DeviceRecordView ? ((DeviceRecordView) other).getId().equals(this.getId()) : false;
	}
}
