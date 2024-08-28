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

import java.io.File;

import javax.ejb.*;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.eventbus.annotation.Consumes;
import com.ericsson.oss.mediation.pm.enums.FileTransferError;
import com.ericsson.oss.services.pm.service.events.FileCollectionResponse;

/**
 * Mock class used for testing MediationService in isolation, this class will
 * observe notifications sent as modeled events from mediation service and
 * create notification marker files used in tests to verify that notification
 * has arrived
 * 
 * @author edejket
 * 
 */
@Stateless
public class FileCollectionResultListener {

    private static final Logger logger = LoggerFactory
            .getLogger(FileCollectionResultListener.class);

    /**
     * Observe for success notifications
     * 
     * @param response
     *            Modeled event representing success notification
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void observeFileCollectionSuccessQueue(
            @Observes @Consumes(endpoint = "jms:/queue/FileCollectionResponseQueue") final FileCollectionResponse response) {
        logger.debug(
                "FileCollectionResponse has arrived, jobId=[{}], jobStartTime=[{}], jobEndTime=[{}], bytesStored=[{}], bytesTransferred=[{}], errorCode=[{}], errorMessage=[{}]",
                new Object[] { response.getJobId(), response.getJobStartTime(),
                        response.getJobEndTime(), response.getBytesStored(),
                        response.getBytesTransferred(),
                        response.getErrorCode(), response.getErrorDescription() });

        try {
            if (response.getErrorCode() == FileTransferError.FILE_NOT_AVAILABLE
                    .getErrorCode()) {
                final File file = new File(
                        NotificationMarkers.NOTIFICATIONS_MARKER_DIR
                                + FileTransferError.FILE_NOT_AVAILABLE
                                        .toString());
                file.createNewFile();

            } else if (response.getErrorCode() == -1) {

                final File file = new File(
                        NotificationMarkers.NOTIFICATIONS_MARKER_DIR
                                + NotificationMarkers.SUCCESS_NOTIFICATION_FILE_MARKER);
                file.createNewFile();
            } else {

            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
