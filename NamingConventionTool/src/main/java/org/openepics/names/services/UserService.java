package org.openepics.names.services;

import java.util.logging.Logger;

import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;
import org.openepics.names.util.As;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
	 * The UserAccount of the user with given name. Updates the db if username does not exist. 
	 * @param username
	 * @param role
	 */
	public UserAccount updatedUserWithName(String username, Role role) {
		try{
			return em.createQuery("SELECT u FROM UserAccount u WHERE u.username = :username", UserAccount.class).setParameter("username", username).getSingleResult();
		} catch (NoResultException e){
			em.persist(new UserAccount(username, role));
			return em.createQuery("SELECT u FROM UserAccount u WHERE u.username = :username", UserAccount.class).setParameter("username", username).getSingleResult();
		} 
	}

	/**
	 * The EntityManager-attached entity corresponding to the given UserAccount entity.
	 *
	 * @param user the (possibly detached) UserAccount entity
	 */
	public UserAccount emAttached(UserAccount user) {
		return As.notNull(em.find(UserAccount.class, user.getId()));
	}

}
