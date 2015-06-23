package org.openepics.names.ui.devices;


import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.openepics.names.services.NamingConvention;
import org.openepics.names.services.views.DeviceRecordView;
import org.openepics.names.services.views.DeviceView;
import org.primefaces.model.TreeNode;

import com.google.common.collect.Lists;

@ManagedBean
@ViewScoped
public class DeviceTableController {
	@Inject private DevicesController devicesController; 
	@Inject private NamingConvention namingConvention;

	
	
	List<DeviceRecordView> devices;

	public List<DeviceRecordView> getDevices() {
		return devices;
	}

	@PostConstruct
	public void init(){
		devices=deviceRecords(devicesController.getViewDevice());
	}


	/**
	 *  Generates a list of device records for views.
	 * @return list of device records
	 */
	public List<DeviceRecordView> deviceRecords(TreeNode root){
		final List<TreeNode> nodeList=getNodeList(root);
		final List<DeviceRecordView> deviceList=Lists.newArrayList();
		for(TreeNode node: nodeList){
			try {
				DeviceView device=(DeviceView) node.getData();
				deviceList.add(new DeviceRecordView(device));				
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return deviceList;
	}

	private List<TreeNode> getNodeList(TreeNode node) {
		final List<TreeNode> nodeList	= Lists.newArrayList();
		nodeList.add(node);
		for(TreeNode child:node.getChildren()){
			nodeList.addAll(getNodeList(child));
		}
		return nodeList;
	}
}
