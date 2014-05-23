package org.openepics.names.services;

import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.openepics.names.model.NamePartRevision;

import com.google.common.collect.Lists;

/**
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 * 
 * Since new field will be added to NamePartRevision this class is here to calculate mnemonic equivalence class for all revisions 
 * that are already in the database and should be removed after this is done.
 */

//TODO Remove after first deploy!!!
@Stateless
public class UpgradeDatabaseService {
    
    @Inject private NamePartService namePartService;
    @Inject private NamingConvention namingConvention;
    @PersistenceContext private EntityManager em;
    
    public void calculateMnemonicEquvalenceClassForRevisions() {
        final ArrayList<NamePartRevision> allRevisions = Lists.newArrayList(namePartService.allNamePartRevisions());
        for (NamePartRevision revision : allRevisions) {
            revision.setMnemonicEqClass(namingConvention.equivalenceClassRepresentative(revision.getMnemonic()));
            em.persist(revision);
        }
    }
}
