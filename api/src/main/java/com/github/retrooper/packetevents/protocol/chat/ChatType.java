/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
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

package com.github.retrooper.packetevents.protocol.chat;

import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public interface ChatType extends MappedEntity, CopyableEntity<ChatType> {

    ChatTypeDecoration getChatDecoration();

    ChatTypeDecoration getNarrationDecoration();

    static ChatType readDirect(PacketWrapper<?> wrapper) {
        ChatTypeDecoration chatDecoration = ChatTypeDecoration.read(wrapper);
        ChatTypeDecoration narrationDecoration = ChatTypeDecoration.read(wrapper);
        return new StaticChatType(chatDecoration, narrationDecoration);
    }

    static void writeDirect(PacketWrapper<?> wrapper, ChatType chatType) {
        ChatTypeDecoration.write(wrapper, chatType.getChatDecoration());
        ChatTypeDecoration.write(wrapper, chatType.getNarrationDecoration());
    }

    static ChatType decode(NBT nbt, ClientVersion version, @Nullable TypesBuilderData data) {
        NBTCompound compound = (NBTCompound) nbt;
        NBTCompound chatTag = compound.getCompoundTagOrThrow("chat");
        ChatTypeDecoration chatDeco = ChatTypeDecoration.decode(chatTag, version, data);
        NBTCompound narrationTag = compound.getCompoundTagOrThrow("narration");
        ChatTypeDecoration narrationDeco = ChatTypeDecoration.decode(narrationTag, version, data);
        return new StaticChatType(data, chatDeco, narrationDeco);
    }

    static NBT encode(ChatType chatType, ClientVersion version) {
        NBTCompound compound = new NBTCompound();
        compound.setTag("chat", ChatTypeDecoration.encode(chatType.getChatDecoration(), version));
        compound.setTag("narration", ChatTypeDecoration.encode(chatType.getNarrationDecoration(), version));
        return compound;
    }

    class Bound extends ChatMessage_v1_19_1.ChatTypeBoundNetwork {
        public Bound(ChatType type, Component name, @Nullable Component targetName) {
            super(type, name, targetName);
        }
    }
}
