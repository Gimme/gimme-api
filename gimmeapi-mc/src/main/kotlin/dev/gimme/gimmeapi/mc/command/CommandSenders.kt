package dev.gimme.gimmeapi.mc.command

import dev.gimme.gimmeapi.core.command.sender.CommandSender as GimmeCommandSender
import org.bukkit.command.CommandSender

/**
 * Returns an adapter object with this [CommandSender] as a [GimmeCommandSender].
 */
internal fun CommandSender.asBotCommandSender(): GimmeCommandSender {

    val spigotCommandSender = this

    return object : GimmeCommandSender {
        override val name = spigotCommandSender.name

        override fun sendMessage(message: String) = spigotCommandSender.sendMessage(message)
    }
}