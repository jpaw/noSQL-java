package de.jpaw.bonaparte.noSQL.ohm.impl;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.collections.ByteArrayConverter;
import de.jpaw.offHeap.PrimitiveLongKeyOffHeapMap;
import de.jpaw.offHeap.Shard;

/** Concrete superclass of the Offheap map which exposes the builder, accepting a specific converter. */
public class OffHeapBonaPortableMap extends PrimitiveLongKeyOffHeapMap<BonaPortable> {

    protected OffHeapBonaPortableMap(ByteArrayConverter<BonaPortable> converter, int size, Shard forShard, int modes, boolean withCommittedView, String name) {
        super(converter, size, forShard, modes, withCommittedView, name);
    }

    public static class Builder extends PrimitiveLongKeyOffHeapMap.Builder<BonaPortable, OffHeapBonaPortableMap> {

        public Builder(ByteArrayConverter<BonaPortable> converter) {
            super(converter);
        }
        @Override
        public OffHeapBonaPortableMap build() {
            return new OffHeapBonaPortableMap(converter, hashSize, shard, mode, withCommittedView, name);
        }
    }
}
