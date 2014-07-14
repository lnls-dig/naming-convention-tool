package org.openepics.names.services.views;

import com.google.common.collect.ImmutableList;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A view of a NamePart that makes it easy to query some of its properties and relations in an object-related fashion.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class NamePartView {

    private final NamePartRevisionProvider namePartRevisionProvider;
    private final @Nullable NamePartRevision currentRevision;
    private final @Nullable NamePartRevision pendingRevision;

    private @Nullable NamePartView parentView = null;

    /**
     * @param namePartRevisionProvider an on-demand provider of NamePart revisions, usually connected to a service bean
     * @param currentRevision the current revision the view is based on. Null if there is no current revision, only a
     * pending one (in case of a newly proposed name part). If null the pendingRevision parameter must not also be null.
     * @param pendingRevision the revision pending relative to the current revision. Null if no revision is pending.
     * @param parentView the view of the name part's parent. This parameter is optional and can be passed for improving
     * performance (avoiding database queries) when we already have a view of the parent. If this is null, the view will
     * be constructed automatically if needed. The parameter is also null when the name part does not have a parent.
     */
    public NamePartView(NamePartRevisionProvider namePartRevisionProvider, @Nullable NamePartRevision currentRevision, @Nullable NamePartRevision pendingRevision, @Nullable NamePartView parentView) {
        this.namePartRevisionProvider = namePartRevisionProvider;
        this.currentRevision = currentRevision;
        this.pendingRevision = pendingRevision;
        this.parentView = parentView;
    }

    /**
     * The name part this is a view of.
     */
    public NamePart getNamePart() { return getCurrentOrElsePendingRevision().getNamePart(); }

    /**
     * Calls getCurrentOrElsePendingRevision(), here for compatibility with old code only.
     */
    @Deprecated
    public NamePartRevision getNameEvent() { return getCurrentOrElsePendingRevision(); }

    @Deprecated
    public Long getId() { return getCurrentOrElsePendingRevision().getId(); }

    /**
     * The view of the name part's parent, null if it does not have one.
     */
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

    /**
     * The depth level in the name part hierarchy, starting at 0 for root nodes.
     */
    public int getLevel() { return getParent() != null ? getParent().getLevel() + 1 : 0; }

    /**
     * The object describing the pending change of the name part. Null if no change is pending.
     */
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

    /**
     * True if the name part is deleted.
     */
    public boolean isDeleted() { return getCurrentOrElsePendingRevision().isDeleted(); }

    /**
     * The current revision of the name part. Null if there is no current revision, only a pending one.
     */
    public @Nullable NamePartRevision getCurrentRevision() { return currentRevision; }

    /**
     * The pending revision of the name part. Null if no revision is pending.
     */
    public @Nullable NamePartRevision getPendingRevision() { return pendingRevision; }

    /**
     * The long, descriptive name of the part. Does not need to follow a convention.
     */
    public String getName() { return getCurrentOrElsePendingRevision().getName(); }

    /**
     * The short, mnemonic name of the part in accordance with the naming convention.
     */
    public String getMnemonic() { return getCurrentOrElsePendingRevision().getMnemonic(); }

    /**
     * The list of name part descriptive names starting from the root of the hierarchy to this name part.
     */
    public List<String> getNamePath() {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        for (NamePartView pathElement = this; pathElement != null; pathElement = pathElement.getParent()) {
            pathElements.add(pathElement.getName());
        }
        return pathElements.build().reverse();
    }

    /**
     * The list of name part mnemonic names starting from the root of the hierarchy to this name part.
     */
    public List<String> getMnemonicPath() {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        for (NamePartView pathElement = this; pathElement != null; pathElement = pathElement.getParent()) {
            pathElements.add(pathElement.getMnemonic());
        }
        return pathElements.build().reverse();
    }

    /**
     * The name part's pending revision, if any, current revision otherwise.
     */
    public NamePartRevision getPendingOrElseCurrentRevision() {
        return pendingRevision != null ? pendingRevision : currentRevision;
    }

    /**
     * The name part's current revision, if any, pending revision otherwise.
     */
    public NamePartRevision getCurrentOrElsePendingRevision() {
        return currentRevision != null ? currentRevision : pendingRevision;
    }


    /**
     * A view of a proposed change to a name part.
     */
    public abstract class Change {
        private final NamePartRevisionStatus status;

        public Change(NamePartRevisionStatus status) {
            this.status = status;
        }

        /**
         * The status of the proposed change in the request / approve workflow.
         */
        public NamePartRevisionStatus getStatus() { return status; }
    }

    /**
     * A view of a proposed addition to a name part.
     */
    public class AddChange extends Change {
        public AddChange(NamePartRevisionStatus status) {
            super(status);
        }
    }

    /**
     * A view of a proposed deletion of the name part.
     */
    public class DeleteChange extends Change {
        public DeleteChange(NamePartRevisionStatus status) {
            super(status);
        }
    }

    /**
     * A view of a proposed modification of a name part.
     */
    public class ModifyChange extends Change {
        private final @Nullable String newName;
        private final @Nullable String newMnemonic;

        public ModifyChange(NamePartRevisionStatus status, String newName, String newMnemonic) {
            super(status);
            this.newName = newName;
            this.newMnemonic = newMnemonic;
        }

        /**
         * The new descriptive name proposed by the change. Null if the name has not changed.
         */
        public @Nullable String getNewName() { return newName; }

        /**
         * The new mnemonic name proposed by the change. Null if the name has not changed.
         */
        public @Nullable String getNewMnemonic() { return newMnemonic; }
    }
}