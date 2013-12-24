package org.openepics.names.model;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
@Table(name = "name_hierarchy")
public class NameHierarchy extends Persistable {

    @OneToMany
    @JoinTable(
        name = "name_hierarchy_section_levels",
        joinColumns = { @JoinColumn(name = "name_hierarchy_id") },
        inverseJoinColumns = { @JoinColumn(name = "name_category_id") }
    )
    @OrderColumn(name = "pos")
    private List<NameCategory> sectionLevels;
    
    @OneToMany
    @JoinTable(
        name = "name_hierarchy_device_type_levels",
        joinColumns = { @JoinColumn(name = "name_hierarchy_id") },
        inverseJoinColumns = { @JoinColumn(name = "name_category_id") }
    )
    @OrderColumn(name = "pos")
    private List<NameCategory> deviceTypeLevels;

    public List<NameCategory> getSectionLevels() { return sectionLevels; }
    public List<NameCategory> getDeviceTypeLevels() { return deviceTypeLevels; }
}
