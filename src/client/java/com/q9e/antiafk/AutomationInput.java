package com.q9e.antiafk;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.client.KeyMapping;

public final class AutomationInput {
    private final Set<KeyMapping> heldMappings = Collections.newSetFromMap(new IdentityHashMap<>());
}
