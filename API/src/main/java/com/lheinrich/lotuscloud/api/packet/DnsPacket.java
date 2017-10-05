package com.lheinrich.lotuscloud.api.packet;

import com.lheinrich.lotuscloud.api.network.Packet;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class DnsPacket extends Packet {

    private String ip;
    private int port;

    public DnsPacket(String ip, int port) {
        super("dns");
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }
}