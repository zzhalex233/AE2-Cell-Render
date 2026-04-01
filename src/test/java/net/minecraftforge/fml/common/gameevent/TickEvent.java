package net.minecraftforge.fml.common.gameevent;

import net.minecraftforge.fml.common.eventhandler.Event;

public class TickEvent extends Event {
    public enum Phase {
        START,
        END
    }

    public static class ServerTickEvent extends TickEvent {
        public final Phase phase;

        public ServerTickEvent(Phase phase) {
            this.phase = phase;
        }
    }
}
