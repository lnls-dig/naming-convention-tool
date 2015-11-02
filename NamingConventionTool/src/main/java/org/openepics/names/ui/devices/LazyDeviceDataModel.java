package org.openepics.names.ui.devices;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openepics.names.services.views.DeviceRecordView;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import com.google.common.collect.Lists;

public class LazyDeviceDataModel extends LazyDataModel<DeviceRecordView> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6972690845413591992L;
	private List<DeviceRecordView> dataSource;
	public LazyDeviceDataModel(List<DeviceRecordView> records) {
		this.dataSource = records;
	}

	@Override
	public DeviceRecordView getRowData(String rowKey){
		for (DeviceRecordView record : dataSource) {
			if(record.getId().equals(rowKey)){
				return record;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(DeviceRecordView record){
		return record.getId();
	}

	@Override
	public List<DeviceRecordView> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters){
		List<DeviceRecordView> data = Lists.newArrayList();

		// filter
		for(DeviceRecordView record: dataSource){
			boolean match=true;
			if(filters!=null){
				for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();){
					try{
						String filterProperty=it.next();
						Object filterValue=filters.get(filterProperty);
						String fieldValue=String.valueOf(record);
						
						if(filterValue== null|| fieldValue.startsWith(filterValue.toString())){
							match= true;
						} else {
							match =false;
							break;
						}
					} catch (Exception e) {
						match = false;
					}
				}
			}
			if(match) {
				data.add(record);
			}
		}
	
		// sort
		if(sortField!=null){
			Collections.sort(data, new LazySorter(sortField,sortOrder));
		}
		
		//rowCount
		int dataSize= data.size();
		this.setRowCount(dataSize);

		//paginate
		if(dataSize> pageSize){
			try{
				return data.subList(first,  first+pageSize);
			} catch(IndexOutOfBoundsException e){
				return data.subList(first,  first+(dataSize % pageSize));
			}
		} else {
			return data;
		}
	}
	

}
