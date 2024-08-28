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
import java.io.InputStream;
import java.util.Properties;

import org.jboss.shrinkwrap.api.*;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.verticalslice.dependencies.Artifact;
import com.ericsson.oss.mediation.verticalslice.mock.*;
import com.ericsson.oss.mediation.verticalslice.test.PMMediationVerticalSliceIntegrationFTPTest;

/**
 * Utility class that describes all deployments needed for this test. It will
 * create following items:
 * 
 * MediationCore.ear deployment (taken from nexus and modified to include test
 * archive, this also includes modification to application.xml so that
 * MediationCoreTest.war is listed there). MediationCoreTest.war deployment
 * (will contain the test itself). MediationService.war that will mimic
 * MediationService application
 * 
 * When adding new deployments please add description about the deployment in
 * this comment.
 * 
 * @author edejket
 * 
 */
public final class MediationVerticalSliceTestDeployments {

    private static final Logger logger = LoggerFactory
            .getLogger(MediationVerticalSliceTestDeployments.class);

    /**
     * Archive names
     */
    private static final String MEDIATION_CORE_EAR = "MediationCore.ear";
    private static final String PMSERVICE_EAR = "PMService.ear";
    private static final String MEDIATION_SERVICE_EAR = "MediationService.ear";
    private static final String VERTICAL_SLICE_WAR = "VerticalSliceTest.war";

    /**
     * Create <code>VerticalSliceTest.war</code> archive.</br> This archive will
     * contain only the test itself and it will be included with
     * <code>PMService.ear</code> deployment, so that we are able to use all
     * local interfaces of ejbs exposed in that deployment.
     * 
     * @return test archive with our arquillian test
     */
    public static final Archive<?> createFTPTestArchive() {

        /**
         * Create deployment that will house our test
         */
        logger.debug("******Creating war archive containing tests******");
        final WebArchive verticalSliceTest = ShrinkWrap.create(
                WebArchive.class, VERTICAL_SLICE_WAR);
        /**
         * add the test class in the deployment
         */
        verticalSliceTest
                .addClass(PMMediationVerticalSliceIntegrationFTPTest.class);

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
         * Since test class is importing embedded FTP server, we need to add
         * deps on it, to prevent ClassDefNotFound exceptions
         * 
         * TODO: Optimise this, if this dependency is importing others not
         * needed for test
         */
        verticalSliceTest.addAsLibraries(Artifact.getMavenResolver()
                .artifact(Artifact.APACHE_EMBD_FTPSERVER).resolveAsFiles());
        logger.debug("******Test archive created******");
        return verticalSliceTest;
    }

    /**
     * Create archive representing <code>MediationCore.ear</code>, used in
     * process of testing frp file transfer in full vertical slice.
     * 
     * @return MediationCore.ear
     */
    public static final Archive<?> createMedationCoreArchive() {
        logger.debug("******Creating MediationCore.ear archive for test******");
        final File archiveFile = Artifact
                .resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_OSS_MEDIATION_MEDIATIONCORE_EAR);
        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact "
                    + Artifact.COM_ERICSSON_OSS_MEDIATION_MEDIATIONCORE_EAR);
        }
        final EnterpriseArchive mediationCoreEAR = ShrinkWrap
                .createFromZipFile(EnterpriseArchive.class, archiveFile);

        /**
         * This is how we rename nexus artifact that has version in its name, if
         * we dont want version as part of deployment name, for example if we
         * need to do specific jndi lookup
         */
        final EnterpriseArchive deployment = ShrinkWrap.create(
                EnterpriseArchive.class, MEDIATION_CORE_EAR);
        deployment.merge(mediationCoreEAR);

        logger.debug(
                "******Created MediationCore.ear for deployment, created from maven artifact with coordinates {} ******",
                Artifact.COM_ERICSSON_OSS_MEDIATION_MEDIATIONCORE_EAR);
        return deployment;
    }

    /**
     * Create archive representing <code>MediationService.ear</code>, used in
     * process of testing ftp file transfer in full vertical slice.
     * 
     * @return MediationService.ear
     * 
     */
    public static final Archive<?> createMedationServiceArchive() {
        logger.debug("******Creating MediationService archive******");

        final File archiveFile = Artifact
                .resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_NMS_MEDIATION_MEDIATIONSERVICE_EAR);
        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact "
                    + Artifact.COM_ERICSSON_NMS_MEDIATION_MEDIATIONSERVICE_EAR);
        }
        final EnterpriseArchive mediationServiceEAR = ShrinkWrap
                .createFromZipFile(EnterpriseArchive.class, archiveFile);

        /**
         * This is how we rename nexus artifact that has version in its name, if
         * we dont want version as part of deployment name, for example if we
         * need to do specific jndi lookup
         */
        final EnterpriseArchive deployment = ShrinkWrap.create(
                EnterpriseArchive.class, MEDIATION_SERVICE_EAR);
        deployment.merge(mediationServiceEAR);

        logger.debug(
                "******Created MediationService.ear for deployment from maven artifact with coordinates {} archive******",
                Artifact.COM_ERICSSON_NMS_MEDIATION_MEDIATIONSERVICE_EAR);
        return deployment;
    }

    /**
     * Create archive representing <code>PMService.ear</code>, used in process
     * of testing the ftp file transfer in full vertical slice. </br><b>Note
     * that this ear is modified to include test archive as well</b>
     * 
     * @return PMService.ear
     * 
     */
    public static final Archive<?> createPMServiceArchive() {
        logger.debug("******Creating PMService.ear archive******");

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
        pmServiceEAR.addAsModule(createFTPTestArchive());

        /**
         * We replace application.xml in PMService.ear from nexus with our own
         * custom one, that will contain test war module as well as the ones
         * from original artifact
         */

        Artifact.createCustomApplicationXmlFile(pmServiceEAR,
                "VerticalSliceTest");

        logger.debug("******Created PMService.ear archive******");
        return pmServiceEAR;
    }

    /**
     * Create AccessControl archive, used in this test
     * 
     * @return tss-service.ear
     * 
     */
    public static final Archive<?> createAccessControlServicerchive() {
        /**
         * Create arq ear archive from file we got from maven repository
         */
        try {
            final String tssArtifactName = "tss-service.version";
            final String tssVersion = getArtifactVersion(tssArtifactName);
            // Append versions to tss-service artifacts to support EService
            final String tssServiceEarWithVersion = "tss-service-ear-"
                    + tssVersion + ".ear";
            final String tssServiceEjbJarWithVersion = "tss-service-ejb-"
                    + tssVersion + ".jar";
            final String tssServiceWarWithVersion = "tss.war";
            final EnterpriseArchive tssEAR = ShrinkWrap.create(
                    EnterpriseArchive.class, tssServiceEarWithVersion);
            final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class,
                    tssServiceEjbJarWithVersion);
            ejbJar.addManifest();
            ejbJar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
            ejbJar.addClass(AccessControlServiceBean.class);
            tssEAR.addAsModule(ejbJar);
            final WebArchive war = ShrinkWrap.create(WebArchive.class,
                    tssServiceWarWithVersion);
            war.addAsWebInfResource(
                    new FileAsset(
                            new File(
                                    "src/test/resources/verticalslice/tests/descriptors/beans.xml")),
                    "beans.xml");
            ;
            war.addAsWebInfResource(
                    new FileAsset(
                            new File(
                                    "src/test/resources/verticalslice/tests/descriptors/web.xml")),
                    "web.xml");
            war.addClass(AccessControlWebServiceMock.class);
            tssEAR.addAsModule(war);

            tssEAR.addAsLibraries(Artifact
                    .resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_NMS_MEDIATION_TSS_SERVICE_API));
            tssEAR.addAsLibraries(Artifact
                    .resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_NMS_MEDIATION_TSS_SERVICE_COMMON));

            return tssEAR;

        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static Archive<?> createMockEar() {

        try {
            final String dpsArtifactName = "data-persistence-service.version";
            final String dpsVersion = getArtifactVersion(dpsArtifactName);
            // Append versions to tss-service artifacts to support EService
            final String dpsServiceEjbJarWithVersion = "DataPersistence-ejb-"
                    + dpsVersion + ".jar";

            final File archiveFile = Artifact
                    .resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_OSS_DATAPERSISTENCE_EAR);
            if (archiveFile == null) {
                throw new IllegalStateException("Unable to resolve artifact "
                        + Artifact.COM_ERICSSON_OSS_DATAPERSISTENCE_EAR);
            }
            final EnterpriseArchive dataPersistenceEAR = ShrinkWrap
                    .createFromZipFile(EnterpriseArchive.class, archiveFile);

            final Node node = dataPersistenceEAR
                    .get(dpsServiceEjbJarWithVersion);
            dataPersistenceEAR.delete(node.getPath());

            final JavaArchive dpEjb = ShrinkWrap.create(JavaArchive.class,
                    dpsServiceEjbJarWithVersion);
            dpEjb.addClass(DataPersistenceManagerBean.class);
            dpEjb.addClass(CacheManager.class);
            dpEjb.addClass(DataPathLookupBean.class);
            dpEjb.addAsManifestResource(
                    new FileAsset(
                            new File(
                                    "src/test/resources/verticalslice/tests/descriptors/beans.xml")),
                    "beans.xml");
            dataPersistenceEAR.addAsModule(dpEjb);
            dataPersistenceEAR
                    .addAsLibraries(Artifact
                            .resolveArtifactDependencies("com.ericsson.oss.itpf.sdk:sdk-resources:jar"));

            return dataPersistenceEAR;
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Gets artifact version.
     * 
     * @param artifactName
     * 
     * @return artifact version
     * @throws Exception
     */
    public static String getArtifactVersion(final String artifactName)
            throws Exception {

        final Properties eServiceArtifactVersionProperties = new Properties();
        final InputStream inputStream = MediationVerticalSliceTestDeployments.class
                .getClassLoader().getResourceAsStream(
                        "eServiceArtifactVersion.properties");
        eServiceArtifactVersionProperties.load(inputStream);
        inputStream.close();
        final String artifactVersion = eServiceArtifactVersionProperties
                .getProperty(artifactName);
        return artifactVersion;
    }

    /**
     * @return
     */
    public static Archive<?> createCamelEngineArchive() {
        final File archiveFile = Artifact
                .resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_NMS_CAMEL_ENGINE_RAR);
        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact "
                    + Artifact.COM_ERICSSON_NMS_CAMEL_ENGINE_RAR);
        }
        /**
         * Create arq ear archive from file we got from maven repository
         */
        final ResourceAdapterArchive camelEngineRAR = ShrinkWrap
                .createFromZipFile(ResourceAdapterArchive.class, archiveFile);
        return camelEngineRAR;
    }
}
