package net.minecraft.item;

import net.minecraft.nbt.NBTTagCompound;

public class ItemStack {
    public static final ItemStack EMPTY = new ItemStack(null, 0, 0, null);

    private final Item item;
    private final int count;
    private final int metadata;
    private final NBTTagCompound tagCompound;

    public ItemStack(Item item) {
        this(item, item == null ? 0 : 1, 0, null);
    }

    public ItemStack(Item item, int count) {
        this(item, count, 0, null);
    }

    public ItemStack(Item item, int count, int metadata) {
        this(item, count, metadata, null);
    }

    public ItemStack(Item item, int count, int metadata, NBTTagCompound tagCompound) {
        this.item = item;
        this.count = count;
        this.metadata = metadata;
        this.tagCompound = tagCompound;
    }

    public boolean isEmpty() {
        return item == null || count <= 0;
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public int getMetadata() {
        return metadata;
    }

    public int getItemDamage() {
        return metadata;
    }

    public NBTTagCompound getTagCompound() {
        return tagCompound;
    }

    public void setItemDamage(int itemDamage) {
        throw new UnsupportedOperationException("Test ItemStack is immutable");
    }

    public void setTagCompound(NBTTagCompound tagCompound) {
        throw new UnsupportedOperationException("Test ItemStack is immutable");
    }

    public ItemStack copy() {
        return isEmpty() ? EMPTY : new ItemStack(item, count, metadata, tagCompound == null ? null : tagCompound.copy());
    }
}
