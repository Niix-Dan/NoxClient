package com.noxclient.utils.server;

import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class LegacyServerPinger {
    private static final AtomicInteger threadNumber = new AtomicInteger(0);
    private ServerInfo server;
    private boolean done = false;
    private boolean failed = false;

    public void ping(String ip) {
        ping(ip, 25565);
    }

    public void ping(String ip, int port) {
        server = new ServerInfo("", ip + ":" + port, false);

        new Thread(() -> pingInCurrentThread(ip, port),
            "Server Pinger #" + threadNumber.incrementAndGet()).start();
    }

    private void pingInCurrentThread(String ip, int port) {
        MultiplayerServerListPinger pinger = new MultiplayerServerListPinger();

        try {
            pinger.add(server, () -> {
            });
        } catch (UnknownHostException e) {
            failed = true;
        } catch (Exception e2) {
            failed = true;
        }

        pinger.cancel();
        done = true;
    }

    public boolean isStillPinging() {
        return !done;
    }

    public boolean isWorking() {
        return !failed;
    }

    public String getServerIP() {
        return server.address;
    }
}
