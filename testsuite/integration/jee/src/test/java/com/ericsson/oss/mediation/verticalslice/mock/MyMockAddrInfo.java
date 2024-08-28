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

import java.util.*;

import com.ericsson.oss.itpf.datalayer.datapersistence.api.DataPersistenceException;
import com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject;
import com.ericsson.oss.itpf.datalayer.model.po.FtpProtocolInfo;

public class MyMockAddrInfo implements PersistentObject {

    private final String fdn;

    public MyMockAddrInfo(final String fdn) {
        this.fdn = fdn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#getId()
     */
    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#getType()
     */
    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#getAssociation(java.lang.String
     * )
     */
    @Override
    public Collection<PersistentObject> getAssociation(
            final String associationName) {
        final Map<String, Object> initialAttr = new HashMap<String, Object>();
        initialAttr.put("name", "PM");
        initialAttr.put("protocol", "FTP");
        initialAttr.put("ipAddress", "localhost");

        if ("FDN1".equals(fdn) || "FDN3".equals(fdn)) {
            initialAttr.put("ftpType", "FTP");
            initialAttr.put("port", "2221");
        } else if ("FDN2".equals(fdn)) {
            initialAttr.put("ftpType", "SFTP");
            initialAttr.put("port", "11009");
        }
        final FtpProtocolInfo protInfo = new FtpProtocolInfo("FTP", initialAttr);
        final Collection<PersistentObject> col = new ArrayList<>();
        col.add(protInfo);
        return col;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#getAssociationNames()
     */
    @Override
    public Collection<String> getAssociationNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#addAssociation(java.lang.String
     * , com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject)
     */
    @Override
    public void addAssociation(final String assocName,
            final PersistentObject assocObject) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#setEntityAddressInfo(com.ericsson
     * .oss.data.api.PersistentObject)
     */
    @Override
    public void setEntityAddressInfo(final PersistentObject persistentObject) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#getEntityAddressInfo()
     */
    @Override
    public PersistentObject getEntityAddressInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#getAttribute(java.lang.String)
     */
    @Override
    public <T> T getAttribute(final String name)
            throws DataPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#getAttributes()
     */
    @Override
    public Map<String, Object> getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#addAssociation(java.lang.String
     * , java.util.Collection)
     */
    @Override
    public void addAssociation(final String name,
            final Collection<PersistentObject> objectCollection) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.oss.itpf.datalayer.api.PersistentObject#setAttributes(java
     * .util.Map)
     */
    @Override
    public void setAttributes(final Map<String, Object> arg0)
            throws DataPersistenceException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.oss.itpf.datalayer.api.PersistentObject#setAttribute(java
     * .lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(final String arg0, final Object arg1) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject#
     * removeAssociation(java.lang.String,
     * com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject)
     */
    @Override
    public void removeAssociation(String arg0, PersistentObject arg1) {
        // TODO Auto-generated method stub

    }

}