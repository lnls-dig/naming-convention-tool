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
            //fillDeviceNames();
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
        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
       
        while (rowIterator.hasNext()) {
            final Row row = rowIterator.next();            
            int parent = (int)row.getCell(0).getNumericCellValue();
            int id = (int)row.getCell(1).getNumericCellValue();
            String fullName = row.getCell(2).getStringCellValue();
            String name = row.getCell(3).getStringCellValue();
            String comment = row.getCell(4).getStringCellValue();
            String type = row.getCell(5).getStringCellValue();
            namePartsMap.put(id, isSection ? addSection(namePartsMap.get(parent), fullName, name) : addDeviceType(namePartsMap.get(parent), fullName, name)); 
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
            final Iterator<Cell> cellIterator = row.cellIterator();
            int cellNumber = 0;
            
            int subsectionId = 0;
            int deviceTypeId = 0;
            String instanceIndex = "";
            String comment = "";
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cellNumber == 0) {
                    
                } else if (cellNumber == 1) {
                    subsectionId = (int)cell.getNumericCellValue();
                } else if (cellNumber == 2) {
                    deviceTypeId = (int)cell.getNumericCellValue();
                } else if (cellNumber == 3) {
                    instanceIndex = cell.getStringCellValue();
                } else if (cellNumber == 4) {
                    comment = cell.getStringCellValue();
                } else {
                    throw new IllegalStateException();
                }               
                cellNumber++;
            }
            
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
