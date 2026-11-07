package com.q9e.antiafk;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.client.KeyMapping;

public final class AutomationInput {
    private final Set<KeyMapping> heldMappings = Collections.newSetFromMap(new IdentityHashMap<>());
    public boolean tap(KeyMapping mapping) {
        if (mapping.isDown()) {
            return false;
        }
        KeyMapping.click(mapping.getKey());
        return true;
    }

    public boolean hold(KeyMapping mapping) {
        if (mapping.isDown()) {
            return false;
        }
        mapping.setDown(true);
        heldMappings.add(mapping);
        return true;
    }

    public void release(KeyMapping mapping) {
        if (heldMappings.remove(mapping)) {
            mapping.setDown(false);
        }
    }

    public void releaseAll() {
        for (KeyMapping mapping : heldMappings) {
            mapping.setDown(false);
        }
        heldMappings.clear();
    }

    public boolean owns(KeyMapping mapping) {
        return heldMappings.contains(mapping);
    }

}
