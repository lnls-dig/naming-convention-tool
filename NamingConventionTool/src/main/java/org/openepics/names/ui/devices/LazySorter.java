package org.openepics.names.ui.devices;

import java.util.Comparator;

import org.openepics.names.services.views.DeviceRecordView;
import org.primefaces.model.SortOrder;

public class LazySorter implements Comparator<DeviceRecordView> {

	private String sortField;
	private SortOrder sortOrder;

	public LazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
	}

	@Override
	public int compare(DeviceRecordView record1, DeviceRecordView record2) {
        try {
            Object value1 = DeviceRecordView.class.getField(this.sortField).get(record1);
            Object value2 = DeviceRecordView.class.getField(this.sortField).get(record2);
 
            int value = ((Comparable)value1).compareTo(value2);
             
            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        }
        catch(Exception e) {
            throw new RuntimeException();
        }
	}

}
