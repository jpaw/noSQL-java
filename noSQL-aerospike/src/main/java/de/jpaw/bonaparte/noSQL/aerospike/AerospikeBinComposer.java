package de.jpaw.bonaparte.noSQL.aerospike;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import com.aerospike.client.Bin;

import de.jpaw.bonaparte.core.BonaCustom;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.core.MessageComposer;
import de.jpaw.bonaparte.core.StaticMeta;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.BasicNumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.BinaryElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.EnumDataItem;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.MiscElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.NumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;
import de.jpaw.bonaparte.pojos.meta.TemporalElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.XEnumDataItem;
import de.jpaw.bonaparte.util.DayTime;
import de.jpaw.enums.TokenizableEnum;
import de.jpaw.enums.XEnum;
import de.jpaw.util.ByteArray;

/** Composer translates types as possible in Bonaparte into the valid types supported by Aerospike, namely String, Long and byte []. */
public class AerospikeBinComposer implements MessageComposer<RuntimeException> {
    private final int INITIAL_BUFFER_SIZE = 4000;           // initial buffer size for object serialization
    private final Long ZERO = Long.valueOf(0);
    private final Long ONE = Long.valueOf(1);
    private final boolean writeBooleanAsString = false;     // compile time preference, currently
    private final boolean writeNulls;
    private final List<Bin> bins;
    private CompactByteArrayComposer objectComposer = null;  // will be created on demand
    
    public AerospikeBinComposer(List<Bin> bins, boolean writeNulls) {
        this.bins = bins;
        this.writeNulls = writeNulls;
    }
    public AerospikeBinComposer(int maxBins, boolean writeNulls) {
        this.bins = new ArrayList<Bin>(maxBins);
        this.writeNulls = writeNulls;
    }
    
    public void reset() {
        bins.clear();
    }
    
    public Bin [] toArray() {
        return bins.toArray(new Bin[bins.size()]);
    }
    
    
    private void addIt(FieldDefinition di, long numeric) {
        bins.add(new Bin(di.getName(), Long.valueOf(numeric)));
    }
    private void addIt(FieldDefinition di, String s) {
        bins.add(new Bin(di.getName(), s));
    }
    private void addIt(FieldDefinition di, byte [] b) {
        bins.add(new Bin(di.getName(), b));
    }
    
    @Override
    public void writeNull(FieldDefinition di) throws RuntimeException {
        if (writeNulls)
            bins.add(Bin.asNull(di.getName()));
    }

    @Override
    public void writeNullCollection(FieldDefinition di) throws RuntimeException {
    }

    @Override
    public void startTransmission() throws RuntimeException {
    }

    @Override
    public void startRecord() throws RuntimeException {
    }

    @Override
    public void startArray(FieldDefinition di, int currentMembers, int sizeOfElement) throws RuntimeException {
        throw new UnsupportedOperationException("Cannot write arrays into Aerospike currently");
    }

    @Override
    public void startMap(FieldDefinition di, int currentMembers) throws RuntimeException {
        throw new UnsupportedOperationException("Cannot write maps into Aerospike currently");
    }

    @Override
    public void writeSuperclassSeparator() throws RuntimeException {
    }

    @Override
    public void terminateMap() throws RuntimeException {
        throw new UnsupportedOperationException("Cannot write maps into Aerospike currently");
    }

    @Override
    public void terminateArray() throws RuntimeException {
        throw new UnsupportedOperationException("Cannot write arrays into Aerospike currently");
    }

    @Override
    public void terminateRecord() throws RuntimeException {
    }

    @Override
    public void terminateTransmission() throws RuntimeException {
    }

    @Override
    public void writeRecord(BonaCustom o) throws RuntimeException {
        addField(StaticMeta.OUTER_BONAPORTABLE, o);
    }

    @Override
    public void startObject(ObjectReference di, BonaCustom o) throws RuntimeException {
    }

    @Override
    public void terminateObject(ObjectReference di, BonaCustom o) throws RuntimeException {
    }

    @Override
    public void addField(MiscElementaryDataItem di, boolean b) throws RuntimeException {
        if (writeBooleanAsString)
            addIt(di, b ? "1" : "0");
        else
            addIt(di, b ? ONE : ZERO);
    }

    @Override
    public void addField(MiscElementaryDataItem di, char c) throws RuntimeException {
        addIt(di, String.valueOf(c));
    }

    @Override
    public void addField(BasicNumericElementaryDataItem di, double d) throws RuntimeException {
        addIt(di, Double.toString(d));
    }

    @Override
    public void addField(BasicNumericElementaryDataItem di, float f) throws RuntimeException {
        addIt(di, Float.toString(f));
    }

    @Override
    public void addField(BasicNumericElementaryDataItem di, byte n) throws RuntimeException {
        addIt(di, n);
    }

    @Override
    public void addField(BasicNumericElementaryDataItem di, short n) throws RuntimeException {
        addIt(di, n);
    }

    @Override
    public void addField(BasicNumericElementaryDataItem di, int n) throws RuntimeException {
        addIt(di, n);
    }

    @Override
    public void addField(BasicNumericElementaryDataItem di, long n) throws RuntimeException {
        addIt(di, n);
    }

    @Override
    public void addField(AlphanumericElementaryDataItem di, String s) throws RuntimeException {
        if (s != null) {
            addIt(di, s);
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addField(ObjectReference di, BonaCustom obj) throws RuntimeException {
        if (obj == null) {
            writeNull(di);
            return;
        }
        // set up a composer, unless already existing
        if (objectComposer == null) {
            objectComposer = new CompactByteArrayComposer(INITIAL_BUFFER_SIZE, false);
        } else {
            objectComposer.reset();
        }
        // write the data
        objectComposer.addField(di, obj);
        // get the result and write it as a byte array
        // addIt(di, objectComposer.getBuilder().getBytes());  // copies the buffer
        bins.add(new Bin(di.getName(), objectComposer.getBuilder().getCurrentBuffer(), 0, objectComposer.getBuilder().length()));
    }

    @Override
    public void addField(MiscElementaryDataItem di, UUID n) throws RuntimeException {
        if (n != null) {
            addIt(di, n.toString());
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addField(BinaryElementaryDataItem di, ByteArray b) throws RuntimeException {
        if (b != null) {
            addIt(di, b.getBytes());
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addField(BinaryElementaryDataItem di, byte[] b) throws RuntimeException {
        if (b != null) {
            addIt(di, b);
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addField(BasicNumericElementaryDataItem di, BigInteger n) throws RuntimeException {
        if (n != null) {
            addIt(di, n.toString());
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addField(NumericElementaryDataItem di, BigDecimal n) throws RuntimeException {
        if (n != null) {
            addIt(di, n.toPlainString());
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addField(TemporalElementaryDataItem di, Instant t) throws RuntimeException {
        if (t != null) {
            addIt(di, t.getMillis());
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addField(TemporalElementaryDataItem di, LocalDate t) throws RuntimeException {
        if (t != null) {
            addIt(di, DayTime.dayAsInt(t));
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addField(TemporalElementaryDataItem di, LocalTime t) throws RuntimeException {
        if (t != null) {
            addIt(di, di.getHhmmss() ? DayTime.timeAsInt(t) : t.getMillisOfDay());
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addField(TemporalElementaryDataItem di, LocalDateTime t) throws RuntimeException {
        if (t != null) {
            addIt(di, DayTime.dayAsInt(t) * 1000000000L + (di.getHhmmss() ? DayTime.timeAsInt(t) : t.getMillisOfDay()));
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addEnum(EnumDataItem di, BasicNumericElementaryDataItem ord, Enum<?> n) throws RuntimeException {
        if (n != null) {
            addIt(di, n.ordinal());
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addEnum(EnumDataItem di, AlphanumericElementaryDataItem token, TokenizableEnum n) throws RuntimeException {
        if (n != null) {
            addIt(di, n.getToken());
        } else {
            writeNull(di);
        }
    }

    @Override
    public void addEnum(XEnumDataItem di, AlphanumericElementaryDataItem token, XEnum<?> n) throws RuntimeException {
        if (n != null) {
            addIt(di, n.getToken());
        } else {
            writeNull(di);
        }
    }
}
