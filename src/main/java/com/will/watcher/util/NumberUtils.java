package com.will.watcher.util;

import java.text.DecimalFormat;


public class NumberUtils {
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static String formatPrice(Number price) {
        if (price == null) {
            return "";
        }
        return df.format(price.doubleValue() / 100.0D);
    }
}
