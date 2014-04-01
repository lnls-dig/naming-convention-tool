package org.openepics.names.services.views;

import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;

import javax.annotation.Nullable;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public interface NamePartRevisionProvider {
    @Nullable NamePartRevision approvedRevision(NamePart namePart);

    @Nullable NamePartRevision pendingRevision(NamePart namePart);
}
