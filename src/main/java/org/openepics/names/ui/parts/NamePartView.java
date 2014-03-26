package org.openepics.names.ui.parts;

import com.google.common.collect.ImmutableList;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;
import org.openepics.names.services.restricted.RestrictedNamePartService;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class NamePartView {

    public abstract class Change {
        private final NamePartRevisionStatus status;

        public Change(NamePartRevisionStatus status) {
            this.status = status;
        }

        public NamePartRevisionStatus getStatus() { return status; }
    }

    public class AddChange extends Change {
        public AddChange(NamePartRevisionStatus status) {
            super(status);
        }
    }

    public class DeleteChange extends Change {
        public DeleteChange(NamePartRevisionStatus status) {
            super(status);
        }
    }

    public class ModifyChange extends Change {
        private final @Nullable String newName;
        private final @Nullable String newMnemonic;

        public ModifyChange(NamePartRevisionStatus status, String newName, String newMnemonic) {
            super(status);
            this.newName = newName;
            this.newMnemonic = newMnemonic;
        }

        public @Nullable String getNewName() { return newName; }
        public @Nullable String getNewMnemonic() { return newMnemonic; }
    }

    private final RestrictedNamePartService namePartService;
    private final @Nullable NamePartRevision currentRevision;
    private final @Nullable NamePartRevision pendingRevision;

    public NamePartView(RestrictedNamePartService namePartService, @Nullable NamePartRevision currentRevision, @Nullable NamePartRevision pendingRevision) {
        this.namePartService = namePartService;
        this.currentRevision = currentRevision;
        this.pendingRevision = pendingRevision;
    }

    public NamePart getNamePart() { return getCurrentOrElsePendingRevision().getNamePart(); }

    public NamePartRevision getNameEvent() { return getCurrentOrElsePendingRevision(); }

    public Integer getId() { return getCurrentOrElsePendingRevision().getId(); }

    public @Nullable NamePartView getParent() {
        final @Nullable NamePart parent = getCurrentOrElsePendingRevision().getParent();
        if (parent != null) {
            return new NamePartView(namePartService, namePartService.approvedRevision(parent), namePartService.pendingRevision(parent));
        } else {
            return null;
        }
    }

    public int getLevel() {
        return getParent() == null ? 0 : getParent().getLevel() + 1;
    }

    public @Nullable Change getPendingChange() {
        if (pendingRevision == null) {
            return null;
        } else {
            if (pendingRevision.isDeleted()) {
                return new DeleteChange(pendingRevision.getStatus());
            } else if (currentRevision == null) {
                return new AddChange(pendingRevision.getStatus());
            } else {
                final @Nullable String newName = !pendingRevision.getName().equals(currentRevision.getName()) ? pendingRevision.getName() : null;
                final @Nullable String newMnemonic = !pendingRevision.getMnemonic().equals(currentRevision.getMnemonic()) ? pendingRevision.getMnemonic() : null;
                return new ModifyChange(pendingRevision.getStatus(), newName, newMnemonic);
            }
        }
    }

    public boolean isDeleted() { return getCurrentOrElsePendingRevision().isDeleted(); }

    public @Nullable NamePartRevision getCurrentRevision() { return currentRevision; }

    public @Nullable NamePartRevision getPendingRevision() { return pendingRevision; }

    public String getName() { return getCurrentOrElsePendingRevision().getName(); }

    public @Nullable String getMnemonic() { return getCurrentOrElsePendingRevision().getMnemonic(); }

    public List<String> getNamePath() {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        for (NamePartView pathElement = this; pathElement != null; pathElement = pathElement.getParent()) {
            pathElements.add(pathElement.getName());
        }
        return pathElements.build().reverse();
    }

    public List<String> getMnemonicPath() {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        for (NamePartView pathElement = this; pathElement != null; pathElement = pathElement.getParent()) {
            pathElements.add(pathElement.getMnemonic());
        }
        return pathElements.build().reverse();
    }

    public NamePartRevision getPendingOrElseCurrentRevision() {
        return pendingRevision != null ? pendingRevision : currentRevision;
    }

    private NamePartRevision getCurrentOrElsePendingRevision() {
        return currentRevision != null ? currentRevision : pendingRevision;
    }
}
