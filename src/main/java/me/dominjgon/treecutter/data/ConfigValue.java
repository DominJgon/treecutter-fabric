package me.dominjgon.treecutter.data;

public class ConfigValue {
    private final String raw;

    public ConfigValue(String raw) {
        this.raw = raw;
    }

    public String asString() {
        return raw;
    }

    public int asInt() {
        try {
            return Integer.parseInt(raw.trim());
        } catch(NumberFormatException e) {
            throw new RuntimeException("Config value '" + raw + "' is not a valid int");
        }
    }

    public boolean asBoolean() {
        return Boolean.parseBoolean(raw.trim());
    }

    public float asFloat() {
        try {
            return Float.parseFloat(raw.trim());
        } catch(NumberFormatException e) {
            throw new RuntimeException("Config value '" + raw + "' is not a valid float");
        }
    }
}
