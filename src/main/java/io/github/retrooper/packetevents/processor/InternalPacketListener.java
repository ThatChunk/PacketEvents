/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents.processor;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.PacketListener;
import io.github.retrooper.packetevents.event.impl.PacketReceiveEvent;
import io.github.retrooper.packetevents.event.impl.PacketSendEvent;
import io.github.retrooper.packetevents.manager.player.ClientVersion;
import io.github.retrooper.packetevents.manager.server.SystemOS;
import io.github.retrooper.packetevents.protocol.ConnectionState;
import io.github.retrooper.packetevents.protocol.PacketType;
import io.github.retrooper.packetevents.utils.netty.buffer.ByteBufAbstract;
import io.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.github.retrooper.packetevents.wrapper.game.client.WrapperGameClientInteractEntity;
import io.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import io.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class InternalPacketListener implements PacketListener {
    //Make this specific event be at MONITOR priority
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS) {
            //Transition into the GAME connection state
            PacketEvents.get().getInjector().changeConnectionState(event.getChannel().rawChannel(), ConnectionState.GAME);
            System.out.println("TRANSITIONED!");
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        switch (event.getConnectionState()) {
            case HANDSHAKING:
                if (event.getPacketType() == PacketType.Handshaking.Client.HANDSHAKE) {
                    WrapperHandshakingClientHandshake handshake = new WrapperHandshakingClientHandshake(event);
                    ClientVersion clientVersion = handshake.getClientVersion();

                    //Update client version for this event call
                    event.setClientVersion(clientVersion);

                    //Map netty channel with the client version.
                    Object rawChannel = event.getChannel().rawChannel();
                    PacketEvents.get().getPlayerManager().clientVersions.put(rawChannel, clientVersion);

                    //Transition into the LOGIN OR STATUS connection state
                    PacketEvents.get().getInjector().changeConnectionState(rawChannel, handshake.getNextConnectionState());
                    System.out.println("INCOMING VERSION: " + clientVersion.name());
                }
                break;
            case LOGIN:
                if (event.getPacketType() == PacketType.Login.Client.LOGIN_START) {
                    WrapperLoginClientLoginStart start = new WrapperLoginClientLoginStart(event);
                    //Map the player usernames with their netty channels
                    PacketEvents.get().getPlayerManager().channels.put(start.getUsername(), event.getChannel().rawChannel());
                }
                break;
            case GAME:
                if (event.getPacketType() == PacketType.Game.Client.INTERACT_ENTITY) {
                    WrapperGameClientInteractEntity interactEntity = new WrapperGameClientInteractEntity(event);
                    int entityID = interactEntity.getEntityID();
                    Entity entity = PacketEvents.get().getServerManager().getEntityByID(entityID);
                    event.getPlayer().sendMessage("entity name: " + entity.getName() + ", type: " + interactEntity.getType().name());
                } else if (event.getPacketType() == PacketType.Game.Client.PLAYER_POSITION) {
                    ByteBufAbstract pre = event.getByteBuf().copy();

                    double x = pre.readDouble();
                    double y = pre.readDouble();
                    double z = pre.readDouble();

                    boolean ground = pre.readBoolean();
                    event.getPlayer().sendMessage("X: " + x + ", Y: " + y + ", Z: " + z + ", GROUND: " + ground);
                    event.getPlayer().sendMessage("BYTES LEFT: " + pre.readableBytes());
                    System.out.println("X: " + x + ", Y: " + y + ", Z: " + z + ", GROUND: " + ground);
                    System.out.println("BYTES LEFT: " + pre.readableBytes());
                }
        }
    }
}
