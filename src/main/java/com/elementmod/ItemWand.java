package com.elementmod;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/** Sağ tık: büyüyü at. Shift + sağ tık: sıradaki büyüye geç. */
public class ItemWand extends Item {
    public final Element element;

    public ItemWand(Element element) {
        this.element = element;
        setRegistryName(ElementMod.MODID, element.id + "_wand");
        setUnlocalizedName(ElementMod.MODID + "." + element.id + "_wand");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer p, EnumHand hand) {
        ItemStack stack = p.getHeldItem(hand);
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) { tag = new NBTTagCompound(); stack.setTagCompound(tag); }
        List<Spell> spells = SpellRegistry.get(element);
        int idx = tag.getInteger("spell") % spells.size();

        if (p.isSneaking()) {
            idx = (idx + 1) % spells.size();
            tag.setInteger("spell", idx);
            if (!world.isRemote) {
                p.sendStatusMessage(new TextComponentString(element.fmt + "Büyü " + (idx + 1) + "/" + spells.size()
                        + ": " + spells.get(idx).name), true);
            }
        } else if (!world.isRemote && !p.getCooldownTracker().hasCooldown(this)) {
            Spell s = spells.get(idx);
            s.cast(p);
            p.getCooldownTracker().setCooldown(this, s.cooldown());
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
