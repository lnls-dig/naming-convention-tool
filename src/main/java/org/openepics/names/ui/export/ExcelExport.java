package org.openepics.names.ui.export;

import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class ExcelExport {
    
    @Inject private RestrictedDeviceService deviceService;
    @Inject private RestrictedNamePartService namePartService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private ViewFactory viewFactory;
    
    private List<DeviceRevision> allDevices;
    private TreeNode sectionsTree, typesTree;
    
    public InputStream exportFile() {  
        organizeDataFromDatabase();
        
        XSSFWorkbook workbook = excellExportWorkbook(sectionsTree, typesTree); 
                 
        InputStream inputStream = null;
        try {
            final File temporaryFile = File.createTempFile("temp", "xlsx");
            FileOutputStream outputStream = new FileOutputStream(temporaryFile);
            workbook.write(outputStream);          
            outputStream.close();
            inputStream = new FileInputStream(temporaryFile);
            temporaryFile.delete();
        } catch (IOException neverHappens) {
        }
        return inputStream;       
    }
    
    private void organizeDataFromDatabase() {
        final List<NamePartRevision> approvedSectionsRevisions = namePartService.currentApprovedRevisions(NamePartType.SECTION, false);
        sectionsTree = namePartTreeBuilder.newNamePartTree(approvedSectionsRevisions, Lists.<NamePartRevision>newArrayList(), true);
        
        final List<NamePartRevision> approvedTypeRevisions = namePartService.currentApprovedRevisions(NamePartType.DEVICE_TYPE, false);
        typesTree = namePartTreeBuilder.newNamePartTree(approvedTypeRevisions, Lists.<NamePartRevision>newArrayList(), true);
                       
        allDevices = Lists.newArrayList();
        for (Device device : deviceService.devices(false)) {
            allDevices.add(deviceService.currentRevision(device));
        }
    }
    
    private XSSFWorkbook excellExportWorkbook(TreeNode sectionsTree, TreeNode typesTree) {
        final XSSFWorkbook workbook = new XSSFWorkbook();
        
        XSSFSheet sheet = workbook.createSheet("SuperSection");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("SuperSection::ID");
        row.createCell(1).setCellValue("SuperSection::FullName");
        row.createCell(2).setCellValue("SuperSection::Name");
        namePartExcellSheet(sheet, 1, 1, sectionsTree, null);
        
        sheet = workbook.createSheet("Section");
        row = sheet.createRow(0);
        row.createCell(0).setCellValue("SuperSection::FullName");
        row.createCell(1).setCellValue("SuperSection::Name");
        row.createCell(2).setCellValue("Section::ID");
        row.createCell(3).setCellValue("Section::FullName");
        row.createCell(4).setCellValue("Section::Name");
        namePartExcellSheet(sheet, 2, 1, sectionsTree, null);
        
        sheet = workbook.createSheet("SubSection");
        row = sheet.createRow(0);
        row.createCell(0).setCellValue("SuperSection::FullName");
        row.createCell(1).setCellValue("SuperSection::Name");
        row.createCell(2).setCellValue("Section::FullName");
        row.createCell(3).setCellValue("Section::Name");
        row.createCell(4).setCellValue("SubSection::ID");
        row.createCell(5).setCellValue("SubSection::FullName");
        row.createCell(6).setCellValue("SubSection::Name");
        namePartExcellSheet(sheet, 3, 1, sectionsTree, null);
        
        sheet = workbook.createSheet("Discipline");
        row = sheet.createRow(0);
        row.createCell(0).setCellValue("Discipline::ID");
        row.createCell(1).setCellValue("Discipline::FullName");
        row.createCell(2).setCellValue("Discipline::Name");
        namePartExcellSheet(sheet, 1, 1, typesTree, null);
        
        sheet = workbook.createSheet("Category");
        row = sheet.createRow(0);
        row.createCell(0).setCellValue("Discipline::FullName");
        row.createCell(1).setCellValue("Discipline::Name");
        row.createCell(2).setCellValue("Category::ID");
        row.createCell(3).setCellValue("Category::FullName");
        row.createCell(4).setCellValue("Category::Name");
        namePartExcellSheet(sheet, 2, 1, typesTree, null);
        
        sheet = workbook.createSheet("DeviceType");
        row = sheet.createRow(0);
        row.createCell(0).setCellValue("Discipline::FullName");
        row.createCell(1).setCellValue("Discipline::Name");
        row.createCell(2).setCellValue("Category::FullName");
        row.createCell(3).setCellValue("Category::Name");
        row.createCell(4).setCellValue("DeviceType::ID");
        row.createCell(5).setCellValue("DeviceType::FullName");
        row.createCell(6).setCellValue("DeviceType::Name");
        namePartExcellSheet(sheet, 3, 1, typesTree, null);
        
        sheet = workbook.createSheet("NamedDevice");
        row = sheet.createRow(0);
        row.createCell(0).setCellValue("ID");
        row.createCell(1).setCellValue("Section");
        row.createCell(2).setCellValue("SubSection");
        row.createCell(3).setCellValue("Discipline");
        row.createCell(4).setCellValue("DeviceType");
        row.createCell(5).setCellValue("InstanceIndex");
        row.createCell(6).setCellValue("Name");
        deviceNameExcellSheet(sheet);
        
        return workbook;
    }
    
    private void namePartExcellSheet(XSSFSheet sheet, int maxLevel, int currentLevel, TreeNode node, List<String> rowData) {
        for(TreeNode child : node.getChildren()) {
            final @Nullable NamePartView childView = (NamePartView) child.getData();
            if (childView != null) {
                if (currentLevel == 1) {
                    rowData = Lists.newArrayList();
                }
                
                if (currentLevel < maxLevel) {
                    final ArrayList<String> ancestorInfo = Lists.newArrayList(rowData);
                    ancestorInfo.add(childView.getName());
                    ancestorInfo.add(childView.getMnemonic());
                    namePartExcellSheet(sheet, maxLevel, currentLevel+1, child, ancestorInfo);
                } else if (currentLevel == maxLevel) {
                    int cellCounter = 0;
                    final Row row = sheet.createRow(sheet.getLastRowNum()+1);
                    for (String sectionInfo : rowData) {
                        row.createCell(cellCounter++).setCellValue(sectionInfo);
                    }
                    row.createCell(cellCounter++).setCellValue(childView.getNamePart().getUuid().toString());
                    row.createCell(cellCounter++).setCellValue(childView.getName());
                    row.createCell(cellCounter++).setCellValue(childView.getMnemonic());
                } else {
                    throw new IllegalStateException();
                }
            } else {
                return;
            }
        }        
    }
    
    private void deviceNameExcellSheet(XSSFSheet sheet) {
        for (DeviceRevision device : allDevices) {
            int cellNumber = 0;
            Row row = sheet.createRow(sheet.getLastRowNum()+1);
            row.createCell(cellNumber++).setCellValue(device.getDevice().getUuid().toString());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device.getSection()).getParent().getMnemonic());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device.getSection()).getMnemonic());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device.getDeviceType()).getParent().getParent().getMnemonic());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device.getDeviceType()).getMnemonic());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device).getInstanceIndex());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device).getConventionName());
        }
    }
}
