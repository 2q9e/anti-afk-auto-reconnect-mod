package com.q9e.antiafk;

import net.minecraft.client.multiplayer.ClientPacketListener;

public final class ClientChatSendGate {
    private boolean sentThisTick;

    public void beginTick() {
        sentThisTick = false;
    }

    public boolean trySendChat(ClientPacketListener connection, String message) {
        if (sentThisTick || connection == null) {
            return false;
        }
        connection.sendChat(message);
        sentThisTick = true;
        return true;
    }

    public boolean trySendCommand(ClientPacketListener connection, String command) {
        if (sentThisTick || connection == null) {
            return false;
        }
        connection.sendCommand(command);
        sentThisTick = true;
        return true;
    }
}