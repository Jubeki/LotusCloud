package com.lheinrich.lotuscloud.api.packet;

import com.lheinrich.lotuscloud.api.network.Packet;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class ErrorPacket extends Packet {

    private String error;

    public ErrorPacket(String error) {
        super("error");
        this.error = error;
    }

    public String getError() {
        return error;
    }
}