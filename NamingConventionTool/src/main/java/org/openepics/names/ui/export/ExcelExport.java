/*-
* Copyright (c) 2014 European Spallation Source
* Copyright (c) 2014 Cosylab d.d.
*
* This file is part of Naming Service.
* Naming Service is free software: you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free
* Software Foundation, either version 2 of the License, or any newer version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
*/

package org.openepics.names.ui.export;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.SessionViewService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.BatchViewProvider;
import org.openepics.names.services.views.DeviceRecordView;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.common.TreeNodeManager;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.devices.DeviceTableController;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.util.As;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * A bean for exporting sections, device types and devices to Excel.
 */
@Stateless
public class ExcelExport {
    
//    @Inject private RestrictedNamePartService namePartService;
//    @Inject private NamePartTreeBuilder namePartTreeBuilder;
//    @Inject private SessionViewService sessionViewService;
//    @Inject private TreeNodeManager treeNodeManager;

	/**
	 * 
	 * @return current bean controlling the device record table.
	 */
	private final DeviceTableController deviceTableController(){
		FacesContext facesContext=FacesContext.getCurrentInstance();
        return (DeviceTableController) facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{deviceTableController}", Object.class).getValue(facesContext.getELContext()); 
	}

    
    /**
     * Exports the entities from the database, producing a stream which can be streamed to the user over HTTP.
     *
     * @return an Excel input stream containing the exported data
     */
    public InputStream exportFile() {
//        final List<NamePartRevision> approvedSectionsRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
//        final TreeNode sectionsTree = treeNodeManager.filteredNode(namePartTreeBuilder.newNamePartTree(approvedSectionsRevisions, Lists.<NamePartRevision>newArrayList(), true));

        deviceTableController().update();
        final TreeNode areaStructure=deviceTableController().getFilteredAreaStructure();
        final TreeNode deviceStructure=deviceTableController().getFilteredDeviceStructure();
        final List<DeviceRecordView> records=deviceTableController().getRecords(); 
        
//        final List<NamePartRevision> approvedTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
//        final TreeNode typesTree = treeNodeManager.filteredNode(namePartTreeBuilder.newNamePartTree(approvedTypeRevisions, Lists.<NamePartRevision>newArrayList(), true));
        
        
//        final List<DeviceRevision> devices = Lists.newArrayList();
//        for (DeviceRevision deviceRevision : namePartService.currentDeviceRevisions(false)) {
//        	boolean filteredSection=sessionViewService.isFiltered(deviceRevision.getSection());
//        	boolean filteredDeviceType=sessionViewService.isFiltered(deviceRevision.getDeviceType());
//        	if(filteredSection&&filteredDeviceType) devices.add(deviceRevision);
//        }
        
//        final XSSFWorkbook workbook = exportWorkbook(sectionsTree, typesTree, devices);
        final XSSFWorkbook workbook = exportWorkbook(areaStructure, deviceStructure, records);

        
        final InputStream inputStream;
        try {
            final File temporaryFile = File.createTempFile("temp", "xlsx");
            FileOutputStream outputStream = new FileOutputStream(temporaryFile);
            workbook.write(outputStream);
            outputStream.close();
            inputStream = new FileInputStream(temporaryFile);
            temporaryFile.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return inputStream;       
    }
    
    private XSSFWorkbook exportWorkbook(TreeNode sectionsTree, TreeNode typesTree, List<DeviceRecordView> devices) {
        final XSSFWorkbook workbook = new XSSFWorkbook();

        final XSSFSheet superSectionSheet = createSheetWithHeader(workbook, "Super Section", "Super Section::ID", "Super Section::FullName", "Super Section::Mnemonic", "Super Section::Date Modified");
        fillNamePartSheet(superSectionSheet, 1, sectionsTree);

        final XSSFSheet sectionSheet = createSheetWithHeader(workbook, "Section", "Super Section::FullName", "Super Section::Mnemonic", "Section::ID", "Section::FullName", "Section::Mnemonic", "Section::Date Modified");
        fillNamePartSheet(sectionSheet, 2, sectionsTree);

        final XSSFSheet subSectionSheet = createSheetWithHeader(workbook, "Subsection", "Super Section::FullName", "Super Section::Mnemonic", "Section::FullName", "Section::Mnemonic", "Subsection::ID", "Subsection::FullName", "Subsection::Mnemonic", "Subsection::Date Modified");
        fillNamePartSheet(subSectionSheet, 3, sectionsTree);

        final XSSFSheet disciplineSheet = createSheetWithHeader(workbook, "Discipline", "Discipline::ID", "Discipline::FullName", "Discipline::Mnemonic", "Discipline::Date Modified");
        fillNamePartSheet(disciplineSheet, 1, typesTree);

        final XSSFSheet categorySheet = createSheetWithHeader(workbook, "Device Group", "Discipline::FullName", "Discipline::Mnemonic", "Device Group::ID", "Device Group::FullName", "Device Group::Mnemonic", "Device Group::Date Modified");
        fillNamePartSheet(categorySheet, 2, typesTree);

        final XSSFSheet deviceTypeSheet = createSheetWithHeader(workbook, "Device Type", "Discipline::FullName", "Discipline::Mnemonic", "Device Group::FullName", "Device Group::Mnemonic", "Device Type::ID", "Device Type::FullName", "Device Type::Mnemonic", "Device Type::Date Modified");
        fillNamePartSheet(deviceTypeSheet, 3, typesTree);

        final XSSFSheet namedDeviceSheet = createSheetWithHeader(workbook, "Device Names", "ID", "Super Section", "Section", "Subsection", "Discipline", "Device Type", "Instance Index", "Description/Comment", "Device Name", "Date Modified");
        fillDeviceSheet(namedDeviceSheet, devices);
        
        return workbook;
    }

    private void fillNamePartSheet(XSSFSheet sheet, int maxLevel, TreeNode node) {
        fillNamePartSheet(sheet, maxLevel, 1, node, Lists.<String>newArrayList());
    }
    
    private void fillNamePartSheet(XSSFSheet sheet, int maxLevel, int currentLevel, TreeNode node, List<String> rowData) {
        for (TreeNode child : node.getChildren()) {
            final @Nullable NamePartView childView = (NamePartView) child.getData();
            if (childView != null ) {
                if (currentLevel < maxLevel) {
                    final List<String> ancestorData = ImmutableList.<String>builder().addAll(rowData).add(childView.getName(), childView.getMnemonic()!=null? childView.getMnemonic():"").build();
                    fillNamePartSheet(sheet, maxLevel, currentLevel + 1, child, ancestorData);
                } else if (!childView.isDeleted()){
                    final Row row = appendRow(sheet);
                    for (String sectionInfo : rowData) {
                        appendCell(row, sectionInfo);
                    }
                    appendCell(row, childView.getNamePart().getUuid().toString());
                    appendCell(row, childView.getName());
                    appendCell(row, childView.getMnemonic()!=null? childView.getMnemonic():"");
                    appendCell(row, new SimpleDateFormat("yyyy-MM-dd").format(As.notNull(childView.getCurrentRevision()).getProcessDate()));
                }
            } else {
                return;
            }
        }        
    }
    
//    private void fillDeviceSheet(XSSFSheet sheet, List<DeviceRevision> devices) {
//        final List<NamePartRevision> sectionRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
//        final List<NamePartRevision> deviceTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
//        final List<DeviceRevision> deviceRevisions = namePartService.currentDeviceRevisions(false);
//        final BatchViewProvider viewProvider = new BatchViewProvider(sectionRevisions, deviceTypeRevisions, deviceRevisions);
//        for (DeviceRevision device : devices) {
//            final Row row = appendRow(sheet);
//            appendCell(row, device.getDevice().getUuid().toString());
//            appendCell(row, As.notNull(viewProvider.view(device.getSection()).getParent().getParent()).getMnemonic());
//            appendCell(row, As.notNull(viewProvider.view(device.getSection()).getParent()).getMnemonic());
//            appendCell(row, viewProvider.view(device.getSection()).getMnemonic());
//            appendCell(row, As.notNull(As.notNull(viewProvider.view(device.getDeviceType()).getParent()).getParent()).getMnemonic());
//            appendCell(row, viewProvider.view(device.getDeviceType()).getMnemonic());
//            appendCell(row, viewProvider.view(device).getInstanceIndex());
//            appendCell(row, viewProvider.view(device).getAdditionalInfo());
//            appendCell(row, viewProvider.view(device).getConventionName());
//            appendCell(row, new SimpleDateFormat("yyyy-MM-dd").format(device.getRequestDate()));
//        }
//    }

    private void fillDeviceSheet(XSSFSheet sheet, List<DeviceRecordView> records) {
//        final List<NamePartRevision> sectionRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
//        final List<NamePartRevision> deviceTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
//        final List<DeviceRevision> deviceRevisions = namePartService.currentDeviceRevisions(false);
//       final BatchViewProvider viewProvider = new BatchViewProvider(sectionRevisions, deviceTypeRevisions, deviceRevisions);
        for (DeviceRecordView record : records) {
        	if(!record.isDeleted()){
            final Row row = appendRow(sheet);
            appendCell(row, record.getDevice().getUuid().toString());
            appendCell(row, record.getSuperSection().getMnemonic());
            appendCell(row, record.getSection().getMnemonic());
            appendCell(row, record.getSubsection().getMnemonic());
            appendCell(row, record.getDiscipline().getMnemonic());
            appendCell(row, record.getDeviceGroup().getMnemonic());
            appendCell(row, record.getInstanceIndex());
            appendCell(row, record.getDescription());
            appendCell(row, record.getConventionName());
            appendCell(row, new SimpleDateFormat("yyyy-MM-dd").format(record.getDeviceRevision().getRequestDate()));
        	}
        }
    }

    
    private XSSFSheet createSheetWithHeader(XSSFWorkbook workbook, String sheetName, String... columnNames) {
        final XSSFSheet sheet = workbook.createSheet(sheetName);
        final Row row = appendRow(sheet);
        for (String columnName : columnNames) {
            appendCell(row, columnName);
        }
        return sheet;
    }

    private Row appendRow(XSSFSheet sheet) {
        return sheet.createRow(sheet.getRow(0) == null ? 0 : sheet.getLastRowNum()+1);
    }

    private Cell appendCell(Row row, String value) {
        final Cell cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(value);
        return cell;
    }
}
