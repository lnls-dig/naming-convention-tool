package org.openepics.names.ui.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.common.UserManager;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
import org.primefaces.model.TreeNode;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

@Stateless
public class ExcellExport {
    
    @Inject private RestrictedDeviceService deviceService;
    @Inject private RestrictedNamePartService namePartService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private ViewFactory viewFactory;
    
    private List<DeviceRevision> allDevices;
    private TreeNode sectionsTree, typesTree;
    
    public InputStream exportFile() {  
        organizeDataFromDatabase();
        
        XSSFWorkbook workbook = excellExportWorkbook(sectionsTree, typesTree); 
                 
        File f;
        InputStream in = null;
        try {
            f = File.createTempFile("temp", "xlsx");
            FileOutputStream out = new FileOutputStream(f);
            workbook.write(out);          
            out.close();
            in = new FileInputStream(f);
            f.delete();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return in;
            
            
       
    }
    
    private void organizeDataFromDatabase() {
        buildNamePartTrees();
                       
        allDevices = Lists.newArrayList();
        for (Device device : deviceService.devices(false)) {
            allDevices.add(deviceService.currentRevision(device));
        }
    }
    
    private void buildNamePartTrees() {
        final List<NamePartRevision> approvedSectionsRevisions = ImmutableList.copyOf(Collections2.filter(namePartService.currentApprovedRevisions(false), new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == NamePartType.SECTION; }
        })); 
        
        final List<NamePartRevision> pendingRevisions = Lists.newArrayList();  
        sectionsTree = namePartTreeBuilder.namePartApprovalTree(approvedSectionsRevisions, pendingRevisions, true);
        
        final List<NamePartRevision> approvedTypeRevisions = ImmutableList.copyOf(Collections2.filter(namePartService.currentApprovedRevisions(false), new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == NamePartType.DEVICE_TYPE; }
        }));
        typesTree = namePartTreeBuilder.namePartApprovalTree(approvedTypeRevisions, pendingRevisions, true);
    }
    
    private XSSFWorkbook excellExportWorkbook(TreeNode sectionsTree, TreeNode typesTree) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
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
                    rowData = new ArrayList<String>();
                }
                if (currentLevel < maxLevel) {
                    ArrayList<String> ancestorInfo = Lists.newArrayList();
                    ancestorInfo.addAll(rowData);
                    ancestorInfo.add(childView.getFullName());
                    ancestorInfo.add(childView.getName());                    
                    namePartExcellSheet(sheet, maxLevel, currentLevel+1, child,ancestorInfo);
                } else if (maxLevel == currentLevel) {
                    ArrayList<String> ancestorInfo = Lists.newArrayList();
                    ancestorInfo.addAll(rowData);
                    ancestorInfo.add(childView.getNamePart().getUuid());
                    ancestorInfo.add(childView.getFullName());
                    ancestorInfo.add(childView.getName());
                    
                    Row row = sheet.createRow(sheet.getLastRowNum()+1);
                    int cellCounter = 0;
                    for (String sectionInfo : ancestorInfo) {
                        row.createCell(cellCounter++).setCellValue(sectionInfo);
                    }
                } else {
                    throw new IllegalStateException();
                }
            } else {
                return;
            }
        }
        return;
        
    }
    
    private void deviceNameExcellSheet(XSSFSheet sheet) {
        for (DeviceRevision device : allDevices) {
            int cellNumber = 0;
            Row row = sheet.createRow(sheet.getLastRowNum()+1);
            row.createCell(cellNumber++).setCellValue(device.getDevice().getUuid());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device.getSection()).getParent().getName());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device.getSection()).getName());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device.getDeviceType()).getParent().getParent().getName());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device.getDeviceType()).getName());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device).getQualifier());
            row.createCell(cellNumber++).setCellValue(viewFactory.getView(device).getConventionName());
        }
    }
}
