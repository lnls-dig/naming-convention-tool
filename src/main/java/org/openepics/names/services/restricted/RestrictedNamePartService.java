package org.openepics.names.services.restricted;

import com.google.common.base.Preconditions;
import org.openepics.names.model.*;
import org.openepics.names.services.NamePartService;
import org.openepics.names.services.SessionService;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * A gateway to a NamePartService bean that enforces user access control rules on each call. All calls from UI code should
 * go through this.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class RestrictedNamePartService {

    @Inject private SessionService sessionService;
    @Inject private NamePartService namePartService;

    public boolean isMnemonicUnique(NamePartType namePartType, @Nullable NamePart parent, String mnemonic) {
        return namePartService.isMnemonicUnique(namePartType, parent, mnemonic);
    }

    public boolean isMnemonicValid(NamePartType namePartType, @Nullable NamePart parent, String mnemonic) {
        return namePartService.isMnemonicValid(namePartType, parent, mnemonic);
    }

    public NamePartRevision addNamePart(String name, String mnemonic, NamePartType nameType, @Nullable NamePart parent, @Nullable String comment) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.addNamePart(name, mnemonic, nameType, parent, sessionService.user(), comment);
    }

    public NamePartRevision modifyNamePart(NamePart namePart, String name, String mnemonic, @Nullable String comment) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.modifyNamePart(namePart, name, mnemonic, sessionService.user(), comment);
    }

    public NamePartRevision deleteNamePart(NamePart namePart, @Nullable String comment) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.deleteNamePart(namePart, sessionService.user(), comment);
    }

    public NamePartRevision cancelChangesForNamePart(NamePart namePart, @Nullable String comment) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.cancelChangesForNamePart(namePart, sessionService.user(), comment, false);
    }

    public NamePartRevision rejectChangesForNamePart(NamePart namePart, String comment) {
        Preconditions.checkState(sessionService.isSuperUser());
        return namePartService.cancelChangesForNamePart(namePart, sessionService.user(), comment, true);
    }

    public void approveNamePartRevision(NamePartRevision namePartRevision, @Nullable String comment) {
        Preconditions.checkState(sessionService.isSuperUser());
        namePartService.approveNamePartRevision(namePartRevision, sessionService.user(), comment);
    }

    public List<Device> associatedDevices(NamePart namePart) {
        return namePartService.associatedDevices(namePart);
    }

    public List<NamePartRevision> currentApprovedRevisions(NamePartType type, boolean includeDeleted) {
        return namePartService.currentApprovedRevisions(type, includeDeleted);
    }

    public List<NamePartRevision> currentPendingRevisions(NamePartType type, boolean includeDeleted) {
        return namePartService.currentPendingRevisions(type, includeDeleted);
    }

    public List<NamePartRevision> revisions(NamePart namePart) {
        return namePartService.revisions(namePart);
    }

    public @Nullable NamePartRevision approvedRevision(NamePart namePart) {
        return namePartService.approvedRevision(namePart);
    }

    public @Nullable NamePartRevision pendingRevision(NamePart namePart) {
        return namePartService.pendingRevision(namePart);
    }
}
