package dev.matthiesen.custom_gateways.common.util;

public enum GeoType {
    BLOCK("block"),
    ITEM("item");

    private final String name;

    GeoType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
