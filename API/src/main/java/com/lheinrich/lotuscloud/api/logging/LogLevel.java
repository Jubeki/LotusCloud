package com.lheinrich.lotuscloud.api.logging;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public enum LogLevel {

    DEBUG(0), INFO(10), WARNING(20), ERROR(30), CRITICAL(40);

    private int level;

    LogLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}