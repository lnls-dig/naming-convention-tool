package org.openepics.names.services;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.names.model.AppInfo;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Singleton
@Startup
public class ApplicationService {

    @PersistenceContext private EntityManager em;
    @Inject private TestService testService;

    public AppInfo appInfo() {
        return em.createQuery("SELECT a FROM AppInfo a", AppInfo.class).getSingleResult();
    }

    @PostConstruct
    private void init() {
        if (em.createQuery("SELECT a FROM AppInfo a", AppInfo.class).getResultList().size() != 1) {
            em.persist(new AppInfo());
            testService.fillDatabaseWithTestData();
        }
    }
}
