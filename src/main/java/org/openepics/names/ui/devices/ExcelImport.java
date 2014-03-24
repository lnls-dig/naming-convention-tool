package org.openepics.names.ui.devices;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Cell;
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
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
import org.primefaces.model.TreeNode;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;



@Stateless
public class ExcelImport {
    @Inject private RestrictedDeviceService deviceService;
    @Inject private RestrictedNamePartService namePartService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    
    private Table<String, String, NamePart> sectionsTable, typesTable;
    private List<DeviceRevision> allDevices;
    
    public void parseDeviceImportFile(InputStream file) throws Exception {
        organizeDataFromDatabase(true);
        int rowNumber = 2;        
        
        try {
            final XSSFWorkbook workbook = new XSSFWorkbook(file);
            final XSSFSheet sheet = workbook.getSheetAt(0);
 
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
           
            while (rowIterator.hasNext()) {
                final Row row = rowIterator.next();
                String section = row.getCell(0).getStringCellValue();
                String subsection = row.getCell(1).getCellType() == Cell.CELL_TYPE_NUMERIC ? String.valueOf((int)row.getCell(1).getNumericCellValue()) : row.getCell(1).getStringCellValue();
                String discipline = row.getCell(2).getStringCellValue();;
                String deviceType = row.getCell(3).getStringCellValue();;
                String index = row.getCell(4).getCellType() == Cell.CELL_TYPE_NUMERIC ? String.valueOf((int)row.getCell(4).getNumericCellValue()) : row.getCell(4).getStringCellValue();
                addDeviceName(section,subsection,discipline,deviceType,index,rowNumber);
                rowNumber++;
            }
            file.close();
        } catch (Exception e) {
            throw e;
        }
    }
    
    
    
    private void organizeDataFromDatabase(boolean isImport) {
        sectionsTable = HashBasedTable.create();
        final List<NamePartRevision> approvedSectionsRevisions = namePartService.currentApprovedRevisions(NamePartType.SECTION, false);
        populateSectionsTable(namePartTreeBuilder.namePartApprovalTree(approvedSectionsRevisions, Lists.<NamePartRevision>newArrayList(), true), 0);
        
        typesTable = HashBasedTable.create();
        final List<NamePartRevision> approvedTypeRevisions = namePartService.currentApprovedRevisions(NamePartType.DEVICE_TYPE, false);
        populateTypesTable(namePartTreeBuilder.namePartApprovalTree(approvedTypeRevisions, Lists.<NamePartRevision>newArrayList(), true), 0, "");
               
        allDevices = Lists.newArrayList();
        for (Device device : deviceService.devices(false)) {
            allDevices.add(deviceService.currentRevision(device));
        }
    }
    
    private void addDeviceName(String section, String subsection, String discipline, String deviceType, String index, int rowCounter) throws Exception {
        final NamePart sectionPart = sectionsTable.get(section, subsection);
        if (sectionPart == null) {
            throw new Exception("Error occured in row: "+rowCounter+ ". Logical area part was not fount in the database.");
        }
        
        final NamePart typePart = typesTable.get(discipline, deviceType);
        if (typePart == null) {
            throw new Exception("Error occured in row: "+rowCounter+ ". Device category part was not fount in the database.");
        }
        
        for (DeviceRevision deviceRevision : allDevices) {
            if (deviceRevision.getSection().equals(sectionPart) && deviceRevision.getDeviceType().equals(typePart) && deviceRevision.getQualifier().equals(index)) {
                return;
            }
        }
        allDevices.add(deviceService.createDevice(sectionPart, typePart, index));
    }
    
    private void populateSectionsTable(TreeNode node, int level) {
        final @Nullable NamePartView nodeView = (NamePartView) node.getData();
        if (node.getChildCount() > 0) {
            for (TreeNode childNode : node.getChildren()) {
                final @Nullable NamePartView childView = (NamePartView) childNode.getData();
                if (childView != null && (level == 0 || level == 1)) {
                    populateSectionsTable(childNode, level+1);
                } else if  (nodeView != null && childView != null && level == 2) {
                    sectionsTable.put(nodeView.getName(), childView.getName(), childView.getNamePart());
                }  
            }            
        }
    }
    
    private void populateTypesTable(TreeNode node, int level, String discipline) {
        if (node.getChildCount() > 0) {
            for (TreeNode childNode : node.getChildren()) {
                final @Nullable NamePartView childView = (NamePartView) childNode.getData();
                if (childView != null && (level == 1)) {
                    populateTypesTable(childNode, level+1, discipline);
                } else if (childView != null && level == 0) {
                    populateTypesTable(childNode, level+1, childView.getName());
                } else if  (childView != null && level == 2) {
                    typesTable.put(discipline, childView.getName(), childView.getNamePart());
                }  
            }            
        }
    }
}
