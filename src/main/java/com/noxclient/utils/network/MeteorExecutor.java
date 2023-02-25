/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.network;

import com.noxclient.utils.PreInit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MeteorExecutor {
    public static ExecutorService executor;

    @PreInit
    public static void init() {
        executor = Executors.newSingleThreadExecutor();
    }

    public static void execute(Runnable task) {
        executor.execute(task);
    }
}
