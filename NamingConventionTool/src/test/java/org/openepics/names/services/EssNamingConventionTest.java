/*-
* Copyright (c) 2014 European Spallation Source
* Copyright (c) 2014 Cosylab d.d.
*
* This file is part of Naming Service.
* Naming Service is free software: you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free
* Software Foundation, either version 2 of the License, or any newer version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
*/
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
    
    public boolean testSuperSection(String mnemonic){
    	return namingConvention.isMnemonicValid(ImmutableList.of(mnemonic), NamePartType.SECTION);
    }
    public boolean testSection(String mnemonic){
    	return namingConvention.isMnemonicValid(ImmutableList.of("",mnemonic), NamePartType.SECTION);
    }
    public boolean testSubsection(String mnemonic){
    	return namingConvention.isMnemonicValid(ImmutableList.of("","Sec",mnemonic), NamePartType.SECTION);
    }
    public boolean testDiscipline(String mnemonic){
    	return namingConvention.isMnemonicValid(ImmutableList.of(mnemonic), NamePartType.DEVICE_TYPE);
    }
    public boolean testDeviceGroup(String mnemonic){
    	return namingConvention.isMnemonicValid(ImmutableList.of("Dis",mnemonic), NamePartType.DEVICE_TYPE);
    }
    public boolean testDeviceType(String mnemonic){
    	return namingConvention.isMnemonicValid(ImmutableList.of("Dis","",mnemonic), NamePartType.DEVICE_TYPE);
    }
    
    @Test 
    public void isSuperSectionNameValidTest(){
    	assertTrue("Empty super section is allowed", testSuperSection(""));
        assertTrue("Only Zeros are allowed",testSuperSection("000"));   
    	assertTrue("Alphabetic super section is allowed",testSuperSection("Sup"));
        assertFalse("Blanks are not allowed", testSuperSection("Sup "));
        assertTrue("Numeric super section is allowed",testSuperSection("123"));
        assertTrue("AlphaNumeric super section is allowed",testSuperSection("Sup0"));
        assertFalse("Non-Alphanumerical subsection is not allowed",testSuperSection("Sup:"));
        assertTrue("Short super section is allowed", testSuperSection("1"));
        assertTrue("Long super section is allowed", testSuperSection("123456"));
        assertTrue("Very long super section is allowed",testSuperSection("123456789012345"));
    }
    
    @Test
    public void isSectionNameValidTest() {
        assertTrue("Alphabetic section is allowed",testSection("Sec"));
        assertTrue("Numeric section is allowed",testSection("123"));
        assertTrue("AlphaNumeric section is allowed",testSection("Sec0"));
        assertFalse("Empty section is not allowed",testSection(""));
        assertTrue("Only Zeros are allowed",testSection("000"));   
        assertFalse("Blanks are not allowed", testSection("Sec "));
        assertFalse("Non-Alphanumerical section is not allowed",testSection("Sec:"));
        assertTrue("Short section is allowed", testSection("1"));
        assertTrue("Long section is allowed", testSection("123456"));
        assertFalse("Very long section is not allowed",testSection("1234567"));
         }
    
    @Test
    public void isSubSectionNameValidTest() {
        assertTrue("Alphabetic subsection is allowed",testSubsection("Sub"));
        assertTrue("Numeric subsection is allowed",testSubsection("123"));
        assertTrue("AlphaNumeric subsection is allowed",testSubsection("Sub0"));
        assertFalse("Empty subsection is not allowed",testSubsection(""));
        assertTrue("Only Zeros are allowed",testSubsection("000"));   
        assertFalse("Blanks are not allowed", testSubsection("Sub "));
        assertFalse("Non-Alphanumerical subsection is not allowed",testSubsection("Sub:"));
        assertTrue("Short subsection is allowed", testSubsection("1"));
        assertTrue("Long subsection is allowed", testSubsection("123456"));
        assertFalse("Very long subsection is not allowed",testSubsection("1234567"));
         }

    @Test
    public void isDisciplineNameValidTest() {
        assertTrue("Discipline: Alphabetic mnemonic is allowed",testDiscipline("Dis"));
        assertTrue("Discipline: Numeric mnemonic is allowed",testDiscipline("123"));
        assertTrue("Discipline: AlphaNumeric mnemonic is allowed",testDiscipline("Dis0"));
        assertFalse("Discipline: Empty mnemonic is not allowed",testDiscipline(""));
        assertTrue("Discipline: Only Zeros are allowed",testDiscipline("000"));   
        assertFalse("Discipline: Blanks are not allowed", testDiscipline("Dis "));
        assertFalse("Discipline: Non-Alphanumerical mnemonic is not allowed",testDiscipline("Dis:"));
        assertTrue("Discipline: Short mnemonic is allowed", testDiscipline("1"));
        assertTrue("Discipline: Long mnemonic is allowed", testDiscipline("123456"));
        assertFalse("Discipline: Very long mnemonic is not allowed",testDiscipline("1234567"));
         }

    @Test
    public void isDeviceGroupNameValidTest() {
        assertTrue("DeviceGroup: Alphabetic mnemonic is allowed",testDeviceGroup("Grp"));
        assertTrue("DeviceGroup: Numeric mnemonic is allowed",testDeviceGroup("123"));
        assertTrue("DeviceGroup: AlphaNumeric mnemonic is allowed",testDeviceGroup("Grp0"));
        assertTrue("DeviceGroup: Empty mnemonic is allowed",testDeviceGroup(""));
        assertTrue("DeviceGroup: Only Zeros are allowed",testDeviceGroup("000"));   
        assertFalse("DeviceGroup: Blanks are not allowed", testDeviceGroup("Grp "));
        assertFalse("DeviceGroup: Non-Alphanumerical mnemonic is not allowed",testDeviceGroup("Grp:"));
        assertTrue("DeviceGroup: Short mnemonic is allowed", testDeviceGroup("1"));
        assertTrue("DeviceGroup: Long mnemonic is allowed", testDeviceGroup("123456"));
        assertTrue("DeviceGroup: Very long mnemonic is allowed",testDeviceGroup("123456789012345"));
         }

    @Test
    public void isDeviceTypeNameValidTest() {
        assertTrue("DeviceType: Alphabetic mnemonic is allowed",testDeviceType("Dev"));
        assertTrue("DeviceType: Numeric mnemonic is allowed",testDeviceType("123"));
        assertTrue("DeviceType: AlphaNumeric mnemonic is allowed",testDeviceType("Dev0"));
        assertFalse("DeviceType: Empty mnemonic is not allowed",testDeviceType(""));
        assertTrue("DeviceType: Only Zeros are allowed",testDeviceType("000"));   
        assertFalse("DeviceType: Blanks are not allowed", testDeviceType("Dev "));
        assertFalse("DeviceType: Non-Alphanumerical mnemonic is not allowed",testDeviceType("Dev:"));
        assertTrue("DeviceType: Short mnemonic is allowed", testDeviceType("1"));
        assertTrue("DeviceType: Long mnemonic is allowed", testDeviceType("123456"));
        assertFalse("DeviceType: Very long mnemonic is not allowed",testDeviceType("1234567"));
    }
    
    @Test
    public void isMnemonicValidTest() {
        assertTrue("Alphabetic section is allowed",namingConvention.isMnemonicValid(ImmutableList.of("Sup", "Sec"), NamePartType.SECTION));
        assertTrue("Alphanumeric section is allowed",namingConvention.isMnemonicValid(ImmutableList.of("Sup", "Sec01"), NamePartType.SECTION));
        assertTrue("Numeric section is allowed",namingConvention.isMnemonicValid(ImmutableList.of("Sup", "01"), NamePartType.SECTION));
        assertFalse("Empty section is not allowed",namingConvention.isMnemonicValid(ImmutableList.of("Sup", "  "), NamePartType.SECTION));
        assertFalse("Non-alphanumerical char are not allowed", namingConvention.isMnemonicValid(ImmutableList.of("Sup", "Sec!"), NamePartType.SECTION));
        assertTrue(namingConvention.isMnemonicValid(ImmutableList.of("Lin", "Sec", "cryo"), NamePartType.SECTION));
        assertTrue(namingConvention.isMnemonicValid(ImmutableList.of("Acc1"), NamePartType.SECTION));
        assertTrue(namingConvention.isMnemonicValid(ImmutableList.of("Acc"), NamePartType.SECTION));
        assertTrue(namingConvention.isMnemonicValid(ImmutableList.of("Acc", "Sec"), NamePartType.SECTION));
        assertTrue(namingConvention.isMnemonicValid(ImmutableList.of("Lin", "Sec", "Cryo"), NamePartType.SECTION));
    }
    
    @Test
    public void sectionNameLengthTest() {      
        assertTrue(namingConvention.isMnemonicValid(ImmutableList.of("Sup", "S"), NamePartType.SECTION));
        assertFalse(namingConvention.isMnemonicValid(ImmutableList.of("Sup", "Section"), NamePartType.SECTION));
        assertTrue(namingConvention.isMnemonicValid(ImmutableList.of("Sup", "Se"), NamePartType.SECTION));
        assertTrue(namingConvention.isMnemonicValid(ImmutableList.of("Sup", "Sectio"), NamePartType.SECTION));
        assertTrue(namingConvention.isMnemonicValid(ImmutableList.of("Sup", "Sec"), NamePartType.SECTION));
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

        assertEquals(namingConvention.equivalenceClassRepresentative("V1"), namingConvention.equivalenceClassRepresentative("w1"));
        assertEquals(namingConvention.equivalenceClassRepresentative("V1"), namingConvention.equivalenceClassRepresentative("W1"));
        assertFalse(namingConvention.equivalenceClassRepresentative("V").equals(namingConvention.equivalenceClassRepresentative("w")));
        assertFalse(namingConvention.equivalenceClassRepresentative("V").equals(namingConvention.equivalenceClassRepresentative("W")));
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
    public void canDisciplineCoexistIfEqualTest(){
    	assertFalse("Discipline and Discipline cannot coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE, ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE));
    	assertTrue("Discipline and Device group can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE, ImmutableList.of("Dis", "Dis"), NamePartType.DEVICE_TYPE));
    	assertFalse("Discipline and Device type cannot coexist in a parent child relation", namingConvention.canMnemonicsCoexist(ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE, ImmutableList.of("Dis","","Dis"),NamePartType.DEVICE_TYPE));
    	assertFalse("Discipline and Device type cannot coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE, ImmutableList.of("Other","","Dis"),NamePartType.DEVICE_TYPE));
        assertTrue("Discipline and Super section can coexist",namingConvention.canMnemonicsCoexist(ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE, ImmutableList.of("Dis"), NamePartType.SECTION));
        assertFalse("Discipline and Section cannot coexist",namingConvention.canMnemonicsCoexist( ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE, ImmutableList.of("", "Dis"), NamePartType.SECTION));
        assertFalse("Discipline and Subsection cannot coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE, ImmutableList.of("","Sec","Dis"),NamePartType.SECTION)); 
    }

    @Test 
    public void canDisciplineCoexistIfSimilarTest(){
    	assertFalse("Discipline and Discipline cannot coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("D1s"), NamePartType.DEVICE_TYPE, ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE));
    	assertTrue("Discipline and Device group can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("D1s"), NamePartType.DEVICE_TYPE, ImmutableList.of("Dis", "Dis"), NamePartType.DEVICE_TYPE));
    	assertFalse("Discipline and Device type cannot coexist in a parent child relation", namingConvention.canMnemonicsCoexist(ImmutableList.of("D1s"), NamePartType.DEVICE_TYPE, ImmutableList.of("D1s","","Dis"),NamePartType.DEVICE_TYPE));
    	assertTrue("Discipline and Device type can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("D1s"), NamePartType.DEVICE_TYPE, ImmutableList.of("Other","","Dis"),NamePartType.DEVICE_TYPE));
        assertTrue("Discipline and Super section can coexist",namingConvention.canMnemonicsCoexist(ImmutableList.of("D1s"), NamePartType.DEVICE_TYPE, ImmutableList.of("Dis"), NamePartType.SECTION));
        assertFalse("Discipline and Section cannot coexist",namingConvention.canMnemonicsCoexist( ImmutableList.of("D1s"), NamePartType.DEVICE_TYPE, ImmutableList.of("", "Dis"), NamePartType.SECTION));
        assertTrue("Discipline and Subsection can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("D1s"), NamePartType.DEVICE_TYPE, ImmutableList.of("","Sec","Dis"),NamePartType.SECTION)); 
    }

    @Test
    public void canFirstLevelCoexistIfEqualTest() {
        assertTrue("Supersection and section can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("Sec"), NamePartType.SECTION, ImmutableList.of("Sup", "Sec"), NamePartType.SECTION));
    }
    
    @Test
    public void canSectionsCoexistTest() {
        assertFalse("Section and subsection cannot coexist in a parent child relation",namingConvention.canMnemonicsCoexist(ImmutableList.of("","Sec"), NamePartType.SECTION, ImmutableList.of("", "Sec", "Sec"), NamePartType.SECTION));
        assertFalse("Section and subsection cannot coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("","Sec"), NamePartType.SECTION, ImmutableList.of("", "Other", "Sec"), NamePartType.SECTION));
        assertTrue("Devicegroup and section can coexist",namingConvention.canMnemonicsCoexist(ImmutableList.of("Dis", "Sec"), NamePartType.DEVICE_TYPE, ImmutableList.of("Sup", "Sec"), NamePartType.SECTION));
    }
    
    @Test
    public void canDeviceTypesCoexistTest() {
        assertTrue("Device type and subsection can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("Acc", "HBL", "ChopG"), NamePartType.SECTION));
        assertTrue("Two device types can coexist under different disicplines", namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BML", "", "ChopG"), NamePartType.DEVICE_TYPE));
        assertTrue("Device type and device group can coexist",namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BMD", "ChopG"), NamePartType.DEVICE_TYPE));
        assertTrue("Device groups can coexist", namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop"), NamePartType.DEVICE_TYPE, ImmutableList.of("BMD", "Chop"), NamePartType.DEVICE_TYPE));
        assertFalse("Two device types cannot coexist under the same discipline", namingConvention.canMnemonicsCoexist(ImmutableList.of("BMD", "Chop", "ChopG"), NamePartType.DEVICE_TYPE, ImmutableList.of("BMD", "", "ChopG"), NamePartType.DEVICE_TYPE));
    }
    
//    @Test
//    public void namePartTypeNameTest(){
//    	assertEquals("Discipline","Discipline", namingConvention.getNamePartTypeName(ImmutableList.of("Dis"), NamePartType.DEVICE_TYPE));
//    	assertEquals("Device Group","Device Group", namingConvention.getNamePartTypeName(ImmutableList.of("Dis",""), NamePartType.DEVICE_TYPE));
//    	assertEquals("Device Type","Device Type", namingConvention.getNamePartTypeName(ImmutableList.of("Dis","","Dev"), NamePartType.DEVICE_TYPE));
//    	assertEquals("Super Section","Super Section", namingConvention.getNamePartTypeName(ImmutableList.of(""), NamePartType.SECTION));
//    	assertEquals("Section","Section", namingConvention.getNamePartTypeName(ImmutableList.of("","Sec"), NamePartType.SECTION));
//    	assertEquals("Subsection","Subsection", namingConvention.getNamePartTypeName(ImmutableList.of("","Sec","Sub"), NamePartType.SECTION));
//    }
//    
//    @Test
//    public void namePartTypeMnemonicTest(){
//    	assertEquals("Dis","Dis", namingConvention.getNamePartTypeMnemonic(ImmutableList.of(""), NamePartType.DEVICE_TYPE));
//    	assertEquals("Grp","", namingConvention.getNamePartTypeMnemonic(ImmutableList.of("",""), NamePartType.DEVICE_TYPE));
//    	assertEquals("Dev","Dev", namingConvention.getNamePartTypeMnemonic(ImmutableList.of("","",""), NamePartType.DEVICE_TYPE));
//    	assertEquals("Sup","", namingConvention.getNamePartTypeMnemonic(ImmutableList.of(""), NamePartType.SECTION));
//    	assertEquals("Sec","Sec", namingConvention.getNamePartTypeMnemonic(ImmutableList.of("",""), NamePartType.SECTION));
//    	assertEquals("Sub","Sub", namingConvention.getNamePartTypeMnemonic(ImmutableList.of("","",""), NamePartType.SECTION));
//    }
}


