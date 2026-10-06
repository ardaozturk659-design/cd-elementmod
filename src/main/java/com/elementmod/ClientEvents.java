package com.elementmod;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ElementMod.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void models(ModelRegistryEvent e) {
        RenderingRegistry.registerEntityRenderingHandler(EntityWaterBoss.class, m -> new RenderElementBoss(m, Element.WATER));
        RenderingRegistry.registerEntityRenderingHandler(EntityFireBoss.class, m -> new RenderElementBoss(m, Element.FIRE));
        RenderingRegistry.registerEntityRenderingHandler(EntityEarthBoss.class, m -> new RenderElementBoss(m, Element.EARTH));
        RenderingRegistry.registerEntityRenderingHandler(EntityElectricBoss.class, m -> new RenderElementBoss(m, Element.ELECTRIC));
        RenderingRegistry.registerEntityRenderingHandler(EntityLightBoss.class, m -> new RenderElementBoss(m, Element.LIGHT));
        RenderingRegistry.registerEntityRenderingHandler(EntityDarkBoss.class, m -> new RenderElementBoss(m, Element.DARK));
    }
}
