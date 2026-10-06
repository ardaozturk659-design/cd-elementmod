package com.elementmod;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ElementMod.MODID)
public class ModEntities {
    @SubscribeEvent
    public static void register(RegistryEvent.Register<EntityEntry> e) {
        IForgeRegistry<EntityEntry> r = e.getRegistry();
        reg(r, EntityWaterBoss.class, "water_boss", 0, 0x1E66FF, 0x001A66);
        reg(r, EntityFireBoss.class, "fire_boss", 1, 0xFF5A00, 0x4A0A00);
        reg(r, EntityEarthBoss.class, "earth_boss", 2, 0x1ED71E, 0x3A2F1A);
        reg(r, EntityElectricBoss.class, "electric_boss", 3, 0x66E6FF, 0x1A2850);
        reg(r, EntityLightBoss.class, "light_boss", 4, 0xFFE61E, 0xFFFFFF);
        reg(r, EntityDarkBoss.class, "dark_boss", 5, 0x120A1E, 0xFF1414);
    }

    private static void reg(IForgeRegistry<EntityEntry> r, Class<? extends Entity> c, String name, int id, int c1, int c2) {
        r.register(EntityEntryBuilder.create().entity(c)
                .id(new ResourceLocation(ElementMod.MODID, name), id)
                .name(name).tracker(96, 3, true).egg(c1, c2).build());
    }
}
