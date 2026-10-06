package com.elementmod;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class Spell {
    public final Element el;
    public final SpellType type;
    public final String name;

    public Spell(Element el, SpellType type, String name) {
        this.el = el; this.type = type; this.name = name;
    }

    public int cooldown() { return type.cooldown; }

    public void cast(EntityPlayer p) {
        if (p.world.isRemote) return;
        WorldServer w = (WorldServer) p.world;
        p.sendStatusMessage(new TextComponentString(el.fmt + name), true);
        Vec3d eye = p.getPositionEyes(1f);
        Vec3d look = p.getLookVec();

        switch (type) {
            case BOLT: {
                EntityLivingBase t = Fx.rayTarget(p, 30);
                Vec3d from = eye.add(look.scale(0.5));
                Vec3d to = t != null ? Fx.center(t) : eye.add(look.scale(30));
                Fx.line(w, el, from, to);
                if (t != null) Fx.hit(p, t, el, 6f);
                break;
            }
            case LIGHTNING: {
                Vec3d g = Fx.groundAim(p, 40);
                Fx.lightning(w, el, g.addVector(0, 14, 0), g);
                Fx.thunder(w, g);
                Fx.area(p, w, g, 2.5, 10f, el);
                break;
            }
            case STORM: {
                Vec3d g = Fx.groundAim(p, 35);
                for (int i = 0; i < 6; i++) {
                    Vec3d s = g.addVector((Fx.R.nextDouble() - 0.5) * 10, 0, (Fx.R.nextDouble() - 0.5) * 10);
                    Fx.lightning(w, el, s.addVector(0, 14, 0), s);
                    Fx.area(p, w, s, 2.0, 6f, el);
                }
                Fx.thunder(w, g);
                break;
            }
            case NOVA: {
                Vec3d c = p.getPositionVector();
                for (double r = 1; r <= 6; r += 1.5) Fx.ring(w, el, c, r, 0.3);
                List<EntityLivingBase> hit = Fx.area(p, w, c, 6, 8f, el);
                for (EntityLivingBase e : hit) {
                    double dx = e.posX - p.posX, dz = e.posZ - p.posZ;
                    double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                    e.motionX += dx / len * 1.0;
                    e.motionZ += dz / len * 1.0;
                    e.motionY += 0.4;
                    e.velocityChanged = true;
                }
                Fx.thunder(w, c);
                break;
            }
            case BEAM: {
                Vec3d from = eye.add(look.scale(0.5));
                Vec3d to = eye.add(look.scale(25));
                Fx.line(w, el, from, to);
                for (EntityLivingBase e : w.getEntitiesWithinAABB(EntityLivingBase.class,
                        p.getEntityBoundingBox().expand(look.x * 25, look.y * 25, look.z * 25).grow(2))) {
                    if (e == p) continue;
                    if (e.getEntityBoundingBox().grow(0.5).calculateIntercept(from, to) != null) Fx.hit(p, e, el, 9f);
                }
                break;
            }
            case SHIELD: {
                p.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 300, 1));
                p.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 300, 2));
                Vec3d c = p.getPositionVector();
                for (double y = 0; y <= 2; y += 0.5) Fx.ring(w, el, c, 1.2, y);
                break;
            }
            case HEAL: {
                Vec3d c = p.getPositionVector();
                if (el == Element.DARK) { // Ruh Emme: çevreden can çalar
                    List<EntityLivingBase> hit = Fx.area(p, w, c, 6, 4f, el);
                    p.heal(4f * hit.size());
                    Fx.ring(w, el, c, 6, 0.5);
                } else {
                    p.heal(10f);
                    p.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 1));
                    for (double y = 0; y <= 2; y += 0.25) Fx.ring(w, el, c, 0.8, y);
                }
                break;
            }
            case HASTE: {
                p.addPotionEffect(new PotionEffect(MobEffects.SPEED, 400, 1));
                p.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 400, 1));
                switch (el) {
                    case WATER: p.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 400, 0)); break;
                    case FIRE: p.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 400, 0)); break;
                    case EARTH: p.addPotionEffect(new PotionEffect(MobEffects.HASTE, 400, 1)); break;
                    case LIGHT: p.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 400, 0)); break;
                    case DARK: p.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 400, 0)); break;
                    default: break;
                }
                Fx.ring(w, el, p.getPositionVector(), 1.0, 0.2);
                break;
            }
            case CURSE: {
                EntityLivingBase t = Fx.rayTarget(p, 30);
                if (t == null) { p.sendStatusMessage(new TextComponentString("Hedef yok"), true); break; }
                Fx.line(w, el, eye.add(look.scale(0.5)), Fx.center(t));
                Fx.hit(p, t, el, 3f);
                t.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 200, 1));
                t.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 200, 1));
                break;
            }
            case LEAP: {
                p.motionX = look.x * 1.8;
                p.motionY = 0.6 + look.y * 0.8;
                p.motionZ = look.z * 1.8;
                p.velocityChanged = true;
                p.fallDistance = 0;
                Fx.ring(w, el, p.getPositionVector(), 1.0, 0.1);
                break;
            }
            case BARRAGE: {
                Vec3d g = Fx.groundAim(p, 30);
                for (int i = 0; i < 10; i++) {
                    Vec3d s = g.addVector((Fx.R.nextDouble() - 0.5) * 16, 0, (Fx.R.nextDouble() - 0.5) * 16);
                    Fx.lightning(w, el, s.addVector(0, 10, 0), s);
                    Fx.area(p, w, s, 1.5, 5f, el);
                }
                Fx.thunder(w, g);
                break;
            }
            case ULTIMATE: {
                Vec3d g = Fx.groundAim(p, 40);
                Fx.lightning(w, el, g.addVector(0, 20, 0), g);
                Fx.area(p, w, g, 4.0, 20f, el);
                for (int i = 0; i < 14; i++) {
                    Vec3d s = g.addVector((Fx.R.nextDouble() - 0.5) * 20, 0, (Fx.R.nextDouble() - 0.5) * 20);
                    Fx.lightning(w, el, s.addVector(0, 16, 0), s);
                    Fx.area(p, w, s, 3.0, 12f, el);
                }
                Fx.thunder(w, g);
                break;
            }
        }
    }
}
