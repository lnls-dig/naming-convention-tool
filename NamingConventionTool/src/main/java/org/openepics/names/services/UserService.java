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
 * @author Marko Kolar  
 * @author K. Rathsman  
 */
@Stateless
public class UserService {

	@PersistenceContext private EntityManager em;

	/**
	 * @param userName The name of the user
	 * @return The UserAccount of the user with the given user name.
	 */
	public UserAccount userWithName(String userName) {
		return em.createQuery("SELECT u FROM UserAccount u WHERE u.username = :userName", UserAccount.class).setParameter("userName", userName).getSingleResult();
	}

	/**
	 * Creates new user with the given user name if not already exists.  
	 * @param userName The user name
	 * @param role The Role
	 */
	public void createUser(String userName, Role role){
		try {
			userWithName(userName);
		} catch (NoResultException e) {
			em.persist(new UserAccount(userName, role));
		}
	} 

	/**
	 *
	 * @param user the (possibly detached) UserAccount entity
	 * @return The EntityManager-attached entity corresponding to the given UserAccount entity.
	 */
	public UserAccount emAttached(UserAccount user) {
		return As.notNull(em.find(UserAccount.class, user.getId()));
	}

}
