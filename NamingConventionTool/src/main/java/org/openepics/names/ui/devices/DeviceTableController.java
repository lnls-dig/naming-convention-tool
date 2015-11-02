package org.openepics.names.ui.devices;


import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceRecordView;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.ui.common.SelectRecordManager;
import org.openepics.names.ui.common.TreeNodeManager;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.devices.DevicesController.DevicesViewFilter;
import org.openepics.names.util.As;
import org.primefaces.model.LazyDataModel;
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
	
	
	private List<DeviceRecordView> records;
	private DevicesViewFilter displayView=DevicesViewFilter.ACTIVE;
	private List<DeviceView> historyDeviceNames;
	private LazyDataModel<DeviceRecordView> lazyModel;
	
	@PostConstruct
	public void update(){
		boolean includeDeleted= displayView==DevicesViewFilter.ARCHIVED;
		@Nullable String deviceName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("deviceName");
		records=generateRecords(devicesTreeBuilder.devicesTree(includeDeleted));
		if(deviceName!=null){
		for (DeviceRecordView record : records) {
			if (record.getConventionName().equals(deviceName)){
				selectRecordManager.setSelectedRecords(Lists.newArrayList(record));
			}
		}
		}
		lazyModel=new LazyDeviceDataModel(records);
//		lazyModel.setRowCount(records.size());
	}

	
	
    public LazyDataModel<DeviceRecordView> getLazyModel() {
        return lazyModel;
    }


	public String getCcdbUrl(){
		return getSelectedRecord()!=null? System.getProperty("names.ccdbURL").concat("?name=").concat(getSelectedRecord().getConventionName()):"";		
	}
	
	public List<SelectItem> getNames(List<NamePartRevision> namePartRevisions){
		List<SelectItem> names=Lists.newArrayList();
		names.add(new SelectItem("","<Select All>", "<Select All>"));
		if(namePartRevisions!=null){
			for (NamePartRevision part : namePartRevisions) {
				names.add(new SelectItem(part.getName(),part.getName(), part.getDescription()));
			}
		}

		return names;
	}

	public List<DeviceRecordView> getRecords() {
//		update();
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

	public boolean canConfigure(){
		return getSelectedRecord()!=null && !getSelectedRecord().isDeleted(); 
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

	public DeviceRecordView getRowData(String rowKey){
		return lazyModel.getRowData(rowKey);
	}
	
	public Object getRowKey(DeviceRecordView record){
		return lazyModel.getRowKey(record);
	}


}



