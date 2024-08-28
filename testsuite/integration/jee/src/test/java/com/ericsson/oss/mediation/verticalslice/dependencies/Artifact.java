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

package com.ericsson.oss.mediation.verticalslice.dependencies;

import java.io.*;
import java.util.*;

import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.application.ApplicationDescriptor;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

/**
 * <b>Dependencies and artifacts used in VerticalSliceTest</b> </br>This class
 * contains all dependencies used in this test listed as constants for easier
 * use. Dependencies should be added in sections, for service framework, this
 * project artifacts etc...</br> When adding dependencies here, make sure same
 * are added into pom file as well. </br><b>It is forbidden to put versions of
 * dependencies here all versions need to be defined in pom files, this list
 * here should never contain dependency with version.<b>
 * 
 * </br></br>In order to be able to maintain this file easily, please add only
 * required dependencies, and make sure they are used. If any dependency is not
 * used any more, make sure it is removed from this file and pom file.
 * 
 * @author edejket
 * 
 */
public class Artifact {

    /**
     * Service Framework artifacts needed for this test
     */

    /**
     * MediationCore artifacts needed for this test
     */
    public static final String COM_ERICSSON_OSS_MEDIATION_MEDIATIONCORE_API_JAR = "com.ericsson.nms.mediation:mediation-core-api:jar";
    public static final String COM_ERICSSON_OSS_MEDIATION_MEDIATION_SERVICE_LOCATOR_API = "com.ericsson.nms.mediation:mediation-service-locator-api:jar";
    public static final String COM_ERICSSON_OSS_MEDIATION_MEDIATIONCORE_EAR = "com.ericsson.nms.mediation:mediation-core-ear:ear";

    /**
     * MediationService artifacts needed for this test
     */
    public static final String COM_ERICSSON_NMS_MEDIATION_MEDIATIONSERVICE_EAR = "com.ericsson.nms.mediation:mediation-service-ear:ear";
    public static final String COM_ERICSSON_NMS_CAMEL_ENGINE_RAR = "com.ericsson.nms.mediation:camel-engine-jca-rar:rar";
    /**
     * DPS artifacts needed for this test
     */
    public static final String COM_ERICSSON_NMS_MEDIATION_DPS_API_JAR = "com.ericsson.oss.itpf.datalayer:DataPersistence-api:jar";
    public static final String COM_ERICSSON_NMS_MEDIATION_DPS_MODELS_JAR = "com.ericsson.oss.itpf.datalayer:DataPersistence-jar:jar";
    public static final String COM_ERICSSON_OSS_DATAPERSISTENCE_EAR = "com.ericsson.oss.itpf.datalayer:DataPersistence-ear:ear";

    /**
     * PMService artifacts needed for this test
     */
    public static final String COM_ERICSSON_NMS_SERVICES_PMSERVICE_EAR = "com.ericsson.nms.services:pmservice-ear:ear";

    /**
     * AccessControl/TSS artifacts needed for test
     */
    public static final String COM_ERICSSON_NMS_MEDIATION_TSS_SERVICE_API = "com.ericsson.nms.mediation:tss-service-api:jar";
    public static final String COM_ERICSSON_NMS_MEDIATION_TSS_SERVICE_COMMON = "com.ericsson.nms.mediation:tss-service-common:jar";

    /**
     * Apache embedded ftp server and jsh sftp server
     */
    public static final String APACHE_EMBD_FTPSERVER = "org.apache.ftpserver:ftpserver-core:jar";
    public static final String APACHE_EMBD_SFTPSERVER = "org.apache.sshd:sshd-core:jar";

    /**
     * Model Service
     */
    public static final String COM_ERICSSON_MODEL_SERVICE = "com.ericsson.oss.itpf.datalayer:model-service-ear:ear";

    /* ------------------------Utility methods ----------------------------- */

    /**
     * Resolve artifact with given coordinates without any dependencies, this
     * method should be used to resolve just the artifact with given name, and
     * it can be used for adding artifacts as modules into EAR
     * 
     * If artifact can not be resolved, or the artifact was resolved into more
     * then one file then the IllegalStateException will be thrown
     * 
     * 
     * @param artifactCoordinates
     *            in usual maven format
     * 
     *            <pre>
     * {@code<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}
     * </pre>
     * @return File representing resolved artifact
     */
    public static File resolveArtifactWithoutDependencies(
            final String artifactCoordinates) {
        final File[] artifacts = getMavenResolver()
                .artifact(artifactCoordinates).exclusion("*").resolveAsFiles();
        if (artifacts == null) {
            throw new IllegalStateException("Artifact with coordinates "
                    + artifactCoordinates + " was not resolved");
        }

        if (artifacts.length != 1) {
            throw new IllegalStateException(
                    "Resolved more then one artifact with coordinates "
                            + artifactCoordinates);
        }
        return artifacts[0];
    }

    /**
     * Resolve dependencies for artifact with given coordinates, if artifact can
     * not be resolved IllegalState exception will be thrown
     * 
     * @param artifactCoordinates
     *            in usual maven format
     * 
     *            <pre>
     * {@code<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}
     * </pre>
     * 
     * @return resolved dependencies
     */
    public static File[] resolveArtifactDependencies(
            final String artifactCoordinates) {
        File[] artifacts = getMavenResolver().artifact(artifactCoordinates)
                .scope("compile").exclusion(artifactCoordinates)
                .resolveAsFiles();

        if (artifacts == null) {
            throw new IllegalStateException(
                    "No dependencies resolved for artifact with coordinates "
                            + artifactCoordinates);
        }

        if (artifacts.length > 0) {
            // find artifact that has given coordinates
            final File artifact = resolveArtifactWithoutDependencies(artifactCoordinates);
            final List<File> filteredDeps = new ArrayList<File>(
                    Arrays.asList(artifacts));
            // remove it from the list
            filteredDeps.remove(artifact);
            artifacts = new File[0];
            // return the resolved list with only dependencies
            return filteredDeps.toArray(artifacts);
        } else {
            return artifacts;
        }

    }

    /**
     * Utility method giving access to maven dependency resolver, allowing
     * dependency resolving with transitive dependency taken into account
     * 
     * @return MavenDependencyResolver built on local pom file
     */
    public static MavenDependencyResolver getMavenResolver() {
        return DependencyResolvers.use(MavenDependencyResolver.class)
                .loadMetadataFromPom("pom.xml");
    }

    public static void createCustomApplicationXmlFile(
            final EnterpriseArchive serviceEar, final String webModuleName) {

        final Node node = serviceEar.get("META-INF/application.xml");
        ApplicationDescriptor desc = Descriptors.importAs(
                ApplicationDescriptor.class).fromStream(
                node.getAsset().openStream());

        desc.webModule(webModuleName + ".war", webModuleName);
        final String descriptorAsString = desc.exportAsString();

        serviceEar.delete(node.getPath());
        desc = Descriptors.importAs(ApplicationDescriptor.class).fromString(
                descriptorAsString);

        final Asset asset = new Asset() {
            @Override
            public InputStream openStream() {
                final ByteArrayInputStream bi = new ByteArrayInputStream(
                        descriptorAsString.getBytes());
                return bi;
            }
        };
        serviceEar.addAsManifestResource(asset, "application.xml");
    }
}
