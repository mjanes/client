package org.msf.records.utils;

import com.google.common.base.Joiner;

import junit.framework.TestCase;

import java.util.Arrays;

public class UtilsTest extends TestCase {
    public void testAlphanumericComparator() throws Exception {
        String[] elements = {"b1", "a11a", "a11", "a2", "a2b", "a02b", "a2a", "a1"};
        String[] sorted = elements.clone();
        String[] expected = {"a1", "a2", "a2a", "a02b", "a2b", "a11", "a11a", "b1"};
        Arrays.sort(sorted, Utils.alphanumericComparator);
        Joiner joiner = Joiner.on("/");
        assertEquals(joiner.join(expected), joiner.join(sorted));
    }
}