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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import javax.cache.Cache;
import javax.ejb.*;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.datapersistence.api.*;
import com.ericsson.oss.itpf.sdk.cache.annotation.NamedCache;

@Stateless
@Remote(DataPersistenceManager.class)
public class DataPersistenceManagerBean implements DataPersistenceManager {

    public static final String MO_PACKAGE = "com.ericsson.oss.itpf.datalayer.model.mo.";
    public static final String PO_PACKAGE = "com.ericsson.oss.itpf.datalayer.model.po.";

    @Inject
    @NamedCache("CONFIG_DIST")
    private Cache<String, PersistentObject> cache;

    @Override
    public ManagedObject findMO(final String fdn) {
        final PersistentObject possibleMO = find(fdn);
        if (isMO(possibleMO)) {
            return (ManagedObject) possibleMO;
        } else {
            throw new DataPersistenceException("Object stored with ID " + fdn
                    + " is not a Managed Object");
        }
    }

    private boolean isMO(final PersistentObject possibleMO) {
        return possibleMO == null || possibleMO instanceof ManagedObject;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public ManagedObject createMO(final String name, final String type,
            ManagedObject parent, final Map<String, Object> initialAttributes)
            throws DataPersistenceException {

        ManagedObject mo = null;
        try {
            final ClassLoader loader = Thread.currentThread()
                    .getContextClassLoader();
            final Class<?> clazz = loader.loadClass(MO_PACKAGE + type);
            final Constructor<?> constructor = clazz.getConstructor(
                    String.class, String.class, ManagedObject.class, Map.class);
            if (parent != null) {
                parent = (ManagedObject) cache.get(parent.getFDN());
            }
            mo = (ManagedObject) constructor.newInstance(name, type, parent,
                    initialAttributes);
            cache.put(mo.getFDN(), mo);
            if (parent != null) {
                updateParentObject(parent);
            }

        } catch (final ClassNotFoundException e) {
            throw new DataPersistenceException("The supplied type (" + type
                    + ") is not valid");
        } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new DataPersistenceException(e);
        }
        return mo;

    }

    private void updateParentObject(final ManagedObject parent) {
        cache.put(parent.getFDN(), parent);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public PersistentObject createPO(final String type,
            final Map<String, Object> initialAttributes)
            throws DataPersistenceException {
        PersistentObject po = null;
        try {
            final ClassLoader loader = Thread.currentThread()
                    .getContextClassLoader();
            final Class<?> clazz = loader.loadClass(PO_PACKAGE + type);
            final Constructor<?> constructor = clazz.getConstructor(
                    String.class, Map.class);

            po = (PersistentObject) constructor.newInstance(type,
                    initialAttributes);

        } catch (final ClassNotFoundException e) {
            throw new DataPersistenceException("The supplied type (" + type
                    + ") is not valid");
        } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new DataPersistenceException(e);
        }
        cache.put(po.getId(), po);
        return po;
    }

    @Override
    public PersistentObject find(final String id) {
        if (id == null) {
            return null;
        }
        return cache.get(id);
    }

    @Override
    public void persist(final PersistentObject persistentObject) {
        cache.put(persistentObject.getId(), persistentObject);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.oss.itpf.datalayer.datapersistence.api.DataPersistenceManager#findByType(java.lang
     * .String )
     */
    @Override
    public Collection<PersistentObject> findByType(final String arg0)
            throws DataPersistenceException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.oss.itpf.datalayer.datapersistence.api.DataPersistenceManager
     * #deletePO
     * (com.ericsson.oss.itpf.datalayer.datapersistence.api.PersistentObject)
     */
    @Override
    public void deletePO(PersistentObject arg0) throws DataPersistenceException {

    }

}