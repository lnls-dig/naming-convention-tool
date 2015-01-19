/**
 * 
 */
package org.openepics.names.ui.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.openepics.names.services.SessionViewService;
import org.openepics.names.services.views.NamePartView;
import org.primefaces.model.TreeNode;

/**
 * @author Karin Rathsman <Karin.Rathsman@esss.se>
 *
 */

@ManagedBean
@ViewScoped
public class TreeNodeManager{
	private Map<TreeNode,Object> nodeMap;
	private TreeNode root;

	@Inject private SessionViewService sessionViewService;
	/**
	 * 
	 */
	
	public void init(TreeNode node){
		nodeMap=new HashMap<TreeNode,Object>();
		root=root(node);
		addChildren(root);
	}
		
	private TreeNode root(TreeNode node){
		if(node==null || node.getParent() == null){
			return node;
		} else {
			return root(node.getParent());
		}
	}
	
	private void addChildren(@Nullable TreeNode node){
		if(node == null){
			return;	
		} else {
			if(getData(node) != null){
				nodeMap.put(node, getData(node));
			}
			List<TreeNode> children = node.getChildren();
			if(children!=null && !children.isEmpty()) {
				for (TreeNode child : children) {
				addChildren(child);
				}
			}
		}
	}

	private Object getData(@Nullable TreeNode node){
		return  node!=null && node.getData() != null && node.getData() instanceof NamePartView? ((NamePartView) node.getData()).getNamePart():null;
	}	
		
	private boolean isExpanded(TreeNode node){
		if(node.getParent()==null){
			return true;
		} else if (nodeMap.containsKey(node)){
			return sessionViewService.isExpanded(nodeMap.get(node));
		} else {
			return false;
		}
	}

	public void expandCustomized(TreeNode treeNode){
		init(treeNode);
		for (TreeNode node : nodeMap.keySet()) {
			node.setExpanded(isExpanded(node));
		}
	}
	
	public void expandAll(TreeNode treeNode){
		init(treeNode);
		for (TreeNode node : nodeMap.keySet()) {
			expand(node);
			node.setExpanded(isExpanded(node));
		}
		expandCustomized(treeNode);
	}
	
	public void collapseAll(TreeNode treeNode){
		init(treeNode);
		for (TreeNode node : nodeMap.keySet()) {
			collapse(node);
		}
		expandCustomized(treeNode);
	}

	public void expand(TreeNode treeNode) {
		if(treeNode!=null && !isExpanded(treeNode)&& nodeMap.containsKey(treeNode)) {		
			sessionViewService.expand(nodeMap.get(treeNode));			
		}
	}

	public void collapse(TreeNode treeNode) {
		if(treeNode!=null && isExpanded(treeNode)&& nodeMap.containsKey(treeNode)) {
			sessionViewService.collapse(nodeMap.get(treeNode));
		}		
	}
}