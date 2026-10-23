package com.zakhar.aegistorch.proxy;

import com.zakhar.aegistorch.client.ZoneRenderer;

import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(new ZoneRenderer());
    }
}
