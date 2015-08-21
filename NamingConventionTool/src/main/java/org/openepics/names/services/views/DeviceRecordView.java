package org.openepics.names.services.views;




public class DeviceRecordView {
	private DeviceView deviceView;
	private NamePartView section;
	private NamePartView subsection;
	private NamePartView superSection;
	private NamePartView discipline;
	private NamePartView deviceGroup;
	private NamePartView deviceType;

	public DeviceRecordView(DeviceView deviceView) {
		this.deviceView=deviceView;
		update();
	}
	public void update(){
		subsection=deviceView.getSection();
		section=subsection.getParent();
		superSection=section.getParent();
		deviceType=deviceView.getDeviceType();
		deviceGroup=deviceType.getParent();
		discipline=deviceGroup.getParent();
	}
	
	public Long getId(){
		return getDeviceView().getDevice().getDevice().getId();
	}
	
	public String getConventionName() {
		return deviceView.getConventionName();
	}
	
	public NamePartView getSubsection() {
		return subsection;
	}
	
	public void setSubsection(NamePartView subsection) {
		this.subsection = subsection;
	}
	
	public DeviceView getDeviceView() {
		return deviceView;
	}
	
	public NamePartView getSection() {
		return section;
	}

	/**
	 * @return the superSection
	 */
	public NamePartView getSuperSection() {
		return superSection;
	}
	/**
	 * @return the discipline
	 */
	public NamePartView getDiscipline() {
		return discipline;
	}

	/**
	 * @return the deviceGroup
	 */
	public NamePartView getDeviceGroup() {
		return deviceGroup;
	}

	/**
	 * @return the deviceType
	 */
	public NamePartView getDeviceType() {
		return deviceType;
	}
	
	public String getDescription(){
		return deviceView.getAdditionalInfo();
	}
	
	public boolean isDeleted(){
		return deviceView.getDevice().isDeleted();
	}
}
