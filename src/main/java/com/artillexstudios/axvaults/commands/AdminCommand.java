package com.artillexstudios.axvaults.commands;

import com.artillexstudios.axvaults.commands.subcommands.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

public class AdminCommand implements OrphanCommand {

    @CommandPermission("axvaults.admin")
    @DefaultFor({"~", "~ help"})
    public void help(CommandSender sender) {
        Help.INSTANCE.execute(sender);
    }

    @CommandPermission("axvaults.admin.reload")
    @Subcommand("reload")
    public void reload(CommandSender sender) {
        Reload.INSTANCE.execute(sender);
    }

    @CommandPermission("axvaults.admin.forceopen")
    @Subcommand("forceopen")
    public void forceOpen(CommandSender sender, Player player, @Optional @Range(min = 1) Integer number) {
        ForceOpen.INSTANCE.execute(sender, player, number);
    }

    @CommandPermission("axvaults.admin.view")
    @Subcommand("view")
    public void view(Player sender, OfflinePlayer player, @Optional @Range(min = 1) Integer number) {
        View.INSTANCE.execute(sender, player, number);
    }

    @CommandPermission("axvaults.admin.delete")
    @Subcommand("delete")
    public void delete(CommandSender sender, OfflinePlayer player, int number) {
        Delete.INSTANCE.execute(sender, player, number);
    }

    @CommandPermission("axvaults.admin.set")
    @Subcommand("set")
    public void set(Player sender, @Optional Integer number) {
        Set.INSTANCE.execute(sender, number);
    }

    @CommandPermission("axvaults.admin.stats")
    @Subcommand("stats")
    public void stats(CommandSender sender) {
        Stats.INSTANCE.execute(sender);
    }

    @CommandPermission("axvaults.admin.converter")
    @Subcommand("converter PlayerVaultsX")
    public void converter(CommandSender sender) {
        Converter.INSTANCE.execute(sender);
    }

    @CommandPermission("axvaults.admin.save")
    @Subcommand("save")
    public void save(CommandSender sender) {
        Save.INSTANCE.execute(sender);
    }

    @CommandPermission("axvaults.admin.debug")
    @Subcommand("debug")
    public void debug(CommandSender sender) {
        Debug.INSTANCE.execute(sender);
    }
}
