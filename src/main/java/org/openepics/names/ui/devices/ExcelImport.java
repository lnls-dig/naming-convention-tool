package org.openepics.names.ui.devices;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openepics.names.model.*;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

@Stateless
public class ExcelImport {
    @Inject private RestrictedDeviceService deviceService;
    @Inject private RestrictedNamePartService namePartService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    
    private Table<String, String, NamePart> sectionsTable;
    private Table<String, String, NamePart> typesTable;
    private List<DeviceRevision> allDevices;
    private List<NewDeviceName> newDevices;
    
    public abstract class ExcelImportResult {}
    
    public class SuccessExcelImportResult extends ExcelImportResult {}
    
    public class FaliureExcelImportResult extends ExcelImportResult {
        final private int rowNumber;
        final private NamePartType namePartType;
        
        public FaliureExcelImportResult(int rowNumber, NamePartType namePartType) {
            this.rowNumber = rowNumber;
            this.namePartType = namePartType;
        }

        public int getRowNumber() { return rowNumber; }
        public NamePartType getNamePartType() { return namePartType; } 
    }
    
    public ExcelImportResult parseDeviceImportFile(InputStream input) {
        sectionsTable = HashBasedTable.create();
        typesTable = HashBasedTable.create();
        allDevices = Lists.newArrayList();
        newDevices = Lists.newArrayList();
        loadDataFromDatabase();

        try {
            final XSSFWorkbook workbook = new XSSFWorkbook(input);
            final XSSFSheet sheet = workbook.getSheetAt(0);
 
            final Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNumber = 2;
            while (rowIterator.hasNext()) {
                final Row row = rowIterator.next();
                final String section = row.getCell(0).getStringCellValue();
                final String subsection = row.getCell(1).getCellType() == Cell.CELL_TYPE_NUMERIC ? String.valueOf((int)row.getCell(1).getNumericCellValue()) : row.getCell(1).getStringCellValue();
                final String discipline = row.getCell(2).getStringCellValue();
                final String deviceType = row.getCell(3).getStringCellValue();
                final @Nullable String index = row.getCell(4) != null ? (row.getCell(4).getCellType() == Cell.CELL_TYPE_NUMERIC ? String.valueOf((int)row.getCell(4).getNumericCellValue()) : row.getCell(4).getStringCellValue()) : null;
                ExcelImportResult addDeviceNameResult = addDeviceName(section, subsection, discipline, deviceType, index, rowNumber++);
                if (addDeviceNameResult instanceof FaliureExcelImportResult) {
                    return addDeviceNameResult;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (NewDeviceName newDeviceName : newDevices) {
            deviceService.createDevice(newDeviceName.getSectionPart(), newDeviceName.getDeviceTypePart(), newDeviceName.getIndex());
        }
        return new SuccessExcelImportResult();
    }
    
    private void loadDataFromDatabase() {
        final List<NamePartRevision> approvedSectionsRevisions = namePartService.currentApprovedRevisions(NamePartType.SECTION, false);
        populateSectionsTable(namePartTreeBuilder.newNamePartTree(approvedSectionsRevisions, Lists.<NamePartRevision>newArrayList(), true), 0);

        final List<NamePartRevision> approvedTypeRevisions = namePartService.currentApprovedRevisions(NamePartType.DEVICE_TYPE, false);
        populateTypesTable(namePartTreeBuilder.newNamePartTree(approvedTypeRevisions, Lists.<NamePartRevision>newArrayList(), true), 0, "");

        for (Device device : deviceService.devices(false)) {
            allDevices.add(deviceService.currentRevision(device));
        }
    }
    
    private ExcelImportResult addDeviceName(String section, String subsection, String discipline, String deviceType, @Nullable String index, int rowCounter) {
        final  @Nullable NamePart sectionPart = sectionsTable.get(section, subsection);
        if (sectionPart == null) {
            return new FaliureExcelImportResult(rowCounter, NamePartType.SECTION);
        }
        
        final @Nullable NamePart typePart = typesTable.get(discipline, deviceType);
        if (typePart == null) {
            return new FaliureExcelImportResult(rowCounter, NamePartType.DEVICE_TYPE);
        }
        
        for (DeviceRevision deviceRevision : allDevices) {
            if (deviceRevision.getSection().equals(sectionPart) && deviceRevision.getDeviceType().equals(typePart) && (deviceRevision.getInstanceIndex() != null && deviceRevision.getInstanceIndex().equals(index) || index == null && deviceRevision.getInstanceIndex() == null)) {
                return new SuccessExcelImportResult();
            }
        }
        addNewDeviceName(sectionPart, typePart, index);
        return new SuccessExcelImportResult();
    }
    
    private void addNewDeviceName(NamePart sectionPart, NamePart typePart, @Nullable String index) {
        for (NewDeviceName deviceName : newDevices) {
            if (deviceName.getSectionPart().equals(sectionPart) && deviceName.getDeviceTypePart().equals(typePart) && (deviceName.getIndex() == null && index == null || deviceName.getIndex().equals(index))) {
                return;
            }
        }
        newDevices.add(new NewDeviceName(sectionPart, typePart, index));
    }
    
    private void populateSectionsTable(TreeNode node, int level) {
        final @Nullable NamePartView nodeView = (NamePartView) node.getData();
        for (TreeNode childNode : node.getChildren()) {
            final @Nullable NamePartView childView = (NamePartView) childNode.getData();
            if (childView != null && (level == 0 || level == 1)) {
                populateSectionsTable(childNode, level + 1);
            } else if (nodeView != null && childView != null && level == 2) {
                sectionsTable.put(nodeView.getMnemonic(), childView.getMnemonic(), childView.getNamePart());
            }
        }
    }
    
    private void populateTypesTable(TreeNode node, int level, String discipline) {
        for (TreeNode childNode : node.getChildren()) {
            final @Nullable NamePartView childView = (NamePartView) childNode.getData();
            if (childView != null && (level == 1)) {
                populateTypesTable(childNode, level + 1, discipline);
            } else if (childView != null && level == 0) {
                populateTypesTable(childNode, level + 1, childView.getMnemonic());
            } else if  (childView != null && level == 2) {
                typesTable.put(discipline, childView.getMnemonic(), childView.getNamePart());
            }
        }
    }
}

class NewDeviceName {
    private final NamePart sectionPart;
    private final NamePart deviceTypePart;
    private final @Nullable String index;
            
    public NewDeviceName(NamePart sectionPart, NamePart deviceTypePart, @Nullable String index) {
        this.sectionPart = sectionPart;
        this.deviceTypePart = deviceTypePart;
        this.index = index;
    }

    public NamePart getSectionPart() { return sectionPart; }
    public NamePart getDeviceTypePart() { return deviceTypePart; }
    public @Nullable String getIndex() { return index; }        
}
