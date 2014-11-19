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
        assertFalse("Device cannot start with number", namingConvention.isDeviceTypeNameValid(parentPath,"1Dev"));
        assertFalse("Empty names are not allowed",namingConvention.isDeviceTypeNameValid(parentPath,"  "));
    }
    
    @Test
    public void deviceTypeNameLengthTest() {
        assertFalse(namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "Chopper"));
        assertTrue(namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "C"));
        assertTrue(namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "BMD"));
        assertTrue(namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "Ch"));
        assertTrue(namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "Choppe"));        
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
    public void typeDConventionNameTest() {
        final List<String> sectionPath = ImmutableList.of("Sup", "Sec", "Sub");
        final List<String> deviceTypePath = ImmutableList.of("Dis", "Cat", "Dev");
        assertEquals(namingConvention.conventionName(sectionPath, deviceTypePath, "Idx"), "Sec-Sub:Dis-Dev-Idx");
    }
    
    @Test
    public void canFirstLevelMnemonicsCoexistTest() {
        assertFalse(namingConvention.canMnemonicsCoexist(ImmutableList.of("Acc"), NamePartType.SECTION, ImmutableList.of("Test", "Acc"), NamePartType.SECTION));
        assertFalse(namingConvention.canMnemonicsCoexist(ImmutableList.of("Acc"), NamePartType.SECTION, ImmutableList.of("Acc"), NamePartType.DEVICE_TYPE));
        assertFalse(namingConvention.canMnemonicsCoexist(ImmutableList.of("Chop"), NamePartType.DEVICE_TYPE, ImmutableList.of("Test", "A2T", "Chop"), NamePartType.SECTION));
    }
    
    @Test
    public void canSectionMnemonicsCoexistTest() {
        assertFalse(namingConvention.canMnemonicsCoexist(ImmutableList.of("Acc", "A2T"), NamePartType.SECTION, ImmutableList.of("Test", "Acc", "A2T"), NamePartType.SECTION));
        assertFalse(namingConvention.canMnemonicsCoexist(ImmutableList.of("Acc", "A2T"), NamePartType.SECTION, ImmutableList.of("A2T"), NamePartType.DEVICE_TYPE));
        assertFalse(namingConvention.canMnemonicsCoexist(ImmutableList.of("Acc", "A2T"), NamePartType.DEVICE_TYPE, ImmutableList.of("Test", "A2T"), NamePartType.SECTION));
    }
    
    @Test
    public void canGenericDeviceTypeMnemonicsCoexistTest() {
        assertTrue(namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("Test", "Acc", "ChopG"), NamePartType.SECTION));
        assertTrue(namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BML", "Chop", "ChopG"), NamePartType.DEVICE_TYPE));
        assertTrue(namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BMD", "ChopG"), NamePartType.DEVICE_TYPE));
        assertFalse(namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BMD", "ChopN", "ChopG"), NamePartType.DEVICE_TYPE));
        assertFalse(namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BMD", "ChopN", "ChopG"), NamePartType.DEVICE_TYPE));
    }
}


