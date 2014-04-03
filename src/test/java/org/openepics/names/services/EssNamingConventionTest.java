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
        assertTrue(namingConvention.isSectionNameValid(parentPath, "01"));
        assertTrue(namingConvention.isSectionNameValid(parentPath, "123"));
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
        assertFalse(namingConvention.isInstanceIndexValid("1"));
        assertFalse(namingConvention.isInstanceIndexValid("1a"));
        assertFalse(namingConvention.isInstanceIndexValid("a!"));
        assertTrue(namingConvention.isInstanceIndexValid("a"));
        assertTrue(namingConvention.isInstanceIndexValid("a1"));
        assertTrue(namingConvention.isInstanceIndexValid("aA1"));
    }
    
    @Test
    public void isInstanceIndexOfTypeDValidTest() {
        assertFalse(namingConvention.isInstanceIndexValid("a!"));
        assertTrue(namingConvention.isInstanceIndexValid("a"));
        assertTrue(namingConvention.isInstanceIndexValid("a1"));
        assertTrue(namingConvention.isInstanceIndexValid("aA1"));
        assertTrue(namingConvention.isInstanceIndexValid("1"));
        assertTrue(namingConvention.isInstanceIndexValid("1a"));
    }
    
    @Test
    public void symbolsSimilarTo1Test() {
        assertEquals(namingConvention.nameNormalizedForEquivalence("1"), namingConvention.nameNormalizedForEquivalence("1"));
        assertEquals(namingConvention.nameNormalizedForEquivalence("1"), namingConvention.nameNormalizedForEquivalence("I"));
        assertEquals(namingConvention.nameNormalizedForEquivalence("1"), namingConvention.nameNormalizedForEquivalence("l"));
        assertEquals(namingConvention.nameNormalizedForEquivalence("1"), namingConvention.nameNormalizedForEquivalence("L"));
        assertEquals(namingConvention.nameNormalizedForEquivalence("1"), namingConvention.nameNormalizedForEquivalence("i"));
        assertFalse(namingConvention.nameNormalizedForEquivalence("1").equals(namingConvention.nameNormalizedForEquivalence("b")));
    }
    
    @Test
    public void symbolsSimilarTo0Test() {
        assertEquals(namingConvention.nameNormalizedForEquivalence("0"), namingConvention.nameNormalizedForEquivalence("o"));
        assertEquals(namingConvention.nameNormalizedForEquivalence("0"), namingConvention.nameNormalizedForEquivalence("O"));
        assertEquals(namingConvention.nameNormalizedForEquivalence("0"), namingConvention.nameNormalizedForEquivalence("0"));
        assertFalse(namingConvention.nameNormalizedForEquivalence("0").equals(namingConvention.nameNormalizedForEquivalence("b")));
    }
    
    @Test
    public void symbolsSimilarToVTest() {
        assertEquals(namingConvention.nameNormalizedForEquivalence("V"), namingConvention.nameNormalizedForEquivalence("v"));
        assertEquals(namingConvention.nameNormalizedForEquivalence("V"), namingConvention.nameNormalizedForEquivalence("V"));
        assertEquals(namingConvention.nameNormalizedForEquivalence("V"), namingConvention.nameNormalizedForEquivalence("w"));
        assertEquals(namingConvention.nameNormalizedForEquivalence("V"), namingConvention.nameNormalizedForEquivalence("W"));
        assertFalse(namingConvention.nameNormalizedForEquivalence("V").equals(namingConvention.nameNormalizedForEquivalence("b")));
    }
    
    @Test
    public void lowerAndUpperCaseCharactersTest() {
        assertEquals(namingConvention.nameNormalizedForEquivalence("tEsTS"), namingConvention.nameNormalizedForEquivalence("TeSts"));
    }
    
    @Test
    public void zeroPrefixedNumberTest() {
        assertEquals(namingConvention.nameNormalizedForEquivalence("zero01"), namingConvention.nameNormalizedForEquivalence("zero1"));
        assertEquals(namingConvention.nameNormalizedForEquivalence("ze01ro"), namingConvention.nameNormalizedForEquivalence("ze1ro"));
    }
}


