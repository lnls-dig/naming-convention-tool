package org.openepics.names.services.views;

import org.openepics.names.model.Device;
import org.openepics.names.model.NamePart;


public class DeviceRecordView {
	private Long id;
	private Boolean deleted;
	private String conventionName;
	private DeviceView deviceView;
	private DeviceElement section;
	private DeviceElement subsection;
	private DeviceElement superSection;
	private DeviceElement discipline;
	private DeviceElement deviceGroup;
	private DeviceElement deviceType;
	private String style;

	public DeviceRecordView(DeviceView deviceView) {
		this.deviceView=deviceView;
		this.conventionName=deviceView.getConventionName();
		update();
	}
	
	public void update(){
		id=deviceView.getDevice().getDevice().getId();
		deleted=deviceView.getDevice().isDeleted();
		style=deleted? "deleted":"approved";
		NamePartView view=deviceView.getSection();
		subsection=new DeviceElement(view);
		view=view.getParent();
		section=new DeviceElement(view);
		view=view.getParent();
		superSection=new DeviceElement(view);
		view=deviceView.getDeviceType();		
		deviceType=new DeviceElement(view);
		view=view.getParent();
		deviceGroup=new DeviceElement(view);
		view=view.getParent();
		discipline=new DeviceElement(view);
		
		
	}
	
	public Long getId(){
		return id;
	}
	public String getStyle(){
		return style;
	}
	
	public  Device getDevice(){
		return getDeviceView().getDevice().getDevice(); 
	}
	
	public String getConventionName() {
		return conventionName;
	}
	
	public DeviceElement getSubsection() {
		return subsection;
	}	
	
	public DeviceView getDeviceView() {
		return deviceView;
	}
	
	public DeviceElement getSection() {
		return section;
	}

	/**
	 * @return the superSection
	 */
	public DeviceElement getSuperSection() {
		return superSection;
	}
	/**
	 * @return the discipline
	 */
	public DeviceElement getDiscipline() {
		return discipline;
	}

	/**
	 * @return the deviceGroup
	 */
	public DeviceElement getDeviceGroup() {
		return deviceGroup;
	}

	/**
	 * @return the deviceType
	 */
	public DeviceElement getDeviceType() {
		return deviceType;
	}
	
	public String getDescription(){
		return deviceView.getAdditionalInfo();
	}
	
	public boolean isDeleted(){
		return deleted;
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
		
		public DeviceElement(DeviceView view){
			name=view.getInstanceIndex();
			mnemonic=view.getConventionName();
			description=view.getAdditionalInfo();
			namePart=null;
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
	
}
