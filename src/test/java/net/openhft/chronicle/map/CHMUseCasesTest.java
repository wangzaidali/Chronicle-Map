package net.openhft.chronicle.map;

import net.openhft.lang.model.DataValueClasses;
import net.openhft.lang.values.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * This test enumerates common usecases for keys and values.
 */
@Ignore
public class CHMUseCasesTest {
    /**
     * String is not as efficient as CharSequence as a key or value but easier to use
     * The key can only be on heap and variable length serialised.
     */
    @Test
    public void testStringStringMap() {
        ChronicleMap<String, String> map = ChronicleMapBuilder.of(String.class, String.class).create();
        map.put("Hello", "World");
        map.close();
    }

    /**
     * CharSequence is more efficient when object creation is avoided.
     * The key can only be on heap and variable length serialised.
     */
    @Test
    public void testCharSequenceCharSequenceMap() {
        ChronicleMap<CharSequence, CharSequence> map = ChronicleMapBuilder.of(CharSequence.class, CharSequence.class).create();
        map.put("Hello", "World");
        StringBuilder key = new StringBuilder();
        key.append("key-").append(1);

        StringBuilder value = new StringBuilder();
        value.append("value-").append(1);
        map.put(key, value);
        assertEquals("value-1", map.get("key-1"));

        assertEquals(value, map.getUsing(key, value));
        assertEquals("value-1", value.toString());
        map.remove("key-1");
        assertNull(map.getUsing(key, value));
        map.close();
    }

    /**
     * StringValue represents any bean which contains a String Value
     */
    @Test
    public void testStringValueStringValueMap() {
        ChronicleMap<StringValue, StringValue> map = ChronicleMapBuilder.of(StringValue.class, StringValue.class).create();
        StringValue key1 = DataValueClasses.newDirectInstance(StringValue.class);
        StringValue key2 = DataValueClasses.newInstance(StringValue.class);
        StringValue value1 = DataValueClasses.newDirectInstance(StringValue.class);
        StringValue value2 = DataValueClasses.newInstance(StringValue.class);

        key1.setValue(new StringBuilder("1"));
        value1.setValue("11");
        map.put(key1, value1);
        assertEquals(value1, map.get(key1));

        key2.setValue("2");
        value2.setValue(new StringBuilder("22"));
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        StringBuilder sb = new StringBuilder();
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals("11", value1.getValue());
            value1.getUsingValue(sb);
            assertEquals("11", sb.toString());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals("22", value2.getValue());
            value2.getUsingValue(sb);
            assertEquals("22", sb.toString());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals("11", value2.getValue());
            value2.getUsingValue(sb);
            assertEquals("11", sb.toString());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals("22", value2.getValue());
            value2.getUsingValue(sb);
            assertEquals("22", sb.toString());
        }
        key1.setValue("3");
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue("4");
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals("", value1.getValue());
            value1.getUsingValue(sb);
            assertEquals("", sb.toString());
            sb.append(123);
            value1.setValue(sb);
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals("123", value2.getValue());
            value2.setValue(value2.getValue() + '4');
            assertEquals("1234", value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals("1234", value1.getValue());
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals("", value2.getValue());
            value2.getUsingValue(sb);
            assertEquals("", sb.toString());
            sb.append(123);
            value2.setValue(sb);
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals("123", value1.getValue());
            value1.setValue(value1.getValue() + '4');
            assertEquals("1234", value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals("1234", value2.getValue());
        }
        map.close();
    }

    @Test
    public void testIntegerIntegerMap() {
        ChronicleMap<Integer, Integer> map = ChronicleMapBuilder.of(Integer.class, Integer.class).entrySize(8).create();
        Integer key1;
        Integer key2;
        Integer value1;
        Integer value2;

        key1 = 1;
        value1 = 11;
        map.put(key1, value1);
        assertEquals(value1, map.get(key1));

        key2 = 2;
        value2 = 22;
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        assertEquals((Integer) 11, map.get(key1));
        assertEquals((Integer) 22, map.get(key2));
        assertEquals(null, map.get(3));
        assertEquals(null, map.get(4));

        map.close();
    }

    @Test
    public void testLongLongMap() {
        ChronicleMap<Long, Long> map = ChronicleMapBuilder.of(Long.class, Long.class).entrySize(16).create();

        map.put(1L, 11L);
        assertEquals((Long) 11L, map.get(1L));

        map.put(2L, 22L);
        assertEquals((Long) 22L, map.get(2L));

        assertEquals(null, map.get(3));
        assertEquals(null, map.get(4));

        map.close();
    }

    @Test
    public void testDoubleDoubleMap() {
        ChronicleMap<Double, Double> map = ChronicleMapBuilder.of(Double.class, Double.class).entrySize(16).create();

        map.put(1.0, 11.0);
        assertEquals((Double) 11.0, map.get(1.0));

        map.put(2.0, 22.0);
        assertEquals((Double) 22.0, map.get(2.0));

        assertEquals(null, map.get(3));
        assertEquals(null, map.get(4));

        map.close();
    }

    @Test
    public void testIntValueIntValueMap() {
        ChronicleMap<IntValue, IntValue> map = ChronicleMapBuilder.of(IntValue.class, IntValue.class).entrySize(8).create();
        IntValue key1 = DataValueClasses.newDirectInstance(IntValue.class);
        IntValue key2 = DataValueClasses.newInstance(IntValue.class);
        IntValue value1 = DataValueClasses.newDirectInstance(IntValue.class);
        IntValue value2 = DataValueClasses.newInstance(IntValue.class);

        key1.setValue(1);
        value1.setValue(11);
        map.put(key1, value1);
        assertEquals(value1, map.get(key1));

        key2.setValue(2);
        value2.setValue(22);
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(11, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals(11, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        key1.setValue(3);
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue(4);
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals(0, value1.getValue());
            value1.addValue(123);
            assertEquals(123, value1.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals(123, value2.getValue());
            value2.addValue(1230 - 123);
            assertEquals(1230, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(1230, value1.getValue());
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals(0, value2.getValue());
            value2.addValue(123);
            assertEquals(123, value2.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals(123, value1.getValue());
            value1.addValue(1230 - 123);
            assertEquals(1230, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(1230, value2.getValue());
        }
        map.close();
    }

    /**
     * For unsigned int -> unsigned int entries, the key can be on heap or off heap.
     */
    @Test
    public void testUnsignedIntValueUnsignedIntValueMap() {
        ChronicleMap<UnsignedIntValue, UnsignedIntValue> map = ChronicleMapBuilder.of(UnsignedIntValue.class, UnsignedIntValue.class).entrySize(8).create();
        UnsignedIntValue key1 = DataValueClasses.newDirectInstance(UnsignedIntValue.class);
        UnsignedIntValue key2 = DataValueClasses.newInstance(UnsignedIntValue.class);
        UnsignedIntValue value1 = DataValueClasses.newDirectInstance(UnsignedIntValue.class);
        UnsignedIntValue value2 = DataValueClasses.newInstance(UnsignedIntValue.class);

        key1.setValue(1);
        value1.setValue(11);
        map.put(key1, value1);
        assertEquals(value1, map.get(key1));

        key2.setValue(2);
        value2.setValue(22);
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(11, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals(11, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        key1.setValue(3);
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue(4);
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals(0, value1.getValue());
            value1.addValue(123);
            assertEquals(123, value1.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals(123, value2.getValue());
            value2.addValue(1230 - 123);
            assertEquals(1230, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(1230, value1.getValue());
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals(0, value2.getValue());
            value2.addValue(123);
            assertEquals(123, value2.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals(123, value1.getValue());
            value1.addValue(1230 - 123);
            assertEquals(1230, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(1230, value2.getValue());
        }
        map.close();
    }

    /**
     * For int values, the key can be on heap or off heap.
     */
    @Test
    public void testIntValueShortValueMap() {
        ChronicleMap<IntValue, ShortValue> map = ChronicleMapBuilder.of(IntValue.class, ShortValue.class).entrySize(6).create();
        IntValue key1 = DataValueClasses.newDirectInstance(IntValue.class);
        IntValue key2 = DataValueClasses.newInstance(IntValue.class);
        ShortValue value1 = DataValueClasses.newDirectInstance(ShortValue.class);
        ShortValue value2 = DataValueClasses.newInstance(ShortValue.class);

        key1.setValue(1);
        value1.setValue((short) 11);
        map.put(key1, value1);
        assertEquals(value1, map.get(key1));

        key2.setValue(2);
        value2.setValue((short) 22);
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(11, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals(11, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        key1.setValue(3);
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue(4);
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals(0, value1.getValue());
            value1.addValue((short) 123);
            assertEquals(123, value1.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals(123, value2.getValue());
            value2.addValue((short) (1230 - 123));
            assertEquals(1230, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(1230, value1.getValue());
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals(0, value2.getValue());
            value2.addValue((short) 123);
            assertEquals(123, value2.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals(123, value1.getValue());
            value1.addValue((short) (1230 - 123));
            assertEquals(1230, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(1230, value2.getValue());
        }
        map.close();
    }

    /**
     * For int -> unsigned short values, the key can be on heap or off heap.
     */
    @Test
    public void testIntValueUnsignedShortValueMap() {
        ChronicleMap<IntValue, UnsignedShortValue> map = ChronicleMapBuilder.of(IntValue.class, UnsignedShortValue.class).entrySize(6).create();
        IntValue key1 = DataValueClasses.newDirectInstance(IntValue.class);
        IntValue key2 = DataValueClasses.newInstance(IntValue.class);
        UnsignedShortValue value1 = DataValueClasses.newDirectInstance(UnsignedShortValue.class);
        UnsignedShortValue value2 = DataValueClasses.newInstance(UnsignedShortValue.class);

        key1.setValue(1);
        value1.setValue(11);
        map.put(key1, value1);
        assertEquals(value1, map.get(key1));

        key2.setValue(2);
        value2.setValue(22);
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(11, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals(11, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        key1.setValue(3);
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue(4);
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals(0, value1.getValue());
            value1.addValue(123);
            assertEquals(123, value1.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals(123, value2.getValue());
            value2.addValue(1230 - 123);
            assertEquals(1230, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(1230, value1.getValue());
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals(0, value2.getValue());
            value2.addValue(123);
            assertEquals(123, value2.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals(123, value1.getValue());
            value1.addValue(1230 - 123);
            assertEquals(1230, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(1230, value2.getValue());
        }
        map.close();
    }

    /**
     * For int values, the key can be on heap or off heap.
     */
    @Test
    public void testIntValueCharValueMap() {
        ChronicleMap<IntValue, CharValue> map = ChronicleMapBuilder.of(IntValue.class, CharValue.class).entrySize(6).create();
        IntValue key1 = DataValueClasses.newDirectInstance(IntValue.class);
        IntValue key2 = DataValueClasses.newInstance(IntValue.class);
        CharValue value1 = DataValueClasses.newDirectInstance(CharValue.class);
        CharValue value2 = DataValueClasses.newInstance(CharValue.class);

        key1.setValue(1);
        value1.setValue((char) 11);
        map.put(key1, value1);
        assertEquals(value1, map.get(key1));

        key2.setValue(2);
        value2.setValue((char) 22);
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(11, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals(11, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        key1.setValue(3);
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue(4);
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals('\0', value1.getValue());
            value1.setValue('@');
            assertEquals('@', value1.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals('@', value2.getValue());
            value2.setValue('#');
            assertEquals('#', value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals('#', value1.getValue());
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals('\0', value2.getValue());
            value2.setValue(';');
            assertEquals(';', value2.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals(';', value1.getValue());
            value1.setValue('[');
            assertEquals('[', value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals('[', value2.getValue());
        }
        map.close();
    }

    /**
     * For int-> byte entries, the key can be on heap or off heap.
     */
    @Test
    public void testIntValueUnsignedByteMap() {
        ChronicleMap<IntValue, UnsignedByteValue> map = ChronicleMapBuilder.of(IntValue.class, UnsignedByteValue.class).entrySize(5).create();
        IntValue key1 = DataValueClasses.newDirectInstance(IntValue.class);
        IntValue key2 = DataValueClasses.newInstance(IntValue.class);
        UnsignedByteValue value1 = DataValueClasses.newDirectInstance(UnsignedByteValue.class);
        UnsignedByteValue value2 = DataValueClasses.newInstance(UnsignedByteValue.class);

        key1.setValue(1);
        value1.setValue(11);
        map.put(key1, value1);
        assertEquals(value1, map.get(key1));

        key2.setValue(2);
        value2.setValue(22);
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(11, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals(11, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        key1.setValue(3);
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue(4);
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals(0, value1.getValue());
            value1.addValue(234);
            assertEquals(234, value1.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals(234, value2.getValue());
            value2.addValue(-100);
            assertEquals(134, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(134, value1.getValue());
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals(0, value2.getValue());
            value2.addValue((byte) 123);
            assertEquals(123, value2.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals(123, value1.getValue());
            value1.addValue((byte) -111);
            assertEquals(12, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(12, value2.getValue());
        }
        map.close();
    }

    /**
     * For int values, the key can be on heap or off heap.
     */
    @Test
    public void testIntValueBooleanValueMap() {
        ChronicleMap<IntValue, BooleanValue> map = ChronicleMapBuilder.of(IntValue.class, BooleanValue.class).entrySize(5).create();
        IntValue key1 = DataValueClasses.newDirectInstance(IntValue.class);
        IntValue key2 = DataValueClasses.newInstance(IntValue.class);
        BooleanValue value1 = DataValueClasses.newDirectInstance(BooleanValue.class);
        BooleanValue value2 = DataValueClasses.newInstance(BooleanValue.class);

        key1.setValue(1);
        value1.setValue(true);
        map.put(key1, value1);
        assertEquals(value1, map.get(key1));

        key2.setValue(2);
        value2.setValue(false);
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(true, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals(false, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals(true, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(false, value2.getValue());
        }
        key1.setValue(3);
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue(4);
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals(false, value1.getValue());
            value1.setValue(true);
            assertEquals(true, value1.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals(true, value2.getValue());
            value2.setValue(false);
            assertEquals(false, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(false, value1.getValue());
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals(false, value2.getValue());
            value2.setValue(true);
            assertEquals(true, value2.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals(true, value1.getValue());
            value1.setValue(false);
            assertEquals(false, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(false, value2.getValue());
        }
        map.close();
    }

    /**
     * For float values, the key can be on heap or off heap.
     */
    @Test
    public void testFloatValueFloatValueMap() {
        ChronicleMap<FloatValue, FloatValue> map = ChronicleMapBuilder.of(FloatValue.class, FloatValue.class).entrySize(8).create();
        FloatValue key1 = DataValueClasses.newDirectInstance(FloatValue.class);
        FloatValue key2 = DataValueClasses.newInstance(FloatValue.class);
        FloatValue value1 = DataValueClasses.newDirectInstance(FloatValue.class);
        FloatValue value2 = DataValueClasses.newInstance(FloatValue.class);

        key1.setValue(1);
        value1.setValue(11);
        map.put(key1, value1);
        assertEquals(value1, map.get(key1));

        key2.setValue(2);
        value2.setValue(22);
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(11, value1.getValue(), 0);
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue(), 0);
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals(11, value2.getValue(), 0);
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue(), 0);
        }
        key1.setValue(3);
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue(4);
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals(0, value1.getValue(), 0);
            value1.addValue(123);
            assertEquals(123, value1.getValue(), 0);
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals(123, value2.getValue(), 0);
            value2.addValue(1230 - 123);
            assertEquals(1230, value2.getValue(), 0);
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(1230, value1.getValue(), 0);
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals(0, value2.getValue(), 0);
            value2.addValue(123);
            assertEquals(123, value2.getValue(), 0);
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals(123, value1.getValue(), 0);
            value1.addValue(1230 - 123);
            assertEquals(1230, value1.getValue(), 0);
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(1230, value2.getValue(), 0);
        }
        map.close();
    }

    /**
     * For double values, the key can be on heap or off heap.
     */
    @Test
    public void testDoubleValueDoubleValueMap() {
        ChronicleMap<DoubleValue, DoubleValue> map = ChronicleMapBuilder.of(DoubleValue.class, DoubleValue.class).entrySize(16).create();
        DoubleValue key1 = DataValueClasses.newDirectInstance(DoubleValue.class);
        DoubleValue key2 = DataValueClasses.newInstance(DoubleValue.class);
        DoubleValue value1 = DataValueClasses.newDirectInstance(DoubleValue.class);
        DoubleValue value2 = DataValueClasses.newInstance(DoubleValue.class);

        key1.setValue(1);
        value1.setValue(11);
        assertEquals(value1, map.get(key1));

        key2.setValue(2);
        value2.setValue(22);
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(11, value1.getValue(), 0);
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue(), 0);
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals(11, value2.getValue(), 0);
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue(), 0);
        }
        key1.setValue(3);
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue(4);
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals(0, value1.getValue(), 0);
            value1.addValue(123);
            assertEquals(123, value1.getValue(), 0);
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals(123, value2.getValue(), 0);
            value2.addValue(1230 - 123);
            assertEquals(1230, value2.getValue(), 0);
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(1230, value1.getValue(), 0);
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals(0, value2.getValue(), 0);
            value2.addValue(123);
            assertEquals(123, value2.getValue(), 0);
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals(123, value1.getValue(), 0);
            value1.addValue(1230 - 123);
            assertEquals(1230, value1.getValue(), 0);
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(1230, value2.getValue(), 0);
        }
        map.close();
    }

    /**
     * For long values, the key can be on heap or off heap.
     */
    @Test
    public void testLongValueLongValueMap() {
        ChronicleMap<LongValue, LongValue> map = ChronicleMapBuilder.of(LongValue.class, LongValue.class).entrySize(16).create();
        LongValue key1 = DataValueClasses.newDirectInstance(LongValue.class);
        LongValue key2 = DataValueClasses.newInstance(LongValue.class);
        LongValue value1 = DataValueClasses.newDirectInstance(LongValue.class);
        LongValue value2 = DataValueClasses.newInstance(LongValue.class);

        key1.setValue(1);
        value1.setValue(11);
        assertEquals(value1, map.get(key1));

        key2.setValue(2);
        value2.setValue(22);
        map.put(key2, value2);
        assertEquals(value2, map.get(key2));

        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(11, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value1)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value2)) {
            assertTrue(rc.present());
            assertEquals(11, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(22, value2.getValue());
        }
        key1.setValue(3);
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertFalse(rc.present());
        }
        key2.setValue(4);
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertFalse(rc.present());
        }

        try (WriteContext wc = map.acquireUsingLocked(key1, value1)) {
            assertEquals(0, value1.getValue());
            value1.addValue(123);
            assertEquals(123, value1.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key1, value2)) {
            assertEquals(123, value2.getValue());
            value2.addValue(1230 - 123);
            assertEquals(1230, value2.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key1, value1)) {
            assertTrue(rc.present());
            assertEquals(1230, value1.getValue());
        }

        try (WriteContext wc = map.acquireUsingLocked(key2, value2)) {
            assertEquals(0, value2.getValue());
            value2.addValue(123);
            assertEquals(123, value2.getValue());
        }
        try (WriteContext wc = map.acquireUsingLocked(key2, value1)) {
            assertEquals(123, value1.getValue());
            value1.addValue(1230 - 123);
            assertEquals(1230, value1.getValue());
        }
        try (ReadContext rc = map.getUsingLocked(key2, value2)) {
            assertTrue(rc.present());
            assertEquals(1230, value2.getValue());
        }
        map.close();
    }

    /**
     * For beans, the key can be on heap or off heap as long as the bean is not variable length.
     */
    @Test
    public void testBeanBeanMap() {

    }


    @Test
    public void testListValue() {
        ChronicleMap<String, List<String>> map = ChronicleMapBuilder.of(String.class, (Class<List<String>>) (Class) List.class).create();

        map.put("1", Collections.<String>emptyList());
        map.put("2", Arrays.asList("one"));

        List<String> list1 = new ArrayList<>();
        try (WriteContext wc = map.acquireUsingLocked("1", list1)) {
            list1.add("two");
            assertEquals(Arrays.asList("two"), list1);
        }
        List<String> list2 = new ArrayList<>();
        try (ReadContext rc = map.getUsingLocked("1", list2)) {
            assertTrue(rc.present());
            assertEquals(Arrays.asList("two"), list2);
        }
        try (WriteContext wc = map.acquireUsingLocked("2", list1)) {
            list1.add("three");
            assertEquals(Arrays.asList("three"), list1);
        }
        try (ReadContext rc = map.getUsingLocked("2", list2)) {
            assertTrue(rc.present());
            assertEquals(Arrays.asList("one", "three"), list2);
        }
        map.close();
    }

    @Test
    public void testSetValue() {
        ChronicleMap<String, Set<String>> map = ChronicleMapBuilder.of(String.class, (Class<Set<String>>) (Class) Set.class).create();

        map.put("1", Collections.<String>emptySet());
        map.put("2", new LinkedHashSet<String>(Arrays.asList("one")));

        Set<String> list1 = new LinkedHashSet<>();
        try (WriteContext wc = map.acquireUsingLocked("1", list1)) {
            list1.add("two");
            assertEquals(new LinkedHashSet<String>(Arrays.asList("two")), list1);
        }
        Set<String> list2 = new LinkedHashSet<>();
        try (ReadContext rc = map.getUsingLocked("1", list2)) {
            assertTrue(rc.present());
            assertEquals(new LinkedHashSet<String>(Arrays.asList("two")), list2);
        }
        try (WriteContext wc = map.acquireUsingLocked("2", list1)) {
            list1.add("three");
            assertEquals(new LinkedHashSet<String>(Arrays.asList("three")), list1);
        }
        try (ReadContext rc = map.getUsingLocked("2", list2)) {
            assertTrue(rc.present());
            assertEquals(new LinkedHashSet<String>(Arrays.asList("one", "three")), list2);
        }
        map.close();
    }

    @Test
    public void testMapStringStringValue() {
        ChronicleMap<String, Map<String, String>> map = ChronicleMapBuilder.of(String.class, (Class<Map<String, String>>) (Class) Map.class).create();

        map.put("1", Collections.<String, String>emptyMap());
        map.put("2", mapOf("one", "uni"));

        Map<String, String> map1 = new LinkedHashMap<>();
        try (WriteContext wc = map.acquireUsingLocked("1", map1)) {
            map1.put("two", "bi");
            assertEquals(mapOf("two", "bi"), map1);
        }
        Map<String, String> map2 = new LinkedHashMap<>();
        try (ReadContext rc = map.getUsingLocked("1", map2)) {
            assertTrue(rc.present());
            assertEquals(mapOf("two", "bi"), map2);
        }
        try (WriteContext wc = map.acquireUsingLocked("2", map1)) {
            map1.put("three", "tri");
            assertEquals(mapOf("one", "uni", "three", "tri"), map1);
        }
        try (ReadContext rc = map.getUsingLocked("2", map2)) {
            assertTrue(rc.present());
            assertEquals(mapOf("one", "uni", "three", "tri"), map2);
        }
        map.close();
    }

    @Test
    public void testMapStringIntegerValue() {
        ChronicleMap<String, Map<String, Integer>> map = ChronicleMapBuilder.of(String.class, (Class<Map<String, Integer>>) (Class) Map.class).create();

        map.put("1", Collections.<String, Integer>emptyMap());
        map.put("2", mapOf("one", 1));

        Map<String, Integer> map1 = new LinkedHashMap<>();
        try (WriteContext wc = map.acquireUsingLocked("1", map1)) {
            map1.put("two", 2);
            assertEquals(mapOf("two", 2), map1);
        }
        Map<String, Integer> map2 = new LinkedHashMap<>();
        try (ReadContext rc = map.getUsingLocked("1", map2)) {
            assertTrue(rc.present());
            assertEquals(mapOf("two", 2), map2);
        }
        try (WriteContext wc = map.acquireUsingLocked("2", map1)) {
            map1.put("three", 3);
            assertEquals(mapOf("one", 1, "three", 3), map1);
        }
        try (ReadContext rc = map.getUsingLocked("2", map2)) {
            assertTrue(rc.present());
            assertEquals(mapOf("one", 1, "three", 3), map2);
        }
        map.close();
    }

    public static <K, V> Map<K, V> mapOf(K k, V v, Object... keysAndValues) {
        Map<K, V> ret = new LinkedHashMap<>();
        ret.put(k, v);
        for (int i = 0; i < keysAndValues.length - 1; i += 2) {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];
            ret.put((K) key, (V) value);
        }
        return ret;
    }
}

enum ToString implements Function<Object, String> {
    INSTANCE;

    @Override
    public String apply(Object o) {
        return String.valueOf(o);
    }

}
/*
interface IBean {
    long getLong();

    void setLong(long num);

    double getDouble();

    void setDouble(double d);

    int getInt();

    void setInt(int i);
}
*/