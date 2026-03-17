package net.minecraftforge.client.model.pipeline;

public interface IVertexConsumer {

    default void put(int element, float... values) {
    }
}
