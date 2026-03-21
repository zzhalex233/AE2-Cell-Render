package net.minecraftforge.fml.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.nio.charset.StandardCharsets;

public final class ByteBufUtils {
    private ByteBufUtils() {
    }

    public static void writeItemStack(ByteBuf buffer, ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() == null || stack.getItem().getRegistryName() == null) {
            buffer.writeBoolean(false);
            return;
        }

        ResourceLocation registryName = stack.getItem().getRegistryName();
        buffer.writeBoolean(true);
        writeString(buffer, registryName.getResourceDomain());
        writeString(buffer, registryName.getResourcePath());
        buffer.writeInt(stack.getCount());
        buffer.writeInt(stack.getMetadata());

        NBTTagCompound tagCompound = stack.getTagCompound();
        buffer.writeBoolean(tagCompound != null);
        if (tagCompound != null) {
            writeString(buffer, tagCompound.getString("value"));
        }
    }

    public static ItemStack readItemStack(ByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return ItemStack.EMPTY;
        }

        String domain = readString(buffer);
        String path = readString(buffer);
        int count = buffer.readInt();
        int metadata = buffer.readInt();

        NBTTagCompound tagCompound = null;
        if (buffer.readBoolean()) {
            tagCompound = new NBTTagCompound();
            tagCompound.setString("value", readString(buffer));
        }

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(domain, path));
        return item == null ? ItemStack.EMPTY : new ItemStack(item, count, metadata, tagCompound);
    }

    private static void writeString(ByteBuf buffer, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    private static String readString(ByteBuf buffer) {
        int length = buffer.readInt();
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
