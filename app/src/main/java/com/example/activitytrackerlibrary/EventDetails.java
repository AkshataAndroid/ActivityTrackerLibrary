package com.example.activitytrackerlibrary;

public class EventDetails {
    private int count, firstTime, lastTime;
    private String name;

    public EventDetails(int count, int firstTime, int lastTime, String name) {
        this.count = count;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public int getFirstTime() {
        return firstTime;
    }

    public int getLastTime() {
        return lastTime;
    }

    public String getName() {
        return name;
    }
}

