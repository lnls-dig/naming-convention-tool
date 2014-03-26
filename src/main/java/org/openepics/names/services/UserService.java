package org.openepics.names.services;

import org.openepics.names.model.UserAccount;
import org.openepics.names.util.As;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class UserService {

    @PersistenceContext private EntityManager em;

    public UserAccount userWithName(String userName) {
        return em.createQuery("SELECT u FROM UserAccount u WHERE u.username = :userName", UserAccount.class).setParameter("userName", userName).getSingleResult();
    }

    public UserAccount emAttached(UserAccount user) {
        return As.notNull(em.find(UserAccount.class, user.getId()));
    }
}
