package com.elementmod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/** Element koruyucusu (boss). 6 elementin hepsi bu sınıftan türer. */
public abstract class EntityElementBoss extends EntityMob {
    private static final DataParameter<Integer> CAST = EntityDataManager.createKey(EntityElementBoss.class, DataSerializers.VARINT);

    public final Element el;
    private final BossInfoServer bossInfo;

    private int attackCd = 60;
    private int pendingTimer = 0;
    private int mode = 0; // 0 tek yıldırım, 1 nova, 2 fırtına, 3 öfke
    private float pendingDmg = 8f;
    private double pendingRadius = 2.5;
    private final List<Vec3d> pending = new ArrayList<>();

    public EntityElementBoss(World w, Element el) {
        super(w);
        this.el = el;
        float s = scaleOf(el);
        setSize(0.85f * s, 1.5f * s);
        this.isImmuneToFire = (el == Element.FIRE);
        this.experienceValue = 0;
        enablePersistence();
        bossInfo = new BossInfoServer(new TextComponentString(el.fmt + el.title + " Koruyucusu"), colorOf(el), BossInfo.Overlay.NOTCHED_10);
    }

    public static float scaleOf(Element e) {
        switch (e) {
            case EARTH: return 2.6f;
            case DARK: return 2.2f;
            case ELECTRIC: return 2.2f;
            default: return 2.2f;
        }
    }

    private static BossInfo.Color colorOf(Element e) {
        switch (e) {
            case WATER: return BossInfo.Color.BLUE;
            case FIRE: return BossInfo.Color.RED;
            case EARTH: return BossInfo.Color.GREEN;
            case ELECTRIC: return BossInfo.Color.WHITE;
            case LIGHT: return BossInfo.Color.YELLOW;
            default: return BossInfo.Color.PURPLE;
        }
    }

    public int getCast() { return dataManager.get(CAST); }

    @Override protected void entityInit() {
        super.entityInit();
        dataManager.register(CAST, 0);
    }

    @Override public String getName() { return el == null ? "Koruyucu" : el.title + " Koruyucusu"; }

    @Override protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        tasks.addTask(7, new EntityAIWander(this, 0.8D));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 32.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.27D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.8D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
    }

    @Override public boolean isNonBoss() { return false; }

    // ---- boss bar ----
    @Override public void addTrackingPlayer(EntityPlayerMP p) { super.addTrackingPlayer(p); bossInfo.addPlayer(p); }
    @Override public void removeTrackingPlayer(EntityPlayerMP p) { super.removeTrackingPlayer(p); bossInfo.removePlayer(p); }
    @Override protected void updateAITasks() {
        super.updateAITasks();
        bossInfo.setPercent(getHealth() / getMaxHealth());
    }

    // ---- ses ----
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_WITHER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.ENTITY_WITHER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_WITHER_DEATH; }
    @Override protected float getSoundPitch() { return 0.5f + el.ordinal() * 0.08f; }

    // ---- ödül ----
    @Override protected void dropFewItems(boolean recentlyHit, int looting) {
        entityDropItem(new ItemStack(ModItems.WANDS[el.ordinal()]), 0f);
    }

    // ---- yavaş ölüm animasyonu (5 sn): yere yıkılır, çevresine yıldırımlar düşer ----
    @Override protected void onDeathUpdate() {
        ++deathTime;
        if (!world.isRemote) {
            WorldServer ws = (WorldServer) world;
            if (deathTime % 8 == 0) {
                Vec3d g = new Vec3d(posX + (rand.nextDouble() - 0.5) * 8, posY, posZ + (rand.nextDouble() - 0.5) * 8);
                Fx.lightning(ws, el, g.addVector(0, 14, 0), g);
                Fx.thunder(ws, g);
            }
            if (deathTime >= 100) {
                int xp = 600;
                while (xp > 0) {
                    int i = EntityXPOrb.getXPSplit(xp);
                    xp -= i;
                    world.spawnEntity(new EntityXPOrb(world, posX, posY, posZ, i));
                }
                setDead();
            }
        }
    }

    // ---- ana döngü ----
    @Override public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) { aura(); return; }
        if (getHealth() <= 0) return;
        WorldServer ws = (WorldServer) world;

        int c = dataManager.get(CAST);
        if (c > 0) dataManager.set(CAST, c - 1);

        if (pendingTimer > 0) {
            if (pendingTimer % 3 == 0) for (Vec3d p : pending) Fx.ring(ws, el, p, pendingRadius, 0.15); // uyarı halkası
            if (--pendingTimer == 0) fire(ws);
        } else {
            EntityLivingBase t = getAttackTarget();
            if (t != null && t.isEntityAlive() && --attackCd <= 0) startAttack(t);
        }
    }

    private void startAttack(EntityLivingBase t) {
        float hp = getHealth() / getMaxHealth();
        int roll = rand.nextInt(100);
        Vec3d tp = new Vec3d(t.posX, t.posY, t.posZ);
        pending.clear();
        pendingRadius = 2.5; pendingDmg = 8f; mode = 0;

        if (hp < 0.33f && roll < 35) {            // ÖFKE: 8 yıldırım
            mode = 3; pendingRadius = 3; pendingDmg = 8f; attackCd = 90;
            for (int i = 0; i < 8; i++) pending.add(tp.addVector((rand.nextDouble() - 0.5) * 16, 0, (rand.nextDouble() - 0.5) * 16));
        } else if (getDistanceSq(t) < 64 && roll < 55) { // NOVA
            mode = 1; pendingRadius = 7; attackCd = 60;
            pending.add(getPositionVector());
        } else if (hp < 0.66f && roll < 80) {     // FIRTINA: 5 yıldırım
            mode = 2; pendingRadius = 2; pendingDmg = 6f; attackCd = 70;
            for (int i = 0; i < 5; i++) pending.add(tp.addVector((rand.nextDouble() - 0.5) * 10, 0, (rand.nextDouble() - 0.5) * 10));
        } else {                                  // tek yıldırım
            attackCd = 40;
            pending.add(tp);
        }
        pendingTimer = 22;
        dataManager.set(CAST, 30);
        playSound(SoundEvents.EVOCATION_ILLAGER_PREPARE_ATTACK, 2.0f, 0.6f);
    }

    private void fire(WorldServer ws) {
        if (mode == 1) {
            Vec3d c = getPositionVector();
            for (double r = 1; r <= 7; r += 1.5) Fx.ring(ws, el, c, r, 0.3);
            for (EntityLivingBase e : Fx.area(this, ws, c, 7, 6f, el)) {
                double dx = e.posX - posX, dz = e.posZ - posZ;
                double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                e.motionX += dx / len * 1.2;
                e.motionZ += dz / len * 1.2;
                e.motionY += 0.5;
                e.velocityChanged = true;
            }
            Fx.thunder(ws, c);
        } else {
            for (Vec3d p : pending) {
                Fx.lightning(ws, el, p.addVector(0, 16, 0), p);
                Fx.area(this, ws, p, pendingRadius, pendingDmg, el);
            }
            if (!pending.isEmpty()) Fx.thunder(ws, pending.get(0));
        }
        pending.clear();
    }

    // ---- istemci: element aurası (parçacık) ----
    private void aura() {
        double w = width * 0.6;
        for (int i = 0; i < 2; i++) {
            double x = posX + (rand.nextDouble() - 0.5) * w * 2;
            double y = posY + rand.nextDouble() * height;
            double z = posZ + (rand.nextDouble() - 0.5) * w * 2;
            switch (el) {
                case WATER: world.spawnParticle(EnumParticleTypes.WATER_SPLASH, x, y, z, 0, 0.05, 0); break;
                case FIRE: world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0.04, 0); break;
                case EARTH: world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, x, y, z, 0, 0, 0); break;
                case ELECTRIC: world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, x, y, z, 0, 0.05, 0); break;
                case LIGHT: world.spawnParticle(EnumParticleTypes.END_ROD, x, y, z, 0, 0.03, 0); break;
                default: world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 0, 0.02, 0); break;
            }
        }
        if (rand.nextInt(3) == 0) { // elementin renginde kıvılcım
            world.spawnParticle(EnumParticleTypes.REDSTONE,
                    posX + (rand.nextDouble() - 0.5) * width * 2, posY + rand.nextDouble() * height,
                    posZ + (rand.nextDouble() - 0.5) * width * 2, el.r, el.g, el.b);
        }
    }
}
