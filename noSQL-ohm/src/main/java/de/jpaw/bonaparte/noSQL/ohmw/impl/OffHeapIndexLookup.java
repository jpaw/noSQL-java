package de.jpaw.bonaparte.noSQL.ohmw.impl;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.AbstractRef;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;
import de.jpaw.bonaparte.refs.PersistenceException;
import de.jpaw.bonaparte.refsw.RefResolver;
import de.jpaw.bonaparte.refsw.ReferencingComposer;
import de.jpaw.offHeap.PrimitiveLongKeyOffHeapIndexView;
import de.jpaw.util.ByteBuilder;

/**
 * Utility class to perform index lookups. An instance specific composer is stored, to avoid GC overhead. Lookup is potentially recursive. The life cycle for
 * the composer is a transaction, if the transaction is single treaded, it will be shared across all indexes.
 *
 * Not really working, most likely also not required.
 */
@Deprecated
public class OffHeapIndexLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(OffHeapIndexLookup.class);
    private final ReferencingComposer myComposer; // required in case of index serializations (which can be nested).

    public ReferencingComposer getMyComposer() {
        return myComposer;
    }

    private final ByteBuilder myBuffer;

    public ByteBuilder getMyBuffer() {
        return myBuffer;
    }


    protected int indexHash(int off) {
        int hash = 1;
        final byte [] buffer = myBuffer.getCurrentBuffer();
        while (off < myBuffer.length()) {
            hash = 31 * hash + buffer[off++];
        }
        return hash;
    }

    public OffHeapIndexLookup(Map<ClassDefinition, RefResolver<AbstractRef, ?, ?>> resolvers) {
        myBuffer = new ByteBuilder(220, StandardCharsets.UTF_8); // estimated max index size
        myComposer = new ReferencingComposer(myBuffer, resolvers);
    }

    /** Returns the key for the object referenced by indexValue. Null checks have been performed before. */
    public <I extends AbstractRef> long getKeyForIndex(I indexValue, PrimitiveLongKeyOffHeapIndexView<BonaPortable> indexTable, ObjectReference indexClass)
            throws PersistenceException {
        // serialize index to a buffer. remember the previous position for later restore, to allow for nested indexes.
        int currentWriterPos = myBuffer.length();

        // not working currently: must do object for recursion, but also must do tenantRef!   FIXME TODO
        myComposer.excludeObject(indexValue);
        myComposer.addField(indexClass, indexValue);
        // RequestContext ctx = Jdp.getRequired(RequestContext.class);
        int indexHash = indexHash(currentWriterPos);
        LOGGER.info("LOOKUP: HACKED Index hash is " + indexHash);

        // TODO: treat uncommitted changes? assuming data has been flushed to in mem DB already
        long key = indexTable.getUniqueKeyByIndex(myBuffer.getCurrentBuffer(), currentWriterPos, myBuffer.length() - currentWriterPos, indexHash);
        if (key <= 0)
            throw new PersistenceException(PersistenceException.NO_RECORD_FOR_INDEX, key, null, indexClass.get$PQON(), indexValue.toString());
        // restore position to previous state
        myBuffer.setLength(currentWriterPos);
        return key;
    }
}
