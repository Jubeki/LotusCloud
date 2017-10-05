package com.lheinrich.lotuscloud.api.packet;

import com.lheinrich.lotuscloud.api.network.Packet;

import java.io.Serializable;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class OkPacket extends Packet {

    public static final OkPacket init = new OkPacket(null);
    private Serializable data;

    public OkPacket(Serializable data) {
        super("ok");
        this.data = data;
    }

    public Serializable getData() {
        return data;
    }
}