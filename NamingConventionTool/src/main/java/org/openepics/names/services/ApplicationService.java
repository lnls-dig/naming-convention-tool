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

import org.openepics.names.model.AppInfo;
import org.openepics.names.model.NamePartRevision;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * A service bean managing global application settings and database initialization.
 *
 * @author Marko Kolar 
 */
@Singleton
@Startup
public class ApplicationService {

    @PersistenceContext private EntityManager em;
    @Inject private InitialDataImportService importService;
    @Inject private NamePartService namePartService;
    @Inject private NamingConvention namingConvention;

    /**
     * @return The singleton entity representing the installed Naming Tool application and its configuration.
     */
    public AppInfo appInfo() {
        return em.createQuery("SELECT a FROM AppInfo a", AppInfo.class).getSingleResult();
    }

    /**
     * Initializes the database with the bundled initial data on the first run of the application.
     */
    @PostConstruct
    private void init() {
        final List<AppInfo> appInfo = em.createQuery("SELECT a FROM AppInfo a", AppInfo.class).getResultList();
        if (appInfo.size() != 1) {
            em.persist(new AppInfo());
            importService.fillDatabaseWithInitialData();
        } else if (appInfo.get(0).getSchemaVersion() == 0) {
            calculateMnemonicEquvalenceClassForRevisions();
            final AppInfo info = appInfo.get(0);
            info.incrementSchemaVersion();
            em.persist(info);
        }
    }
    
    private void calculateMnemonicEquvalenceClassForRevisions() {
        for (NamePartRevision revision : namePartService.allNamePartRevisions()) {
            revision.setMnemonicEqClass(namingConvention.equivalenceClassRepresentative(revision.getMnemonic()));
            em.persist(revision);
        }
    }
}
