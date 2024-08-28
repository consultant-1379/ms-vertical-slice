/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.mediation.verticalslice.mock;

/**
 * Utility class defining constants for
 * <code>VerticalSliceIntegrationTest</code> </br> Use this class to define
 * directories and files related to notification marker files created in
 * <code>FileCollectionResultListener.java</code>
 * 
 * @author edejket
 * 
 */
public final class NotificationMarkers {

    /**
     * This is the directory where both failure and success notification marker
     * files will be stored
     */
    public static final String NOTIFICATIONS_MARKER_DIR = "src/test/ftp_test/notifications_markers/";
    /**
     * Name of the marker file that will represent successfull file transfer
     * notification
     */
    public static final String SUCCESS_NOTIFICATION_FILE_MARKER = "Notification.success";

}
