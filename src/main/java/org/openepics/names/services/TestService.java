package org.openepics.names.services;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.names.model.Configuration;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameHierarchy;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartType;
import org.openepics.names.model.UserAccount;
import org.openepics.names.model.Role;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Singleton
@Startup
public class TestService {

    @Inject private NamePartService namePartService;
    @PersistenceContext private EntityManager em;

    @PostConstruct
    private void init() {
        if (namePartService.approvedNames().isEmpty()) {
            fillConfiguration();
            fillPrivileges();
            fillSections();
            fillDeviceTypes();
        }
    }

    private void fillConfiguration() {
        em.persist(new Configuration("version", "3.0"));
    }

    private void fillPrivileges() {
        em.persist(new UserAccount("root", Role.SUPERUSER));
        em.persist(new UserAccount("admin", Role.SUPERUSER));
        em.persist(new UserAccount("jaba", Role.EDITOR));
        em.persist(new UserAccount("miha", Role.EDITOR));
        em.persist(new UserAccount("marko", Role.EDITOR));
    }

    private void fillHierarchy() {
        final List<NameCategory> sectionLevels = ImmutableList.of(new NameCategory("SUP"), new NameCategory("SECT"), new NameCategory("SUB"));
        final List<NameCategory> deviceTypeLevels = ImmutableList.of(new NameCategory("DSCP"), new NameCategory("CAT"), new NameCategory("GDEV"), new NameCategory("SDEV"));
        em.persist(new NameHierarchy(sectionLevels, deviceTypeLevels));
    }

    private void fillSections() {
        final NamePart Acc = addSection(null, "Accelerator", "Acc");
        addSection(Acc, "Ion Source", "ISrc");
        addSection(Acc, "Low Energy Beam Transport", "LEBT");
        addSection(Acc, "Radio Frequency Quadrupole", "RFQ");
        addSection(Acc, "Medium Energy Beam Transport", "MEBT");
        addSection(Acc, "Drift Tube Linac", "DTL");
        addSection(Acc, "Spoke Linac", "Spk");
        addSection(Acc, "Medium Beta Linac", "MBL");
        addSection(Acc, "High Beta", "HBL");
        addSection(Acc, "Upper High Beta", "UHB");
        addSection(Acc, "Dogleg", "DgLg");
        addSection(Acc, "Accelerator to Target", "A2T");
        addSection(Acc, "Monolith beam Line", "MnBL");
        addSection(Acc, "DumpLine", "DmpL");
        addSection(Acc, "Bent Dump Line", "HEBD");


        final NamePart TS = addSection(null, "Target Station", "TS");

        final NamePart ActC = addSection(TS, "Active Cells", "ActC");
        addSection(ActC, "Confinement", "Cn");
        addSection(ActC, "Equipment", "Eq");
        addSection(ActC, "Handling", "Hnd");

        final NamePart ActF = addSection(TS, "Active Fluids", "ActF");
        addSection(ActF, "Active gas storage", "GSg");
        addSection(ActF, "Active liquid storage", "LSg");
        addSection(ActF, "Cover gas system", "CvrG");
        addSection(ActF, "Water purification system", "WtrP");
        addSection(ActF, "Helium purification system", "HeP");

        final NamePart ActH = addSection(TS, "Active Handling", "ActH");
        addSection(ActH, "Cranes and lifting tools", "Lift");
        addSection(ActH, "Casks", "Csk");
        addSection(ActH, "Wheel handling system", "Whl");

        final NamePart ActWS = addSection(TS, "Active Workshops", "ActWS");
        addSection(ActWS, "Welding workshop", "Wld");
        addSection(ActWS, "Active laboratory", "Lab");
        addSection(ActWS, "Machines", "Mch");
        addSection(ActWS, "Mobile testing tools", "MTst");

        final NamePart HMU = addSection(TS, "Handling mock-up", "HMU");
        addSection(HMU, "Active cells mock-up geometry", "ActC");
        addSection(HMU, "Monolith mock-up geometry", "MnltG");
        addSection(HMU, "MR plug replica", "MRRp");
        addSection(HMU, "Neutron beam guide insert replica", "NBGRp");
        addSection(HMU, "PBW plug replica", "PBWRp");
        addSection(HMU, "Target replica", "TgtRp");

        final NamePart Inm = addSection(TS, "Intermediate cooling Systems", "Inm");
        addSection(Inm, "Low temperature adsorber refrigeration system", "C4Ads");
        addSection(Inm, "Cryogenic moderator refrigeration system", "C4CryM");
        addSection(Inm, "Intermediate cooling system for gaseous systems", "C4G");
        addSection(Inm, "Intermediate cooling system for liquid systems", "C4L");
        addSection(Inm, "Intermediate target cooling system", "C4Tgt");

        final NamePart ActVM = addSection(TS, "Mobile Active Vacuum", "ActVM");
        addSection(ActVM, "Mobile vacuum pumps", "Pmp");
        addSection(ActVM, "Mobile vacuum cold traps", "Trp");

        final NamePart MR = addSection(TS, "Moderator-Reflector", "MR");
        addSection(MR, "Water moderator cooling system", "MCol");
        addSection(MR, "Reflector cooling system A", "RColA");
        addSection(MR, "Reflector cooling system B", "RColB");
        addSection(MR, "Cryogenic moderator system", "CryM");
        addSection(MR, "MR plug", "Plg");

        final NamePart Mnlt = addSection(TS, "Monolith", "Mnlt");
        addSection(Mnlt, "Shielding cooling system", "ShCol");
        addSection(Mnlt, "Monolith atmosphere system", "Atm");
        addSection(Mnlt, "Bulk shielding", "BlkSh");
        addSection(Mnlt, "Irradiation plugs", "IrPlg");
        addSection(Mnlt, "Liner", "Lin");
        addSection(Mnlt, "Removable shielding", "RvSh");
        addSection(Mnlt, "Shutter systems", "Shut");
        addSection(Mnlt, "Neutron beam ports (NBP)", "NBP");
        addSection(Mnlt, "Proton beam diagnostics plug", "PBDPlg");
        addSection(Mnlt, "Proton beam diagnostics skeleton", "PBDS");
        addSection(Mnlt, "Internal supporting structure", "SpStr");
        addSection(Mnlt, "Target diagnostics plug", "TDPlg");
        addSection(Mnlt, "Target diagnostics skeleton", "TDS");

        final NamePart PBW = addSection(TS, "Proton Beam Window", "PBW");
        addSection(PBW, "PBW cooling system", "Col");
        addSection(PBW, "PBW plug", "Plg");

        final NamePart RGEC = addSection(TS, "Radioactive Gaseous Effluent Confinement", "RGEC");
        addSection(RGEC, "Active cells branch", "ActC");
        addSection(RGEC, "Active workshops branch", "ActWS");
        addSection(RGEC, "Basement branch", "Bsm");
        addSection(RGEC, "Connection cell branch", "CncC");
        addSection(RGEC, "High Bay Branch", "HBB");
        addSection(RGEC, "Internal neutron guide extraction zone branch", "NG");
        addSection(RGEC, "Tritiated zone branch", "Trt");
        addSection(RGEC, "Transfer zone branch", "Trn");
        addSection(RGEC, "Room cooling for active areas", "ActCol");

        final NamePart TSh = addSection(TS, "Shielding", "TSh");
        addSection(TSh, "Shielding around beam extraction in exp hall", "Bex");
        addSection(TSh, "Shielding for Development beam dump systems", "Dmp");
        addSection(TSh, "Neutron beam catcher system", "NBC");
        addSection(TSh, "Proton beam line shielding", "PBL");
        addSection(TSh, "Shielding above upper cells", "UpC");

        final NamePart Tgt = addSection(TS, "Target", "Tgt");
        addSection(Tgt, "Primary target cooling system", "Col");
        addSection(Tgt, "Target unit (drive)", "Drv");
        addSection(Tgt, "Target unit (wheel)", "Whl");

        final NamePart TSS = addSection(TS, "Target Safety System", "TSS");
        addSection(TSS, "Active monitoring", "ActM");
        addSection(TSS, "Confinement barriers control system", "CnfB");
        addSection(TSS, "Proton beam characteristic monitoring", "PBM");
        addSection(TSS, "Target Wheel monitoring system", "TWM");

        final NamePart TSg = addSection(TS, "TS Storage", "TSg");
        addSection(TSg, "Storage of contaminated components", "ActSg");
        addSection(TSg, "Storage of new components", "NwSg");

        final NamePart TDmp = addSection(TS, "Tune-up dump", "TDmp");
        addSection(TDmp, "Beam dump control system", "Ctrl");
        addSection(TDmp, "Proton beam dump handling system", "Hnd");
        addSection(TDmp, "Charge monitor", "CgM");
        addSection(TDmp, "Vacuum sealing systems", "Vac");
        addSection(TDmp, "Inner inserts", "InIs");
        addSection(TDmp, "Outer inserts", "OtIs");
        addSection(TDmp, "Shutter system", "Shut");
        addSection(TDmp, "Enclosure tank", "ETnk");


        final NamePart CS = addSection(null, "Central Services", "CS");

        final NamePart WCP = addSection(CS, "Water cooling plant", "WCP");
        addSection(WCP, "Central Utilities Water Station", "CUWS");
        addSection(WCP, "Back Up Water Station", "BUWS");
        addSection(WCP, "Low Beta Water Station", "LBWS");
        addSection(WCP, "Medium Beta Water Station", "MBWS");
        addSection(WCP, "HEBT Etcetera Water station", "HEWS");
        addSection(WCP, "Target Water Station", "TWS");

        final NamePart CP = addSection(CS, "Cryo Plant", "CP");
        addSection(CP, "Linac Cryoline Gallery", "LCG");
    }

    private void fillDeviceTypes() {
        final NamePart BMD = addDeviceType(null, "Beam Magnets and Deflectors", "BMD");

        final NamePart Dipoles = addDeviceType(BMD, "Dipoles", null);
        addDeviceType(Dipoles, "Chopper", "Chop");
        addDeviceType(Dipoles, "Horizontal Corrector", "CorH");
        addDeviceType(Dipoles, "Horizontal Dipole", "DH");
        addDeviceType(Dipoles, "Horizontal Raster Magnet", "RstH");
        addDeviceType(Dipoles, "Vertical Corrector", "CorV");
        addDeviceType(Dipoles, "Vertical Dipole", "DV");
        addDeviceType(Dipoles, "Vertical Raster Magnet", "RstV");

        final NamePart Misc = addDeviceType(BMD, "Misc", null);
        addDeviceType(Misc, "Linac Warm Unit", "LWU");
        addDeviceType(Misc, "Proton Beam Window", "PBW");

        final NamePart Octupoles = addDeviceType(BMD, "Octupoles", null);
        addDeviceType(Octupoles, "Octupole, folding in horizontal plane", "OctH");
        addDeviceType(Octupoles, "Octupole, folding in vertical plane", "OctV");

        final NamePart Quadrupoles = addDeviceType(BMD, "Quadrupoles", null);
        addDeviceType(Quadrupoles, "Horizontal Focusing Quadrupole", "QH");
        addDeviceType(Quadrupoles, "Vertical Focusing Quadrupole", "QV");

        final NamePart Solenoids = addDeviceType(BMD, "Solenoids", null);
        addDeviceType(Solenoids, "Solenoid", "Sol");

        final NamePart Steerers = addDeviceType(BMD, "Steerers", null);
        addDeviceType(Steerers, "Steerer", "Str");


        final NamePart Cryo = addDeviceType(null, "Cryogenics", "Cryo");

        final NamePart CryogenicBoxes = addDeviceType(Cryo, "Cryogenic Boxes", null);
        addDeviceType(CryogenicBoxes, "End Box", "EBox");
        addDeviceType(CryogenicBoxes, "Splitting Box", "SBox");
        addDeviceType(CryogenicBoxes, "Valve Box", "VBox");

        final NamePart CryogenicLines = addDeviceType(Cryo, "Cryogenic Lines", null);
        addDeviceType(CryogenicLines, "Distribution Line", "CDL");
        addDeviceType(CryogenicLines, "Transfer Line", "CTL");

        final NamePart Indicators = addDeviceType(Cryo, "Indicators", null);
        addDeviceType(Indicators, "Flow Indicator", "CTL");
        addDeviceType(Indicators, "Level Indicator", "CTL");
        addDeviceType(Indicators, "Pressure Indicator", "CTL");
        addDeviceType(Indicators, "Temperature Indicator", "CTL");
    }

    private NamePart addSection(@Nullable NamePart parent, String longName, String shortName) {
        return namePartService.addNamePart(shortName, longName, NamePartType.SECTION, parent, "Test data").getNamePart();
    }

    private NamePart addDeviceType(@Nullable NamePart parent, String longName, String shortName) {
        return namePartService.addNamePart(shortName, longName, NamePartType.DEVICE_TYPE, parent, "Test data").getNamePart();
    }
}