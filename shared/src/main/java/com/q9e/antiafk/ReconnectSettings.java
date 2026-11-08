package com.q9e.antiafk;

public final class ReconnectSettings {
    public boolean enabled = false;
    public String serverAddress = "";
    public int firstRetryDelaySeconds = 5;
    public int subsequentRetryDelaySeconds = 15;
}
