package org.openepics.names.services.views;

import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;

import javax.annotation.Nullable;

/**
 * An interface for retrieving revisions of NameParts.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public interface NamePartRevisionProvider {

    /**
     * The relevant (usually current) approved revision of the namePart.
     *
     * @param namePart the name part to retrieve the revision for
     */
    @Nullable NamePartRevision approvedRevision(NamePart namePart);

    /**
     * The relevant (usually current) pending revision of the namePart.
     *
     * @param namePart the name part to retrieve the revision for
     */
    @Nullable NamePartRevision pendingRevision(NamePart namePart);
}
