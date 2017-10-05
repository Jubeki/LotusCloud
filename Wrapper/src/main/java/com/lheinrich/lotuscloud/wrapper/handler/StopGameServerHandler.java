/*
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lheinrich.lotuscloud.wrapper.handler;

import com.lheinrich.lotuscloud.api.network.Handler;
import com.lheinrich.lotuscloud.api.network.Packet;
import com.lheinrich.lotuscloud.api.packet.OkPacket;
import com.lheinrich.lotuscloud.api.packet.StopGameServerPacket;
import com.lheinrich.lotuscloud.wrapper.main.Wrapper;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class StopGameServerHandler extends Handler {

    public Packet handle(Packet rawPacket, String client) {
        StopGameServerPacket packet = (StopGameServerPacket) rawPacket;
        Wrapper.instance.worker.stopGame(packet.getServerName());
        return OkPacket.init;
    }
}