package com.q9e.antiafk;

import java.util.ArrayList;
import java.util.List;

public final class AutoLogoutSettings {
    public boolean enabled = false;
    public int detectionRangeBlocks = 10;
    public List<String> ignoredUsernames = new ArrayList<>();
}
