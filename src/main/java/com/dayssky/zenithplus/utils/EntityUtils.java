package com.dayssky.zenithplus.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.dayssky.zenithplus.ZenithPlusClient;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class EntityUtils {
    private static final AtomicInteger FAKE_ENTITY_ID_COUNTER = new AtomicInteger(-1);
    private static final TreeMap<Long, Set<Entity>> scheduledRemovals = new TreeMap<>();
    private static final Map<Integer, Integer> fakeEntityGlowColors = new HashMap<>();

    private static boolean tickRegistered = false;
    private static long currentTick = 0;

    public static boolean isFakeEntity(Entity entity) {
        return fakeEntityGlowColors.containsKey(entity.getId());
    }

    public static Integer getGlowColor(Entity entity) {
        return fakeEntityGlowColors.get(entity.getId());
    }

    public static <T extends Mob> T spawnFakeEntity(
            EntityType<T> type,
            Vec3 pos,
            int ticks,
            boolean glowing,
            int glowColor
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return null;
        }

        T entity = type.create(mc.level);
        if (entity == null) {
            ZenithPlusClient.LOGGER.warn("[EntityUtils] Failed to create entity of type {}", type);
            return null;
        }

        entity.setPos(pos.x, pos.y, pos.z);
        entity.setNoAi(true);
        entity.setSilent(true);
        entity.setInvulnerable(true);
        entity.setInvisible(true);

        int fakeId = FAKE_ENTITY_ID_COUNTER.getAndDecrement();
        entity.setId(fakeId);

        if (glowing) {
            entity.setGlowingTag(true);
            fakeEntityGlowColors.put(fakeId, glowColor);
        }

        mc.level.addEntity(entity);

        long removalTick = currentTick + ticks;
        scheduledRemovals.computeIfAbsent(removalTick, k -> new HashSet<>()).add(entity);

        registerTickHandler();
        return entity;
    }

    private static void registerTickHandler() {
        if (tickRegistered) return;
        tickRegistered = true;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            currentTick++;

            NavigableMap<Long, Set<Entity>> expired = scheduledRemovals.headMap(currentTick, true);
            if (expired.isEmpty()) return;

            for (Set<Entity> entities : expired.values()) {
                for (Entity entity : entities) {
                    if (client.level != null) {
                        client.level.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
                    }
                    entity.discard();
                    fakeEntityGlowColors.remove(entity.getId());
                }
            }
            expired.clear();
        });
    }

    public static void cleanup() {
        Minecraft mc = Minecraft.getInstance();
        for (Set<Entity> entities : scheduledRemovals.values()) {
            for (Entity entity : entities) {
                if (mc.level != null) {
                    mc.level.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
                }
                entity.discard();
            }
        }

        scheduledRemovals.clear();
        fakeEntityGlowColors.clear();
        FAKE_ENTITY_ID_COUNTER.set(-1);
        currentTick = 0;
    }
}
