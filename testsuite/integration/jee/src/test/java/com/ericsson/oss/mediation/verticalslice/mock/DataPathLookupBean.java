/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.mediation.verticalslice.mock;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.ejb.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.datapersistence.api.DataPersistenceManager;
import com.ericsson.oss.itpf.datalayer.datapersistence.api.ManagedObject;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.itpf.sdk.resources.Resources;

/**
 * Mock data path lookup bean for use with mediation service integration tets.
 * Datapath location is hardcoded.
 * 
 * @author etonayr
 * 
 */
@Startup
@Singleton
public class DataPathLookupBean {

    private Resource resource;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DataPathLookupBean.class.getName());

    @EJB
    DataPersistenceManager manager = null;

    private Properties properties;

    @PostConstruct
    public void startUp() {
        readPropertiesFile();
        createDataPathLookupInCache();
    }

    public void readPropertiesFile() {
        this.resource = Resources
                .getFileSystemResource("src/test/app_properties/models/datapath.properties");

        if (resource.exists()) {
            properties = new Properties();
            try {
                properties.load(resource.getInputStream());
            } catch (final IOException e) {
                LOGGER.error("Could not read properties from resource file {}",
                        e);
            }
        }

    }

    public void createDataPathLookupInCache() {
        final Map<String, Object> mapFormat = new HashMap<String, Object>();

        final Set<Entry<Object, Object>> props = properties.entrySet();
        for (final Entry<Object, Object> entry : props) {
            mapFormat.put((String) entry.getKey(), entry.getValue());
        }

        final ManagedObject mo = manager.createMO("1", "DataPathLookup", null,
                mapFormat);
        LOGGER.info("Created " + mo.getFDN());
    }

}
