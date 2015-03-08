package de.jpaw.bonaparte.noSQL.ohm.refsp.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.noSQL.ohm.impl.PersistenceProviderOHM;
import de.jpaw.bonaparte.noSQL.ohmp.OffHeapEntity;
import de.jpaw.bonaparte.noSQL.ohmp.impl.BonaPortableOffHeapConverter;
import de.jpaw.bonaparte.pojos.api.AbstractRef;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.refsp.RefResolver;
import de.jpaw.bonaparte.refsp.ReferencingComposer;
import de.jpaw.bonaparte.refsp.ReferencingParser;
import de.jpaw.dp.Jdp;
import de.jpaw.offHeap.OffHeapTransaction;
import de.jpaw.offHeap.Shard;
import de.jpaw.util.ByteBuilder;
import de.jpaw.xenums.init.ReflectionsPackageCache;

public class OffHeapMapEntityCollector implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(OffHeapMapEntityCollector.class);
    private final Shard myShard;
    private final OffHeapTransaction myTransaction;
    private final List<OffHeapEntity> tables;
    private final ReferencingComposer myComposer;
    private final BonaPortableOffHeapConverter converter;
    private final Map<ClassDefinition, RefResolver<AbstractRef, ?, ?>> resolvers;

    // collects all relevant entities from the provided packages
    public OffHeapMapEntityCollector(List<String> packagesToScan) {
        // create a transaction shard
        myTransaction = new OffHeapTransaction(OffHeapTransaction.TRANSACTIONAL);
        myShard = new Shard();
        myShard.setOwningTransaction(myTransaction);
        tables = new ArrayList<OffHeapEntity>(100);

        // must create the provider before installing any of the IInMemDBs
        PersistenceProviderOHM ohmContext = new PersistenceProviderOHM(myTransaction);  // sets the singleton
        Jdp.registerWithCustomProvider(PersistenceProviderOHM.class, new PersistenceProviderOHMProvider(ohmContext));

        resolvers = new ConcurrentHashMap<ClassDefinition, RefResolver<AbstractRef,?,?>>(100);
        ByteBuilder myBuffer = new ByteBuilder(220, StandardCharsets.UTF_8); // estimated max index size
        myComposer = new ReferencingComposer(myBuffer, resolvers);
        // build a common converter
        converter = new BonaPortableOffHeapConverter(myComposer, new ReferencingParser(null, 0, 0, resolvers, false));

        for (String packageName : packagesToScan) {
            for (Class<? extends OffHeapEntity> cls : ReflectionsPackageCache.get(packageName).getSubTypesOf(OffHeapEntity.class)) {
                try {
                    OffHeapEntity db = Jdp.getRequired(cls);
                    db.open(myShard, converter, myComposer, resolvers);
                    tables.add(db);
                } catch (Exception e) {
                    LOGGER.warn("Cannot initialize in memory DB table {}: {}", cls.getCanonicalName(), e.getMessage());
                }
            }
        }
    }

    /** Returns the number of entities found. */
    public int size() {
        return tables.size();
    }

    @Override
    public void close() {
        for (OffHeapEntity db : tables)
            db.close();
    }
}
