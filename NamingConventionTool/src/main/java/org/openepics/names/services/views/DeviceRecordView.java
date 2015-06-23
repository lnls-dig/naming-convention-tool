package org.openepics.names.services.views;

public class DeviceRecordView {
	private DeviceView device;
	private NamePartView section;
	private NamePartView subsection;
	private NamePartView superSection;
	private NamePartView discipline;
	private NamePartView deviceGroup;
	private NamePartView deviceType;
	private String deviceName;
	public DeviceRecordView(DeviceView device) {
		subsection=device.getSection();
		section=subsection.getParent();
		superSection=section.getParent();
		deviceType=device.getDeviceType();
		deviceGroup=deviceType.getParent();
		discipline=deviceGroup.getParent();
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public NamePartView getSubsection() {
		return subsection;
	}
	public void setSubsection(NamePartView subsection) {
		this.subsection = subsection;
	}
	public DeviceView getDevice() {
		return device;
	}
	public void setDevice(DeviceView device) {
		this.device = device;
	}
	public NamePartView getSection() {
		return section;
	}
	public void setSection(NamePartView section) {
		this.section = section;
	}
}
