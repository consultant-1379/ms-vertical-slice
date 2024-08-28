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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.cache.Cache;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.datapersistence.api.DataPersistenceManager;
import com.ericsson.oss.itpf.datalayer.datapersistence.api.ManagedObject;
import com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject;
import com.ericsson.oss.itpf.sdk.cache.annotation.NamedCache;
import com.ericsson.oss.itpf.sdk.modeling.cache.CacheMode;
import com.ericsson.oss.itpf.sdk.modeling.cache.EvictionStrategy;
import com.ericsson.oss.itpf.sdk.modeling.cache.annotation.ModeledNamedCacheDefinition;

@Singleton
@Startup
@ModeledNamedCacheDefinition(name = "CONFIG_DIST", cacheMode = CacheMode.DISTRIBUTED_SYNC, transactional = true, evictionStrategy = EvictionStrategy.NONE, keyClass = String.class, valueClass = PersistentObject.class, description = "")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CacheManager {

    @Inject
    @NamedCache("CONFIG_DIST")
    public Cache<String, PersistentObject> cache;

    @Inject
    private Logger logger;

    @EJB
    private DataPersistenceManager manager;

    @PostConstruct
    public void setInitialData() {
        logger.debug("Infinispan cache manager loaded from jboss");
        createDefaultData();
    }

    /**
         * 
         */
    private void createDefaultData() {
        // Topology Root
        final String name = "ONRM_ROOT_MO";
        final String type = "NW";
        final Map<String, Object> initialAttributesr = new HashMap<String, Object>();
        initialAttributesr.put("nwId", name);

        final ManagedObject root = manager.createMO(name, type, null, initialAttributesr);

        // Resource group
        final String nameRg = "resourceGroup1";
        final String typeRg = "SN";
        final ManagedObject parentRg = root;
        final Map<String, Object> initialAttributesRg = new HashMap<>();
        initialAttributesRg.put("snId", "Group1");

        final ManagedObject rg = manager.createMO(nameRg, typeRg, parentRg, initialAttributesRg);

        // MEContext
        final String nameMc = "meContext1";
        final String typeMc = "MeC";
        final ManagedObject parentMc = rg;
        final Map<String, Object> initialAttributesMc = new HashMap<>();
        initialAttributesMc.put("mecId", "meContext1");

        final ManagedObject mc = manager.createMO(nameMc, typeMc, parentMc, initialAttributesMc);

        // ManagedElement
        final String nameMe = "1";
        final String typeMe = "ManagedElement";
        final ManagedObject parent = mc;
        final Map<String, Object> initialAttributesMe = new HashMap<>();
        initialAttributesMe.put("managedElementId", "managedElement1");
        initialAttributesMe.put("siteLocation", "Athlone");
        initialAttributesMe.put("userLabel", "managedElementUserLabel");
        initialAttributesMe.put("dnPrefix", "tor");
        initialAttributesMe.put("nodeVersion", "14A");

        final ManagedObject me = manager.createMO(nameMe, typeMe, parent, initialAttributesMe);

        // PmAccess
        final Map<String, Object> pmAtt = new HashMap<String, Object>();
        pmAtt.put("pmAccessId", "pmAccess1");
        pmAtt.put("datapathId", "com.ericsson.nms.mediation.singleFileTransfer");

        manager.createMO("1", "PmAccess", me, pmAtt);

        // EntityAddressInfo
        final Map<String, Object> eaiAttributes = new HashMap<String, Object>();
        eaiAttributes.put("nodeSecurityState", "OFF");
        final PersistentObject entityAddressInfo = manager.createPO("EntityAddressInfo", eaiAttributes);

        // FtpProtocolInfo
        final Map<String, Object> initialAttributes = new HashMap<>();
        initialAttributes.put("ipAddress", "127.0.0.1");
        initialAttributes.put("port", 2221);
        initialAttributes.put("name", "PM");
        initialAttributes.put("protocol", "FTP");
        initialAttributes.put("ftpType", "FTP");
        initialAttributes.put("ossFdn", "FDN1");
        initialAttributes.put("nodeSecurityState", "OFF");

        final PersistentObject ftpInfo = manager.createPO("FtpProtocolInfo", initialAttributes);

        final Collection<PersistentObject> protocolInfos = new LinkedList<>();
        protocolInfos.add(ftpInfo);
        entityAddressInfo.addAssociation("EntityAddressInfo_to_Protocol", protocolInfos);

        ManagedObject freshMe = manager.findMO(me.getFDN());
        freshMe.setEntityAddressInfo(manager.find(entityAddressInfo.getId()));

        // SFTP element

        // MEContext
        final String nameMc2 = "meContext2";
        final Map<String, Object> initialAttributesMc2 = new HashMap<>();
        initialAttributesMc2.put("mecId", "meContext2");

        final ManagedObject mc2 = manager.createMO(nameMc2, typeMc, parentMc, initialAttributesMc2);

        // ManagedElement
        final String nameMe2 = "1";
        final ManagedObject parent2 = mc2;

        final ManagedObject me2 = manager.createMO(nameMe2, typeMe, parent2, initialAttributesMe);

        // PmAccess
        final Map<String, Object> pmAtt2 = new HashMap<String, Object>();
        pmAtt2.put("pmAccessId", "pmAccess1");
        pmAtt2.put("datapathId", "com.ericsson.nms.mediation.singleFileTransfer");

        manager.createMO("1", "PmAccess", me2, pmAtt2);

        // EntityAddressInfo
        eaiAttributes.clear();
        eaiAttributes.put("nodeSecurityState", "ON");
        final PersistentObject entityAddressInfo2 = manager.createPO("EntityAddressInfo", eaiAttributes);

        // SftpProtocolInfo
        final Map<String, Object> initialAttributes2 = new HashMap<>();
        initialAttributes2.put("ipAddress", "127.0.0.1");
        initialAttributes2.put("port", 11009);
        initialAttributes2.put("name", "PM");
        initialAttributes2.put("protocol", "FTP");
        initialAttributes2.put("ftpType", "SFTP");
        initialAttributes2.put("ossFdn", "FDN2");
        initialAttributes2.put("ftpPort", 2221);
        initialAttributes2.put("sftpPort", 11009);
        initialAttributes.put("nodeSecurityState", "ON");

        final PersistentObject ftpInfo2 = manager.createPO("SftpProtocolInfo", initialAttributes2);

        final Collection<PersistentObject> protocolInfos2 = new LinkedList<>();
        protocolInfos2.add(ftpInfo2);
        entityAddressInfo2.addAssociation("EntityAddressInfo_to_Protocol", protocolInfos2);

        ManagedObject freshMe2 = manager.findMO(me2.getFDN());
        freshMe2.setEntityAddressInfo(manager.find(entityAddressInfo2.getId()));

        final String nameMc2sftp = "meContext3";
        final Map<String, Object> initialAttributesMc2sftp = new HashMap<>();
        initialAttributesMc2sftp.put("mecId", "meContext3");

        final ManagedObject mc2sftp = manager.createMO(nameMc2sftp, typeMc, parentMc, initialAttributesMc2sftp);

        final String nameMe2sftp = "1";
        final ManagedObject parent6 = mc2sftp;

        final ManagedObject me2sftp = manager.createMO(nameMe2sftp, typeMe, parent6, initialAttributesMe);

        final Map<String, Object> pmAtt2sftp = new HashMap<String, Object>();
        pmAtt2sftp.put("pmAccessId", "pmAccess3");
        pmAtt2sftp.put("datapathId", "com.ericsson.nms.mediation.LinkedDataPath");

        manager.createMO("1", "PmAccess", me2sftp, pmAtt2sftp);

        ManagedObject freshMe3 = manager.findMO(me2sftp.getFDN());
        freshMe3.setEntityAddressInfo(manager.find(entityAddressInfo2.getId()));
        logger.debug("***************DataPersistance is filled with initial data**********");

    }

    @PreDestroy
    public void clearCache() {
        logger.debug("Predestroy cache cleanup");
        cache.removeAll();
    }
}
