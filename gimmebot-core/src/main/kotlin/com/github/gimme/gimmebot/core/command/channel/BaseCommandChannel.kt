package com.github.gimme.gimmebot.core.command.channel

import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.exception.ErrorCode
import com.github.gimme.gimmebot.core.command.manager.CommandManager
import com.github.gimme.gimmebot.core.command.sender.CommandSender
import com.github.gimme.gimmebot.core.command.sender.ConsoleCommandSender
import com.github.gimme.gimmebot.core.command.sender.MessageReceiver
import com.github.gimme.gimmebot.core.common.Enableable
import com.github.gimme.gimmebot.core.common.grouped.Grouped

/**
 * Represents a command input/output channel with base functionality.
 *
 * @param R the output response type
 */
abstract class BaseCommandChannel<R>(
    final override val commandManager: CommandManager<R>,
    includeConsoleListener: Boolean = false,
) : CommandChannel<R> {

    /** This medium's registered command managers. */
    protected val registeredCommandManagers: MutableList<CommandManagerRegistration<*, R>> = mutableListOf()
    private val ioListeners: MutableList<MessageReceiver> = mutableListOf()

    override val commandManagers: List<CommandManager<*>>
        get() = registeredCommandManagers.map { it.commandManager }

    override var enabled: Boolean = false
        set(enabled) {
            field = Enableable.enable(this, enabled)
        }

    init {
        if (includeConsoleListener) {
            addIOListener(ConsoleCommandSender)
        }

        registerCommandManager(commandManager) { it }
    }

    override fun parseInput(sender: CommandSender, input: String): Boolean {
        ioListeners.forEach { it.sendMessage("${sender.name}: $input") }

        return false
    }

    override fun respond(commandSender: CommandSender, response: R) {
        ioListeners.forEach { it.sendMessage(response.toString()) }
    }

    /**
     * Executes a command through one of the [registeredCommandManagers].
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    protected fun executeCommand(commandSender: CommandSender, commandId: Grouped, arguments: List<String>): R {
        registeredCommandManagers.forEach {
            if (!it.commandManager.hasCommand(commandId)) return@forEach

            return it.executeCommand(commandSender, commandId, arguments)
        }

        throw ErrorCode.NOT_A_COMMAND.createException()
    }

    final override fun <T> registerCommandManager(commandManager: CommandManager<T>, responseWrapper: (T) -> R) {
        registeredCommandManagers.add(CommandManagerRegistration(commandManager, responseWrapper))
    }

    final override fun addIOListener(messageReceiver: MessageReceiver) {
        ioListeners.add(messageReceiver)
    }

    /**
     * Represents a registered [commandManager] that can be used to execute commands with its responses converted to a
     * specific type, [R], through the specified [responseWrapper]
     *
     * @param T the response type of the command manager
     * @param R the converted response output type
     */
    protected data class CommandManagerRegistration<T, R>(
        /** The wrapped command manager. */
        val commandManager: CommandManager<T>,
        /** The response wrapper. */
        val responseWrapper: (T) -> R,
    ) {
        /**
         * Executes the command with the specified [commandId] through this registered [commandManager] converting the
         * response through the [responseWrapper].
         *
         * @throws CommandException if the command execution was unsuccessful
         */
        @Throws(CommandException::class)
        fun executeCommand(commandSender: CommandSender, commandId: Grouped, arguments: List<String> = listOf()): R {
            val response = commandManager.executeCommand(commandSender, commandId, arguments)
            return responseWrapper(response)
        }
    }
}