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

import java.util.List;

import com.ericsson.oss.itpf.datalayer.datapersistence.api.DataPersistenceException;
import com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject;
import com.ericsson.oss.itpf.datalayer.model.impl.AttributeInfo;
import com.ericsson.oss.itpf.datalayer.model.impl.ManagedObjectImpl;

public class MyMockImpl extends ManagedObjectImpl {

    /**
     * 
     */
    private static final long serialVersionUID = 1772160819468181222L;

    private final String fdn;

    public MyMockImpl(final String fdn) {

        super("MyMock", "MyMockType", null, null);
        this.fdn = fdn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.oss.itpf.datalayer.model.impl.ManagedObjectImpl#getEntityAddressInfo()
     */
    @Override
    public PersistentObject getEntityAddressInfo() {
        return new MyMockAddrInfo(fdn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.oss.itpf.datalayer.model.impl.ManagedObjectImpl#
     * validateChildSupportedByThisParent(java.lang.String)
     */
    @Override
    protected void validateChildSupportedByThisParent(final String childFDN)
            throws DataPersistenceException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.oss.itpf.datalayer.model.impl.ManagedObjectImpl#getSupportedChildren()
     */
    @Override
    protected List<String> getSupportedChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.oss.itpf.datalayer.model.impl.ManagedObjectImpl#getExpectedAttributeInfo()
     */
    @Override
    protected List<AttributeInfo> getExpectedAttributeInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
