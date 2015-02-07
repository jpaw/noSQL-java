package de.jpaw.bonaparte.noSQL.ohm.impl;

import de.jpaw.bonaparte.pojos.api.PersistenceProviders;
import de.jpaw.bonaparte.refs.PersistenceProvider;
import de.jpaw.offHeap.OffHeapTransaction;

public class PersistenceProviderOHM  implements PersistenceProvider {
    private final OffHeapTransaction myTransaction;
    
    /** invoked by factory method? */
    public PersistenceProviderOHM(OffHeapTransaction myTransaction) {
        this.myTransaction = myTransaction;
    }
    
    public OffHeapTransaction getTransaction() {
        return myTransaction;
    }
    
    @Override
    public String getId() {
        return PersistenceProviders.OFFHEAPMAP.name();
    }

    @Override
    public int getPriority() {
        return PersistenceProviders.OFFHEAPMAP.ordinal();
    }

    @Override
    public void open() {
        myTransaction.beginTransaction(171717L);        // TODO: FIXME: which processref to add?
    }
    @Override
    public void rollback() {
        myTransaction.rollback();
    }

    @Override
    public void commit() throws Exception {
        myTransaction.commit();
    }


    @Override
    public void close() {
//        if (myTransaction. != null) {
//            LOGGER.warn("attempt to close an open transaction, performing an implicit rollback");
//            transaction.rollback();
//            transaction = null;
//        }
        // close is actually a NOP because we reuse the transaction....  (TODO FIXME: clear buffers....)
    }
}
