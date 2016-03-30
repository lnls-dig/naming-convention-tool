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
package org.openepics.names.services.views;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * A view of a NamePart that makes it easy to query some of its properties and relations in an object-related fashion.
 *
 * @author Marko Kolar  
 * @author Karin Rathsman 
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
   
    @Override
    public boolean equals(Object other){
    	return other instanceof NamePartView? this.getNamePart().getId().equals(((NamePartView) other).getNamePart().getId()):false;
    }
    
    @Override
    public String toString(){
 	   if(getMnemonic()!=null) {
 		   return getName() + " ("+getMnemonic()+")";
 	   } else {
 		   return getName();
 	   }
    }

    
    /**
     * @return The name part this is a view of.
     */    
    public NamePart getNamePart() { 
    	return getCurrentOrElsePendingRevision().getNamePart(); 
    }

    /**
     * Calls getCurrentOrElsePendingRevision(), here for compatibility with old code only.
     * @return Current or else pending revision of the name part
     */
    @Deprecated
    public NamePartRevision getNameEvent() { return getCurrentOrElsePendingRevision(); }

    @Deprecated
    public Long getId() { return getCurrentOrElsePendingRevision().getId(); }

    /**
     * @return The view of the name part's parent, null if it does not have one.
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
     * 
     * @return The list of view of the name parts approved children, null if it does not have any. 
     */
	public @Nullable List<NamePartView> getCurrentApprovedChildren() {
	     final @Nullable NamePart namePart = getNamePart();
	     final List<NamePartRevision> childRevisions=namePartRevisionProvider.approvedChildrenRevisions(namePart);
	     final List<NamePartView> children=Lists.newArrayList();
	     for(NamePartRevision childRevision:childRevisions){
	    	 children.add(new NamePartView(namePartRevisionProvider,childRevision,null,this));
	     }
	     return children;	
	}

    /**
     * @return The depth level in the name part hierarchy, starting at 0 for root nodes.
     */
    public int getLevel() { 
    	return getParent() != null ? getParent().getLevel() + 1 : 0; 
    }

    /**
     * @return The object describing the pending change of the name part. Null if no change is pending.
     */
    public @Nullable Change getPendingChange() {
        if (pendingRevision == null) {
            return null;
        } else {
            if (isPendingDeletion()) {
                return new DeleteChange(pendingRevision.getStatus());
            } else if (isProposed()) {
                return new AddChange(pendingRevision.getStatus());
            } else {
                return new ModifyChange(pendingRevision.getStatus(), getPendingName(), getPendingMnemonic(), getPendingDescription());
            }
        }
    }
    
    /**
     * 
     * @return true if the name part is pending deletion
     */
    public boolean isPendingDeletion(){
    	return pendingRevision!=null && pendingRevision.isDeleted();
    }
    
    /**
     * 
     * @return true if the name part is new and not yet approved.
     */
    public boolean isProposed(){
    	return pendingRevision!=null && !pendingRevision.isDeleted() && currentRevision==null;
    }
    
    /**
     * 
     * @return true if the name part has previously been approved but is pending modification
     */
    public boolean isPendingModification() {
    	return pendingRevision!=null && !pendingRevision.isDeleted() && currentRevision!=null;
    }
    
    public boolean isPendingInThisLevelAndAbove() {
    	NamePartView object=this;
    	while(object!=null){
    		if(object.isPendingModification() || object.isPendingDeletion() || object.isDeleted()){
    			return true;
    		}
    		object=object.getParent();
    	}
    	return false;
    }
    
    /**
     * 
     * @return true if the full name is pending modification.
     */
    public boolean isNameModified(){
    	return isPendingModification() && !pendingRevision.getName().equals(currentRevision.getName());
    }
    
    /**
     * 
     * @return true if the mnemonic is pending modification
     */
    public boolean isMnemonicModified(){
    	return isPendingModification() && !Objects.toString(pendingRevision.getMnemonic(),"").equals(Objects.toString(currentRevision.getMnemonic(),""));
    }

    /**
     * 
     * @return true if the description/comment is pending modification
     */
    public boolean isDescriptionModified(){
    	return isPendingModification() && !Objects.toString(pendingRevision.getDescription(),"").equals(Objects.toString(currentRevision.getDescription(),""));
    }
    
    /**
     * 
     * @return true if the parent is pending modification
     */
    public boolean isParentModified(){
    	return isPendingModification() && !Objects.equals(pendingRevision.getParent(),currentRevision.getParent());
    }

    /**
     * 
     * @return the pending full name
     */
    public @Nullable String getPendingName(){
    	return isNameModified() ? pendingRevision.getName() : null;
    }
    
    /** 
     * 
     * @return the pending mnemonic
     */
    public @Nullable String getPendingMnemonic(){
    	return isMnemonicModified() ? pendingRevision.getMnemonic() : null;
    }
    
    /**
     * 
     * @return the pending description/comment
     */
    public @Nullable String getPendingDescription(){
    	return isDescriptionModified() ? pendingRevision.getDescription() : null;
    }
    
    /**
     * 
     * @return the pending parent name part
     */
    public @Nullable NamePart getPendingParent(){
    	return isParentModified()? pendingRevision.getParent() : null;
    }
    
    /** 
     * 
     * @return the pending or else current name
     */
    public String getNewName(){
    	return getPendingOrElseCurrentRevision().getName();
    }
    
    /**
     * 
     * @return the pending or else current mnemonic
     */
    public String getNewMnemonic(){
    	return getPendingOrElseCurrentRevision().getMnemonic();
    }
    
    /**
     * 
     * @return the pending or else current description
     */
    public String getNewDescription(){
    	return getPendingOrElseCurrentRevision().getDescription();
    }
    
    /** 
     * 
     * @return the pending or else current parent
     */
    public NamePart getNewParent(){
    	return getPendingOrElseCurrentRevision().getParent();
    }
    
    /**
     * @return True if the name part is deleted.
     */
    public boolean isDeleted() { 
    	return getCurrentOrElsePendingRevision().isDeleted();
    }
    
    /**
     * @return The current revision of the name part. Null if there is no current revision, only a pending one.
     */
    public @Nullable NamePartRevision getCurrentRevision() { 
    	return currentRevision; 
    }

    /**
     * @return The pending revision of the name part. Null if no revision is pending.
     */
    public @Nullable NamePartRevision getPendingRevision() { 
    	return pendingRevision; 
    }

    /**
     * @return The full name of the part. Does not need to follow a convention.
     */
    public String getName() { 
    	return getCurrentOrElsePendingRevision().getName(); 
    }
    
    /**
     * @return The description of the part.
     */
    public String getDescription() { 
    	return getCurrentOrElsePendingRevision().getDescription();
    }

    /**
     * @return The short, mnemonic name of the part in accordance with the naming convention.
     */
    public String getMnemonic() { 
    	return getCurrentOrElsePendingRevision().getMnemonic(); 
    }

    /**
     * @return The list of name part descriptive names starting from the root of the hierarchy to this name part.
     *
     */    
    public List<String> getNamePath() {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        for (NamePartView pathElement = this; pathElement != null; pathElement = pathElement.getParent()) {
            pathElements.add(pathElement.getName());
        }
        return pathElements.build().reverse();
    }

//    public boolean canMoveTo(NamePartView destinationParent){
//    	NamePartView sourceParent=getParent();
//    	NamePartType sourceType=sourceParent!=null? sourceParent.getNamePart().getNamePartType(): getNamePart().getNamePartType();
//    	NamePartType destinationType= destinationParent!=null? destinationParent.getNamePart().getNamePartType(): sourceType;
//		List<String> destinationPath=destinationParent!=null? destinationParent.getMnemonicPath():ImmutableList.<String>of();
//		List<String> sourcePath=sourceParent!=null? sourceParent.getMnemonicPath():ImmutableList.<String>of();
//		return namingConvention.canNamePartMove(sourcePath, sourceType, destinationPath, destinationType);
//    }
    
    /**
     * @return The list of name part mnemonic names starting from the root of the hierarchy to this name part.
     */
    public List<String> getMnemonicPath() {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        for (NamePartView pathElement = this; pathElement != null; pathElement = pathElement.getParent()) {
            pathElements.add(pathElement.getMnemonic()!=null? pathElement.getMnemonic():"");
        }
        return pathElements.build().reverse();
    }

    /**
     * @return The list of name part mnemonic names starting from the root of the hierarchy to the child that is be added to this name part.
     * @param mnemonic the mnemonic of the name part
     */
    public List<String> getMnemonicPathWithChild(String mnemonic) {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        pathElements.add(mnemonic !=null? mnemonic:"");
        for (NamePartView pathElement = this; pathElement != null; pathElement = pathElement.getParent()) {
            pathElements.add(pathElement.getMnemonic()!=null? pathElement.getMnemonic():"");
        }
        return pathElements.build().reverse();
    }

    
    /**
     * @return The name part's pending revision, if any, current revision otherwise.
     */
    public NamePartRevision getPendingOrElseCurrentRevision() {
        return pendingRevision != null ? pendingRevision : currentRevision;
    }

    /**
     * @return The name part's current revision, if any, pending revision otherwise.
     */
    public NamePartRevision getCurrentOrElsePendingRevision() {
        return currentRevision != null ? currentRevision : pendingRevision;
    }

    /**
     * 
     * @return The CSS style depending on the status of the current name part. 
     */
    public String getStyle(){
    	return isDeleted()? "Deleted":"Approved";
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
         * @return The status of the proposed change in the request / approve workflow.
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
        private final @Nullable String newDescription;

        public ModifyChange(NamePartRevisionStatus status, String newName, String newMnemonic, @Nullable String newDescription) {
            super(status);
            this.newName = newName;
            this.newMnemonic = newMnemonic;
            this.newDescription=newDescription;
        }

        /**
         * @return The new descriptive name proposed by the change. Null if the name has not changed.
         */
        public @Nullable String getNewName() { return newName; }

        /**
         * @return The new mnemonic name proposed by the change. Null if the name has not changed.
         */
        public @Nullable String getNewMnemonic() { return newMnemonic; }

        
        /** 
         * @return The new description proposed by the change. Null if the description has not changed. 
         */
		public @Nullable String getNewDescription() { return newDescription; }
    }
    
}
