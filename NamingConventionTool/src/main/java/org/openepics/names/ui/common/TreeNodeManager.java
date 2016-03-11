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
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.services.SessionViewService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.google.common.collect.Lists;


/**
 * @author Karin Rathsman  
 *
 */
@ManagedBean
@ViewScoped
public class TreeNodeManager{

	
	@Inject private NamePartTreeBuilder namePartTreeBuilder;
	@Inject private RestrictedNamePartService namePartService;

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
	public static List<TreeNode> nodeList(@Nullable TreeNode node) {
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
	public static List<TreeNode> parentList(@Nullable TreeNode node){
		final List<TreeNode> nodeList = Lists.newArrayList();
		if(node!=null && node.getParent()!=null) nodeList.addAll(parentList(node.getParent()));
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
		expandSelected(treeNode);
	}

	/**
	 * Set treeNode and its children to be selected/unselected according to a list
	 * @param treeNode The tree node root to be customized 
	 */
	public void selectCustomized(TreeNode treeNode){
		for(TreeNode node: nodeList(treeNode)){
			node.setSelected(isSelected(node));
		}		
	}
	
	/**
	 * Set the selectable level in the treeNode recursively starting from the root. 
	 * @param treeNode The tree node root.
	 * @param level first selectable level below the root tree node. 
	 */
	private void setSelectable(TreeNode treeNode, int level){
		treeNode.setSelectable(level<=0);
		int nextLevel=level-1;
		for (TreeNode child : treeNode.getChildren()) {
			setSelectable(child,nextLevel);	
		}
	}
	
	/**
	 * Set treeNode and its children to be selected/unselected according to filter. 
	 * @param treeNode The tree node root to be customized 
	 */
	public void selectFiltered(TreeNode treeNode){
		for(TreeNode node: nodeList(treeNode)){
			node.setSelected(isFiltered(node));
		}
		expandCustomized(treeNode);
		expandSelected(treeNode);
	}

	/**
	 * Set treeNode and its children to be expanded/collapsed according to a list
	 * @param treeNode The tree node root to be customized 
	 */
	public void expandSelected(TreeNode treeNode){
		for(TreeNode node: nodeList(treeNode)){
			if(node.isSelected()){
				expandParents(node);
			}
		}
	}
	
	
	
	/**
	 * 
	 * @param treeNode the node containing the data
	 * @return true if data is filtered
	 */
	private boolean isFiltered(TreeNode treeNode) {
		return sessionViewService.isFiltered(getNamePart(treeNode));
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
	
//	/**
//	 * Selects node on an event
//	 * @param event event containing the treenode
//	 */
//	public void onNodeSelectRecursively(NodeSelectEvent event){
//		if(event!=null && event.getTreeNode() !=null){
//			selectRecursively(event.getTreeNode());
//		}
//	}
	
//	/**
//	 * Unselects node on an event
//	 * @param event event containing the treenode
//	 */
//	public void onNodeUnselectRecursively(NodeUnselectEvent event){
//		if(event!=null && event.getTreeNode() !=null){
//			unselectRecursively(event.getTreeNode());
//		}
//	}
	
	
	/**
	 * Selects the node as well as the name part
	 * @param treeNode to be selected
	 */
	public void select(TreeNode treeNode){
		if(treeNode!=null && !isSelected(treeNode) && getNamePart(treeNode)!=null ){
			sessionViewService.select(getNamePart(treeNode));
			treeNode.setSelected(true);
		}
	}
	
	/**
	 * Unselects the node as well as the name part
	 * @param treeNode to be unselected
	 */
	public void unselect(TreeNode treeNode){
		if(treeNode!=null && isSelected(treeNode) && getNamePart(treeNode)!=null ){
			sessionViewService.unselect(getNamePart(treeNode));
			treeNode.setSelected(false);
		}
	}

//	/**
//	 * Selects node and name part including children.
//	 * @param treeNode to be selected
//	 */
//	public void selectRecursively(@Nullable TreeNode treeNode){
//		for (TreeNode node : nodeList(treeNode)) {
//			select(node);
//		}
//	}
//	
//	/**
//	 * Unselects node including children and parents. 
//	 * @param treeNode to be unselected
//	 */
//	public void unselectRecursively(@Nullable TreeNode treeNode){
//		for (TreeNode node : nodeList(treeNode)) {
//				unselect(node);
//		}
//		for (TreeNode node: parentList(treeNode)){
//			unselect(node);
//		}
//	}
	
	private boolean isSelected(@Nullable TreeNode treeNode){
		if(treeNode!=null && getNamePart(treeNode)!=null){
			return sessionViewService.isSelected(getNamePart(treeNode));
		} else {
			return false;
		}
	}

//	private boolean hasSelectedParent(TreeNode treeNode){
//		for(TreeNode node: parentList(treeNode)){
//			if (node.isSelected()) return true;
//		}
//		return false;
//	}

	/**
	 * filter/unfilter all selected treeNodes hierarchically.
	 * @param treeNode root of the node tree
	 */
	public void filterSelected(TreeNode treeNode) {
		for (TreeNode node : nodeList(treeNode)){
			if(node.isSelected()) {
				sessionViewService.filter(getNamePart(node));
			} else {
				sessionViewService.unfilter(getNamePart(node));
			}
		}
		selectCustomized(treeNode);
		expandCustomized(treeNode);
	}

	
	/** 
	 * 
	 * @param node TreeNode populated with NamePartViews as data
	 * @param includeDeleted Logical flag indicating whether deleted should be included or not
	 * @param includeUnfiltered Logical flag indicating whether unfiltered data should be included or not
	 * @return Filtered list of treeNodes staring from the root
	 */
	public List<TreeNode> filteredNodeList(TreeNode node, boolean includeDeleted, boolean includeUnfiltered){
//		final List<TreeNode> views = Lists.newArrayList();
//		final List<TreeNode> children=Lists.newArrayList();
//		NamePartView view= node.getData() instanceof NamePartView?  (NamePartView) node.getData():null;
//			if(view==null || (!view.isDeleted() || includeDeleted) ){
//				boolean filtered = view!=null && sessionViewService.isFiltered(view.getNamePart()) || includeUnfiltered;			
//				for(TreeNode child:node.getChildren()){					
//					children.addAll(filteredNodeList(child, includeDeleted, filtered));
//				}
//				boolean hasFilteredChildren= children!=null && !children.isEmpty();
//							
//				if(hasFilteredChildren || filtered){
//					views.add(node);
//					views.addAll(children);
//				}
//			}
//			return views;
		
		return nodeList(filteredNode(node, includeDeleted,includeUnfiltered));
	}


	public TreeNode filteredNode(TreeNode node, boolean includeUnfiltered){
		return filteredNode(node, sessionViewService.isIncludeDeleted(), includeUnfiltered);
	}
	
	public TreeNode filteredNode(TreeNode node, boolean includeDeleted, boolean includeUnfiltered){
		NamePartView view= node.getData() instanceof NamePartView? (NamePartView) node.getData():null;
		if(view!=null && view.isDeleted() && ! includeDeleted ){
			return null;
		} else {
			final boolean included = includeUnfiltered || view!=null && sessionViewService.isFiltered(view.getNamePart());	
			final List<TreeNode> filteredChildren=Lists.newArrayList();
			for(TreeNode child:node.getChildren()){
				final TreeNode filteredChild = filteredNode(child, includeDeleted, included);
				if(filteredChild!=null) filteredChildren.add(filteredChild);
			}
			final boolean hasIncludedChildren= !filteredChildren.isEmpty();

			if(included || hasIncludedChildren ){
				final DefaultTreeNode filteredNode = new DefaultTreeNode(view,null);
				filteredNode.setChildren(filteredChildren);
				for(TreeNode child :filteredNode.getChildren()){
					child.setParent(filteredNode);
				}
				filteredNode.setExpanded(node.isExpanded());
//				filteredNode.setRowKey(node.getRowKey());
				filteredNode.setSelectable(node.isSelectable());
				filteredNode.setType(node.getType());
				filteredNode.setSelected(node.isSelected());
				filteredNode.setPartialSelected(node.isPartialSelected());
				return filteredNode;
			}else{
				return null;
			}
		}
	}	

}
