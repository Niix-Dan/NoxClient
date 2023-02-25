package com.noxclient.utils.server;

public interface IServerFinderDoneListener {
    void onServerDone(ServerPinger pinger);

    void onServerFailed(ServerPinger pinger);
}
