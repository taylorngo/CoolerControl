package com.example.coolercontrol;

import java.util.HashMap;

public class ConnectionManager {
    private HashMap<Integer, String> messages = new HashMap<>();
    private int currentMarker = 0;

    public ConnectionManager() {
    }

    public void addMessage(String data) {
        if(!this.messages.containsValue(data)) {
            this.messages.put(Integer.valueOf(this.messages.size()), data);
        }

    }

    public byte removeMessage(String reply) {
        String tmp = this.retrieveAndAdvance();
        if(tmp == null) {
            return (byte)0;
        } else if(reply.equals(tmp)) {
            this.messages.remove(Integer.valueOf(this.currentMarker - 1));
            this.currentMarker = 0;
            return (byte)2;
        } else {
            return (byte)1;
        }
    }

    public String getReplay() {
        return this.retrieveAndAdvance();
    }

    private String retrieveAndAdvance() {
        if(this.messages.size() > 0) {
            if(this.currentMarker >= this.messages.size()) {
                this.currentMarker = 0;
                return null;
            } else {
                String msg = (String)this.messages.get(Integer.valueOf(this.currentMarker));
                ++this.currentMarker;
                return msg;
            }
        } else {
            return null;
        }
    }
}
