package com.q9e.antiafk;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class AutomationInput {
    private final Set<KeyMapping> heldMappings = Collections.newSetFromMap(new IdentityHashMap<>());

    public boolean tap(KeyMapping mapping) {
        if (mapping.isDown()) {
            return false;
        }
        return queueClick(mapping);
    }

    public boolean queueClick(KeyMapping mapping) {
        if (mapping.isUnbound()) {
            return false;
        }
        KeyMapping.click(InputConstants.getKey(mapping.saveString()));
        return true;
    }

    public boolean hold(KeyMapping mapping) {
        if (mapping.isDown() || mapping.isUnbound()) {
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
