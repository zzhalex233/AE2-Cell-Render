package com.zzhalex233.ae2cellrender.network.message;

import com.zzhalex233.ae2cellrender.client.drive.CellColorResolver;
import com.zzhalex233.ae2cellrender.client.drive.DriveRenderCache;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveSyncMessageSupport;
import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SyncDriveRenderDataMessage implements IMessage {

    private int digest;
    private DriveRenderSnapshot snapshot = DriveRenderSnapshot.empty(0, 0L, 10);

    public SyncDriveRenderDataMessage() {
    }

    public SyncDriveRenderDataMessage(DriveRenderSnapshot snapshot, int digest) {
        this.snapshot = snapshot;
        this.digest = digest;
    }

    public DriveRenderSnapshot getSnapshot() {
        return snapshot;
    }

    public int getDigest() {
        return digest;
    }

    public void writeTo(DataOutputStream output) throws IOException {
        output.writeInt(digest);
        snapshot.writeTo(output);
    }

    public void readFrom(DataInputStream input) throws IOException {
        digest = input.readInt();
        snapshot = DriveRenderSnapshot.readFrom(input);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try (DataInputStream input = new DataInputStream(new ByteBufInputStream(buf))) {
            readFrom(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode sync packet", e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try (DataOutputStream output = new DataOutputStream(new ByteBufOutputStream(buf))) {
            writeTo(output);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode sync packet", e);
        }
    }

    public static final class Handler implements IMessageHandler<SyncDriveRenderDataMessage, IMessage> {

        @Override
        public IMessage onMessage(SyncDriveRenderDataMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft minecraft = Minecraft.getMinecraft();
                if (minecraft.world == null) {
                    return;
                }

                DriveRenderSnapshot snapshot = message.getSnapshot();
                if (minecraft.world.provider.getDimension() != snapshot.getDimensionId()) {
                    return;
                }

                BlockPos pos = BlockPos.fromLong(snapshot.getPositionKey());
                TileEntity tile = minecraft.world.getTileEntity(pos);
                if (!DriveSyncMessageSupport.accepts(minecraft.world.getBlockState(pos).getBlock(), tile)) {
                    return;
                }

                CellColorResolver.INSTANCE.prime(snapshot.getSlots());
                DriveRenderCache.getInstance().store(snapshot, message.getDigest());
                minecraft.world.markBlockRangeForRenderUpdate(pos, pos);
            });
            return null;
        }
    }
}
