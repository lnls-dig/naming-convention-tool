package org.openepics.names.services;

import org.openepics.names.model.AppInfo;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
    @Inject private UpgradeDatabaseService upgradeDatabase;

    /**
     * The singleton entity representing the installed Naming Tool application and its configuration.
     */
    public AppInfo appInfo() {
        return em.createQuery("SELECT a FROM AppInfo a", AppInfo.class).getSingleResult();
    }

    /**
     * Initializes the database with the bundled initial data on the first run of the application.
     */
    @PostConstruct
    private void init() {
        if (em.createQuery("SELECT a FROM AppInfo a", AppInfo.class).getResultList().size() != 1) {
            em.persist(new AppInfo());
            importService.fillDatabaseWithInitialData();
        } else if (em.createQuery("SELECT a FROM AppInfo a", AppInfo.class).getResultList().get(0).getVersion() == 0) {
            //TODO Remove after first deploy!!!
            
            upgradeDatabase.calculateMnemonicEquvalenceClassForRevisions();
            final AppInfo info = em.createQuery("SELECT a FROM AppInfo a", AppInfo.class).getResultList().get(0);
            info.upgradeAppVersion();
            em.persist(info);
        }
    }
}
