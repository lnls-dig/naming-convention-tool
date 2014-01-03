package org.openepics.names.ui.names;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.NameEventType;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class NameView {

    public abstract class Change {}

    public class AddChange extends Change {}

    public class DeleteChange extends Change {}

    public class ModifiyChange extends Change {
        private final @Nullable String newName;
        private final @Nullable String newFullName;

        public ModifiyChange(String newName, String newFullName) {
            this.newName = newName;
            this.newFullName = newFullName;
        }

        public @Nullable String getNewName() { return newName; }
        public @Nullable String getNewFullName() { return newFullName; }
    }



    private final @Nullable NameEvent currentRevision;
    private final @Nullable NameEvent pendingRevision;

    public NameView(@Nullable NameEvent currentRevison, @Nullable NameEvent pendingRevision) {
        this.currentRevision = currentRevison;
        this.pendingRevision = pendingRevision;
    }

    public NameEvent getNameEvent() { return baseRevision(); }

    public Integer getId() { return baseRevision().getId(); }

    public @Nullable Change getPendingChange() {
        if (pendingRevision == null) {
            return null;
        } else {
            if (pendingRevision.getEventType() == NameEventType.DELETE) {
                return new DeleteChange();
            } else if (pendingRevision.getEventType() == NameEventType.INSERT) {
                return new AddChange();
            } else if (pendingRevision.getEventType() == NameEventType.MODIFY) {
                final @Nullable String newName = !pendingRevision.getName().equals(currentRevision.getName()) ? pendingRevision.getName() : null;
                final @Nullable String newFullName = !pendingRevision.getName().equals(currentRevision.getName()) ? pendingRevision.getName() : null;
                return new ModifiyChange(newName, newFullName);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public boolean isDeleted() { return baseRevision().getEventType() == NameEventType.DELETE; }

    public String getNameCategory() { return baseRevision().getNameCategory().getDescription(); }

    public String getName() { return baseRevision().getName(); }

    public String getFullName() { return baseRevision().getFullName(); }

    public List<String> getNamePath() {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        for (NameEvent pathElement = baseRevision(); pathElement != null; pathElement = pathElement.getParentName()) {
            pathElements.add(pathElement.getName());
        }
        return pathElements.build().reverse();
    }

    public List<String> getFullNamePath() {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        for (NameEvent pathElement = baseRevision(); pathElement != null; pathElement = pathElement.getParentName()) {
            pathElements.add(pathElement.getFullName());
        }
        return pathElements.build().reverse();
    }

    private NameEvent baseRevision() {
        return currentRevision != null ? currentRevision : pendingRevision;
    }
}
