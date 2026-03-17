package net.minecraftforge.common.property;

public interface IUnlistedProperty<T> {

    String getName();

    boolean isValid(T value);

    Class<T> getType();

    String valueToString(T value);
}
