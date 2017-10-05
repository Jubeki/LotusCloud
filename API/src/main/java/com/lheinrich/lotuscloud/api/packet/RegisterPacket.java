package com.lheinrich.lotuscloud.api.packet;

import com.lheinrich.lotuscloud.api.network.Packet;
import java.security.PublicKey;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class RegisterPacket extends Packet {

    public int port;
    public PublicKey key;

    public RegisterPacket(int port, PublicKey key) {
        super("register");
        this.port = port;
        this.key = key;
    }
}