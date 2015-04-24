package org.openepics.names.services;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openepics.names.model.NamePartType;

import com.google.common.collect.ImmutableList;

import static org.junit.Assert.*;

public class EssNamingConventionTest {
    private static EssNamingConvention namingConvention;
        
    @BeforeClass
    public static void setUp() {
        namingConvention = new EssNamingConvention();
    }
    
    @Test
    public void isSubSectionNameValidTest() {
        List<String> parentPath = ImmutableList.of("Sup", "Sec");
        assertTrue("Alphabetic subsection is allowed",namingConvention.isSectionNameValid(parentPath, "Sub"));
        assertFalse("Empty sub is not allowed", namingConvention.isSectionNameValid(parentPath, "   "));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "1sub1"));
        assertFalse(namingConvention.isSectionNameValid(parentPath, "00:1"));
        assertFalse(namingConvention.isSectionNameValid(parentPath, "1234567"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "1"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "12"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "00"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "123"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "1234"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "12345"));
    }
    
    @Test
    public void isSectionNameValidTest() {
        List<String> parentPath = ImmutableList.of("Sup");
        assertTrue("Alphabetic section is allowed",namingConvention.isSectionNameValid(parentPath, "Sec"));
        assertTrue("Alphanumeric section is allowed",namingConvention.isSectionNameValid(parentPath, "Sec01"));
        assertTrue("Numeric section is allowed",namingConvention.isSectionNameValid(parentPath, "01"));
        assertFalse("Empty section is not allowed",namingConvention.isSectionNameValid(parentPath, "  "));
        assertFalse("Non-alphanumerical char are not allowed", namingConvention.isSectionNameValid(parentPath, "Sec!"));
        assertTrue(namingConvention.isSectionNameValid(ImmutableList.of("Lin", "Sec"), "cryo"));
        assertTrue(namingConvention.isSectionNameValid(ImmutableList.<String>of(), "Acc1"));
        assertTrue(namingConvention.isSectionNameValid(ImmutableList.<String>of(), "Acc"));
        assertTrue(namingConvention.isSectionNameValid(ImmutableList.of("Acc"), "Sec"));
        assertTrue(namingConvention.isSectionNameValid(ImmutableList.of("Lin", "Sec"), "Cryo"));
    }
    
    @Test
    public void sectionNameLengthTest() {
        List<String> parentPath = ImmutableList.of("Sup");        
        assertTrue(namingConvention.isSectionNameValid(parentPath, "S"));
        assertFalse(namingConvention.isSectionNameValid(parentPath, "Section"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "Se"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "Sectio"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "Sec")); 
    }
    
    @Test
    public void isDeviceTypeNameValidTest() {
        List<String> parentPath = ImmutableList.of("Dis","Cat");        

    	assertTrue("Alphanumeric Discipline is allowed", namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "Dis"));
        assertTrue("Numeric Discipline is allowed", namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "01"));
        assertFalse("Non-alphanumerical char is not allowed", namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "Dis:"));        
        assertTrue("Alphanumeric Device type is allowed", namingConvention.isDeviceTypeNameValid(parentPath,"Dev1"));
//        assertFalse("Device cannot start with number", namingConvention.isDeviceTypeNameValid(parentPath,"1Dev"));
        assertFalse("Empty names are not allowed",namingConvention.isDeviceTypeNameValid(parentPath,"  "));
    }
    
    @Test
    public void deviceTypeNameLengthTest() {
        assertFalse("Discipline name with 0 chars is not allowed",namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), ""));    	
        assertTrue("Discipline name shorter than 6 chars is allowed",namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "BMD"));
        assertTrue("Discipline name of 6 chars is allowed",namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "BMD123"));
        assertFalse("Discipline name longer than 6 chars is not allowed", namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "BMD1234"));
        assertTrue("Device category with 0 chars is allowed", namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), ""));
        assertTrue(namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "Ch"));
        assertTrue("Device category longer than 6 chars is allowed", namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "Choppers"));
        assertFalse("Device type with 0 chars is not allowed", namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD", "Choppers"), ""));
        assertTrue("One charater device type is allowed", namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD","Choppers"), "C"));
        assertTrue("Six char device type is allowed", namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD","Choppers"), "Choppe"));  
        assertFalse("Device type longer than 6 chars is not allowed", namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD", "Choppers"), "Chopper"));    	
    }
        
    @Test
    public void isInstanceIndexValidTest() {
        final List<String> sectionPath = ImmutableList.of("Sup", "Sec", "Sub");
        final List<String> deviceTypePath = ImmutableList.of("Dis", "Cat", "Dev");
        assertTrue("Idx can be null",namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, null));
        assertTrue("Numeric Idx is allowed", namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "123"));
        assertTrue("Alphabetic Idx is allowed",namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "abc"));
        assertFalse("Non-alphanumical char is not allowed", namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "a!"));
        assertTrue("One charaters is allowed", namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "1"));
        assertTrue("Six Char is allowed",namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "abcdef"));
        assertFalse("Seven Char is not allowed", namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "1234567"));
    }
    
    @Test
    public void symbolsSimilarTo1Test() {
        assertEquals(namingConvention.equivalenceClassRepresentative("1"), namingConvention.equivalenceClassRepresentative("1"));
        assertEquals(namingConvention.equivalenceClassRepresentative("1"), namingConvention.equivalenceClassRepresentative("I"));
        assertEquals(namingConvention.equivalenceClassRepresentative("1"), namingConvention.equivalenceClassRepresentative("l"));
        assertEquals(namingConvention.equivalenceClassRepresentative("1"), namingConvention.equivalenceClassRepresentative("L"));
        assertEquals(namingConvention.equivalenceClassRepresentative("1"), namingConvention.equivalenceClassRepresentative("i"));
        assertFalse(namingConvention.equivalenceClassRepresentative("1").equals(namingConvention.equivalenceClassRepresentative("b")));
    }
    
    @Test
    public void symbolsSimilarTo0Test() {
        assertEquals(namingConvention.equivalenceClassRepresentative("0"), namingConvention.equivalenceClassRepresentative("o"));
        assertEquals(namingConvention.equivalenceClassRepresentative("0"), namingConvention.equivalenceClassRepresentative("O"));
        assertEquals(namingConvention.equivalenceClassRepresentative("0"), namingConvention.equivalenceClassRepresentative("0"));
        assertFalse(namingConvention.equivalenceClassRepresentative("0").equals(namingConvention.equivalenceClassRepresentative("b")));
    }
    
    @Test
    public void symbolsSimilarToVTest() {
        assertEquals(namingConvention.equivalenceClassRepresentative("V"), namingConvention.equivalenceClassRepresentative("v"));
        assertEquals(namingConvention.equivalenceClassRepresentative("V"), namingConvention.equivalenceClassRepresentative("V"));
        assertEquals(namingConvention.equivalenceClassRepresentative("V"), namingConvention.equivalenceClassRepresentative("w"));
        assertEquals(namingConvention.equivalenceClassRepresentative("V"), namingConvention.equivalenceClassRepresentative("W"));
        assertFalse(namingConvention.equivalenceClassRepresentative("V").equals(namingConvention.equivalenceClassRepresentative("b")));
    }
    
    @Test
    public void lowerAndUpperCaseCharactersTest() {
        assertEquals(namingConvention.equivalenceClassRepresentative("tEsTS"), namingConvention.equivalenceClassRepresentative("TeSts"));
    }
    
    @Test
    public void zeroPrefixedNumberTest() {
        assertEquals(namingConvention.equivalenceClassRepresentative("zero01"), namingConvention.equivalenceClassRepresentative("zero1"));
        assertEquals(namingConvention.equivalenceClassRepresentative("ze01ro"), namingConvention.equivalenceClassRepresentative("ze1ro"));
    }
    
    @Test
    public void zeroAfterAlphaCharacterTest() {
        assertEquals(namingConvention.equivalenceClassRepresentative("Sub0001"), namingConvention.equivalenceClassRepresentative("Sub1"));
        assertEquals(namingConvention.equivalenceClassRepresentative("01Sub001"), namingConvention.equivalenceClassRepresentative("01Sub1"));
    }
    
    @Test
    public void conventionNameTest() {
        final List<String> sectionPath = ImmutableList.of("Sup", "Sec", "Sub");
        final List<String> deviceTypePath = ImmutableList.of("Dis", "Cat", "Dev");
        assertEquals(namingConvention.conventionName(sectionPath, deviceTypePath, "Idx"), "Sec-Sub:Dis-Dev-Idx");
        assertEquals(namingConvention.conventionName(sectionPath, deviceTypePath, ""),"Sec-Sub:Dis-Dev");
        assertEquals(namingConvention.conventionName(sectionPath, ImmutableList.of("Dis","Cat"), "Idx"),null);
    }
    
    @Test
    public void canFirstLevelMnemonicsCoexistTest() {
        assertTrue("Supersection and device group can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("Sec"), NamePartType.SECTION, ImmutableList.of("Sup", "Sec"), NamePartType.SECTION));
        assertTrue("Supersection and Discipline can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("Dis"), NamePartType.SECTION, ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE));
        assertFalse("Discipline and subsection cannot coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("Sub"), NamePartType.DEVICE_TYPE, ImmutableList.of("Sup", "Sec", "Sub"), NamePartType.SECTION));
    }
    
    @Test
    public void canSectionMnemonicsCoexistTest() {
        assertFalse("Section and subsection cannot coexist",namingConvention.canMnemonicsCoexist(ImmutableList.of("Sec", "Sub"), NamePartType.SECTION, ImmutableList.of("Sup", "Sec", "Sub"), NamePartType.SECTION));
        assertFalse("Discipline and section cannot coexist",namingConvention.canMnemonicsCoexist(ImmutableList.of("Sup", "Dis"), NamePartType.SECTION, ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE));
        assertTrue("Devicegroup and section can coexist",namingConvention.canMnemonicsCoexist(ImmutableList.of("Dis", "Sec"), NamePartType.DEVICE_TYPE, ImmutableList.of("Sup", "Sec"), NamePartType.SECTION));
    }
    
    @Test
    public void canDeviceTypeMnemonicsCoexistTest() {
        assertTrue("Device type and subsection can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("Acc", "HBL", "ChopG"), NamePartType.SECTION));
        assertTrue("Two device types can coexist under different disicplines", namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BML", "Chop", "ChopG"), NamePartType.DEVICE_TYPE));
        assertTrue("Device type and device group can coexist",namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BMD", "ChopG"), NamePartType.DEVICE_TYPE));
        assertTrue("Device groups can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop"), NamePartType.DEVICE_TYPE, ImmutableList.of("BMD", "Chop"), NamePartType.DEVICE_TYPE));
        assertFalse("Two device types cannot coexist under the same discipline",namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BMD", "ChopN", "ChopG"), NamePartType.DEVICE_TYPE));
        assertFalse("Two device types cannot coexist under the same discipline", namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BMD", "ChopN", "ChopG"), NamePartType.DEVICE_TYPE));
    }
}


