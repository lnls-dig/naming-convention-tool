package org.openepics.names.services;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openepics.names.model.*;
import org.openepics.names.util.As;
import org.openepics.names.util.ExcelCell;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A service bean used to initialize the database with data from the bundled Excel file when the application is ran for
 * the first time.
 *
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
*/

@Stateless
public class InitialDataImportService {
    
    @Inject private NamePartService namePartService;
    @PersistenceContext private EntityManager em;

    private XSSFWorkbook workbook;
    private Map<Integer, NamePart> namePartsMap = Maps.newHashMap();

    /**
     * Populates the database with initial data bundled as a resource within the application.
     */
    public void fillDatabaseWithInitialData() {
        try {
            workbook = new XSSFWorkbook(this.getClass().getResourceAsStream("NamingDatabaseImport.xlsx"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
 //       fillUserAccounts();
        fillNameParts(true);
        fillNameParts(false);
        fillDeviceNames();
    }
    
    private void fillUserAccounts() {
        em.persist(new UserAccount("root", Role.SUPERUSER));
        em.persist(new UserAccount("admin", Role.SUPERUSER));
        em.persist(new UserAccount("editor", Role.EDITOR));
        em.persist(new UserAccount("miha", Role.EDITOR));
        em.persist(new UserAccount("marko", Role.EDITOR));
        em.persist(new UserAccount("apozar", Role.EDITOR));
    }
    
    private void fillNameParts(boolean isSection) {
        final XSSFSheet sheet = workbook.getSheet(isSection ? "LogicalAreaStructure" : "DeviceCategoryStructure");
        for (Row row : sheet) {
            if (row.getRowNum() >= 2) {
                final int parent = (int) ExcelCell.asNumber(row.getCell(0));
                final int id = (int) ExcelCell.asNumber(row.getCell(1));
                final String name = As.notNull(ExcelCell.asString(row.getCell(2)));
                final String mnemonic = As.notNull(ExcelCell.asString(row.getCell(3)));
                @Nullable final String description = ExcelCell.asString(row.getCell(4));
                @Nullable final String type = ExcelCell.asString(row.getCell(5));
                namePartsMap.put(id, isSection ? addSection(namePartsMap.get(parent), name, mnemonic, description) : addDeviceType(namePartsMap.get(parent), name, mnemonic, description));
            }
        }
    }
    
    private void fillDeviceNames() {
        final XSSFSheet sheet = workbook.getSheet("NamedDevices");
        final List<DeviceDefinition> devices = Lists.newArrayList();
        for (Row row : sheet) {
            if (row.getRowNum() >= 1) {
                final int subsectionId = (int) ExcelCell.asNumber(row.getCell(1));
                final int deviceTypeId = (int) ExcelCell.asNumber(row.getCell(2));
                @Nullable final String instanceIndex = ExcelCell.asString(row.getCell(3));
                @Nullable final String additionalInfo = ExcelCell.asString(row.getCell(4));
                devices.add(new DeviceDefinition(namePartsMap.get(subsectionId), namePartsMap.get(deviceTypeId), instanceIndex, additionalInfo));
            }
        }
        namePartService.batchAddDevices(devices, null);
    }

    private NamePart addSection(@Nullable NamePart parent, String name, String mnemonic, @Nullable String description) {
        final NamePartRevision newRevision = namePartService.addNamePart(name, mnemonic, description, NamePartType.SECTION, parent, null, "Initial data");
        namePartService.approveNamePartRevision(newRevision, null, null);
        return newRevision.getNamePart();
    }

    private NamePart addDeviceType(@Nullable NamePart parent, String name, String mnemonic, @Nullable String description) {
        final NamePartRevision newRevision = namePartService.addNamePart(name, mnemonic,description, NamePartType.DEVICE_TYPE, parent, null, "Initial data");
        namePartService.approveNamePartRevision(newRevision, null, null);
        return newRevision.getNamePart();
    }
}
