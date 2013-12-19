package org.openepics.names.services;

import org.openepics.names.services.NamingConventionEJB;
import org.openepics.names.services.NamingConventionException;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.openepics.names.services.NamesEJB;
import org.openepics.names.model.NameCategory;

public class NamingConventionEJBTest {
	
	@EJB
	private NamesEJB namesEJB;
	@PersistenceContext(unitName = "org.openepics.names.punit")
	private EntityManager em;
	
	private NamingConventionEJB ncEJB = new NamingConventionEJB();
	
	@Test
	public void testCreateNcName() {
		fail("Not yet implemented");
	}

	@Test
	public void testNameValidityVerification() {
		fail("Not yet implemented");
	}
	
	//Test Plan 3.6.1
	@Test
	public void testAlphanumericParts() {
		try {
			String namePart1 = "Sec1";
			NameCategory category1 = em.find(NameCategory.class, 2);
			assertTrue(ncEJB.isNamePartValid(namePart1, category1));

			String namePart2 = "Dsc1";
			NameCategory category2 = em.find(NameCategory.class, 4);
			assertTrue(ncEJB.isNamePartValid(namePart2, category2));

			String namePart3 = "GDv1";
			NameCategory category3 = em.find(NameCategory.class, 6);
			assertTrue(ncEJB.isNamePartValid(namePart3, category3));

			String namePart4 = "SDv1";
			NameCategory category4 = em.find(NameCategory.class, 7);
			assertTrue(ncEJB.isNamePartValid(namePart4, category4));
		} catch(NamingConventionException e) {
			fail(e.getMessage());
		}
		
		try {
			String namePart1 = "Sec!";
			NameCategory category1 = em.find(NameCategory.class, 2);
			ncEJB.isNamePartValid(namePart1, category1);
			fail("Section convention name should be alphanumeric!");
		} catch(NamingConventionException e) {
			String message = e.getMessage().toLowerCase();
			assertTrue(message.contains("section"));
			assertTrue(message.contains("alphanumeric"));
		}
		
		try {
			String namePart1 = "Dsc!";
			NameCategory category1 = em.find(NameCategory.class, 4);
			ncEJB.isNamePartValid(namePart1, category1);
			fail("Discipline convention name should be alphanumeric!");
		} catch(NamingConventionException e) {
			String message = e.getMessage().toLowerCase();
			assertTrue(message.contains("discipline"));
			assertTrue(message.contains("alphanumeric"));
		}
		
		try {
			String namePart1 = "GDv!";
			NameCategory category1 = em.find(NameCategory.class, 6);
			ncEJB.isNamePartValid(namePart1, category1);
			fail("Generic device convention name should be alphanumeric!");
		} catch(NamingConventionException e) {
			String message = e.getMessage().toLowerCase();
			assertTrue(message.contains("generic device"));
			assertTrue(message.contains("alphanumeric"));
		}
		
		try {
			String namePart1 = "SDv!";
			NameCategory category1 = em.find(NameCategory.class, 7);
			ncEJB.isNamePartValid(namePart1, category1);
			fail("Specific device convention name should be alphanumeric!");
		} catch(NamingConventionException e) {
			String message = e.getMessage().toLowerCase();
			assertTrue(message.contains("specific device"));
			assertTrue(message.contains("alphanumeric"));
		}
	}
	
	//Test Plan 3.6.2
	@Test
	public void testTypeASubsectionNames() {
		try {
			String namePart1 = "01";
			NameCategory category1 = em.find(NameCategory.class, 4);
			assertTrue(ncEJB.isNamePartValid(namePart1, category1));
		} catch(NamingConventionException e) {
			fail(e.getMessage());
		}
		
		try {
			String namePart1 = "Sub1";
			NameCategory category1 = em.find(NameCategory.class, 4);
			ncEJB.isNamePartValid(namePart1, category1);
			fail("Specific device convention name should be numeric!");
		} catch(NamingConventionException e) {
			String message = e.getMessage().toLowerCase();
			assertTrue(message.contains("specific device"));
			assertTrue(message.contains("numeric"));
		}
	}
	
	//Test Plan 3.6.3
	@Test
	public void testFirstCharacters() {
		try {
			String namePart1 = "Sec1";
			NameCategory category1 = em.find(NameCategory.class, 2);
			assertTrue(ncEJB.isNamePartValid(namePart1, category1));

			String namePart2 = "Dsc1";
			NameCategory category2 = em.find(NameCategory.class, 4);
			assertTrue(ncEJB.isNamePartValid(namePart2, category2));

			String namePart3 = "GDv1";
			NameCategory category3 = em.find(NameCategory.class, 6);
			assertTrue(ncEJB.isNamePartValid(namePart3, category3));

			String namePart4 = "SDv1";
			NameCategory category4 = em.find(NameCategory.class, 7);
			assertTrue(ncEJB.isNamePartValid(namePart4, category4));
		} catch(NamingConventionException e) {
			fail(e.getMessage());
		}
		
		try {
			String namePart1 = "1Sec";
			NameCategory category1 = em.find(NameCategory.class, 2);
			ncEJB.isNamePartValid(namePart1, category1);
			fail("Section convention name's first character should be alphabetic!");
		} catch(NamingConventionException e) {
			String message = e.getMessage().toLowerCase();
			assertTrue(message.contains("section"));
			assertTrue(message.contains("alphabetic"));
		}
		
		try {
			String namePart1 = "1Dsc";
			NameCategory category1 = em.find(NameCategory.class, 4);
			ncEJB.isNamePartValid(namePart1, category1);
			fail("Discipline convention name's first character should be alphabetic!");
		} catch(NamingConventionException e) {
			String message = e.getMessage().toLowerCase();
			assertTrue(message.contains("discipline"));
			assertTrue(message.contains("alphabetic"));
		}
		
		try {
			String namePart1 = "1GDv";
			NameCategory category1 = em.find(NameCategory.class, 6);
			ncEJB.isNamePartValid(namePart1, category1);
			fail("Generic device convention name's first character should be alphabetic!");
		} catch(NamingConventionException e) {
			String message = e.getMessage().toLowerCase();
			assertTrue(message.contains("generic device"));
			assertTrue(message.contains("alphabetic"));
		}
		
		try {
			String namePart1 = "1SDv";
			NameCategory category1 = em.find(NameCategory.class, 7);
			ncEJB.isNamePartValid(namePart1, category1);
			fail("Specific device convention name's first character should be alphabetic!");
		} catch(NamingConventionException e) {
			String message = e.getMessage().toLowerCase();
			assertTrue(message.contains("specific device"));
			assertTrue(message.contains("alphabetic"));
		}
	}
	
	//Test Plan 3.6.4 - 3.6.6
	@Test
	public void testSimilarCharacters() {
	}
	
	//Test Plan 3.6.7
	@Test
	public void testLeadingZeros() {
	}
	
	//Test Plan 3.6.8
	@Test
	public void testFullNames() {
		try {
			//TODO is this correct?
			String name1 = "Sec1-Sub1:Dsc1-GDv1";
			assertTrue(ncEJB.isNameValid(name1));
		} catch(NamingConventionException e) {
			fail(e.getMessage());
		}
		
		try {
			String name1 = "invalid1234";
			assertTrue(ncEJB.isNameValid(name1));
			fail("Name "+name1+" should be invalid!");
		} catch(NamingConventionException e) {
			//TODO what check here?
		}
	}
}
