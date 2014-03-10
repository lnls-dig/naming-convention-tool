package org.openepics.names.model;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
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

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "name_hierarchy_section_levels",
        joinColumns = { @JoinColumn(name = "name_hierarchy_id") },
        inverseJoinColumns = { @JoinColumn(name = "name_category_id") }
    )
    @OrderColumn(name = "pos")
    private List<NameCategory> sectionLevels;

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "name_hierarchy_device_type_levels",
        joinColumns = { @JoinColumn(name = "name_hierarchy_id") },
        inverseJoinColumns = { @JoinColumn(name = "name_category_id") }
    )
    @OrderColumn(name = "pos")
    private List<NameCategory> deviceTypeLevels;

    protected NameHierarchy() {}

    public NameHierarchy(List<NameCategory> sectionLevels, List<NameCategory> deviceTypeLevels) {
        this.sectionLevels = ImmutableList.copyOf(sectionLevels);
        this.deviceTypeLevels = ImmutableList.copyOf(deviceTypeLevels);
    }

    public List<NameCategory> getSectionLevels() { return sectionLevels; }
    public List<NameCategory> getDeviceTypeLevels() { return deviceTypeLevels; }

    public @Nullable NameCategory getSuperCategory(NameCategory nameCategory) {
        final int sectionIndex = sectionLevels.indexOf(nameCategory);
        if (sectionIndex == -1) {
            final int deviceTypeIndex = deviceTypeLevels.indexOf(nameCategory);
            if (deviceTypeIndex == -1) {
                throw new IllegalStateException();
            } else if (deviceTypeIndex == 0) {
                return null;
            } else {
                return deviceTypeLevels.get(deviceTypeIndex - 1);
            }
        } else if (sectionIndex == 0) {
            return null;
        } else {
            return sectionLevels.get(sectionIndex - 1);
        }
    }

    public @Nullable NameCategory getSubCategory(NameCategory nameCategory) {
        final int sectionIndex = sectionLevels.indexOf(nameCategory);
        if (sectionIndex == -1) {
            final int deviceTypeIndex = deviceTypeLevels.indexOf(nameCategory);
            if (deviceTypeIndex == -1) {
                throw new IllegalStateException();
            } else if (deviceTypeIndex == deviceTypeLevels.size() - 1) {
                return null;
            } else {
                return deviceTypeLevels.get(deviceTypeIndex + 1);
            }
        } else if (sectionIndex == sectionLevels.size() - 1) {
            return null;
        } else {
            return sectionLevels.get(sectionIndex + 1);
        }
    }
}
