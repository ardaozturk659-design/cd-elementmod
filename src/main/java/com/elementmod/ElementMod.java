package com.elementmod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = ElementMod.MODID, name = "Element Mod", version = "0.1")
public class ElementMod {
    public static final String MODID = "elementmod";

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandCast());
    }
}
