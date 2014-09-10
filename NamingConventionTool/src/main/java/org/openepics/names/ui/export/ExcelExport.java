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
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.BatchViewProvider;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.util.As;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * A bean for exporting sections, device types and devices to Excel.
 */
@Stateless
public class ExcelExport {
    
    @Inject private RestrictedNamePartService namePartService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;

    /**
     * Exports the entities from the database, producing a stream which can be streamed to the user over HTTP.
     *
     * @return an Excel input stream containing the exported data
     */
    public InputStream exportFile() {
        final List<NamePartRevision> approvedSectionsRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
        final TreeNode sectionsTree = namePartTreeBuilder.newNamePartTree(approvedSectionsRevisions, Lists.<NamePartRevision>newArrayList(), true);

        final List<NamePartRevision> approvedTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
        final TreeNode typesTree = namePartTreeBuilder.newNamePartTree(approvedTypeRevisions, Lists.<NamePartRevision>newArrayList(), true);

        final List<DeviceRevision> devices = Lists.newArrayList();
        for (DeviceRevision deviceRevision : namePartService.currentDeviceRevisions(false)) {
            devices.add(deviceRevision);
        }
        
        final XSSFWorkbook workbook = exportWorkbook(sectionsTree, typesTree, devices);
                 
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
    
    private XSSFWorkbook exportWorkbook(TreeNode sectionsTree, TreeNode typesTree, List<DeviceRevision> devices) {
        final XSSFWorkbook workbook = new XSSFWorkbook();

        final XSSFSheet superSectionSheet = createSheetWithHeader(workbook, "SuperSection", "SuperSection::ID", "SuperSection::FullName", "SuperSection::Name", "SuperSection::Date Modified");
        fillNamePartSheet(superSectionSheet, 1, sectionsTree);

        final XSSFSheet sectionSheet = createSheetWithHeader(workbook, "Section", "SuperSection::FullName", "SuperSection::Name", "Section::ID", "Section::FullName", "Section::Name", "Section::Date Modified");
        fillNamePartSheet(sectionSheet, 2, sectionsTree);

        final XSSFSheet subSectionSheet = createSheetWithHeader(workbook, "SubSection", "SuperSection::FullName", "SuperSection::Name", "Section::FullName", "Section::Name", "SubSection::ID", "SubSection::FullName", "SubSection::Name", "SubSection::Date Modified");
        fillNamePartSheet(subSectionSheet, 3, sectionsTree);

        final XSSFSheet disciplineSheet = createSheetWithHeader(workbook, "Discipline", "Discipline::ID", "Discipline::FullName", "Discipline::Name", "Discipline::Date Modified");
        fillNamePartSheet(disciplineSheet, 1, typesTree);

        final XSSFSheet categorySheet = createSheetWithHeader(workbook, "Category", "Discipline::FullName", "Discipline::Name", "Category::ID", "Category::FullName", "Category::Name", "Category::Date Modified");
        fillNamePartSheet(categorySheet, 2, typesTree);

        final XSSFSheet deviceTypeSheet = createSheetWithHeader(workbook, "DeviceType", "Discipline::FullName", "Discipline::Name", "Category::FullName", "Category::Name", "DeviceType::ID", "DeviceType::FullName", "DeviceType::Name", "DeviceType::Date Modified");
        fillNamePartSheet(deviceTypeSheet, 3, typesTree);

        final XSSFSheet namedDeviceSheet = createSheetWithHeader(workbook, "NamedDevice", "ID", "Section", "SubSection", "Discipline", "DeviceType", "InstanceIndex", "Comment", "Name", "Date Modified");
        fillDeviceSheet(namedDeviceSheet, devices);
        
        return workbook;
    }

    private void fillNamePartSheet(XSSFSheet sheet, int maxLevel, TreeNode node) {
        fillNamePartSheet(sheet, maxLevel, 1, node, Lists.<String>newArrayList());
    }
    
    private void fillNamePartSheet(XSSFSheet sheet, int maxLevel, int currentLevel, TreeNode node, List<String> rowData) {
        for (TreeNode child : node.getChildren()) {
            final @Nullable NamePartView childView = (NamePartView) child.getData();
            if (childView != null) {
                if (currentLevel < maxLevel) {
                    final List<String> ancestorData = ImmutableList.<String>builder().addAll(rowData).add(childView.getName(), childView.getMnemonic()).build();
                    fillNamePartSheet(sheet, maxLevel, currentLevel + 1, child, ancestorData);
                } else {
                    final Row row = appendRow(sheet);
                    for (String sectionInfo : rowData) {
                        appendCell(row, sectionInfo);
                    }
                    appendCell(row, childView.getNamePart().getUuid().toString());
                    appendCell(row, childView.getName());
                    appendCell(row, childView.getMnemonic());
                    appendCell(row, new SimpleDateFormat("yyyy-MM-dd").format(As.notNull(childView.getCurrentRevision()).getProcessDate()));
                }
            } else {
                return;
            }
        }        
    }
    
    private void fillDeviceSheet(XSSFSheet sheet, List<DeviceRevision> devices) {
        final List<NamePartRevision> sectionRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
        final List<NamePartRevision> deviceTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
        final List<DeviceRevision> deviceRevisions = namePartService.currentDeviceRevisions(false);
        final BatchViewProvider viewProvider = new BatchViewProvider(sectionRevisions, deviceTypeRevisions, deviceRevisions);
        for (DeviceRevision device : devices) {
            final Row row = appendRow(sheet);
            appendCell(row, device.getDevice().getUuid().toString());
            appendCell(row, As.notNull(viewProvider.view(device.getSection()).getParent()).getMnemonic());
            appendCell(row, viewProvider.view(device.getSection()).getMnemonic());
            appendCell(row, As.notNull(As.notNull(viewProvider.view(device.getDeviceType()).getParent()).getParent()).getMnemonic());
            appendCell(row, viewProvider.view(device.getDeviceType()).getMnemonic());
            appendCell(row, viewProvider.view(device).getInstanceIndex());
            appendCell(row, viewProvider.view(device).getAdditionalInfo());
            appendCell(row, viewProvider.view(device).getConventionName());
            appendCell(row, new SimpleDateFormat("yyyy-MM-dd").format(device.getRequestDate()));
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
