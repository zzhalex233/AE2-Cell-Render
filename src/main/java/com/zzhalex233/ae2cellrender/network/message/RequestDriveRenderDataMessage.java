package com.zzhalex233.ae2cellrender.network.message;

import appeng.tile.storage.TileDrive;
import com.zzhalex233.ae2cellrender.server.drive.DriveRenderHooks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RequestDriveRenderDataMessage implements IMessage {

    private long positionKey;

    public RequestDriveRenderDataMessage() {
    }

    public RequestDriveRenderDataMessage(long positionKey) {
        this.positionKey = positionKey;
    }

    public long getPositionKey() {
        return positionKey;
    }

    public void writeTo(DataOutputStream output) throws IOException {
        output.writeLong(positionKey);
    }

    public void readFrom(DataInputStream input) throws IOException {
        positionKey = input.readLong();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try (DataInputStream input = new DataInputStream(new ByteBufInputStream(buf))) {
            readFrom(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode request packet", e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try (DataOutputStream output = new DataOutputStream(new ByteBufOutputStream(buf))) {
            writeTo(output);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode request packet", e);
        }
    }

    public static final class Handler implements IMessageHandler<RequestDriveRenderDataMessage, IMessage> {

        @Override
        public IMessage onMessage(RequestDriveRenderDataMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                TileEntity tile = player.world.getTileEntity(BlockPos.fromLong(message.getPositionKey()));
                if (tile instanceof TileDrive) {
                    DriveRenderHooks.sendToPlayer((TileDrive) tile, player);
                }
            });
            return null;
        }
    }
}
