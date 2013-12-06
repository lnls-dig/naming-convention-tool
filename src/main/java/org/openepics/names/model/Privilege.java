/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.names.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Entity
@Table(name = "privilege")
@XmlRootElement
@NamedQueries({ @NamedQuery(name = "Privilege.findAll", query = "SELECT p FROM Privilege p"),
		@NamedQuery(name = "Privilege.findByUsername", query = "SELECT p FROM Privilege p WHERE p.username = :username"),
		@NamedQuery(name = "Privilege.findByOperation", query = "SELECT p FROM Privilege p WHERE p.operation = :operation"),
		@NamedQuery(name = "Privilege.findByVersion", query = "SELECT p FROM Privilege p WHERE p.version = :version") })
public class Privilege implements Serializable {
	private static final long serialVersionUID = -7434982467235827373L;

	@Id
	@Basic(optional = false)
	@NotNull
	@Column(name = "id")
	private Integer id;

	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 64)
	@Column(name = "username")
	private String username;

	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 1)
	@Column(name = "operation")
	private String operation;

	@Basic(optional = false)
	@NotNull
	@Column(name = "version")
	@Version
	private int version;

	public Privilege() {
	}

	public Privilege(String username) {
		this.username = username;
	}

	public Privilege(String username, String operation, int version) {
		this.username = username;
		this.operation = operation;
		this.version = version;
	}

	public Integer getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (username != null ? username.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are
		// not set
		if (!(object instanceof Privilege)) {
			return false;
		}
		Privilege other = (Privilege) object;
		if ((this.username == null && other.username != null)
				|| (this.username != null && !this.username.equals(other.username))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "org.openepics.names.Privilege[ username=" + username + " ]";
	}

}
