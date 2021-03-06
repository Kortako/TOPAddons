package io.github.drmanganese.topaddons.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import io.github.drmanganese.topaddons.TOPAddons;
import io.github.drmanganese.topaddons.config.capabilities.IClientOptsCapability;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;

public class MessageClientOptions implements IMessage, IMessageHandler<MessageClientOptions, IMessage> {

    private Map<String, Integer> optionsToSend, elementIdsToSend;
    private String playerUUID;

    public MessageClientOptions() {
    }

    public MessageClientOptions(Map<String, Integer> options, Map<String, Integer> elementIds, EntityPlayer player) {
        this.optionsToSend = options;
        this.elementIdsToSend = elementIds;
        this.playerUUID = player.getUniqueID().toString();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerUUID = ByteBufUtils.readUTF8String(buf);

        int size = buf.readInt();
        Map<String, Integer> options = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            options.put(ByteBufUtils.readUTF8String(buf), buf.readInt());
        }
        this.optionsToSend = options;

        size = buf.readInt();
        Map<String, Integer> ids = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            ids.put(ByteBufUtils.readUTF8String(buf), buf.readInt());
        }
        this.elementIdsToSend = ids;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.playerUUID);

        buf.writeInt(this.optionsToSend.size());
        this.optionsToSend.forEach((s, i) -> {
            ByteBufUtils.writeUTF8String(buf, s);
            buf.writeInt(i);
        });

        buf.writeInt(this.elementIdsToSend.size());
        this.elementIdsToSend.forEach((s, i) -> {
            ByteBufUtils.writeUTF8String(buf, s);
            buf.writeInt(i);
        });
    }

    @Override
    public IMessage onMessage(MessageClientOptions message, MessageContext ctx) {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
                    IClientOptsCapability cap = ctx.getServerHandler().player.getCapability(TOPAddons.OPTS_CAP, null);
                    cap.setAllOptions(message.optionsToSend);
                    cap.setAllElementIds(message.elementIdsToSend);
                }
        );
        return null;
    }
}
