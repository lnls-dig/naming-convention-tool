package org.openepics.names.ui.devices;


import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.openepics.names.model.DeviceRevision;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceRecordView;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.common.SelectRecordManager;
import org.openepics.names.ui.common.TreeNodeManager;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.devices.DevicesController.DevicesViewFilter;
import org.openepics.names.util.As;
import org.primefaces.model.TreeNode;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@ManagedBean
@ViewScoped
public class DeviceTableController implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -262176781057441889L;
	@Inject private RestrictedNamePartService namePartService;
	@Inject private ViewFactory viewFactory;
	@Inject private SelectRecordManager selectRecordManager;
	@Inject private DevicesTreeBuilder devicesTreeBuilder;

	private List<NamePartView> subsections;
	private List<NamePartView> sections;
	private List<NamePartView> superSections;
	private List<NamePartView> deviceTypes;
	private List<NamePartView> deviceGroups;
	private List<NamePartView> disciplines;
	private List<DeviceRecordView> filteredRecords;
	private List<DeviceRecordView> records;
	private DevicesViewFilter displayView=DevicesViewFilter.ACTIVE;
	private List<DeviceView> historyDeviceNames;

	@PostConstruct
	public void update(){
		records=generateRecords(devicesTreeBuilder.devicesTree(displayView==DevicesViewFilter.ARCHIVED));
		subsections=Lists.newArrayList();
		deviceTypes=Lists.newArrayList();
		for (DeviceRecordView record : records) {
			if(!subsections.contains(record.getSubsection())){
				subsections.add(record.getSubsection());
			}
			if(!deviceTypes.contains(record.getDeviceType())){
				deviceTypes.add(record.getDeviceType());
			}				
		}
		deviceGroups=parentList(deviceTypes);
		sections=parentList(subsections);
		superSections=parentList(sections);
		disciplines=parentList(deviceGroups);
	}

	public List<SelectItem> getSuperSectionNames(){
		return getNames(superSections);				
	}

	public Collection<SelectItem> getSectionNames(){
		return getNames(sections);
	}

	public List<SelectItem> getSubsectionNames(){
		return getNames(subsections);
	}
	public List<SelectItem> getDisciplineNames(){
		return getNames(disciplines);
	}
	public List<SelectItem> getDeviceGroupNames(){
		return getNames(deviceGroups);
	}
	public List<SelectItem> getDeviceTypeNames(){
		return getNames(deviceTypes);
	}

	public List<SelectItem> getNames(List<NamePartView> parts){
		List<SelectItem> names=Lists.newArrayList();
		names.add(new SelectItem("","<Select All>", "<Select All>"));
		if(parts!=null){
			for (NamePartView part : parts) {
				names.add(new SelectItem(part.getName(),part.getName(), part.getDescription()));
			}
		}

		return names;
	}

	public List<DeviceRecordView> getRecords() {
		update();
		return records;
	}

	/**
	 * @return the selectedRecord
	 */
	public DeviceRecordView getSelectedRecord() {
		return selectRecordManager.getSelectedRecord();
	}

	public @Nullable List<DeviceRecordView> getSelectedRecords(){
		return selectRecordManager.getSelectedRecords();
	}

	public void setSelectedRecords(@Nullable List<DeviceRecordView> selectedRecords){
		selectRecordManager.setSelectedRecords(selectedRecords);
	}

	public boolean canModify(){
		return getSelectedRecord()!=null && ! getSelectedRecord().isDeleted();
	}

	public boolean canAdd(){
		return getSelectedRecords()==null || getSelectedRecords().isEmpty();
	}

	public boolean canDelete(){
		if(getSelectedRecords()!=null){
			for (DeviceRecordView record : getSelectedRecords()) {
				if (! record.isDeleted()){
					return true;
				}
			}
		}
		return false;	
	}

	public boolean canShowHistory() { return getSelectedRecord() != null; }

	/**
	 *  Generates a list of device records for views.
	 * @return list of device records
	 */
	public List<DeviceRecordView> generateRecords(TreeNode root){
		final List<DeviceRecordView> recordList=Lists.newArrayList();
		for(TreeNode node: TreeNodeManager.nodeList(root)){
			if(node.getData() instanceof DeviceView){
				recordList.add(new DeviceRecordView((DeviceView) node.getData()));				
			}
		}
		return recordList;
	}

	public List<DeviceRecordView> getFilteredRecords() {
		return filteredRecords;
	}

	public void setFilteredRecords(List<DeviceRecordView> filteredRecords) {
		this.filteredRecords = filteredRecords;
		update();
	}

	private List<NamePartView> parentList(List<NamePartView> parts) {
		List<NamePartView> parents=Lists.newArrayList();
		for (NamePartView namePartView : parts) {
			if(!parents.contains(namePartView.getParent())){
				parents.add(namePartView.getParent());
			}
		}
		return parents;
	}

	public DevicesViewFilter getViewFilter() {return displayView; }
	public void setViewFilter(DevicesViewFilter viewFilter) { this.displayView = viewFilter; }

	public void loadHistory() {
		historyDeviceNames = Lists.transform(namePartService.revisions(As.notNull(getSelectedRecord()).getDevice()), new Function<DeviceRevision, DeviceView>() {
			@Override public DeviceView apply(DeviceRevision f) { return viewFactory.getView(f);}
		});
	}

	public List<DeviceView> getHistoryEvents() { 
		return historyDeviceNames; 
	}

}



