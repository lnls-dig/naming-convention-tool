package org.openepics.names.services;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import static org.junit.Assert.*;

public class EssNamingConventionTest {
    private static EssNamingConvention namingConvention;
        
    @BeforeClass
    public static void setUp() {
        namingConvention = new EssNamingConvention();
    }
    
    @Test
    public void isTypeASubSectionNameValidTest() {
        List<String> parentPath = ImmutableList.of("Acc", "Sec");
        assertFalse(namingConvention.isSectionNameValid(parentPath, "SubS"));
        assertFalse(namingConvention.isSectionNameValid(parentPath, "1sub1"));
        assertFalse(namingConvention.isSectionNameValid(parentPath, "00:1"));
        assertFalse(namingConvention.isSectionNameValid(ImmutableList.of("Acc"), "001"));
        assertFalse(namingConvention.isSectionNameValid(ImmutableList.of("Acc", "Sec", "SubS"), "001"));
        assertFalse(namingConvention.isSectionNameValid(parentPath, "0012345"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "01"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "00"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "123"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "1234"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "00123"));
    }
    
    @Test
    public void isSectionNameValidTest() {
        assertFalse(namingConvention.isSectionNameValid(ImmutableList.of("Lin", "Sec"), "1Cryo"));
        assertFalse(namingConvention.isSectionNameValid(ImmutableList.of("Lin", "Sec"), "Cryo!"));
        assertTrue(namingConvention.isSectionNameValid(ImmutableList.of("Lin", "Sec"), "cryo"));
        assertTrue(namingConvention.isSectionNameValid(ImmutableList.<String>of(), "Acc1"));
        assertTrue(namingConvention.isSectionNameValid(ImmutableList.<String>of(), "Acc"));
        assertTrue(namingConvention.isSectionNameValid(ImmutableList.of("Acc"), "Sec"));
        assertTrue(namingConvention.isSectionNameValid(ImmutableList.of("Lin", "Sec"), "Cryo"));
    }
    
    @Test
    public void sectionNameLengthTest() {
        List<String> parentPath = ImmutableList.of("Acc");        
        assertFalse(namingConvention.isSectionNameValid(parentPath, "S"));
        assertFalse(namingConvention.isSectionNameValid(parentPath, "Section"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "Se"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "Sectio"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "Sec"));
       
    }
    
    @Test
    public void isDeviceTypeNameValidTest() {
        assertFalse(namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "1BMD"));
        assertFalse(namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "BMD!"));        
        assertTrue(namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "BMD"));
        assertTrue(namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "Chop1"));
    }
    
    @Test
    public void deviceTypeNameLengthTest() {
        assertFalse(namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "Chopper"));
        assertFalse(namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "C"));
        assertTrue(namingConvention.isDeviceTypeNameValid(ImmutableList.<String>of(), "BMD"));
        assertTrue(namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "Ch"));
        assertTrue(namingConvention.isDeviceTypeNameValid(ImmutableList.of("BMD"), "Choppe"));        
    }
    
    @Test
    public void isInstanceIndexOfTypeAValidTest() {
        final List<String> sectionPath = ImmutableList.of("Acc", "A2T", "01");
        final List<String> deviceTypePath = ImmutableList.of("BMD", "Chop", "Chop");
        assertFalse(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "1"));
        assertFalse(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "1a"));
        assertFalse(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "a!"));
        assertFalse(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "a0123as"));
        assertTrue(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "a0123a"));
        assertTrue(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "a"));
        assertTrue(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "a1"));
        assertTrue(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "aA1"));
    }
    
    @Test
    public void isInstanceIndexOfTypeDValidTest() {
        final List<String> sectionPath = ImmutableList.of("TS", "ActC", "Cn");
        final List<String> deviceTypePath = ImmutableList.of("BMD", "Chop", "Chop");
        assertTrue(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "1"));
        assertTrue(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "1a"));
        assertFalse(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "a!"));
        assertTrue(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "a"));
        assertTrue(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "a1"));
        assertTrue(namingConvention.isInstanceIndexValid(sectionPath, deviceTypePath, "aA1"));
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
    public void typeAConventionNameTest() {
        final List<String> sectionPath = ImmutableList.of("Acc", "A2T", "01");
        final List<String> deviceTypePath = ImmutableList.of("BMD", "Chop", "ChopG");
        assertEquals(namingConvention.conventionName(sectionPath, deviceTypePath, null), "A2T-BMD:ChopG-01");
        assertEquals(namingConvention.conventionName(sectionPath, deviceTypePath, "a01"), "A2T-BMD:ChopG-01a01");
    }
    
    @Test
    public void typeDConventionNameTest() {
        final List<String> sectionPath = ImmutableList.of("TS", "ActC", "Cn");
        final List<String> deviceTypePath = ImmutableList.of("BMD", "Chop", "ChopG");
        assertEquals(namingConvention.conventionName(sectionPath, deviceTypePath, null), "ActC-Cn:ChopG");
        assertEquals(namingConvention.conventionName(sectionPath, deviceTypePath, "001"), "ActC-Cn:ChopG-001");
    }
}


