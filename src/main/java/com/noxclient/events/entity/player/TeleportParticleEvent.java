/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.noxclient.events.entity.player;

public class TeleportParticleEvent {
    private static final TeleportParticleEvent INSTANCE = new TeleportParticleEvent();

    public double x, y, z;

    public static TeleportParticleEvent get(double x, double y, double z) {
        INSTANCE.x = x;
        INSTANCE.y = y;
        INSTANCE.z = z;
        return INSTANCE;
    }
}
