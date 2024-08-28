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
package com.ericsson.oss.mediation.verticalslice.deployment;

import java.io.File;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.verticalslice.dependencies.Artifact;
import com.ericsson.oss.mediation.verticalslice.mock.FileCollectionResultListener;
import com.ericsson.oss.mediation.verticalslice.mock.NotificationMarkers;
import com.ericsson.oss.mediation.verticalslice.test.PMMediationVerticalSliceIntegrationSFTPTest;

public final class MediationVerticalSliceSFTPTestDeployment {

    private static final String VERTICAL_SLICE_WAR = "VerticalSliceTest.war";
    private static final String PMSERVICE_EAR = "PMService.ear";
    /**
     * Replacement descriptors, if any are needed
     */
    private static final String APPLICATION_XML = "src/test/resources/verticalslice/tests/descriptors/application-verticalslice-test.xml";

    private static final Logger log = LoggerFactory
            .getLogger(MediationVerticalSliceSFTPTestDeployment.class);

    /**
     * Create <code>VerticalSliceTest.war</code> archive.</br> This archive will
     * contain only the test itself and it will be included with
     * <code>PMService.ear</code> deployment, so that we are able to use all
     * local interfaces of ejbs exposed in that deployment.
     * 
     * @return test archive with our arquillian test
     */
    public static final Archive<?> createSFTPTestArchive() {

        /**
         * Create deployment that will house our test
         */
        log.debug("******Creating war archive containing SFTP tests******");
        final WebArchive verticalSliceTest = ShrinkWrap.create(
                WebArchive.class, VERTICAL_SLICE_WAR);
        /**
         * add the test class in the deployment
         */
        verticalSliceTest
                .addClass(PMMediationVerticalSliceIntegrationSFTPTest.class);

        /**
         * add mocked FileCollectionResultListener to test war
         */
        verticalSliceTest.addClass(FileCollectionResultListener.class);
        /**
         * add utility class defining constants related to notification marker
         * files
         */
        verticalSliceTest.addClass(NotificationMarkers.class);

        /**
         * Since test class is importing embedded SFTP server, we need to add
         * deps on it, to prevent ClassDefNotFound exceptions
         * 
         * TODO: Optimise this, if this dependency is importing others not
         * needed for test
         */
        verticalSliceTest.addAsLibraries(Artifact.getMavenResolver()
                .artifact(Artifact.APACHE_EMBD_SFTPSERVER).resolveAsFiles());
        log.debug("******SFTP Test archive created******");
        return verticalSliceTest;
    }

    public static final Archive<?> createPMServiceArchiveforSFTPTest() {
        log.debug("******Creating PMService.ear archive for SFTP Test******");

        final File archiveFile = Artifact
                .resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_NMS_SERVICES_PMSERVICE_EAR);
        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact "
                    + Artifact.COM_ERICSSON_NMS_SERVICES_PMSERVICE_EAR);
        }
        final EnterpriseArchive pmServiceEAR = ShrinkWrap.createFromZipFile(
                EnterpriseArchive.class, archiveFile);

        /**
         * Add test archive to this deployment since this one will be under test
         */
        pmServiceEAR.addAsModule(createSFTPTestArchive());
        /**
         * This is how we rename nexus artifact that has version in its name, if
         * we dont want version as part of deployment name, for example if we
         * need to do specific jndi lookup
         */
        /**
         * We replace application.xml in PMService.ear from nexus with our own
         * custom one, that will contain test war module as well as the ones
         * from original artifact
         */

        Artifact.createCustomApplicationXmlFile(pmServiceEAR,
                "VerticalSliceTest");

        log.debug("******Created PMService.ear archive******");
        return pmServiceEAR;
    }

    /**
     * @return EAR file of Model Service
     */
    public static final Archive<?> createModelServiceArchive() {
        final File archiveFile = Artifact
                .resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_MODEL_SERVICE);
        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact "
                    + Artifact.COM_ERICSSON_MODEL_SERVICE);
        }
        /**
         * Create arq ear archive from file we got from maven repository
         */
        final EnterpriseArchive modelServiceEAR = ShrinkWrap.createFromZipFile(
                EnterpriseArchive.class, archiveFile);

        return modelServiceEAR;
    }
}
