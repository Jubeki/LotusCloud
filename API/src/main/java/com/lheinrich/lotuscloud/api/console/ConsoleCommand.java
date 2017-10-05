package com.lheinrich.lotuscloud.api.console;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public abstract class ConsoleCommand {

    public abstract void process(String command, String[] args);
}
