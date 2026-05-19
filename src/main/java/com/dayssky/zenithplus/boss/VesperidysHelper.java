package com.dayssky.zenithplus.boss;

import com.dayssky.zenithplus.ZenithPlusClient;
import com.dayssky.zenithplus.hud.HudCountdownTimer;
import com.dayssky.zenithplus.hud.HudEntry;
import com.dayssky.zenithplus.hud.HudManager;
import com.dayssky.zenithplus.utils.BossBarUtils;
import com.dayssky.zenithplus.utils.ChatUtils;
import com.dayssky.zenithplus.utils.CircleMath;
import com.dayssky.zenithplus.utils.EntityUtils;
import com.dayssky.zenithplus.utils.ParticleUtils;
import com.dayssky.zenithplus.utils.SoundUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import java.util.Set;
import java.util.function.Consumer;

public class VesperidysHelper {
    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    public static @Nullable WitherSkeleton vesperidys;
    public static ImmutableList<Vec3> platforms;
    private static Vec3 spawnLoc;

    private static final List<double[]> allMagusParticles = new CopyOnWriteArrayList<>();
    private static final Set<String> foundMagusPlatforms = ConcurrentHashMap.newKeySet();
    private static final Set<String> foundCrystalPlatforms = ConcurrentHashMap.newKeySet();
    private static final Set<Entity> fakeMaguses = ConcurrentHashMap.newKeySet();
    private static final Set<Entity> fakeCrystals = ConcurrentHashMap.newKeySet();
    private static final ConcurrentLinkedQueue<Vec3> magusSpawnQueue = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean magusSpawnScheduled = new AtomicBoolean(false);

    private static boolean inFight;

    private static final HudEntry fightTimerHud = new HudEntry(
        "vesperidysTimer",
        "Vesperidys Timer",
        100,
        120
    );
    private static final HudCountdownTimer fightTimer = new HudCountdownTimer(fightTimerHud, "VesperidysTimer");

    public static void register() {
        HudManager.register(fightTimerHud);

        BossBarUtils.onBossBar("vesperidys_timer", name -> {
            if (!name.contains("The Vesperidys")) {
                return;
            }

            inFight = true;

            if (ZenithPlusClient.getConfig().vesperidys.fightTimer) {
                fightTimer.start(12.5);
            }
        });

        ChatUtils.onTitle("vesperidys", text -> {
            if (text.contains("The Vesperidys")) {
                startFight();
            }
        });

        ChatUtils.onSubtitle("vesperidys_magus", text -> {
            if (text.contains("Defeat the Magus!")) {
                predictMagus();
            }
        });

        SoundUtils.onSound("vesperidys_crystal", packet -> {
            if (spawnLoc == null) return;
            if (packet.getSound().value() != SoundEvents.ENDER_CHEST_CLOSE) return;
            if (packet.getSource() != SoundSource.HOSTILE) return;
            if (Math.abs(packet.getVolume() - 1.0f) > 0.01f) return;
            if (Math.abs(packet.getPitch() - 0.5f) > 0.01f) return;

            predictCrystal();
        });


    }

    private static void startFight() {
        if (MINECRAFT.player == null || MINECRAFT.level == null) return;
        AABB detectionBox = MINECRAFT.player.getBoundingBox().inflate(50);

        var list = MINECRAFT.level.getEntitiesOfClass(WitherSkeleton.class, detectionBox, e -> {
            if (!e.hasCustomName()) return false;
            Component name = e.getCustomName();
            return name != null && "The Vesperidys".equals(name.getString());
        });
        if (list.isEmpty()) {
            return;
        }
        vesperidys = list.get(0);

        double bossX = vesperidys.getX();
        double bossY = vesperidys.getY();
        double bossZ = vesperidys.getZ();

        spawnLoc = new Vec3(Math.round(bossX), bossY - 5.0, Math.round(bossZ));

        ImmutableList.Builder<Vec3> builder = ImmutableList.builder();
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                builder.add(new Vec3(
                    spawnLoc.x + (i * 7),
                    spawnLoc.y - 1,
                    spawnLoc.z + (j * 7)
                ));
            }
        }
        platforms = builder.build();
    }

    private static void predictMagus() {
        allMagusParticles.clear();
        foundMagusPlatforms.clear();
        ParticleUtils.removeParticle("magus");
        if (!ZenithPlusClient.getConfig().vesperidys.magusDisplay.enabled) return;

        final long startTime = System.currentTimeMillis();

        ParticleUtils.onParticle("magus", packet -> {
            if (foundMagusPlatforms.size() >= 2) return;

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > 200) {
                ParticleUtils.removeParticle("magus");
                if (!allMagusParticles.isEmpty()) {
                    analyzeMagus();
                }
                return;
            }

            double[] particle = filterParticle(packet, ParticleTypes.SOUL_FIRE_FLAME);
            if (particle == null) return;

            allMagusParticles.add(particle);

            if (hasTwoTicks(allMagusParticles, 4)) {
                ParticleUtils.removeParticle("magus");
                analyzeMagus();
            }
        });
    }

    private static boolean hasTwoTicks(List<double[]> particles, int minParticlesPerGroup) {
        // when to start finding magus
        List<List<double[]>> yGroups = CircleMath.groupByYLevel(particles);
        yGroups.removeIf(g -> g.size() < minParticlesPerGroup);

        Set<Integer> foundTicks = new HashSet<>();

        for (List<double[]> group : yGroups) {
            List<double[]> circles = CircleMath.fitCircles(group, 4);
            for (double[] circle : circles) {
                foundTicks.add((int) circle[2]);
                if (foundTicks.size() >= 2) return true;
            }
        }
        return false;
    }

    private static void analyzeMagus() {
        List<double[]> particles = new ArrayList<>(allMagusParticles);
        analyzeCircles(particles, foundMagusPlatforms, 2,
            VesperidysHelper::displayMagus);
    }

    private static void analyzeCircles(List<double[]> particles, Set<String> foundPlatforms, int maxPlatforms,
            Consumer<int[]> displayFunc) {
        // try and match circles with each other by radius to attempt to find path
        List<List<double[]>> yGroups = CircleMath.groupByYLevel(particles);
        yGroups.removeIf(g -> g.size() < 4);

        if (yGroups.isEmpty()) {
            return;
        }

        List<double[]> allCircles = new ArrayList<>();
        for (List<double[]> group : yGroups) {
            List<double[]> circles = CircleMath.fitCircles(group, maxPlatforms * 2);
            allCircles.addAll(circles);
        }

        if (allCircles.size() < 2) {
            return;
        }

        allCircles.sort((a, b) -> Double.compare(a[2], b[2]));

        Set<double[]> usedCircles = new HashSet<>();
        double[] startloc = null;

        while (foundPlatforms.size() < maxPlatforms) {
            List<double[]> unusedCircles = new ArrayList<>();
            for (double[] c : allCircles) {
                if (!usedCircles.contains(c)) unusedCircles.add(c);
            }

            if (unusedCircles.isEmpty()) break;

            double[] foundVelocity = null;
            double[] foundEndloc = null;
            double[] refCircle = null;
            // oh no
            if (startloc == null) {
                outer:
                for (int i = 0; i < unusedCircles.size(); i++) {
                    for (int j = i + 1; j < unusedCircles.size(); j++) {
                        double[] ci = unusedCircles.get(i);
                        double[] cj = unusedCircles.get(j);

                        if ((int) ci[2] == (int) cj[2]) continue;

                        double[] result = tryMatchCircles(ci, cj);
                        if (result != null) {
                            foundVelocity = new double[]{result[0], result[1]};
                            foundEndloc = new double[]{result[2], result[3]};
                            refCircle = ci[2] < cj[2] ? ci : cj;

                            startloc = new double[]{
                                refCircle[0] - foundVelocity[0] * refCircle[2],
                                refCircle[1] - foundVelocity[1] * refCircle[2]
                            };

                            break outer; // im evil
                        }
                    }
                }

                if (foundEndloc == null) {
                    break;
                }
            } else {
                double[] c = unusedCircles.get(0);
                int tick = (int) c[2];
                if (Math.abs((double) tick) < 1e-9) {
                    usedCircles.add(c);
                    continue;
                }

                double dx = (c[0] - startloc[0]) / tick;
                double dz = (c[1] - startloc[1]) / tick;
                double platformX = startloc[0] + dx * 35;
                double platformZ = startloc[1] + dz * 35;

                int[] platform = isPlatform(platformX, platformZ);
                if (platform != null) {
                    foundVelocity = new double[]{dx, dz};
                    foundEndloc = new double[]{platformX, platformZ};
                    refCircle = c;

                } else {
                    usedCircles.add(c);
                    continue;
                }
            }

            if (foundEndloc == null) break;

            int[] platform = isPlatform(foundEndloc[0], foundEndloc[1]);
            if (platform != null) {
                String key = platform[0] + "," + platform[1];
                if (foundPlatforms.add(key)) {
                    displayFunc.accept(platform);
                }
            }

            for (double[] c : unusedCircles) {
                if (CircleMath.fitsTrajectory(c, refCircle, foundVelocity)) {
                    usedCircles.add(c);
                }
            }

            usedCircles.add(refCircle);
        }
    }

    private static double[] tryMatchCircles(double[] c1, double[] c2) {
        double tickDiff = c2[2] - c1[2];
        double dx = (c2[0] - c1[0]) / tickDiff;
        double dz = (c2[1] - c1[1]) / tickDiff;

        double ticksToEnd = 35 - c1[2];
        double destX = c1[0] + dx * ticksToEnd;
        double destZ = c1[1] + dz * ticksToEnd;

        int[] platform = isPlatform(destX, destZ);
        if (platform != null) {
            return new double[]{dx, dz, destX, destZ};
        }
        return null;
    }

    private static int[] isPlatform(double x, double z) {
        double _i = (x - spawnLoc.x) / 7.0;
        double _j = (z - spawnLoc.z) / 7.0;
        int i = (int) Math.round(_i);
        int j = (int) Math.round(_j);

        if (i < -2 || i > 2 || j < -2 || j > 2 || (i == 0 && j == 0)) {
            return null;
        }

        double platformX = spawnLoc.x + i * 7.0;
        double platformZ = spawnLoc.z + j * 7.0;
        double distX = Math.abs(x - platformX);
        double distZ = Math.abs(z - platformZ);

        if (distX > 0.1 || distZ > 0.1) {
            return null;
        }

        return new int[]{i, j};
    }

    private static final List<double[]> allCrystalParticles = new CopyOnWriteArrayList<>();

    private static void predictCrystal() {
        allCrystalParticles.clear();
        foundCrystalPlatforms.clear();
        ParticleUtils.removeParticle("crystal");
        if (!ZenithPlusClient.getConfig().vesperidys.crystalDisplay.enabled) return;

        final long startTime = System.currentTimeMillis();

        ParticleUtils.onParticle("crystal", packet -> {
            if (foundCrystalPlatforms.size() >= 4) return;

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > 200) {
                ParticleUtils.removeParticle("crystal");
                if (!allCrystalParticles.isEmpty()) {
                    analyzeCrystal();
                }
                return;
            }

            double[] particle = filterParticle(packet, ParticleTypes.END_ROD);
            if (particle == null) return;

            allCrystalParticles.add(particle);

            if (hasTwoTicks(allCrystalParticles, 4)) {
                ParticleUtils.removeParticle("crystal");
                analyzeCrystal();
            }
        });
    }

    private static void analyzeCrystal() {
        List<double[]> particles = new ArrayList<>(allCrystalParticles);
        analyzeCircles(particles, foundCrystalPlatforms, 4,
            VesperidysHelper::displayCrystal);
    }

    private static double @Nullable[] filterParticle(ClientboundLevelParticlesPacket packet,
            net.minecraft.core.particles.ParticleType<?> expectedType) {
        if (spawnLoc == null) return null;
        if (packet.getParticle().getType() != expectedType) return null;
        if (packet.getCount() != 1) return null;
        if (Math.abs(packet.getMaxSpeed() - 10000000.0f) > 0.4f) return null;

        double y = packet.getY();
        if (y > spawnLoc.y + 6.0) return null;

        return new double[]{packet.getX(), y, packet.getZ()};
    }

    private static void scheduleMagusSpawns() {
        if (!magusSpawnScheduled.compareAndSet(false, true)) return;

        MINECRAFT.execute(() -> {
            try {
                Vec3 magusLoc;
                while ((magusLoc = magusSpawnQueue.poll()) != null) {
                    var entity = EntityUtils.spawnFakeEntity(EntityType.WITHER_SKELETON, magusLoc, 39, true, ZenithPlusClient.getConfig().vesperidys.magusDisplay.glowColor);
                    if (entity != null) {
                        fakeMaguses.add(entity);
                    }
                }
            } finally {
                magusSpawnScheduled.set(false);
                if (!magusSpawnQueue.isEmpty()) {
                    scheduleMagusSpawns();
                }
            }
        });
    }

    private static void displayMagus(int[] platform) {
        Vec3 magusLoc = new Vec3(
            spawnLoc.x + platform[0] * 7,
            spawnLoc.y + 1,
            spawnLoc.z + platform[1] * 7
        );
        magusSpawnQueue.add(magusLoc);
        scheduleMagusSpawns();
    }

    private static void displayCrystal(int[] platform) {
        Vec3 crystalLoc = new Vec3(
            spawnLoc.x + platform[0] * 7 + 0.5,
            spawnLoc.y + 1,
            spawnLoc.z + platform[1] * 7 + 0.5
        );
        MINECRAFT.execute(() -> {
            var entity = EntityUtils.spawnFakeEntity(EntityType.SHULKER, crystalLoc, 39, true, ZenithPlusClient.getConfig().vesperidys.crystalDisplay.glowColor);
            if (entity != null) {
                fakeCrystals.add(entity);
            }
        });
    }

    public static ParticleOptions recolorParticle(ParticleOptions options) {
        var config = ZenithPlusClient.getConfig().vesperidys;
        if (!inFight || !config.tpHighlight.enabled) {
            return options;
        }

        if (!(options instanceof DustParticleOptions dust) || dust.getScale() != 0.5F) {
            return options;
        }

        Vector3f color = dust.getColor();
        if (color.x() != 1.0F || color.y() != 0.0F || color.z() != 0.0F) {
            return options;
        }

        int particleColor = config.tpHighlight.color & 0xFFFFFF;
        float size = Mth.clamp(config.tpHighlight.size, 0.5F, 5F);
        float red = (particleColor >> 16 & 0xFF) / 255.0F;
        float green = (particleColor >> 8 & 0xFF) / 255.0F;
        float blue = (particleColor & 0xFF) / 255.0F;
        return new DustParticleOptions(new Vector3f(red, green, blue), size);
    }

    public static void cleanup() {
        fightTimer.stop();
        inFight = false;
        vesperidys = null;
        spawnLoc = null;
        platforms = null;
        foundMagusPlatforms.clear();
        foundCrystalPlatforms.clear();
        allMagusParticles.clear();
        allCrystalParticles.clear();
        magusSpawnQueue.clear();
        magusSpawnScheduled.set(false);
        fakeMaguses.clear();
        fakeCrystals.clear();
        ParticleUtils.removeParticle("magus");
        ParticleUtils.removeParticle("crystal");
    }
}
