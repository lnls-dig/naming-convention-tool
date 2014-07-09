package org.openepics.names.ui.devices;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.DeviceDefinition;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.util.As;
import org.openepics.names.util.ExcelCell;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * A bean for importing devices from Excel.
 */
@Stateless
public class ExcelImport {

    @Inject private RestrictedNamePartService namePartService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    
    private Table<String, String, NamePart> sectionsTable;
    private Table<String, String, NamePart> typesTable;
    private Set<DeviceDefinition> existingDevices;
    private Set<DeviceDefinition> newDevices;

    /**
     * Reports the outcome of the import operation.
     */
    public abstract class ExcelImportResult {}

    /**
     * Reports a successful outcome of the import operation.
     */
    public class SuccessExcelImportResult extends ExcelImportResult {}
    
    /**
     * Reports a failed outcome of the import operation.
     */
    public abstract class FailureExcelImportResult extends ExcelImportResult {}
    
    /**
     * Reports a failed outcome of the import operation, because of wrong format of the import file.
     */
    public class ColumnCountFailureExcelImportResult extends FailureExcelImportResult {}
    
    /**
     * Reports a failed outcome of the import operation, because either the section or device type referred to in the
     * device row could not be found.
     */
    public class CellValueFailureExcelImportResult extends FailureExcelImportResult {
        final private int rowNumber;
        final private NamePartType namePartType;

        /**
         * @param rowNumber the row where the error happened
         * @param namePartType the type of the entity that was not found
         */
        public CellValueFailureExcelImportResult(int rowNumber, NamePartType namePartType) {
            this.rowNumber = rowNumber;
            this.namePartType = namePartType;
        }

        /**
         * The row where the error happened.
         */
        public int getRowNumber() { return rowNumber; }

        /**
         * The type of the entity that was not found.
         */
        public NamePartType getNamePartType() { return namePartType; } 
    }

    /**
     * Parses the input stream read from an Excel file, creating devices in the database. If the device already exists,
     * it's silently ignored.
     *
     * @param input the input stream
     * @return an ExcelImportResult object reporting the outcome of the import operation
     */
    public ExcelImportResult parseDeviceImportFile(InputStream input) {
        init();

        try {
            final XSSFWorkbook workbook = new XSSFWorkbook(input);
            final XSSFSheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                if (row.getRowNum() >= 2) {
                    if (row.getLastCellNum() != 6) {
                        return new ColumnCountFailureExcelImportResult();
                    } else {
                        final String section = As.notNull(ExcelCell.asString(row.getCell(0)));
                        final String subsection = As.notNull(ExcelCell.asString(row.getCell(1)));
                        final String discipline = As.notNull(ExcelCell.asString(row.getCell(2)));
                        final String deviceType = As.notNull(ExcelCell.asString(row.getCell(3)));
                        final @Nullable String index = ExcelCell.asString(row.getCell(4));
                        final ExcelImportResult addDeviceNameResult = addDeviceName(section, subsection, discipline, deviceType, index, row.getRowNum());
                        if (addDeviceNameResult instanceof FailureExcelImportResult) {
                            return addDeviceNameResult;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        namePartService.batchAddDevices(newDevices);
        return new SuccessExcelImportResult();
    }
    
    private void init() {
        newDevices = Sets.newHashSet();

        final List<NamePartRevision> approvedSectionsRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
        final List<NamePartRevision> approvedTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);

        sectionsTable = HashBasedTable.create();
        populateSectionsTable(namePartTreeBuilder.newNamePartTree(approvedSectionsRevisions, Lists.<NamePartRevision>newArrayList(), true), 0);

        typesTable = HashBasedTable.create();
        populateTypesTable(namePartTreeBuilder.newNamePartTree(approvedTypeRevisions, Lists.<NamePartRevision>newArrayList(), true), 0, "");

        existingDevices = Sets.newHashSet();
        for (DeviceRevision deviceRevision : namePartService.currentDeviceRevisions(false)) {
            existingDevices.add(new DeviceDefinition(deviceRevision.getSection(), deviceRevision.getDeviceType(), deviceRevision.getInstanceIndex()));
        }
    }
    
    private ExcelImportResult addDeviceName(String section, String subsection, String discipline, String deviceType, @Nullable String instanceIndex, int rowCounter) {
        final  @Nullable NamePart sectionPart = sectionsTable.get(section, subsection);
        final @Nullable NamePart typePart = typesTable.get(discipline, deviceType);

        if (sectionPart == null) {
            return new CellValueFailureExcelImportResult(rowCounter + 1, NamePartType.SECTION);
        } else if (typePart == null) {
            return new CellValueFailureExcelImportResult(rowCounter + 1, NamePartType.DEVICE_TYPE);
        } else {
            final DeviceDefinition newDevice = new DeviceDefinition(sectionPart, typePart, instanceIndex);
            if (!existingDevices.contains(newDevice)) {
                newDevices.add(newDevice);
            }
            return new SuccessExcelImportResult();
        }
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

