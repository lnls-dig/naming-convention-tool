package org.openepics.names.model;

import com.google.common.base.Preconditions;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;

/**
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Entity
public class UserAccount extends Persistable {

    private String username;

    @Enumerated(EnumType.STRING) private Role role;

    protected UserAccount() {}

    public UserAccount(String username, Role role) {
        Preconditions.checkArgument(username != null && !username.isEmpty());
        Preconditions.checkNotNull(role);
        this.username = username;
        this.role = role;
    }

    public String getUsername() { return username; }

    public Role getRole() { return role; }

    @Override public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override public boolean equals(Object other) {
        return other instanceof UserAccount && ((UserAccount) other).getUsername().equals(getUsername());
    }
}
