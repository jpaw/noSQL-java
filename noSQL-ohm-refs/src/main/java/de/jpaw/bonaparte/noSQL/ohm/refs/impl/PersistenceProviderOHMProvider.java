package de.jpaw.bonaparte.noSQL.ohm.refs.impl;

import de.jpaw.bonaparte.noSQL.ohm.impl.PersistenceProviderOHM;
import de.jpaw.bonaparte.pojos.api.PersistenceProviders;
import de.jpaw.bonaparte.refs.RequestContext;
import de.jpaw.dp.CustomScope;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;


/**
 * The provider for the offHeap in memory persistence context.
 * This implementation hooks into the RequestContext (which could be passed across threads) and checks for an existing OHM provider.
 * If none exists, it creates a new one and registers it.
 */
public class PersistenceProviderOHMProvider implements CustomScope<PersistenceProviderOHM> {
    private final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);
    
    private final PersistenceProviderOHM singleton;
    
    public PersistenceProviderOHMProvider(PersistenceProviderOHM instance) {
        this.singleton = instance;
    }
    
    @Override
    public PersistenceProviderOHM get() {
        RequestContext ctx = ctxProvider.get();
        PersistenceProviderOHM ohmContext = (PersistenceProviderOHM) ctx.getPersistenceProvider(PersistenceProviders.OFFHEAPMAP.ordinal());
        if (ohmContext == null) {
            // does not exist, register it now!
            ctx.addPersistenceContext(singleton);
        }
        return singleton;
    }

    @Override
    public void set(PersistenceProviderOHM instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
}
