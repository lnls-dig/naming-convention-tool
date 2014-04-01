package org.openepics.names.services.views;

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

    private final NamePartRevisionProvider namePartRevisionProvider;
    private final @Nullable NamePartRevision currentRevision;
    private final @Nullable NamePartRevision pendingRevision;

    private @Nullable NamePartView parentView = null;

    public NamePartView(NamePartRevisionProvider namePartRevisionProvider, @Nullable NamePartRevision currentRevision, @Nullable NamePartRevision pendingRevision, @Nullable NamePartView parentView) {
        this.namePartRevisionProvider = namePartRevisionProvider;
        this.currentRevision = currentRevision;
        this.pendingRevision = pendingRevision;
        this.parentView = parentView;
    }

    public NamePart getNamePart() { return getCurrentOrElsePendingRevision().getNamePart(); }

    public NamePartRevision getNameEvent() { return getCurrentOrElsePendingRevision(); }

    public Long getId() { return getCurrentOrElsePendingRevision().getId(); }

    public @Nullable NamePartView getParent() {
        final @Nullable NamePart parent = getCurrentOrElsePendingRevision().getParent();
        if (parent != null) {
            if (parentView == null) {
                parentView = new NamePartView(namePartRevisionProvider, namePartRevisionProvider.approvedRevision(parent), namePartRevisionProvider.pendingRevision(parent), null);
            }
            return parentView;
        } else {
            return null;
        }
    }

    public int getLevel() { return getParent() != null ? getParent().getLevel() + 1 : 0; }

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

    public String getMnemonic() { return getCurrentOrElsePendingRevision().getMnemonic(); }

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

    public NamePartRevision getCurrentOrElsePendingRevision() {
        return currentRevision != null ? currentRevision : pendingRevision;
    }



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
}
