package de.jpaw.bonaparte.noSQL.ohmw;

import java.util.Map;

import de.jpaw.bonaparte.noSQL.ohmw.impl.BonaPortableOffHeapConverter;
import de.jpaw.bonaparte.pojos.api.AbstractRef;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.refsw.RefResolver;
import de.jpaw.bonaparte.refsw.ReferencingComposer;
import de.jpaw.offHeap.Shard;

/** Marker interface, used by Reflections to find the entities, also defines the lifecycle methods to initialize and shut down an off heap map. */
public interface OffHeapEntity {
    /** Initializes an off heap map. Passes some shared composers for byte [] / object conversion. */
    public void open(Shard shard,                       // transaction management
            BonaPortableOffHeapConverter converter,     // data object converter
            ReferencingComposer composer,               // index composer
            Map<ClassDefinition, RefResolver<AbstractRef, ?, ?>> resolvers
            );
    
    /** Clears any allocated off heap memory. */
    public void close(); 
}
