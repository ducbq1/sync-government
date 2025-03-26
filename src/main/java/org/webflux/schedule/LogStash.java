package org.webflux.schedule;

public class LogStash {
    private void Log() {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Log Stash");
                    }
                },
                5000
        );
    }
}
