package com.lheinrich.lotuscloud.api.packet;

import com.lheinrich.lotuscloud.api.network.Packet;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class RegisteredPacket extends Packet {

    public boolean success;
    public String error;
    public Object key;

    public RegisteredPacket(boolean success, String error, Object key) {
        super("registered");
        this.success = success;
        this.error = error;
        this.key = key;
    }
}
