package org.openepics.names.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;
import org.openepics.names.util.As;

import com.google.common.collect.Maps;

/**
*
* @author Andraz Pozar <andraz.pozar@cosylab.com>
*/

@Stateless
public class InitialDataImportService {
    
    @Inject private NamePartService namePartService;
    @Inject private DeviceService deviceService;
    @PersistenceContext private EntityManager em;
    private XSSFWorkbook importFile;
    private HashMap<Integer, NamePart> namePartsMap;

    public void fillDatabaseWithInitialData() throws IOException {
        if (namePartService.approvedNames().isEmpty()) {
            importFile = new XSSFWorkbook(this.getClass().getResourceAsStream("NamingDatabaseImport.xlsx"));
            namePartsMap = Maps.newHashMap();
            fillUserAccounts();
            fillNameParts(true);
            fillNameParts(false);
            fillDeviceNames();
        }
    }
    
    private void fillUserAccounts() {
        em.persist(new UserAccount("root", Role.SUPERUSER));
        em.persist(new UserAccount("admin", Role.SUPERUSER));
        em.persist(new UserAccount("jaba", Role.EDITOR));
        em.persist(new UserAccount("miha", Role.EDITOR));
        em.persist(new UserAccount("marko", Role.EDITOR));
        em.persist(new UserAccount("apozar", Role.EDITOR));
    }
    
    private void fillNameParts(boolean isSection) {
               
        final XSSFSheet sheet = importFile.getSheet(isSection ? "LogicalAreaStructure" : "DeviceCategoryStructure");
        final Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
       
        while (rowIterator.hasNext()) {
            final Row row = rowIterator.next();            
            final int parent = (int) row.getCell(0).getNumericCellValue();
            final int id = (int) row.getCell(1).getNumericCellValue();
            final String fullName = As.notNull(cellAsString(row.getCell(2)));
            final String name = As.notNull(cellAsString(row.getCell(3)));
            @Nullable final String comment = cellAsString(row.getCell(4));
            @Nullable final String type = cellAsString(row.getCell(5));
            namePartsMap.put(id, isSection ? addSection(namePartsMap.get(parent), fullName, name) : addDeviceType(namePartsMap.get(parent), fullName, name)); 
        }
    }
    
    private @Nullable String cellAsString(@Nullable Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return String.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                return cell.getStringCellValue();
            } else {
                throw new IllegalStateException();
            }
        } else {
            return null;
        }
    }
    
    private void fillDeviceNames() {
        final XSSFSheet sheet = importFile.getSheet("NamedDevices");
        final Iterator<Row> rowIterator = sheet.iterator();
        
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
       
        while (rowIterator.hasNext()) {
            final Row row = rowIterator.next();
            
            final int subsectionId = (int) row.getCell(1).getNumericCellValue();
            final int deviceTypeId = (int) row.getCell(2).getNumericCellValue();
            @Nullable final String instanceIndex = cellAsString(row.getCell(3));
            @Nullable final String comment = cellAsString(row.getCell(4));
            
            addDeviceName(namePartsMap.get(subsectionId), namePartsMap.get(deviceTypeId), instanceIndex);
            
        }
    }
    
    private NamePart addSection(@Nullable NamePart parent, String longName, String shortName) {
        final NamePartRevision newRevision = namePartService.addNamePart(shortName, longName, NamePartType.SECTION, parent, null, "Initial data");
        namePartService.approveNamePartRevision(newRevision, null, null);
        return newRevision.getNamePart();
    }

    private NamePart addDeviceType(@Nullable NamePart parent, String longName, String shortName) {
        final NamePartRevision newRevision = namePartService.addNamePart(shortName, longName, NamePartType.DEVICE_TYPE, parent, null, "Initial data");
        namePartService.approveNamePartRevision(newRevision, null, null);
        return newRevision.getNamePart();
    }
    
    private void addDeviceName(NamePart subSection, NamePart deviceType, String instanceIndex) {
        deviceService.createDevice(subSection, deviceType, instanceIndex, null);
    }
    
}
