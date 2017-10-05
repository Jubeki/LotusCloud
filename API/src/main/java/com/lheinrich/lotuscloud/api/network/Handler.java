package com.lheinrich.lotuscloud.api.network;

import java.io.IOException;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public abstract class Handler {
    public abstract Packet handle(Packet packet, String client) throws IOException;
}