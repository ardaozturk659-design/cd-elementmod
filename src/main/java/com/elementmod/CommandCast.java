package com.elementmod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

/**
 * /cast <element> <1-12>   -> büyü at   (örn: /cast fire 2  veya  /cast ates 2)
 * /cast list               -> tüm büyüleri listele
 */
public class CommandCast extends CommandBase {
    @Override public String getName() { return "cast"; }

    @Override public String getUsage(ICommandSender sender) { return "/cast <element> <1-12> | /cast list"; }

    @Override public int getRequiredPermissionLevel() { return 0; } // herkes kullanabilir (sadece op için 2 yap)

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
            for (Element el : Element.values()) {
                StringBuilder sb = new StringBuilder(el.fmt + el.title + " (" + el.id + "): ");
                List<Spell> l = SpellRegistry.get(el);
                for (int i = 0; i < l.size(); i++) sb.append(i + 1).append("-").append(l.get(i).name).append(i < l.size() - 1 ? ", " : "");
                sender.sendMessage(new TextComponentString(sb.toString()));
            }
            return;
        }
        if (args.length < 2) throw new WrongUsageException(getUsage(sender));
        Element el = Element.byId(args[0]);
        if (el == null) throw new CommandException("Bilinmeyen element: " + args[0]);
        List<Spell> spells = SpellRegistry.get(el);
        int n = parseInt(args[1], 1, spells.size());
        EntityPlayerMP p = getCommandSenderAsPlayer(sender);
        spells.get(n - 1).cast(p);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            List<String> l = new ArrayList<>();
            l.add("list");
            for (Element e : Element.values()) l.add(e.id);
            return getListOfStringsMatchingLastWord(args, l);
        }
        if (args.length == 2) {
            List<String> l = new ArrayList<>();
            for (int i = 1; i <= 12; i++) l.add(String.valueOf(i));
            return getListOfStringsMatchingLastWord(args, l);
        }
        return Collections.emptyList();
    }
}
