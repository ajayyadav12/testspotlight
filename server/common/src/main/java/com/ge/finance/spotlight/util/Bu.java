package com.ge.finance.spotlight.util;

import java.util.HashSet;
import java.util.Set;

public class Bu {
    public enum BU {
        AV, CA, CO, HC, PO, OG, RE, GN, GP, IN, TC;
    }

    private final static Set<String> values = new HashSet<String>(BU.values().length);

    public static boolean contains(String value) {
        return values.contains(value);
    }
}