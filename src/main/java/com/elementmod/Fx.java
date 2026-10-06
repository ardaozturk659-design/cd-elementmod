package com.elementmod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

/** Parçacık, yıldırım çizimi ve hasar yardımcıları. */
public class Fx {
    static final Random R = new Random();

    /** Renkli tek nokta (REDSTONE parçacığı: offset = RGB, count 0). */
    static void dot(WorldServer w, Element el, double x, double y, double z) {
        w.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 0, el.r, el.g, el.b, 1.0);
    }

    static void line(WorldServer w, Element el, Vec3d a, Vec3d b) {
        double len = a.distanceTo(b);
        int n = Math.max(1, (int) (len / 0.25));
        for (int i = 0; i <= n; i++) {
            double t = (double) i / n;
            dot(w, el, a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t);
        }
    }

    /** Gökten yere zikzaklı, elementin renginde yıldırım çizer. */
    static void lightning(WorldServer w, Element el, Vec3d top, Vec3d ground) {
        int seg = 12;
        Vec3d prev = top;
        for (int i = 1; i <= seg; i++) {
            double t = (double) i / seg;
            Vec3d p = new Vec3d(top.x + (ground.x - top.x) * t,
                                top.y + (ground.y - top.y) * t,
                                top.z + (ground.z - top.z) * t);
            if (i < seg) p = p.addVector((R.nextDouble() - 0.5) * 1.4, 0, (R.nextDouble() - 0.5) * 1.4);
            line(w, el, prev, p);
            line(w, el, prev.addVector(0.1, 0, 0.1), p.addVector(0.1, 0, 0.1)); // kalınlık
            prev = p;
        }
        for (int i = 0; i < 12; i++) { // yere çarpma kıvılcımı
            dot(w, el, ground.x + (R.nextDouble() - 0.5) * 2, ground.y + R.nextDouble(), ground.z + (R.nextDouble() - 0.5) * 2);
        }
    }

    static void thunder(WorldServer w, Vec3d at) {
        w.playSound(null, new BlockPos(at), SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, 1.2f, 1.0f);
    }

    static Vec3d center(Entity e) {
        return new Vec3d(e.posX, e.posY + e.height / 2.0, e.posZ);
    }

    /** Bakılan canlı (yoksa null). */
    static EntityLivingBase rayTarget(EntityPlayer p, double range) {
        Vec3d eye = p.getPositionEyes(1f);
        Vec3d look = p.getLookVec();
        Vec3d end = eye.add(look.scale(range));
        AxisAlignedBB box = p.getEntityBoundingBox().expand(look.x * range, look.y * range, look.z * range).grow(1);
        EntityLivingBase best = null;
        double bestD = range * range;
        for (Entity en : p.world.getEntitiesWithinAABBExcludingEntity(p, box)) {
            if (!(en instanceof EntityLivingBase)) continue;
            RayTraceResult r = en.getEntityBoundingBox().grow(0.3).calculateIntercept(eye, end);
            if (r != null) {
                double d = eye.squareDistanceTo(r.hitVec);
                if (d < bestD) { bestD = d; best = (EntityLivingBase) en; }
            }
        }
        return best;
    }

    /** Yıldırımın düşeceği zemin noktası. */
    static Vec3d groundAim(EntityPlayer p, double range) {
        EntityLivingBase t = rayTarget(p, range);
        if (t != null) return new Vec3d(t.posX, t.posY, t.posZ);
        RayTraceResult r = p.rayTrace(range, 1f);
        if (r != null && r.hitVec != null) return r.hitVec;
        return p.getPositionEyes(1f).add(p.getLookVec().scale(range));
    }

    /** Tek hedefe hasar + elementin etkisi. */
    static void hit(EntityLivingBase p, EntityLivingBase t, Element el, float dmg) {
        if (t == p) return;
        if (el == Element.LIGHT && t.isEntityUndead()) dmg *= 1.5f;
        t.attackEntityFrom(DamageSource.causeIndirectMagicDamage(p, p), dmg);
        switch (el) {
            case FIRE: t.setFire(6); break;
            case WATER: t.extinguish(); t.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 1)); break;
            case EARTH: t.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 2)); break;
            case ELECTRIC: t.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 20, 6)); break; // şok: kısa felç
            case LIGHT: t.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200, 0)); break;
            case DARK:
                t.addPotionEffect(new PotionEffect(MobEffects.WITHER, 80, 0));
                t.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 0));
                break;
        }
    }

    /** Alan hasarı; vurulan canlıların listesini döner. */
    static List<EntityLivingBase> area(EntityLivingBase p, WorldServer w, Vec3d c, double radius, float dmg, Element el) {
        List<EntityLivingBase> hit = new ArrayList<>();
        AxisAlignedBB box = new AxisAlignedBB(c.x - radius, c.y - radius, c.z - radius, c.x + radius, c.y + radius, c.z + radius);
        for (EntityLivingBase e : w.getEntitiesWithinAABB(EntityLivingBase.class, box)) {
            if (e == p) continue;
            if (center(e).distanceTo(c) <= radius) {
                hit(p, e, el, dmg);
                hit.add(e);
            }
        }
        return hit;
    }

    static void ring(WorldServer w, Element el, Vec3d c, double radius, double yOff) {
        for (int a = 0; a < 360; a += 10) {
            double rad = Math.toRadians(a);
            dot(w, el, c.x + Math.cos(rad) * radius, c.y + yOff, c.z + Math.sin(rad) * radius);
        }
    }
}
