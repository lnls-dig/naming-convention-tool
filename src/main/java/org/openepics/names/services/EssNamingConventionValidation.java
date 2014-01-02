package org.openepics.names.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.NameEventStatus;
import org.openepics.names.ui.NameCategories;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class EssNamingConventionValidation {
    
    @PersistenceContext(unitName = "org.openepics.names.punit")
    private EntityManager em;
    
    /**
     * Checks whether the name is composed of the actual active name parts and
     * this conforms to the naming convention. This can also be called for names
     * that have not been defined yet.
     *
     * @param deviceName
     */
    public boolean isNameValid(String deviceName) {
        String[] majorParts = deviceName.split(":");

        if (majorParts.length < 2) {
            return false;
        }

        int dashIndex = majorParts[0].indexOf('-');
        // section at least one character and not all of the string
        if ((dashIndex < 1) || (dashIndex >= majorParts[0].length() - 1)) {
            return false;
        }
        String sectionName = majorParts[0].substring(0, dashIndex);
        String disciplineName = majorParts[0].substring(dashIndex + 1);

        dashIndex = majorParts[1].indexOf('-');
        if ((dashIndex < 1) || (dashIndex >= majorParts[0].length() - 1)) {
            return false;
        }
        String deviceTypeName = majorParts[1].substring(0, dashIndex);
        String deviceQntf = majorParts[1].substring(dashIndex + 1);

        // checking whether section exists, is it approved and does its category
        // equals SECT
        TypedQuery<NameEvent> sectionQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
        sectionQ.setParameter("name", sectionName);
        NameEvent section = sectionQ.getSingleResult();
        if ((section.getStatus() != NameEventStatus.APPROVED) || !section.getNameCategory().getName().equals(NameCategories.section())) {
            return false;
        }

        // checking whether discipline exists, is it approved and does its
        // category equals DSCP
        TypedQuery<NameEvent> disciplineQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
        disciplineQ.setParameter("name", disciplineName);
        NameEvent discipline = disciplineQ.getSingleResult();
        if (discipline.getStatus() != NameEventStatus.APPROVED) {
            return false;
        } else {
            if (discipline.getNameCategory().getName().equals(NameCategories.discipline())) {
                if (!isDeviceInstanceIndexValid(discipline, deviceQntf)) return false;
            } else if (!discipline.getNameCategory().getName().equals(NameCategories.subsection())) {
                return false;
            }
        }

        // checking whether device exists, is it approved and does its
        // category equals GDEV
        TypedQuery<NameEvent> deviceQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
        deviceQ.setParameter("name", deviceTypeName);
        NameEvent genDevice = deviceQ.getSingleResult();
        if ((genDevice.getStatus() != NameEventStatus.APPROVED) || !genDevice.getNameCategory().getName().equals(NameCategories.genericDevice())) {
            return false;
        }

        return true;
    }
    
    public boolean isNamePartValid(NameEvent namePart) {
        return namePart.getStatus() == NameEventStatus.APPROVED;
    }

    public boolean isNamePartValid(String namePart, NameCategory category) {
        try {
            TypedQuery<NameEvent> query = em.createNamedQuery("NameEvent.findByName", NameEvent.class).setParameter("name", namePart);
            NameEvent nameEvent = query.getSingleResult();
            return (nameEvent.getStatus() == NameEventStatus.APPROVED) && (nameEvent.getNameCategory().equals(category));
        } catch (NoResultException e) {
	    // if the names does not exist, check whether similar names exist
            // according to business logic

            // but first check whether the static rules apply
            // BLED-NAM-032
            for (int i = 0; i < namePart.length(); i++) {
                char c = namePart.charAt(i);
                if (!(c >= '0' && c <= '9') && !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')) {
                    return false;
                }
            }

            // BLED-NAM-034
            if (category.getName().equals(NameCategories.section()) || category.getName().equals(NameCategories.discipline()) || category.getName().equals(NameCategories.genericDevice()) || category.getName().equals(NameCategories.specificDevice())) {
                char c = namePart.charAt(0);
                if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')) {
                    return false;
                }
            }

            // now determine the categories for which the similarity must be
            // checked
            TypedQuery<NameCategory> catQuery = em.createNamedQuery("NameCategory.findByName", NameCategory.class);

            List<NameCategory> categories = new ArrayList<>();
            if (category.getName().equals(NameCategories.section())
                    || category.getName().equals(NameCategories.discipline())
                    || category.getName().equals(NameCategories.specificDevice())) {
                catQuery.setParameter("name", NameCategories.section());
                categories.add(catQuery.getSingleResult());
                catQuery.setParameter("name", NameCategories.discipline());
                categories.add(catQuery.getSingleResult());
                catQuery.setParameter("name", NameCategories.specificDevice());
                categories.add(catQuery.getSingleResult());
            } else if (category.getName().equals(NameCategories.subsection())
                    || category.getName().equals(NameCategories.genericDevice())) {
                catQuery.setParameter("name", NameCategories.section());
                categories.add(catQuery.getSingleResult());
                catQuery.setParameter("name", NameCategories.discipline());
                categories.add(catQuery.getSingleResult());
            } else {
                categories.add(category);
            }

            // build the list of similar names
            List<String> alts = generateNameAlternatives(namePart);
            if (alts == null) {
                return false;
            }
            TypedQuery<NameEvent> similarQuery = em.createQuery(
                    "SELECT n FROM NameEvent n WHERE UPPER(n.name) IN :alternatives AND n.nameCategory IN :nameCategories",
                    NameEvent.class);
            similarQuery.setParameter("alternatives", alts).setParameter("nameCategories", categories);
            List<NameEvent> similarNames = similarQuery.getResultList();
            return !(similarNames.size() > 0);
        } catch (NonUniqueResultException e) {
            return false;
        }

    }

    /**
     * Generates name alternatives according to ESS business logic all in UPPER
     * CASE.
     *
     * @param name - the name to generate alternatives for
     * @return
     */
    private List<String> generateNameAlternatives(String name) {
        List<String> results = new ArrayList<>();
        if (name == null || name.isEmpty()) {
            return null;
        }

        String upName = name.toUpperCase();

        boolean followZero = false;
        char c = upName.charAt(0);
        if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z'))) {
            return null;
        }
        addAlternatives(results, "", upName.charAt(0));

        for (int i = 1; i < upName.length(); i++) {
            List<String> newResults = new ArrayList<>();
            c = upName.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z'))) {
                return null;
            }
            followZero = (c >= 'A' && c <= 'Z') || (followZero && c == '0');
            for (String prefix : results) {
                addAlternatives(newResults, prefix, c);
                if (followZero && c == '0') {
                    newResults.add(prefix);
                }
            }
            results = newResults;
        }

        return results;
    }

    private void addAlternatives(List<String> prefixes, String prefix, char c) {
        switch (c) {
            case '0':
            case 'O':
                prefixes.add(prefix + 'O');
                prefixes.add(prefix + '0');
                break;
            case 'V':
            case 'W':
                prefixes.add(prefix + 'V');
                prefixes.add(prefix + 'W');
                break;
            case 'I':
            case '1':
            case 'L':
                prefixes.add(prefix + 'I');
                prefixes.add(prefix + '1');
                prefixes.add(prefix + 'L');
                break;
            default:
                prefixes.add(prefix + c);
                break;
        }
    }
    
    private boolean isDeviceInstanceIndexValid(NameEvent subsection, String deviceInstanceIndex) {
        if (!subsection.getName().substring(0, 2).equals(deviceInstanceIndex.substring(0, 2))) {
            return false;
        } else {
            return Pattern.matches("[A-Za-z]\\d{0,4}", deviceInstanceIndex.substring(2));
        }
    }
}
