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
import org.openepics.names.model.NamePartType;
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
	 * @param selectDeleted boolean flag indicating whether deleted name parts should be selectable or not
	 */
	public void setSelectableLevel(TreeNode treeNode, int level, boolean selectDeleted ){
		boolean deleted= treeNode.getData() instanceof NamePartView && ((NamePartView) treeNode.getData()).isDeleted();
		treeNode.setSelectable(level<=0 && (!deleted || selectDeleted));
		int nextLevel=level-1;
		for (TreeNode child : treeNode.getChildren()) {
			setSelectableLevel(child,nextLevel,selectDeleted);	
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
			if(isSelected(node)){
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
		if (treeNode!=null){
		for (TreeNode node : parentList(treeNode)) {
			expand(node);
		}
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
	}

	
	/** 
	 * 
	 * @param node TreeNode populated with NamePartViews as data
	 * @param includeDeleted Logical flag indicating whether deleted should be included or not
	 * @param includeUnfiltered Logical flag indicating whether unfiltered data should be included or not
	 * @return Filtered list of treeNodes staring from the root
	 */
	public List<TreeNode> filteredNodeList(TreeNode node, boolean includeDeleted, boolean includeUnfiltered){
		return nodeList(node);
	}


//	public TreeNode filteredNode(TreeNode node, boolean includeUnfiltered){
//		return filteredNode(node, sessionViewService.isIncludeDeleted(), includeUnfiltered);
//	}
	public TreeNode filteredNode(TreeNode node){
		return filteredNode(viewFilteredNode(node,false,true,true,false),false);
	}

	public @Nullable TreeNode viewFilteredNode(TreeNode node, boolean acceptArchived, boolean acceptActive, boolean acceptOnsite, boolean acceptOffsite){
		NamePartView view= node!=null && node.getData() instanceof NamePartView? (NamePartView) node.getData():null;
		if(view!=null && !(acceptArchived&&acceptActive&&acceptOffsite&&acceptOnsite)){
			if(!(acceptArchived || acceptActive) || !(acceptOnsite||acceptOffsite)){
				return null;
			} else {
				final boolean archived=view.isDeleted();
				final boolean active=!archived;
				if(!(archived &&acceptArchived || active&&acceptActive)) return null;								
				final boolean superSection=view.getParent()==null&& view.getNamePart().getNamePartType().equals(NamePartType.SECTION);
				if(superSection) {
					final boolean onsite = view.getMnemonic()==null || view.getMnemonic().isEmpty();
					final boolean offsite= !onsite;
					if(!(offsite&&acceptOffsite || onsite&&acceptOnsite)) return null;
				}
			}
		}
		final List<TreeNode> filteredChildren=Lists.newArrayList();
		for(TreeNode child:node.getChildren()){
			final TreeNode filteredChild = viewFilteredNode(child, acceptArchived,acceptActive,acceptOnsite,acceptOffsite);
			if(filteredChild!=null) filteredChildren.add(filteredChild);
		}
		final DefaultTreeNode filteredNode = new DefaultTreeNode(node.getData(),null);
		filteredNode.setChildren(filteredChildren);
		for(TreeNode child :filteredNode.getChildren()){
			child.setParent(filteredNode);
		}
		filteredNode.setExpanded(node.isExpanded());
		filteredNode.setRowKey(node.getRowKey());
		filteredNode.setSelectable(node.isSelectable());
		filteredNode.setType(node.getType());
		filteredNode.setSelected(node.isSelected());
		filteredNode.setPartialSelected(node.isPartialSelected());
		return filteredNode;
	}

	
	public @Nullable TreeNode filteredNode(TreeNode node, boolean includeUnfiltered){
		NamePartView view= node!=null && node.getData() instanceof NamePartView? (NamePartView) node.getData():null;
		final boolean included = includeUnfiltered || view!=null && sessionViewService.isFiltered(view.getNamePart());	
		final List<TreeNode> filteredChildren=Lists.newArrayList();
		for(TreeNode child:node.getChildren()){
			final TreeNode filteredChild = filteredNode(child, included);
			if(filteredChild!=null) filteredChildren.add(filteredChild);
		}
		final boolean hasIncludedChildren= !filteredChildren.isEmpty();

		if(included || hasIncludedChildren ){
			final DefaultTreeNode filteredNode = new DefaultTreeNode(node.getData(),null);
			filteredNode.setChildren(filteredChildren);
			for(TreeNode child :filteredNode.getChildren()){
				child.setParent(filteredNode);
			}
			filteredNode.setExpanded(node.isExpanded());
			filteredNode.setRowKey(node.getRowKey());
			filteredNode.setSelectable(node.isSelectable());
			filteredNode.setType(node.getType());
			filteredNode.setSelected(node.isSelected());
			filteredNode.setPartialSelected(node.isPartialSelected());
			return filteredNode;
		}else{
			return null;
		}
	}	 
	
	public List<Object> treeNodeDataLevel(TreeNode node, int level){
		final List<Object> nodeDataList = Lists.newArrayList();
		if(level==0) {
			nodeDataList.add(node.getData());
		} else {
			final int nextLevel=level-1;
			for(TreeNode child :node.getChildren()){
				nodeDataList.addAll(treeNodeDataLevel(child,nextLevel));
			}
		}
		return nodeDataList;
	}

	public List<TreeNode> selectedNodes(TreeNode node){
		final List<TreeNode> nodes=Lists.newArrayList();
		if(node.isSelected()){
			nodes.add(node);
		}
		for (TreeNode child : node.getChildren()) {
			nodes.addAll(selectedNodes(child));
		}
		return nodes;
	}
	
}
