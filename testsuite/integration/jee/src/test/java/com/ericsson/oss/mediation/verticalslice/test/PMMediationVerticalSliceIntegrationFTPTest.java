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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJB;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.pm.enums.FileTransferError;
import com.ericsson.oss.mediation.verticalslice.deployment.MediationVerticalSliceTestDeployments;
import com.ericsson.oss.mediation.verticalslice.mock.NotificationMarkers;
import com.ericsson.oss.services.pm.service.api.FileCollectionDetails;
import com.ericsson.oss.services.pm.service.api.FileCollectionServiceRemote;
import com.ericsson.oss.services.pm.service.api.SingleFileTransferDetails;

/**
 * <b>Full vertical slice test for file transfer</b></br>This test includes
 * <code>PMService.ear</code>, <code>MediationCore.ear</code> and
 * <code>MediationService.ear</code> deployments.</br>This test will verify
 * successfull and not successfull file transfer, but does not verify at this
 * moment that notifications have come.
 * 
 * @author edejket
 * 
 */
@RunWith(Arquillian.class)
public class PMMediationVerticalSliceIntegrationFTPTest {

    private static final short FTP_PORT = 2221;
    private static final String FTP_USERNAME = "ftpuser";
    private static final String FTP_PASSWORD = "guestp";

    private static final String REMOTE_FILENAME = "RemoteFile.txt";
    private static final String REMOTE_DIR = "/remote_dir/";
    private static final String FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=resourceGroup1,MeContext=meContext1";

    private static final String TRANSFER_DIR = "src/test/ftp_test/local/transfer_dir/";
    private static final String TRANSFERED_FILENAME = "TransferedFileName.txt";

    private static FtpServer server;

    /**
     * Since we want different scenarios, we will controll arq deployment manually
     * 
     */
    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @EJB
    private FileCollectionServiceRemote fileCollectionService;

    /**
     * our test logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(PMMediationVerticalSliceIntegrationFTPTest.class);


    @Deployment(name = "DataPersistence", testable = false, managed = false)
    public static Archive<?> createDPSMockDeployment() {
        logger.info("******Creating DPS deployment and deploying it to server******");
        return MediationVerticalSliceTestDeployments.createMockEar();
    }

    @Deployment(name = "MediationCore", managed = false, testable = false)
    public static Archive<?> createMedationCoreDeployment() {
        logger.debug("******Creating MediationCore deployment and deploying it to server******");
        return MediationVerticalSliceTestDeployments.createMedationCoreArchive();
    }

    /**
     * Create PMService deployment
     * 
     * @return PMService.ear deployment
     */
    @Deployment(name = "PMService", managed = false, testable = true)
    public static Archive<?> createPMServiceDeployment() {
        logger.debug("******Creating PMService deployment and deploying it to server******");
        return MediationVerticalSliceTestDeployments.createPMServiceArchive();
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

    /**
     * prepare for full integration test
     * 
     * @throws FtpException
     */
    @BeforeClass
    public static void setupEmbeddedFTPServer() throws Exception {

        // first we will clear if there is any stale transfer files
        try {
            final File transferedFile = new File(TRANSFER_DIR
                    + TRANSFERED_FILENAME);
            if (transferedFile.exists()) {
                transferedFile.delete();
            }
        } catch (final Exception ioe) {
            throw new FtpException(ioe);
        }

        final FtpServerFactory serverFactory = new FtpServerFactory();
        final ListenerFactory factory = new ListenerFactory();
        factory.setPort(FTP_PORT);
        serverFactory.addListener("default", factory.createListener());
        server = serverFactory.createServer();
        final PropertiesUserManagerFactory userFactory = new PropertiesUserManagerFactory();

        /**
         * not used atm, using defaults
         */
        final File userFile = new File("src/test/ftp_test/users.properties");
        final File userHome = new File("src/test/ftp_test/remote/user_home");
        userHome.setWritable(true);
        userHome.mkdirs();
        userFactory.setFile(userFile);
        final UserManager um = userFactory.createUserManager();
        final BaseUser user = new BaseUser();
        user.setName(FTP_USERNAME);
        user.setPassword(FTP_PASSWORD);
        user.setHomeDirectory(userHome.getAbsolutePath());

        final List<Authority> auths = new ArrayList<Authority>();
        final Authority auth = new WritePermission();
        auths.add(auth);
        user.setAuthorities(auths);

        um.save(user);

        serverFactory.setUserManager(um);
        server.start();

    }

    private void cleanUpLocalDirectory() throws Exception {
        final File transferedFile = new File(TRANSFER_DIR + TRANSFERED_FILENAME);
        if (transferedFile.exists()) {
            transferedFile.delete();
        }
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
        cleanUpLocalDirectory();
        runSuccessTest();
        logger.info(" ---------------------------------- runSuccessTest using PMService.ear ----------------------------------");
    }

    @Test
    @InSequence(9)
    @OperateOnDeployment("PMService")
    public void testFailedFileTransfer() throws Exception {
        runFailureTest();
        logger.info(" ---------------------------------- runFailureTest using PMService.ear ----------------------------------");
    }

    /**
     * Execute successfull file transfer test
     */
    private void runSuccessTest() {
        final List<SingleFileTransferDetails> singleFileTransfDetails = new ArrayList<SingleFileTransferDetails>();
        final SingleFileTransferDetails singleTransfer = new SingleFileTransferDetails(
                REMOTE_FILENAME, REMOTE_DIR, TRANSFERED_FILENAME, TRANSFER_DIR,
                UUID.randomUUID().toString(), false, "GMT1", (short) 1, null);
        singleFileTransfDetails.add(singleTransfer);
        final FileCollectionDetails fileTransferDetails = new FileCollectionDetails(
                FDN, singleFileTransfDetails);
        try {
            this.fileCollectionService.collectFiles(fileTransferDetails);
			Thread.sleep(20000);
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

    /**
     * Execute failed ftp file transfer test
     */
    private void runFailureTest() {
        final List<SingleFileTransferDetails> singleFileTransfDetails = new ArrayList<SingleFileTransferDetails>();
        final SingleFileTransferDetails singleTransfer = new SingleFileTransferDetails(
                "REMOTE_FILENAME", REMOTE_DIR, TRANSFERED_FILENAME, TRANSFER_DIR,
                UUID.randomUUID().toString(), false, "GMT1", (short) 1, null);
        singleFileTransfDetails.add(singleTransfer);
        final FileCollectionDetails fileTransferDetails = new FileCollectionDetails(
                FDN, singleFileTransfDetails);
        try {
            this.fileCollectionService.collectFiles(fileTransferDetails);
			Thread.sleep(20000);
            /**
             * now check if we have failure notification marker file
             */
            final File file = new File(
                    NotificationMarkers.NOTIFICATIONS_MARKER_DIR
                            + FileTransferError.FILE_NOT_AVAILABLE.toString());
            logger.debug("Failure file exist [{}]", file.exists());
            Assert.assertTrue(file.exists());
        } catch (final InterruptedException ie) {
            Assert.fail(ie.getMessage());
        }
    }

    /**
     * Clean up ftp server and created files
     * 
     * @throws FtpException
     */
    @AfterClass
    public static void teardownEmbeddedFTPServer() throws FtpException {

        /**
         * delete "transfered file"
         */

        try {
            final File transferedFile = new File(TRANSFER_DIR
                    + TRANSFERED_FILENAME);
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
        } catch (final Exception ioe) {
            throw new FtpException(ioe);
        }
        /**
         * Stop embedded FTP server
         */
        server.stop();
    }
}
