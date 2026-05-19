package com.dayssky.zenithplus.clear;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dayssky.zenithplus.ZenithPlusClient;
import com.dayssky.zenithplus.config.ZenithPlusConfig;
import com.dayssky.zenithplus.hud.HudEntry;
import com.dayssky.zenithplus.hud.HudManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SidearmHelper {
    private static final HashMap<String, EffectInfo> effects = new HashMap<>();
    private static final Pattern SIDEARM_DAMAGE = Pattern.compile("dealing (\\d+(?:\\.\\d+)?) projectile damage");

    private static double baseDamage = 0;
    private static double damage = 0;
    private static boolean hasSidearm = false;
    private static int tick = 0;
    private static final HudEntry damageHud = new HudEntry("sidearmDamageHud", "Sidearm Damage HUD", 320, 210);

    public static void register() {
        HudManager.register(damageHud);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                effects.clear();
                hasSidearm = false;
                tick = 0;
                damage = baseDamage;
                updateDamageHud();
                return;
            }

            if (++tick % 5 != 0) return;

            hasSidearm = hasSidearm();
            if (!hasSidearm) {
                damageHud.hide();
                return;
            }

            readEffects();
            updateDamageHud();
        });
    }

    public static boolean isOneShot(Entity entity) {
        ZenithPlusConfig.Clear.SidearmOneShot config = ZenithPlusClient.getConfig().clear.sidearmOneShot;
        if (!config.enabled || !hasSidearm) return false;
        if (!(entity instanceof Mob)) return false;
        if (!(entity instanceof LivingEntity le)) return false;
        if (!entity.isAlive()) return false;
        if (!canSee(entity)) return false;
        updateDamage(le);
        return le.getHealth() + le.getAbsorptionAmount() <= damage;
    }

    public static void handleSkillSelectionClick(ItemStack item) {
        if (item.isEmpty()) return;
        if (!"Sidearm".equals(item.getHoverName().getString())) return;

        Minecraft client = Minecraft.getInstance();
        List<Component> lines = item.getTooltipLines(client.player, TooltipFlag.NORMAL);
        for (Component line : lines) {
            Matcher matcher = SIDEARM_DAMAGE.matcher(line.getString());
            if (!matcher.find()) continue;

            baseDamage = Double.parseDouble(matcher.group(1));
            updateDamage(null);
            return;
        }
    }

    public static int getGlowColor() {
        return ZenithPlusClient.getConfig().clear.sidearmOneShot.color;
    }

    private static boolean hasSidearm() {
        try {
            Object abilityHandler = Class.forName("ch.njol.unofficialmonumentamod.UnofficialMonumentaModClient").getField("abilityHandler").get(null);
            Object data = abilityHandler.getClass().getField("abilityData").get(abilityHandler);
            if (!(data instanceof Collection<?> abilities)) return false;

            synchronized (abilityHandler) {
                for (Object ability : abilities) {
                    if ("Sidearm".equals(field(ability, "name"))) {
                        return true;
                    }
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return false;
    }

    private static boolean canSee(Entity entity) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return false;

        Vec3 start = client.player.getEyePosition();
        Vec3 end = entity.getBoundingBox().getCenter();
        HitResult hit = client.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, client.player));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceTo(start) >= end.distanceTo(start) - 0.1;
    }

    private static void updateDamage(LivingEntity target) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        double projectile = stat(client.player.getInventory().getSelected(), "Attributes", "Projectile Damage Multiply", "multiply", "mainhand")
            + stat(client.player.getOffhandItem(), "Attributes", "Projectile Damage Multiply", "multiply", "offhand");
        double firstStrike = stat(client.player.getInventory().getSelected(), "Enchantments", "First Strike", null, null)
            + stat(client.player.getOffhandItem(), "Enchantments", "First Strike", null, null);
        double regicide = stat(client.player.getInventory().getSelected(), "Enchantments", "Regicide", null, null)
            + stat(client.player.getOffhandItem(), "Enchantments", "Regicide", null, null);
        double focus = stat(client.player.getInventory().getSelected(), "Infusions", "Focus", null, null)
            + stat(client.player.getOffhandItem(), "Infusions", "Focus", null, null);
        String[] slots = {"feet", "legs", "chest", "head"};
        for (int i = 0; i < client.player.getInventory().armor.size(); i++) {
            ItemStack item = client.player.getInventory().armor.get(i);
            projectile += stat(item, "Attributes", "Projectile Damage Multiply", "multiply", slots[i]);
            firstStrike += stat(item, "Enchantments", "First Strike", null, null);
            regicide += stat(item, "Enchantments", "Regicide", null, null);
            focus += stat(item, "Infusions", "Focus", null, null);
        }

        double gearDamage = 0;
        double strength = 0;
        double projectileDamage = 0;
        for (EffectInfo effect : effects.values()) {
            if (!effect.positive || !effect.percentage) continue;
            if ("Gear Damage Dealt".equals(effect.name) || "Projectile Gear Damage Dealt".equals(effect.name)) {
                gearDamage += effect.power / 100.0;
            }
            if ("damage".equals(effect.name) || "Damage".equals(effect.name)) {
                strength += effect.power / 100.0;
            }
            if ("Projectile Damage".equals(effect.name) || "Projectile Damage Dealt".equals(effect.name)) {
                projectileDamage += effect.power / 100.0;
            }
        }

        damage = baseDamage * (1 + projectile + firstStrike * 0.10 + (target != null && isElite(target) ? regicide * 0.10 : 0) + focus * 0.015 + gearDamage) * (1 + strength + projectileDamage);
    }

    private static boolean isElite(LivingEntity entity) {
        if (entity.getTags().contains("Elite") || entity.getTags().contains("Boss")) return true;
        if (isEliteName(entity.getCustomName())) return true;
        if (isEliteName(entity.getDisplayName())) return true;
        if (isEliteName(entity.getName())) return true;
        return false;
    }

    private static boolean isEliteName(Component name) {
        if (name == null) return false;
        String text = name.getString();
        for (int i = 0; i + 1 < text.length(); i++) {
            if (text.charAt(i) != ChatFormatting.PREFIX_CODE) continue;
            char code = Character.toLowerCase(text.charAt(i + 1));
            if (code == '6' || code == 'e' || code == 'l') return true;
        }
        return false;
    }

    private static double stat(ItemStack item, String group, String name, String operation, String slot) {
        if (item.isEmpty()) return 0;
        CompoundTag tag = item.getTag();
        if (tag == null) return 0;
        CompoundTag monumenta = tag.getCompound("Monumenta");

        if ("Attributes".equals(group)) {
            ListTag attributes = monumenta.getCompound("Stock").getList("Attributes", 10);
            for (int i = 0; i < attributes.size(); i++) {
                CompoundTag attribute = attributes.getCompound(i);
                if (name.equals(attribute.getString("AttributeName"))
                    && operation.equals(attribute.getString("Operation"))
                    && slot.equals(attribute.getString("Slot"))) {
                    return attribute.getDouble("Amount");
                }
            }
            return 0;
        }

        CompoundTag stats = monumenta.getCompound(group);
        if (stats.isEmpty()) stats = monumenta.getCompound("Stock").getCompound(group);
        if (stats.isEmpty()) stats = monumenta.getCompound("PlayerModified").getCompound(group);
        if (!stats.contains(name)) return 0;
        return stats.getCompound(name).getDouble("Level");
    }

    private static Object field(Object object, String name) throws ReflectiveOperationException {
        Field field = object.getClass().getField(name);
        return field.get(object);
    }

    private static boolean saveEffects(Collection<?> next) throws ReflectiveOperationException {
        boolean changed = effects.size() != next.size();
        HashMap<String, EffectInfo> fresh = new HashMap<>();
        for (Object effect : next) {
            EffectInfo info = new EffectInfo();
            info.uuid = String.valueOf(field(effect, "uuid"));
            info.name = String.valueOf(field(effect, "name"));
            info.duration = ((Number) field(effect, "effectTime")).intValue();
            info.power = ((Number) field(effect, "effectPower")).doubleValue();
            info.positive = (boolean) field(effect, "positiveEffect");
            info.percentage = (boolean) field(effect, "isPercentage");
            fresh.put(info.uuid, info);

            EffectInfo old = effects.get(info.uuid);
            if (old == null || !old.matches(info)) changed = true;
        }

        if (!changed) return false;
        effects.clear();
        effects.putAll(fresh);
        return true;
    }

    private static class EffectInfo {
        private String uuid;
        private String name;
        private int duration;
        private double power;
        private boolean positive;
        private boolean percentage;

        private boolean matches(EffectInfo effect) {
            return uuid.equals(effect.uuid)
                && name.equals(effect.name)
                && duration == effect.duration
                && power == effect.power
                && positive == effect.positive
                && percentage == effect.percentage;
        }
    }

    private static void updateDamageHud() {
        ZenithPlusConfig.Clear.SidearmOneShot config = ZenithPlusClient.getConfig().clear.sidearmOneShot;
        if (!config.damageHud || !hasSidearm) {
            damageHud.hide();
            return;
        }
        updateDamage(null);
        damageHud.set(String.format("Sidearm: %.2f", damage), config.color);
        damageHud.show();
    }

    private static void readEffects() {
        try {
            Object overlay = Class.forName("ch.njol.unofficialmonumentamod.UnofficialMonumentaModClient").getField("effectOverlay").get(null);
            Method getCumulativeEffects = overlay.getClass().getMethod("getCumulativeEffects");
            Object result = getCumulativeEffects.invoke(overlay);
            if (!(result instanceof Collection<?> next)) return;
            if (!saveEffects(next)) return;

            updateDamage(null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
