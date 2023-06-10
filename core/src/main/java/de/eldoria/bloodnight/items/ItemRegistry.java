package de.eldoria.bloodnight.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    @JsonProperty
    private final Map<String, ItemStack> registrations;

    @JsonCreator
    public ItemRegistry(@JsonProperty("registrations") Map<String, ItemStack> registrations) {
        this.registrations = registrations;
    }

    public ItemRegistry() {
        registrations = new HashMap<>();
    }

    public Map<String, ItemStack> registrations() {
        return Collections.unmodifiableMap(registrations);
    }

    public void register(ItemStack stack) {
        stack = stack.clone();
        stack.setAmount(1);
        registrations.put(Integer.toHexString(stack.hashCode()), stack);
    }
}
