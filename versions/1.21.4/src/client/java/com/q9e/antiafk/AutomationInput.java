package com.q9e.antiafk;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

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

    public void releaseAll(boolean preservePhysicalInput) {
        for (KeyMapping mapping : heldMappings) {
            mapping.setDown(preservePhysicalInput && isPhysicalInputDown(mapping));
        }
        heldMappings.clear();
    }

    public boolean owns(KeyMapping mapping) {
        return heldMappings.contains(mapping);
    }

    private static boolean isPhysicalInputDown(KeyMapping mapping) {
        InputConstants.Key key = InputConstants.getKey(mapping.saveString());
        if (key.getType() == InputConstants.Type.KEYSYM) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getValue());
        }
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), key.getValue()) == GLFW.GLFW_PRESS;
        }
        return false;
    }
}
