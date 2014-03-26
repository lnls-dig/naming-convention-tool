package org.openepics.names.model;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * @author <a href="mailto:jakob.battelino@cosylab.com">Jakob Battelino Prelog</a>
 */
@Entity
public class DeviceRevision extends Persistable {

    private @ManyToOne Device device;

    private @ManyToOne UserAccount requestedBy;

    private Date requestDate;

    private boolean deleted;

    private @ManyToOne NamePart section;

    private @ManyToOne NamePart deviceType;

    private @Nullable String instanceIndex;

    protected DeviceRevision() {}

    public DeviceRevision(Device device, @Nullable UserAccount requestedBy, Date requestDate, boolean deleted, NamePart section, NamePart deviceType, @Nullable String instanceIndex) {
        Preconditions.checkNotNull(device);
        Preconditions.checkNotNull(requestDate);
        Preconditions.checkNotNull(section);
        Preconditions.checkNotNull(deviceType);
        Preconditions.checkArgument(instanceIndex == null || !instanceIndex.isEmpty());
        this.device = device;
        this.requestedBy = requestedBy;
        this.requestDate = requestDate;
        this.section = section;
        this.deviceType = deviceType;
        this.instanceIndex = instanceIndex;
        this.deleted = deleted;
    }

    public Device getDevice() { return device; }

    public UserAccount getRequestedBy() { return requestedBy; }

    public Date getRequestDate() { return requestDate; }

    public boolean isDeleted() { return deleted; }

    public NamePart getSection() { return section; }

    public NamePart getDeviceType() { return deviceType; }

    public @Nullable String getInstanceIndex() { return instanceIndex; }
}
