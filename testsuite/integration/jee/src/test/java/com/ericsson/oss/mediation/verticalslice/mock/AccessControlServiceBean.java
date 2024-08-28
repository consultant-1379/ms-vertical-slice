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

import javax.ejb.Remote;
import javax.ejb.Stateless;

import com.ericsson.oss.mediation.tss.NodeAuthInfo;
import com.ericsson.oss.mediation.tss.exception.AccessControlEntryException;
import com.ericsson.oss.mediation.tss.exception.AccessControlUnavailableException;
import com.ericsson.oss.mediation.tss.service.api.AccessControlService;
import com.ericsson.oss.mediation.tss.service.api.enums.NodeSecurityType;

@Stateless
@Remote(AccessControlService.class)
public class AccessControlServiceBean implements AccessControlService {

    /**
     * 
     */
    private static final long serialVersionUID = 3343967846745991411L;

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.oss.mediation.tss.service.api.AccessControlService#
     * getAuthenticationInfo(java.lang.String,
     * com.ericsson.oss.mediation.tss.service.api.enums.NodeSecurityType)
     */
    @Override
    public NodeAuthInfo getAuthenticationInfo(final String nodeName,
            final NodeSecurityType nodeType)
            throws AccessControlEntryException,
            AccessControlUnavailableException {

        if ("meContext1".equals(nodeName)) {
            return new NodeAuthInfo("ftpuser", "guestp");
        }
        if ("meContext2".equals(nodeName)) {
            return new NodeAuthInfo("sftpuser", "guestp");
        }
        if ("meContext3".equals(nodeName)) {
            return new NodeAuthInfo("sftpuser", "invalidPassword");
        }
        return null;
    }

}
