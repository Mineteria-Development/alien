package io.minimum.minecraft.alien.network.mcpe.util;

import java.util.Objects;

public class Attribute {
    private final String name;
    private final float minimumValue;
    private final float maximumValue;
    private final float value;
    private final float defaultValue;

    public Attribute(String name, float minimumValue, float maximumValue, float value, float defaultValue) {
        this.name = name;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public float getMinimumValue() {
        return minimumValue;
    }

    public float getMaximumValue() {
        return maximumValue;
    }

    public float getValue() {
        return value;
    }

    public float getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return Float.compare(attribute.minimumValue, minimumValue) == 0 &&
                Float.compare(attribute.maximumValue, maximumValue) == 0 &&
                Float.compare(attribute.value, value) == 0 &&
                Float.compare(attribute.defaultValue, defaultValue) == 0 &&
                Objects.equals(name, attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, minimumValue, maximumValue, value, defaultValue);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", minimumValue=" + minimumValue +
                ", maximumValue=" + maximumValue +
                ", value=" + value +
                ", defaultValue=" + defaultValue +
                '}';
    }
}
