package de.jpaw.bonaparte.noSQL.ohmw.impl;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.refsw.ReferencingComposer;
import de.jpaw.bonaparte.refsw.ReferencingParser;
import de.jpaw.collections.ByteArrayConverter;

/**
 * Converter between objects and byte []. One converter is created and shared across all in mem DB tables for the writing process (because the process is single
 * threaded anyway).
 */
public class BonaPortableOffHeapConverter implements ByteArrayConverter<BonaPortable> {
    private final ReferencingComposer composer;
    private final ReferencingParser parser;

    public BonaPortableOffHeapConverter(ReferencingComposer composer, ReferencingParser parser) {
        this.composer = composer;
        this.parser = parser;
    }

    @Override
    public BonaPortable byteArrayToValueType(byte[] data) {
        parser.setSource(data);
        parser.skipNext();
        try {
            return parser.readObject(DataWithTracking.meta$$this, DataWithTracking.class);
        } catch (MessageParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] valueTypeToByteArray(BonaPortable obj) {
        composer.reset();
        composer.addField(DataWithTracking.meta$$this, obj);
        return composer.getBuilder().getBytes();
    }

    @Override
    public byte[] getBuffer(BonaPortable arg0) {
        return composer.getBuilder().getCurrentBuffer();
    }

    @Override
    public int getLength() {
        return composer.getBuilder().length();
    }
}
