package net.minecraft.client.renderer.color;

import net.minecraft.item.ItemStack;

import java.util.function.ToIntBiFunction;

public class ItemColors {
    private ToIntBiFunction<ItemStack, Integer> colorMultiplier = (stack, tintIndex) -> -1;

    public int colorMultiplier(ItemStack stack, int tintIndex) {
        return colorMultiplier.applyAsInt(stack, tintIndex);
    }

    public void setColorMultiplier(ToIntBiFunction<ItemStack, Integer> colorMultiplier) {
        this.colorMultiplier = colorMultiplier == null ? (stack, tintIndex) -> -1 : colorMultiplier;
    }
}
