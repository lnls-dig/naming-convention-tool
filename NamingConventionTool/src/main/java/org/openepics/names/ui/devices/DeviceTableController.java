package org.openepics.names.ui.devices;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

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
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceRecordView;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.common.SelectRecordManager;
import org.openepics.names.ui.common.TreeNodeManager;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.util.As;
import org.openepics.names.util.UnhandledCaseException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

@ManagedBean
@ViewScoped
public class DeviceTableController implements Serializable{
	private static final long serialVersionUID = -262176781057441889L;
	@Inject private RestrictedNamePartService namePartService;
	@Inject private ViewFactory viewFactory;
	@Inject private SelectRecordManager selectRecordManager;
	@Inject private DevicesTreeBuilder devicesTreeBuilder;
	@Inject private ExcelImport excelImport;
	@Inject private TreeNodeManager treeNodeManager;
	private byte[] importData;
	private String importFileName;
	private List<DeviceRecordView> originalRecords;
	private List<DeviceRecordView> records;
	private List<DeviceRecordView> filteredRecords;
	private List<DeviceView> historyDeviceNames;
	private int rowNumber;
	private DevicesViewFilter[] selectedViewFilter;
	private TreeNode originalAreaStructure;
	private TreeNode originalDeviceStructure;
	private TreeNode areaStructure;


	private TreeNode deviceStructure;
//	private List<String> superSections;
//	private List<String> sections;
//	private List<String> subsections;
//	private List<String> disciplines;
//	private List<String> deviceGroups;
//	private List<String> deviceTypes;
	private TreeNode filteredAreaStructure;
	private TreeNode filteredDeviceStructure;
	
		
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
//			final DeviceRevision deviceRevision=namePartService.currentDeviceRevision(deviceName);
//			final NamePart section=deviceRevision.getSection();
//			final NamePart deviceType=deviceRevision.getDeviceType();
//			sessionViewService.filter(section);
//			sessionViewService.filter(deviceType);
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
		originalAreaStructure=As.notNull(devicesTreeBuilder.getAreaStructure());
		originalDeviceStructure=As.notNull(devicesTreeBuilder.getDeviceStructure());
		originalRecords=As.notNull(devicesTreeBuilder.deviceRecords());
		updateViewFilter();
//		setSelectedRecords(filteredRecords(getSelectedRecords()));
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
	
	
	public void updateViewFilter(){
		areaStructure=viewFilteredNamePartStructure(originalAreaStructure);
		deviceStructure=viewFilteredNamePartStructure(originalDeviceStructure);
		updateFilter();
		}
	
	public synchronized void updateFilter() {
		filteredAreaStructure=treeNodeManager.filteredNode(areaStructure,false);
		filteredDeviceStructure =treeNodeManager.filteredNode(deviceStructure,false);
		records=filteredRecords(originalRecords);
		filteredRecords=null;		
	}

	private TreeNode viewFilteredNamePartStructure(TreeNode originalNamePartStructure){
		List<DevicesViewFilter> filters=Lists.newArrayList(getSelectedViewFilter());
//		boolean acceptActive=filters.contains(DevicesViewFilter.ACTIVE);
		boolean acceptArchived=filters.contains(DevicesViewFilter.ARCHIVED);
		boolean acceptOnsite=filters.contains(DevicesViewFilter.ONSITE);
		boolean acceptOffsite=filters.contains(DevicesViewFilter.OFFSITE);
		return treeNodeManager.viewFilteredNode(As.notNull(originalNamePartStructure), acceptArchived, true, acceptOnsite, acceptOffsite);
	}
	
	private List<DeviceRecordView> filteredRecords(List<DeviceRecordView> originalRecords){
		if(originalRecords==null||originalRecords.isEmpty()) return originalRecords;
		List<DeviceRecordView> filteredRecords=Lists.newArrayList();
		List<DevicesViewFilter> filters=Lists.newArrayList(getSelectedViewFilter());
		boolean acceptActive=filters.contains(DevicesViewFilter.ACTIVE);
		boolean acceptArchived=filters.contains(DevicesViewFilter.ARCHIVED);
		List<NamePartView> subsections=namePartViews(filteredAreaStructure, 3);
		List<NamePartView> deviceTypes=namePartViews(filteredDeviceStructure,3);
		
		for( DeviceRecordView record: originalRecords){
			if(subsections.contains(record.getSubsection()) && deviceTypes.contains(record.getDeviceType())&& (acceptActive&&!record.isDeleted() || acceptArchived&&record.isDeleted())){
				filteredRecords.add(record);
			};
		}
		return filteredRecords;
	}
	
	
	public DevicesViewFilter[] getViewFilter(){
		return DevicesViewFilter.values();
	}
	
	/**
	 * @return the selectedViewFilter
	 */
	public DevicesViewFilter[] getSelectedViewFilter() {
		if(selectedViewFilter==null){
			selectedViewFilter=new DevicesViewFilter[] {DevicesViewFilter.ACTIVE,DevicesViewFilter.ONSITE};
		}
		return selectedViewFilter;
	}

	/**
	 * @param selectedViewFilter the selectedViewFilter to set
	 */
	public void setSelectedViewFilter(DevicesViewFilter[] selectedViewFilter) {
		this.selectedViewFilter = selectedViewFilter;
	}

	enum DevicesViewFilter {
		ACTIVE, ARCHIVED, ONSITE, OFFSITE
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

	
	/**
	 * @return the namePartViews contained in the specified level of the specified node   
	 * @param node treeNode containing the  namePartView
	 * @param level the desired level. 
	 */
	private List<NamePartView> namePartViews(TreeNode node, int level) {
		final List<NamePartView> names =Lists.newArrayList();
		if(node!=null){
		final @Nullable List<Object> dataLevel= treeNodeManager.treeNodeDataLevel(node, level);
		for(Object data:dataLevel){
			if(data instanceof NamePartView)
			names.add((NamePartView) data);
		}
		}
		return names;
	}

//	private void generateNamePartViews(){
//		superSections=namePartViews(areaStructure,1);
//		sections=namePartViews(areaStructure,2);
//		subsections=namePartViews(areaStructure,3);
//		disciplines=namePartViews(deviceStructure,1);
//		deviceGroups=namePartViews(deviceStructure,2);
//		deviceTypes=namePartViews(deviceStructure,3);
//	}
	
	

//	/**
//	 * @return the superSections
//	 */
//	public List<String> getSuperSections() {
//		return superSections;
//	}
//
//	
//	/**
//	 * @return the sections
//	 */
//	public List<String> getSections() {
//		return sections;
//	}
//
//	/**
//	 * @return the subsections
//	 */
//	public List<String> getSubsections() {
//		return subsections;
//	}
//
//	/**
//	 * @return the disciplines
//	 */
//	public List<String> getDisciplines() {
//		return disciplines;
//	}
//
//	/**
//	 * @return the deviceGroups
//	 */
//	public List<String> getDeviceGroups() {
//		return deviceGroups;
//	}
//
//
//	/**
//	 * @return the deviceTypes
//	 */
//	public List<String> getDeviceTypes() {
//		return deviceTypes;
//	}

	/**
	 * @return the areaStructure
	 */
	public TreeNode getAreaStructure() {
		return areaStructure;
	}


	/**
	 * @return the deviceStructure
	 */
	public TreeNode getDeviceStructure() {
		return deviceStructure;
	}



}



