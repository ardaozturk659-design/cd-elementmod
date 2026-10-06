package com.elementmod;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = ElementMod.MODID)
public class ModItems {
    public static final ItemWand[] WANDS = new ItemWand[Element.values().length];

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        for (Element el : Element.values()) {
            ItemWand w = new ItemWand(el);
            WANDS[el.ordinal()] = w;
            e.getRegistry().register(w);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {
        for (ItemWand w : WANDS) {
            ModelLoader.setCustomModelResourceLocation(w, 0, new ModelResourceLocation(w.getRegistryName(), "inventory"));
        }
    }
}
