package org.openepics.names.services;

import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;
import org.openepics.names.util.As;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * A service bean managing UserAccount entities.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 * @author K. Rathsman <karin.rathsman@esss.se>
 */
@Stateless
public class UserService {

	@PersistenceContext private EntityManager em;

	/**
	 * The UserAccount of the user with the given user name.
	 *
	 * @param userName The name of the user
	 */
	public UserAccount userWithName(String userName) {
		return em.createQuery("SELECT u FROM UserAccount u WHERE u.username = :userName", UserAccount.class).setParameter("userName", userName).getSingleResult();
	}

	/**
	 * The EntityManager-attached entity corresponding to the given UserAccount entity.
	 *
	 * @param user the (possibly detached) UserAccount entity
	 */
	public UserAccount emAttached(UserAccount user) {
		return As.notNull(em.find(UserAccount.class, user.getId()));
	}

	/**
	 * add user to UserAccounts. 
	 * @param userAccount
	 */
	public void update(UserAccount userAccount) {
		if(userAccount!=null && emAttached(userAccount)==null){
			em.persist(userAccount);
		} 
	}
}
