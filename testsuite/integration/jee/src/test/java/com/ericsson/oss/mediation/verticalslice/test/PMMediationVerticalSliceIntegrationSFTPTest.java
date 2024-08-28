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
package com.ericsson.oss.mediation.verticalslice.test;

import java.io.File;
import java.util.*;

import javax.ejb.EJB;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.*;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.filesystem.NativeFileSystemFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.jboss.arquillian.container.test.api.*;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.pm.enums.FileTransferError;
import com.ericsson.oss.mediation.verticalslice.deployment.MediationVerticalSliceSFTPTestDeployment;
import com.ericsson.oss.mediation.verticalslice.deployment.MediationVerticalSliceTestDeployments;
import com.ericsson.oss.mediation.verticalslice.mock.NotificationMarkers;
import com.ericsson.oss.services.pm.service.api.*;

@RunWith(Arquillian.class)
public class PMMediationVerticalSliceIntegrationSFTPTest {

    /**
     * Since we want different scenarios, we will controll arq deployment
     * manually
     * 
     */
    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @EJB
    private FileCollectionServiceRemote fileCollectionService;

    private static final int SFTP_PORT = 11009;

    private static final String REMOTE_DIR = "/remote_dir/";
    private static final String REMOTE_HOME = "src/test/ftp_test/remote/user_home/";
    private static final String TRANSFER_DIR = "src/test/ftp_test/local/transfer_dir/";
    private static final String TRANSFERED_FILENAME = "TransferedFileName.txt";
    private static final String REMOTE_FILENAME = "RemoteFile.txt";
    private static final String FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=resourceGroup1,MeContext=meContext2";
    private static final String FDN3 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=resourceGroup1,MeContext=meContext3";

    /**
     * Logger for this test
     */
    private static final Logger logger = LoggerFactory
            .getLogger(PMMediationVerticalSliceIntegrationSFTPTest.class);

    /**
     * Embedded SFTP server
     */
    private static SshServer server;

    /**
     * Prepare for full sftp file collection test by setting up embedded sftp,
     * and cleaning up any stale notification markers/transfered files server
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setupEmbeddedSFTPServer() throws Exception {

        // first we will clear if there is any stale transfer files
        final SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(SFTP_PORT);

        sshd.setPasswordAuthenticator(new DummyPasswordAuthenticator());
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.setFileSystemFactory(new NativeFileSystemFactory());

        final List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
        userAuthFactories.add(new UserAuthPassword.Factory());
        sshd.setUserAuthFactories(userAuthFactories);

        sshd.setCommandFactory(new ScpCommandFactory());

        final List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(namedFactoryList);

        try {
            logger.debug("Starting SSHD service");
            sshd.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        /**
         * src/test/ftp_test/remote/user_home/remote_dir/RemoteFile.txt
         */
        final File testFile = new File(REMOTE_HOME + "remote_dir/"
                + REMOTE_FILENAME);
        if (!testFile.exists()) {
            logger.debug(
                    "Created remote file for ssh test since it was not found, file is: {}",
                    REMOTE_HOME + "remote_dir/" + REMOTE_FILENAME);
            testFile.createNewFile();
            testFile.setReadable(true);
        }

    }

    /**
     * Clean up notification markers and any transfered files before each test
     * case
     */

    @Before
    public void cleanUpResources() {
        cleanUpFiles();
    }

    @AfterClass
    public static void teardownEmbeddedSFTPServer() throws Exception {

        try {
            if (server != null) {
                server.stop(true);
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        cleanUpFiles();
    }

    @Deployment(name = "DataPersistence", testable = false, managed = false)
    public static Archive<?> createDPSMockDeployment() {
        logger.info("******Creating DPS deployment and deploying it to server******");
        return MediationVerticalSliceTestDeployments.createMockEar();
    }

    @Deployment(name = "MediationCore", managed = false, testable = false)
    public static Archive<?> createMedationCoreDeployment() {
        logger.debug("******Creating MediationCore deployment and deploying it to server******");
        return MediationVerticalSliceTestDeployments
                .createMedationCoreArchive();
    }

    /**
     * Create CamelEngine deployment
     * 
     * @return CamelEngine.ear
     */
    @Deployment(name = "CamelEngine", testable = false, managed = false)
    public static Archive<?> createCamelEngineMockDeployment() {
        logger.debug("******Creating MediationService deployment and deploying it to server******");
        return MediationVerticalSliceTestDeployments.createCamelEngineArchive();
    }

    /**
     * Create PMService deployment
     * 
     * @return PMService.ear deployment
     */
    @Deployment(name = "PMService", managed = false, testable = true)
    public static Archive<?> createPMServiceDeployment() {
        logger.debug("******Creating PMService deployment and deploying it to server******");
        return MediationVerticalSliceSFTPTestDeployment
                .createPMServiceArchiveforSFTPTest();
    }

    /**
     * Create MediationService deployment
     * 
     * @return MediationService.ear
     */
    @Deployment(name = "MediationService", testable = false, managed = false)
    public static Archive<?> createMedationServiceMockDeployment() {
        logger.debug("******Creating MediationService deployment and deploying it to server******");
        return MediationVerticalSliceTestDeployments
                .createMedationServiceArchive();
    }

    /**
     * Create AccessControl deployment
     * 
     * @return tss-service.ear
     */
    @Deployment(name = "tss-service.ear", managed = false, testable = false)
    public static Archive<?> createAccessControlDeployment() {
        logger.debug("******Creating AccessControl deployment and deploying it to server******");
        return MediationVerticalSliceTestDeployments
                .createAccessControlServicerchive();
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("DataPersistence")
    public void deployDataPersistence() {
        this.deployer.deploy("DataPersistence");
        logger.info(" ---------------------------------- DEPLOY DataPersistence.ear ----------------------------------");
    }

    @Test
    @InSequence(3)
    @OperateOnDeployment("MediationCore")
    public void deployMediationCore() {
        this.deployer.deploy("MediationCore");
        logger.info(" ---------------------------------- DEPLOY MediationCore.ear ----------------------------------");

    }

    @Test
    @InSequence(4)
    @OperateOnDeployment("CamelEngine")
    public void deployCamelEngine() {
        this.deployer.deploy("CamelEngine");
        logger.info(" ---------------------------------- DEPLOY CamelEngine.rar ----------------------------------");

    }

    @Test
    @InSequence(5)
    @OperateOnDeployment("MediationService")
    public void testDeployMediationService() throws Exception {

        this.deployer.deploy("MediationService");
        logger.info(" ---------------------------------- DEPLOY MediationService.ear ----------------------------------");

    }

    @Test
    @InSequence(6)
    @OperateOnDeployment("PMService")
    public void testDeployPMService() throws Exception {
        this.deployer.deploy("PMService");
        logger.info(" ---------------------------------- DEPLOY PMService.ear ----------------------------------");
    }

    @Test
    @InSequence(7)
    @OperateOnDeployment("tss-service.ear")
    public void testDeployTssService() throws Exception {
        logger.info(" ---------------------------------- TEST FTP File Collection - Deploy TSS SERVICE  ----------------------------------");
        this.deployer.deploy("tss-service.ear");
    }

    @Test
    @InSequence(8)
    @OperateOnDeployment("PMService")
    public void testSuccessfullFileTransfer() throws Exception {
        runSuccessTest();
        logger.info(" ---------------------------------- runSuccessTest for SFTP using PMService.ear ----------------------------------");
    }

    @Test
    @InSequence(9)
    @OperateOnDeployment("PMService")
    public void testFailureFileTransfer() throws Exception {
        runFailureTest();
        logger.info(" ---------------------------------- runFailureTest for SFTP using PMService.ear ----------------------------------");
    }

    /**
     * 
     */
    private void runSuccessTest() {

        final List<SingleFileTransferDetails> singleFileTransfDetails = new ArrayList<SingleFileTransferDetails>();
        final SingleFileTransferDetails singleTransfer = new SingleFileTransferDetails(
                REMOTE_FILENAME, REMOTE_HOME + "remote_dir/",
                TRANSFERED_FILENAME, TRANSFER_DIR,
                UUID.randomUUID().toString(), true, "GMT1", (short) 1, null);
        singleFileTransfDetails.add(singleTransfer);
        final FileCollectionDetails fileTransferDetails = new FileCollectionDetails(
                FDN, singleFileTransfDetails);
        try {
            this.fileCollectionService.collectFiles(fileTransferDetails);
            Thread.sleep(15000);
            /**
             * now check if we have transfered file
             */
            final File file = new File(TRANSFER_DIR + TRANSFERED_FILENAME);
            logger.debug("Success file exist [{}]", file.exists());
            Assert.assertTrue(file.exists());
            /**
             * check now for notification of success
             */
            final File successNotificationMarker = new File(
                    NotificationMarkers.NOTIFICATIONS_MARKER_DIR
                            + NotificationMarkers.SUCCESS_NOTIFICATION_FILE_MARKER);
            Assert.assertTrue(successNotificationMarker.exists());
        } catch (final InterruptedException ie) {
            Assert.fail(ie.getMessage());
        }

    }

    private void runFailureTest() {

        final List<SingleFileTransferDetails> singleFileTransfDetails = new ArrayList<SingleFileTransferDetails>();
        final SingleFileTransferDetails singleTransfer = new SingleFileTransferDetails(
                REMOTE_FILENAME, REMOTE_HOME + "remote_dir/",
                TRANSFERED_FILENAME, TRANSFER_DIR,
                UUID.randomUUID().toString(), true, "GMT1", (short) 1, null);
        singleFileTransfDetails.add(singleTransfer);
        final FileCollectionDetails fileTransferDetails = new FileCollectionDetails(
                FDN3, singleFileTransfDetails);
        this.fileCollectionService.collectFiles(fileTransferDetails);
        try {
            Thread.sleep(15000);
        } catch (final InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        /**
         * check for notification of connection failure
         */
        final File file = new File(NotificationMarkers.NOTIFICATIONS_MARKER_DIR
                + FileTransferError.CONNECTION_FAILED.toString());
        logger.debug("Failure file exist [{}]", file.exists());
        Assert.assertTrue(file.exists());

    }

    private static void cleanUpFiles() {
        /**
         * delete "transfered file"
         */

        final File transferedFile = new File(TRANSFER_DIR + TRANSFERED_FILENAME);
        if (transferedFile.exists()) {
            transferedFile.delete();
        }
        /**
         * delete failure notification marker file, if exists
         */
        final File failureNotification = new File(
                NotificationMarkers.NOTIFICATIONS_MARKER_DIR
                        + FileTransferError.FILE_NOT_AVAILABLE.toString());
        if (failureNotification.exists()) {
            failureNotification.delete();
        }
        /**
         * delete success notification marker file, if exists
         */
        final File successNotification = new File(
                NotificationMarkers.NOTIFICATIONS_MARKER_DIR
                        + NotificationMarkers.SUCCESS_NOTIFICATION_FILE_MARKER);
        if (successNotification.exists()) {
            successNotification.delete();
        }

        final File ConnectioFailNotification = new File(
                NotificationMarkers.NOTIFICATIONS_MARKER_DIR
                        + FileTransferError.CONNECTION_FAILED.toString());
        if (ConnectioFailNotification.exists()) {
            ConnectioFailNotification.delete();
        }

    }

    /**
     * Getter method for embedded sftp server
     * 
     * @return the server
     */
    public SshServer getServer() {
        return server;
    }

    /**
     * Setter method for embedded sftp server
     * 
     * @param server
     *            the server to set
     */
    public void setServer(final SshServer server) {
        PMMediationVerticalSliceIntegrationSFTPTest.server = server;
    }

}

class DummyPasswordAuthenticator implements PasswordAuthenticator {

    private final String expectedPassword = "guestp";

    @Override
    public boolean authenticate(final String username, final String password,
            final ServerSession session) {
        return expectedPassword.equalsIgnoreCase(password);
    }
}
