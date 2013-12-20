/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2012.
 * 
 * You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *       http://www.gnu.org/licenses/gpl.txt
 * 
 * Contact Information:
 *   Facilitty for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 * 
 */
package org.openepics.names.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Entity
@Table(name = "name_category")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "NameCategory.findAll", query = "SELECT n FROM NameCategory n"),
    @NamedQuery(name = "NameCategory.findById", query = "SELECT n FROM NameCategory n WHERE n.id = :id"),
    @NamedQuery(name = "NameCategory.findByName", query = "SELECT n FROM NameCategory n WHERE n.name = :name"),
    @NamedQuery(name = "NameCategory.findByDescription", query = "SELECT n FROM NameCategory n WHERE n.description = :description") })
public class NameCategory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 32)
    @Column(name = "name")
    private String name;
	
	@Column(name="approval_needed")
	private boolean approvalNeeded;
    
    @Size(max = 255)
    @Column(name = "description")
    private String description;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "version")
    @Version
    private int version;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "nameCategory")
    private List<NameEvent> nameEventList;

    public NameCategory() {
    }

    public NameCategory(Integer id) {
        this.id = id;
    }

    public NameCategory(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getVersion() {
        return version;
    }

	/**
	 * Indicates whether name parts of this category need superuser approval.
	 * Default value is <code>true</code> meaning approval is need.
	 * If approval is not needed, editors can create such parts and they 
	 * are automatically approved.
	 * Editors can also delete name parts of this type, if they have created them.
	 * Deletion can also happen without approval.
	 * @return <code>true</code> if name part needs superuser approval, 
	 *  <code>false</code> otherwise.
	 */
	public boolean isApprovalNeeded() {
		return approvalNeeded;
	}

	public void setApprovalNeeded(boolean approvalNeeded) {
		this.approvalNeeded = approvalNeeded;
	}

    @XmlTransient
    public List<NameEvent> getNameEventList() {
        return nameEventList;
    }

    public void setNameEventList(List<NameEvent> nameEventList) {
        this.nameEventList = nameEventList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof NameCategory)) {
            return false;
        }
        NameCategory other = (NameCategory) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.openepics.names.NameCategory[ id=" + id + " ]";
    }
    
}
