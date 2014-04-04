package org.openepics.names.model;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * A revision of a Device entity representing its state at some point in time.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
public class DeviceRevision extends Persistable {

    private @ManyToOne Device device;

    private @ManyToOne UserAccount requestedBy;

    private Date requestDate;

    private boolean deleted;

    private @ManyToOne NamePartRevision section;

    private @ManyToOne NamePartRevision deviceType;

    private @Nullable String instanceIndex;

    private String conventionName;

    protected DeviceRevision() {}

    public DeviceRevision(Device device, @Nullable UserAccount requestedBy, Date requestDate, boolean deleted, NamePartRevision section, NamePartRevision deviceType, @Nullable String instanceIndex, String conventionName) {
        Preconditions.checkNotNull(device);
        Preconditions.checkNotNull(requestDate);
        Preconditions.checkNotNull(section);
        Preconditions.checkNotNull(deviceType);
        Preconditions.checkArgument(instanceIndex == null || !instanceIndex.isEmpty());
        Preconditions.checkArgument(conventionName != null && !conventionName.isEmpty());
        this.device = device;
        this.requestedBy = requestedBy;
        this.requestDate = requestDate;
        this.deleted = deleted;
        this.section = section;
        this.deviceType = deviceType;
        this.instanceIndex = instanceIndex;
        this.conventionName = conventionName;
    }

    public Device getDevice() { return device; }

    public UserAccount getRequestedBy() { return requestedBy; }

    public Date getRequestDate() { return requestDate; }

    public boolean isDeleted() { return deleted; }

    public NamePartRevision getSection() { return section; }

    public NamePartRevision getDeviceType() { return deviceType; }

    public @Nullable String getInstanceIndex() { return instanceIndex; }

    public String getConventionName() { return conventionName; }
}
