package org.openepics.names.model;

/**
 * Status of the name part in the request / approve workflow.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public enum NamePartRevisionStatus {
    /** The proposed revision has been approved by the administrator. */
    APPROVED,

    /** The proposed revision has been cancelled by the user that requested it. */
    CANCELLED,

    /** The proposed revision is pending approval by the administrator. */
    PENDING,

    /** The proposed revision has been rejected by the administrator. */
    REJECTED
}
