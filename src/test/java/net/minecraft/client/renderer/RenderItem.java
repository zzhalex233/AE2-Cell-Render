package net.minecraft.client.renderer;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RenderItem {
    private IBakedModel model;
    private final Map<Item, IBakedModel> models = new HashMap<>();
    private final Map<Item, Function<ItemStack, IBakedModel>> modelResolvers = new HashMap<>();
    private ItemColors itemColors = new ItemColors();

    public RenderItem() {
    }

    public IBakedModel getItemModelWithOverrides(ItemStack stack, World world, EntityLivingBase entity) {
        Function<ItemStack, IBakedModel> resolver = stack == null || stack.getItem() == null ? null : modelResolvers.get(stack.getItem());
        if (resolver != null) {
            IBakedModel resolved = resolver.apply(stack);
            if (resolved != null) {
                return resolved;
            }
        }
        IBakedModel stackModel = stack == null || stack.getItem() == null ? null : models.get(stack.getItem());
        return stackModel == null ? model : stackModel;
    }

    public void setModel(IBakedModel model) {
        this.model = model;
    }

    public void setModel(Item item, IBakedModel model) {
        if (item == null) {
            this.model = model;
            return;
        }
        models.put(item, model);
    }

    public void setModelResolver(Item item, Function<ItemStack, IBakedModel> modelResolver) {
        if (item == null) {
            return;
        }
        modelResolvers.put(item, modelResolver);
    }

    public void clearModels() {
        models.clear();
        modelResolvers.clear();
        model = null;
    }

    public ItemColors getItemColors() {
        return itemColors;
    }

    public void setItemColors(ItemColors itemColors) {
        this.itemColors = itemColors;
    }
}
