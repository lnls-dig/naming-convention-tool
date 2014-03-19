package org.openepics.names.ui.devices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.imageio.stream.FileImageInputStream;
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
public class ExcellImport {
    @Inject private RestrictedDeviceService deviceService;
    @Inject private RestrictedNamePartService namePartService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    
    private Table<String, String, NamePart> sectionsTable, typesTable;
    private List<DeviceRevision> allDevices;
    private TreeNode sectionsTree, typesTree;
    
    public void parseDeviceImportFile(InputStream file) throws Exception {
        organizeDataFromDatabase(true);
        int rowCounter = 2;
        
        
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
 
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            long start = System.currentTimeMillis();
            while (rowIterator.hasNext()) 
            {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                int cellNumber = 0;
                String section = "";
                String subsection = "";
                String discipline = "";
                String deviceType = "";
                String index = "";
                while (cellIterator.hasNext()) 
                {
                    Cell cell = cellIterator.next();
                    switch (cellNumber) {
                    case 0:
                        section = cell.getStringCellValue();
                        break;
                    case 1:
                        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            subsection = String.valueOf((int)cell.getNumericCellValue());
                        } else {
                            subsection = cell.getStringCellValue();
                        }
                        break;
                    case 2:
                        discipline = cell.getStringCellValue();
                        break;
                    case 3:
                        deviceType = cell.getStringCellValue();
                        break;
                    case 4:
                        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            index = String.valueOf((int)cell.getNumericCellValue());
                        } else {
                            index = cell.getStringCellValue();
                        }
                        break;
                    }
                    cellNumber++;
                }
                
                addDeviceName(section,subsection,discipline,deviceType,index,rowCounter);
                rowCounter++;
                
            }
            System.out.println("TIME: "+ (System.currentTimeMillis() - start));
            file.close();
        } 
        catch (Exception e) 
        {
            throw e;
        }
    }
    
    
    
    private void organizeDataFromDatabase(boolean isImport) {
        buildNamePartTrees();
        buildMappingTables();
               
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
    
    private void buildMappingTables() {
        sectionsTable = HashBasedTable.create();
        populateSectionsTable(sectionsTree, 0);
        typesTable = HashBasedTable.create();
        populateTypesTable(typesTree, 0, "");
    }    
    
    private void addDeviceName(String section, String subsection, String discipline, String deviceType, String index, int rowCounter) throws Exception {
    
        NamePart sectionPart = sectionsTable.get(section, subsection);
        if (sectionPart == null) {
            throw new Exception("Error occured in row: "+rowCounter+ ". Logical area part was not fount in the database.");
        }
        NamePart typePart = typesTable.get(discipline, deviceType);
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
        return;
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
        return;
    }
    
    

}
