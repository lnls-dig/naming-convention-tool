package org.openepics.names.ui;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.List;
import org.openepics.names.model.NameEvent;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class NameEventView {

    private final NameEvent nameEvent;

    public NameEventView(NameEvent nameEvent) {
        this.nameEvent = nameEvent;
    }

    public NameEvent getNameEvent() { return nameEvent; }
    public Integer getId() { return nameEvent.getId(); }
    public String getEventType() {
        switch (nameEvent.getEventType()) {
            case INSERT: return "Add";
            case MODIFY: return "Modify";
            case DELETE: return "Delete";
            default: throw new IllegalStateException();
        }
    }
    public String getNameCategory() { return nameEvent.getNameCategory().getDescription(); }
    public String getName() { return nameEvent.getName(); }
    public String getFullName() { return nameEvent.getFullName(); }
    public String getNamePath() {
        final List<String> pathElements = Lists.newArrayList();
        for (NameEvent pathElement = nameEvent; pathElement != null; pathElement = pathElement.getParentName()) {
            pathElements.add(0, pathElement.getName());
        }
        return Joiner.on(" ▸ ").join(pathElements);
    }
    public String getFullNamePath() {
        final List<String> pathElements = Lists.newArrayList();
        for (NameEvent pathElement = nameEvent; pathElement != null; pathElement = pathElement.getParentName()) {
            pathElements.add(0, pathElement.getFullName());
        }
        return Joiner.on(" ▸ ").join(pathElements);
    }
}
