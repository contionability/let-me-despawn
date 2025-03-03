package me.contionability.letMeDespawn.handlers

import me.contionability.letMeDespawn.LetMeDespawn
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Material.CARVED_PUMPKIN
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Piglin
import org.bukkit.entity.SpawnCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import io.papermc.paper.event.player.PlayerNameEntityEvent

class DespawnHandler : Listener {

    @EventHandler( priority = EventPriority.LOWEST, ignoreCancelled = true )
    fun onMobPickupItem(event: EntityPickupItemEvent) {
        val entity = event.entity as? LivingEntity ?: return
        if (entity.spawnCategory == SpawnCategory.MONSTER) {
            event.item.itemStack.editMeta { pdc ->
                pdc.persistentDataContainer.set(LetMeDespawn.despMarker!!, PersistentDataType.BOOLEAN, true)
            }
            if (entity is Piglin && entity.equipment.helmet?.type == CARVED_PUMPKIN)
                return
            if (entity.customName() == null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin!!, {
                    if (entity.isPersistent)
                        entity.persistentDataContainer.set(
                            LetMeDespawn.despMarker!!,
                            PersistentDataType.STRING,
                            "desp")
                }, 1L)
            }
        }
    }

    @EventHandler( priority = EventPriority.HIGHEST )
    fun onMobSpawn(event: EntityAddToWorldEvent) {
        val entity = event.entity as? LivingEntity ?: return
        if (entity.spawnCategory != SpawnCategory.MONSTER || entity.isDead)
            return
        if (entity.customName() != null) {
            entity.persistentDataContainer.remove(LetMeDespawn.despMarker!!)
            return
        }
        if (entity.persistentDataContainer.get(LetMeDespawn.despMarker!!, PersistentDataType.STRING) != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin!!, {
                dropMarkedItems(entity)
                entity.remove()
            }, 1L)
        }
    }

    @EventHandler( priority = EventPriority.LOWEST, ignoreCancelled = true )
    fun onMobDropItem(event: EntityDropItemEvent) {
        event.itemDrop.itemStack.editMeta { pdc ->
            pdc.persistentDataContainer.remove(LetMeDespawn.despMarker!!)
        }
    }

    @EventHandler( priority = EventPriority.LOWEST, ignoreCancelled = true )
    fun onMobRename(event: PlayerNameEntityEvent) {
        event.entity.persistentDataContainer.remove(LetMeDespawn.despMarker!!)
    }

    private fun dropMarkedItems(entity: LivingEntity) {
        val world = entity.world
        entity.equipment!!.armorContents.forEach { item ->
            if (item?.type != Material.AIR)
                attemptDropItem(world, entity.location.clone(), item!!.clone())
        }
        attemptDropItem(world, entity.location.clone(), entity.equipment!!.itemInMainHand.clone())
        attemptDropItem(world, entity.location.clone(), entity.equipment!!.itemInOffHand.clone())
    }

    private fun attemptDropItem(world: World, location: Location, itemStack: ItemStack) {
        if (itemStack.type == Material.AIR)
            return
        var dropFlag = false
        itemStack.editMeta { pdc ->
            if (pdc.persistentDataContainer.get(LetMeDespawn.despMarker!!, PersistentDataType.BOOLEAN) == true) {
                pdc.persistentDataContainer.remove(LetMeDespawn.despMarker!!)
                dropFlag = true
            }
        }
        if (dropFlag)
            world.dropItemNaturally(location, itemStack)
    }

    companion object {
        private var plugin: Plugin? = null
        fun register(inst: LetMeDespawn) {
            Bukkit.getPluginManager().registerEvents(DespawnHandler(), inst)
            plugin = inst
        }
    }
}