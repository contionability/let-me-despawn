package me.contionability.letMeDespawn

import me.contionability.letMeDespawn.handlers.DespawnHandler
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class LetMeDespawn : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        despMarker = NamespacedKey(this, "Marker")
        DespawnHandler.register(this)
        slF4JLogger.info("Let Me Despawn Loaded")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    companion object {
        var despMarker: NamespacedKey? = null
    }
}
