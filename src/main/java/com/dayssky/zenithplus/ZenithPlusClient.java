package com.dayssky.zenithplus;

import com.dayssky.zenithplus.boss.BroodHelper;
import com.dayssky.zenithplus.boss.VesperidysHelper;
import com.dayssky.zenithplus.config.ZenithPlusConfig;
import com.dayssky.zenithplus.utils.ChatUtils;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZenithPlusClient implements ClientModInitializer {
    public static final String MOD_ID = "zenithplus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ResourceKey<Level> lastDimension = null;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ZenithPlusConfig.class, GsonConfigSerializer::new);

        ChatUtils.register();
        VesperidysHelper.register();
        BroodHelper.register();
        registerDimensionChangeHandler();
    }


    private void registerDimensionChangeHandler() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                lastDimension = null;
                return;
            }

            ResourceKey<Level> currentDimension = client.level.dimension();
            if (lastDimension != null && !lastDimension.equals(currentDimension)) {
                VesperidysHelper.cleanup();
                BroodHelper.cleanup();
            }
            lastDimension = currentDimension;
        });
    }

    public static ZenithPlusConfig getConfig() {
        return AutoConfig.getConfigHolder(ZenithPlusConfig.class).getConfig();
    }
}
