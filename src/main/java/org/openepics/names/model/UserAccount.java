package org.openepics.names.model;

import com.google.common.base.Preconditions;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;

/**
 * An entity representing a user account used to sing in to the application.
 *
 * @author Vasu V <vuppala@frib.msu.org>
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
public class UserAccount extends Persistable {

    private String username;

    @Enumerated(EnumType.STRING) private Role role;

    protected UserAccount() {}

    /**
     * @param username the name identifying the user
     * @param role the role that determines the user's access control permissions
     */
    public UserAccount(String username, Role role) {
        Preconditions.checkArgument(username != null && !username.isEmpty());
        Preconditions.checkNotNull(role);
        this.username = username;
        this.role = role;
    }

    /**
     * The name identifying the user
     */
    public String getUsername() { return username; }

    /**
     * The role that determines the user's access control permissions
     */
    public Role getRole() { return role; }

    @Override public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override public boolean equals(Object other) {
        return other instanceof UserAccount && ((UserAccount) other).getUsername().equals(getUsername());
    }
}
