package org.openepics.names.services;

import org.openepics.names.model.AppInfo;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;

/**
 * A service bean managing global application settings and database initialization.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Singleton
@Startup
public class ApplicationService {

    @PersistenceContext private EntityManager em;
    @Inject private InitialDataImportService importService;

    public AppInfo appInfo() {
        return em.createQuery("SELECT a FROM AppInfo a", AppInfo.class).getSingleResult();
    }

    @PostConstruct
    private void init() throws IOException {
        if (em.createQuery("SELECT a FROM AppInfo a", AppInfo.class).getResultList().size() != 1) {
            em.persist(new AppInfo());
            importService.fillDatabaseWithInitialData();
        }
    }
}
