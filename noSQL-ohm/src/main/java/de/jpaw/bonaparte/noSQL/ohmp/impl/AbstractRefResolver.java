package de.jpaw.bonaparte.noSQL.ohmp.impl;

import net.openhft.koloboke.collect.map.hash.HashLongObjMap;
import net.openhft.koloboke.collect.map.hash.HashLongObjMaps;
import de.jpaw.bonaparte.core.ObjectValidationException;
import de.jpaw.bonaparte.pojos.api.AbstractRef;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.refs.PersistenceException;
import de.jpaw.bonaparte.refsp.RefResolver;
import de.jpaw.util.ByteBuilder;

//TODO FIXME:   check nochange columns in update method

/**
 * An abstract class which implements the common functionality of a RefResolver for off heap key value stores. The topics are:
 * 
 * The first topic is operation of a first level cache (on heap) for data objects. Similar to the JPA entity manager, its task is to provide a unique identity
 * for subsequent queries to the same object within a single transaction. It also improves read performance when the data object is of significant size, because
 * no repeated deserializations have to be done. No caching is performed on index values, because the index is assumed to be small and the overhead of cache
 * operation may be higher than actual updates or lookups itself.
 * 
 * The second aspect is the maintenance of change tracking fields for audit purposes. The tracking fields are available in read/write mode to the application,
 * most operations work on the business fields only (DTO). Tracking data is provided upon request, and in that case, a read-only copy is created and handed
 * back.
 * 
 * @author Michael Bischoff
 *
 * @param <REF>
 * @param <DTO>
 * @param <TRACKING>
 */
public abstract class AbstractRefResolver<REF extends AbstractRef, DTO extends REF, TRACKING extends TrackingBase> implements RefResolver<REF, DTO, TRACKING> {
    private HashLongObjMap<DataWithTracking<DTO, TRACKING>> cache = HashLongObjMaps.newMutableMap(1024 * 1024);

    protected ByteBuilder builder;
    protected String entityName;

    protected int indexHash(int off) {
        int hash = 1;
        final byte[] buffer = builder.getCurrentBuffer();
        while (off < builder.length()) {
            hash = 31 * hash + buffer[off++];
        }
        return hash;
    }

    /** Look up a primary key by some unique index. */
    protected abstract long getUncachedKey(REF refObject) throws PersistenceException;

    /** Return an object stored in the DB by its primary key. */
    protected abstract DataWithTracking<DTO, TRACKING> getUncached(long ref);

    /** Update some object fwt to have obj as the data portion. (Update tracking and then update the DB and possibly indexes.) */
    protected abstract void uncachedUpdate(DataWithTracking<DTO, TRACKING> dwt, DTO obj) throws PersistenceException;

    /** Removes an object if it exists. */
    protected abstract void uncachedRemove(DataWithTracking<DTO, TRACKING> previous);

    /** Create some object. Returns the object including tracking data, or throws an exception, if the object already exists. */
    protected abstract DataWithTracking<DTO, TRACKING> uncachedCreate(DTO obj) throws PersistenceException;

    @Override
    public final long getRef(REF refObject) throws PersistenceException {
        if (refObject == null)
            return 0;
        long key = refObject.get$RefP();
        if (key > 0)
            return key;
        // shortcuts not possible, try the local reverse cache
        // key = indexCache.get(refObject);
        // if (key > 0)
        // return key;
        // not in cache either, consult second level (in-memory DB)
        key = getUncachedKey(refObject);
        // if (key > 0)
        // indexCache.put(refObject, key);
        return key;
    }

    protected final DataWithTracking<DTO, TRACKING> getDTONoCacheUpd(long ref) {
        // first, try to retrieve a value from the cache, in order to be identity-safe
        DataWithTracking<DTO, TRACKING> value = cache.get(ref);
        if (value != null)
            return value;
        // not here, consult second level (in-memory DB)
        value = getUncached(ref);
        return value;
    }

    @Override
    public final DTO getDTO(REF refObject) throws PersistenceException {
        if (refObject == null)
            return null;
        return getDTO(getRef(refObject));
    }

    @Override
    public final DTO getDTO(long ref) {
        if (ref <= 0L)
            return null;
        DataWithTracking<DTO, TRACKING> value = getDTONoCacheUpd(ref);
        if (value != null) {
            cache.put(ref, value);
            return value.getData();
        }
        return null;
    }

    @Override
    public final void update(DTO obj) throws PersistenceException {
        long key = obj.get$RefP();
        if (key <= 0)
            throw new PersistenceException(PersistenceException.NO_PRIMARY_KEY, 0L, entityName);
        DataWithTracking<DTO, TRACKING> dwt = getDTONoCacheUpd(key);
        if (dwt == null)
            throw new PersistenceException(PersistenceException.RECORD_DOES_NOT_EXIST, key, entityName);
        uncachedUpdate(dwt, obj);
        // it's already in the cache, and the umbrella object hasn't changed, so no cache update required
    }

    @Override
    public final void remove(long key) throws PersistenceException {
        DataWithTracking<DTO, TRACKING> value = getDTONoCacheUpd(key);
        if (value != null) {
            // must remove it
            cache.remove(key);
            uncachedRemove(value);
        }
    }

    @Override
    public void create(DTO obj) throws PersistenceException {
        if (obj.get$RefP() <= 0)
            throw new PersistenceException(PersistenceException.NO_PRIMARY_KEY, 0L, entityName);
        DataWithTracking<DTO, TRACKING> dwt = uncachedCreate(obj);
        cache.put(obj.get$RefP(), dwt);
    }

    @Override
    public TRACKING getTracking(long ref) throws PersistenceException {
        DataWithTracking<DTO, TRACKING> dwt = getDTONoCacheUpd(ref);
        if (dwt == null)
            throw new PersistenceException(PersistenceException.RECORD_DOES_NOT_EXIST, ref, entityName);
        try {
            return (TRACKING) dwt.getTracking().get$FrozenClone();
        } catch (ObjectValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void clear() {
        cache.clear();
    }
    
//    @Override
//    public List<Long> queryKeys(int limit, int offset, List<SearchFilter> filters, List<SortColumn> sortColumns) throws ApplicationException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public List<DataWithTracking<DTO, TRACKING>> query(int limit, int offset, List<SearchFilter> filters, List<SortColumn> sortColumns)
//            throws ApplicationException {
//        throw new UnsupportedOperationException();
//    }
}
