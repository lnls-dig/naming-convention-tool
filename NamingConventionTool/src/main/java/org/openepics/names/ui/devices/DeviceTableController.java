package org.openepics.names.ui.devices;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.SessionViewService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceRecordView;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.ui.common.SelectRecordManager;
import org.openepics.names.ui.common.TreeNodeManager;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.util.As;
import org.openepics.names.util.UnhandledCaseException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

@ManagedBean
@ViewScoped
public class DeviceTableController implements Serializable{
	private static final long serialVersionUID = -262176781057441889L;
	@Inject private RestrictedNamePartService namePartService;
	@Inject private ViewFactory viewFactory;
	@Inject private SelectRecordManager selectRecordManager;
	@Inject private DevicesTreeBuilder devicesTreeBuilder;
	@Inject private NamePartTreeBuilder namePartTreeBuilder;
	@Inject private ExcelImport excelImport;
	@Inject private SessionViewService sessionViewService;
	private byte[] importData;
	

	
	private String importFileName;

	private List<DeviceRecordView> records;
	private List<DeviceRecordView> filteredRecords;
	private List<DeviceView> historyDeviceNames;
	private int rowNumber;
	
	@PostConstruct
	public void init(){
		selectDeviceInUrl();
	}


	/**
	 * Selects the deviceName from the URL and sets the rowNumber
	 */
	private void selectDeviceInUrl() {
		final @Nullable String deviceName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("deviceName");
		if(deviceName!=null){
			final DeviceRevision deviceRevision=namePartService.currentDeviceRevision(deviceName);
			final NamePart section=deviceRevision.getSection();
			final NamePart deviceType=deviceRevision.getDeviceType();
			sessionViewService.filter(section);
			sessionViewService.filter(deviceType);
		}
		update();
		if(deviceName!=null){
			rowNumber=0;
			for (DeviceRecordView record : records) {
				rowNumber++;
				if (record.getConventionName().equals(deviceName)){
					selectRecordManager.setSelectedRecords(Lists.newArrayList(record));
					break;
				}
			}
		}else{
			rowNumber=0;
		}		
	}

	/**
	 * update all data
	 */
	public void update(){
		records=devicesTreeBuilder.deviceRecords();		
	}
		
	/** 
	 * 
	 * @return Link to the device in the controls configuration database (CCDB) 
 	 */
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

	public boolean canFilter(){
		return true;
	}
	
	public boolean canShowHistory() { return getSelectedRecord() != null; }

	/**
	 * Generates a list of device records for views.
	 * @param root Root of the tree node
	 * @return list of device records
	 */
	public List<DeviceRecordView> generateRecords(TreeNode root){		
		
		final List<DeviceRecordView> recordList=Lists.newArrayList();
		for(TreeNode node: TreeNodeManager.nodeList(root)){
			if(node.getData() instanceof DeviceView){
//				recordList.add(new DeviceRecordView((DeviceView) node.getData()));	
				
			}
		}
		return recordList;
	}

	public DevicesViewFilter getViewFilter() {
		return selectRecordManager.isIncludeDeleted()? DevicesViewFilter.ARCHIVED: DevicesViewFilter.ACTIVE; 
	}
	public void setViewFilter(DevicesViewFilter viewFilter) { 
		switch(viewFilter){
		case ARCHIVED: selectRecordManager.setIncludeDeleted(true);
			break;
		default: selectRecordManager.setIncludeDeleted(false); 
		}
	}

	public void loadHistory() {
		historyDeviceNames = Lists.transform(namePartService.revisions(As.notNull(getSelectedRecord()).getDevice()), new Function<DeviceRevision, DeviceView>() {
			@Override public DeviceView apply(DeviceRevision f) { return viewFactory.getView(f);}
		});
	}

	public List<DeviceView> getHistoryEvents() { 
		return historyDeviceNames; 
	}

	
	public void onDelete() {
		int count=0;
		try{			
			for(DeviceRecordView record: selectRecordManager.getSelectedRecords()){

				if(!record.isDeleted()) {
//					namePartService.deleteDevice(record.getDeviceView().getDevice().getDevice());
					namePartService.deleteDevice(record.getDevice());

					count++;
				}
			}
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", printedAffectedQuantity(count) + "deleted.");
		} finally{
			update();
		}
	}
	
	private void showMessage(@Nullable String notificationChannel, FacesMessage.Severity severity, String summary, String message) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(notificationChannel, new FacesMessage(severity, summary, message));
	}

	
	private String printedAffectedQuantity(int n) {
		return n + " device name" + (n > 1 ? "s have been " : " has been ");
	}
	
	enum DevicesViewFilter {
		ACTIVE, ARCHIVED
	}
	
	public void onImport() {
		try (InputStream inputStream = new ByteArrayInputStream(importData)) {
			ExcelImport.ExcelImportResult importResult = excelImport.parseDeviceImportFile(inputStream);
			if (importResult instanceof ExcelImport.SuccessExcelImportResult) {
				update();
				showMessage(null, FacesMessage.SEVERITY_INFO, "Import was successful!", "");
			} else if (importResult instanceof ExcelImport.CellValueFailureExcelImportResult) {
				ExcelImport.CellValueFailureExcelImportResult failureImportResult = (ExcelImport.CellValueFailureExcelImportResult) importResult;
				showMessage(null, FacesMessage.SEVERITY_ERROR, "Import failed!", "Error occurred in row " + failureImportResult.getRowNumber() + ". " + (failureImportResult.getNamePartType().equals(NamePartType.SECTION) ? "Logical area" : "Device category") + " part was not found in the database.");
			} else if (importResult instanceof ExcelImport.ColumnCountFailureExcelImportResult) {
				showMessage(null, FacesMessage.SEVERITY_ERROR, "Import failed!", "Error occurred when reading import file. Column count does not match expected value.");
			} else {
				throw new UnhandledCaseException();
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	public void prepareImportPopup() {
		importData = null;
		importFileName = null;
	}

	public void handleFileUpload(FileUploadEvent event) {
		try (InputStream inputStream = event.getFile().getInputstream()) {
			this.importData = ByteStreams.toByteArray(inputStream);
			this.importFileName = FilenameUtils.getName(event.getFile().getFileName());
		} catch (IOException e) {
			throw new RuntimeException();           
		}
	}
	public String getImportFileName() { return importFileName; }

	public StreamedContent getDownloadableNamesTemplate() {  
		return new DefaultStreamedContent(this.getClass().getResourceAsStream("NamingImportTemplate.xlsx"), "xlsx", "NamingImportTemplate.xlsx");  
	}

	public List<DeviceRecordView> getFilteredRecords() {
		return filteredRecords;
	}

	public void setFilteredRecords(List<DeviceRecordView> filteredRecords) {
		this.filteredRecords = filteredRecords;
	}

	public int getRowNumber() {
		return 30*(rowNumber/30);
	}

}