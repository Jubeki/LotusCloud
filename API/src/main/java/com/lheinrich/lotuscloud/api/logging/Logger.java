package com.lheinrich.lotuscloud.api.logging;

import java.io.PrintStream;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class Logger {

    private LogLevel logLevel;
    private PrintStream printStream;

    public Logger(LogLevel logLevel) {
        this.logLevel = logLevel;
        this.printStream = System.out;
    }

    public Logger(LogLevel logLevel, PrintStream printStream) {
        this.logLevel = logLevel;
        this.printStream = printStream;
    }

    public void log(String message) {
        log(message, LogLevel.CRITICAL);
    }

    public void log(String message, LogLevel logLevel) {
        if (logLevel.getLevel() >= this.logLevel.getLevel())
            printStream.println("[" + logLevel.name() + "] " + message);
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }
}
