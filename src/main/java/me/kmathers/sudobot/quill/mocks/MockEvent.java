package me.kmathers.sudobot.quill.mocks;

public class MockEvent {
    private final String eventName;
    private boolean cancelled;
    
    public MockEvent(String eventName) {
        this.eventName = eventName;
        this.cancelled = false;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    
    @Override
    public String toString() {
        return eventName + (cancelled ? " [CANCELLED]" : "");
    }
}