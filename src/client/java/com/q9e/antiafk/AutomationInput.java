package com.q9e.antiafk;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.sdl.SDLMouse;

public final class AutomationInput {
    private final Set<KeyMapping> heldMappings = Collections.newSetFromMap(new IdentityHashMap<>());

    public boolean tap(KeyMapping mapping) {
        if (mapping.isDown() || mapping.isUnbound()) {
            return false;
        }
        mapping.setDown(true);
        heldMappings.add(mapping);
        return true;
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
            mapping.setDown(isPhysicalInputDown(mapping));
        }
    }

    public void releaseAll() {
        for (KeyMapping mapping : heldMappings) {
            mapping.setDown(isPhysicalInputDown(mapping));
        }
        heldMappings.clear();
    }

    public boolean owns(KeyMapping mapping) {
        return heldMappings.contains(mapping);
    }

    private static boolean isPhysicalInputDown(KeyMapping mapping) {
        InputConstants.Key key = InputConstants.getKey(mapping.saveString());
        if (key.getType() == InputConstants.Type.KEYBOARD) {
            return InputConstants.isKeyDown(key.getValue());
        }
        if (key.getType() == InputConstants.Type.MOUSE) {
            int buttonMask = 1 << (key.getValue() + 1);
            return (SDLMouse.SDL_GetMouseState(null, null) & buttonMask) != 0;
        }
        return false;
    }
}
