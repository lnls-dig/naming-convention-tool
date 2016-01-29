/*-
* Copyright (c) 2014 European Spallation Source
* Copyright (c) 2014 Cosylab d.d.
*
* This file is part of Naming Service.
* Naming Service is free software: you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free
* Software Foundation, either version 2 of the License, or any newer version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
*/

package org.openepics.names.ui.common;

import java.util.List;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.openepics.names.model.NamePart;
import org.openepics.names.services.SessionViewService;
import org.openepics.names.services.views.NamePartView;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.TreeNode;

import com.google.common.collect.Lists;

/**
 * @author Karin Rathsman  
 *
 */
@ManagedBean
@ViewScoped
public class TreeNodeManager{

	@Inject private SessionViewService sessionViewService;

	/**
	 * 
	 * @param node Tree node 
	 * @return the root of the tree node
	 */
	public static TreeNode root(TreeNode node){
		if(node==null || node.getParent() == null){
			return node;
		} else {
			return root(node.getParent());
		}
	}
	
	/**
	 * 
	 * @param node Tree node
	 * @return List of node and successive children
	 */
	public static List<TreeNode> nodeList(TreeNode node) {
		final List<TreeNode> nodeList = Lists.newArrayList();
		if(node!=null){
		nodeList.add(node);
		for(TreeNode child:node.getChildren()){
			nodeList.addAll(nodeList(child));
		}
		}
		return nodeList;
	}
	
	/**
	 * 
	 * @param node Tree node
	 * @return List of successive parents of the node
	 */
	public static List<TreeNode> parentList(TreeNode node){
		final List<TreeNode> nodeList = Lists.newArrayList();
		if(node.getParent()!=null) nodeList.addAll(parentList(node.getParent()));
		nodeList.add(node);
		return nodeList;
	}
	
	private static NamePart getNamePart(@Nullable TreeNode node){
		return  node!=null && node.getData() != null && node.getData() instanceof NamePartView? ((NamePartView) node.getData()).getNamePart():null;
	}	
		
	private boolean isExpanded(TreeNode node){
		if(node.getParent()==null){
			return true;
		} else if(getNamePart(node)!=null){
			return sessionViewService.isExpanded(getNamePart(node));
		} else {
			return false;
		}
	}

	/**
	 * Set treeNode and its children to be expanded/collapsed according to a list
	 * @param treeNode The tree node root to be customized 
	 */
	public void expandCustomized(TreeNode treeNode){
		for(TreeNode node: nodeList(treeNode)){
			node.setExpanded(isExpanded(node));
		}
	}
	
	/**
	 * Expand treeNode and its children
	 * @param treeNode The tree node root
	 */
	public void expandAll(TreeNode treeNode){
		for (TreeNode node : nodeList(treeNode)) {
			expand(node);
		}
	}
	
	/**
	 * Collapse treeNode and its children
	 * @param treeNode The tree node root
	 */
	public void collapseAll(TreeNode treeNode){
		for (TreeNode node : nodeList(treeNode)) {
			collapse(node);
		}
	}

	/**
	 * Expand TreeNode
	 * @param treeNode the node to expand
	 */
	public void expand(TreeNode treeNode) {
		if(treeNode!=null && !isExpanded(treeNode)&& getNamePart(treeNode)!=null) {		
			treeNode.setExpanded(true);
			sessionViewService.expand(getNamePart(treeNode));
		}
	}

	/**
	 * Collapse tree node
	 * @param treeNode the node to collapse
	 */
	public void collapse(TreeNode treeNode) {
		if(treeNode!=null && isExpanded(treeNode)&& getNamePart(treeNode)!=null) {
			treeNode.setExpanded(false);
			sessionViewService.collapse(getNamePart(treeNode));
		}		
	}
	
	/**
	 * Expand all parent nodes to a tree node
	 * @param treeNode the node to expand recursively to root node 
	 */
	public void expandParents(TreeNode treeNode) {
		for (TreeNode node : parentList(treeNode)) {
			expand(node);
		}
	}

	/**
	 * Expands node on an event
	 * @param event containing the treeNode
	 */
	public void onNodeExpand(NodeExpandEvent event){
		if(event!=null && event.getTreeNode() !=null){
			expand(event.getTreeNode());
		}
	}

	/** 
	 * Collapses node on an even
	 * @param event containing the treeNode
	 */
	public void onNodeCollapse(NodeCollapseEvent event){
		if(event!=null && event.getTreeNode() !=null){
			collapse(event.getTreeNode());
		}
	}	
}
