package com.lheinrich.lotuscloud.api.database;

import java.util.regex.Pattern;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class DBTools {

    public static Pattern noCase(String value) {
        return Pattern.compile(value, Pattern.CASE_INSENSITIVE);
    }
}