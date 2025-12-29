package me.Penguin17.koolaidplugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.ArrayList;
import java.util.List;
public class Koolaidplugin extends JavaPlugin implements Listener {
    private FileConfiguration config;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        if (!config.isList("specialPlayers")) {
            config.set("specialPlayers", new ArrayList<>());
        }
        if (!config.isList("vanishedPlayers")) {
            config.set("vanishedPlayers", new ArrayList<>());
        }
        if (!config.isConfigurationSection("vanishGamemodes")) {
            config.createSection("vanishGamemodes");
        }
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String uuid = p.getUniqueId().toString();
        if (config.getStringList("vanishedPlayers").contains(uuid)) {
            Bukkit.getScheduler().runTaskLater(this, () -> vanish(p, true), 1L);
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("specperm")) {
            if (!sender.isOp() || args.length != 1) return true;
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) return true;
            String uuid = target.getUniqueId().toString();
            List<String> list = config.getStringList("specialPlayers");
            if (!list.contains(uuid)) {
                list.add(uuid);
                config.set("specialPlayers", list);
                saveConfig();
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("remspec")) {
            if (!sender.isOp() || args.length != 1) return true;
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) return true;
            String uuid = target.getUniqueId().toString();
            List<String> list = config.getStringList("specialPlayers");
            if (list.remove(uuid)) {
                config.set("specialPlayers", list);
                saveConfig();
            }
            return true;
        }
        if (!(sender instanceof Player p)) return true;
        String uuid = p.getUniqueId().toString();
        if (!config.getStringList("specialPlayers").contains(uuid)) return true;
        switch (cmd.getName().toLowerCase()) {
            case "fly" -> p.setAllowFlight(!p.getAllowFlight());
            case "vanish" -> {
                boolean vanished = config.getStringList("vanishedPlayers").contains(uuid);
                vanish(p, !vanished);
            }

            case "god" -> p.setInvulnerable(!p.isInvulnerable());
            case "heal" -> {
                p.setHealth(p.getMaxHealth());
                p.setFoodLevel(20);
            }
            case "feed" -> p.setFoodLevel(20);
            case "speed" -> {
                if (p.hasPotionEffect(PotionEffectType.SPEED)) {
                    p.removePotionEffect(PotionEffectType.SPEED);
                } else {
                    p.addPotionEffect(new PotionEffect(
                            PotionEffectType.SPEED,
                            Integer.MAX_VALUE, 1, false, false
                    ));
                }
            }
        }
        return true;
    }
    //just use a real vanish plugin at this point 😭😭 (idk why i can add those)
    private void vanish(Player p, boolean vanish) {
        String uuid = p.getUniqueId().toString();
        List<String> vanished = config.getStringList("vanishedPlayers");
        if (vanish) {
            if (!vanished.contains(uuid)) {
                vanished.add(uuid);
                config.set("vanishedPlayers", vanished);
                config.set("vanishGamemodes." + uuid, p.getGameMode().name());
                saveConfig();
            }
            p.setGameMode(GameMode.SPECTATOR);
            p.setInvisible(true);
            p.setSilent(true);
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.hidePlayer(this, p);
            }
        } else {
            vanished.remove(uuid);
            config.set("vanishedPlayers", vanished);
            String gm = config.getString("vanishGamemodes." + uuid, "SURVIVAL");
            p.setGameMode(GameMode.valueOf(gm));
            config.set("vanishGamemodes." + uuid, null);
            saveConfig();
            p.setInvisible(false);
            p.setSilent(false);

            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.showPlayer(this, p);
            }
        }
    }
}
