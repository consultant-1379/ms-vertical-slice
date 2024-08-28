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
import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("/updateServiceTss")
@Path("/")
@ApplicationScoped
public class AccessControlWebServiceMock extends Application {

    private static final Logger log = LoggerFactory
            .getLogger(AccessControlWebServiceMock.class);

    // http://127.0.0.1:8080/tss/updateServiceTss/deleteCacheEntry?nodeName=nodeName

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/deleteCacheEntry")
    public void processUpgradeRequest(
            @QueryParam("nodeName") final String nodeName) {

        log.debug("Received request for cahce entry update for node {}",
                nodeName);
        if (nodeName.equals("meContext3")) {
            log.debug("Node {} has been removed form the cache", nodeName);
            final File file = new File(
                    NotificationMarkers.NOTIFICATIONS_MARKER_DIR
                            + "CONNECTION_FAILED");
            try {
                file.createNewFile();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            log.debug("Node {} was not present in the cache", nodeName);
        }

    }

}
