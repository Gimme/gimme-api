package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

class SimpleCommandManagerTest {

    private val commandManager: CommandManager = SimpleCommandManager()

    @Test
    fun `should have a help command by default`() {
        assertNotNull(commandManager.getCommand("help"))
    }

    @Test
    fun `should register command`() {
        commandManager.registerCommand(DUMMY_COMMAND)

        assertAll(
            Executable { assertNull(commandManager.getCommand("test726")) },
            Executable { assertEquals(DUMMY_COMMAND, commandManager.getCommand("test")) },
        )
    }

    @Test
    fun `register command with same name should overwrite`() {
        commandManager.registerCommand(object : BaseCommand("test") {})
        commandManager.registerCommand(DUMMY_COMMAND)

        assertEquals(DUMMY_COMMAND, commandManager.getCommand("test"))
    }

    @ParameterizedTest
    @CsvSource(
        "test, test",
        "Test, test",
        "test, TEST",
        "Test, Test",
        "test1 test2, test1 test2",
    )
    fun `should execute command`(commandName: String, inputCommand: String) {
        var executed = false

        val command: Command = object : BaseCommand(commandName) {
            override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
                executed = true
                return null
            }
        }

        commandManager.registerCommand(command)
        commandManager.parseInput(DUMMY_COMMAND_SENDER, inputCommand)

        assertTrue(executed)
    }

    @Test
    fun `should execute parent child commands separately`() {
        var parentExecuted = false
        var childExecuted = false

        val parentCommand: Command = object : BaseCommand("parent") {
            override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
                assertIterableEquals(listOf("a", "b"), args)
                parentExecuted = true
                return null
            }
        }
        val childCommand: Command = object : BaseCommand("parent child") {
            override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
                assertIterableEquals(listOf("x", "y"), args)
                childExecuted = true
                return null
            }
        }

        commandManager.registerCommand(parentCommand)
        commandManager.registerCommand(childCommand)
        commandManager.parseInput(DUMMY_COMMAND_SENDER, "parent a b")
        commandManager.parseInput(DUMMY_COMMAND_SENDER, "parent child x y")

        assertTrue(parentExecuted)
        assertTrue(childExecuted)

        val commandManager2 = SimpleCommandManager()
        commandManager2.registerCommand(childCommand)
        commandManager2.registerCommand(parentCommand)
        commandManager2.parseInput(DUMMY_COMMAND_SENDER, "parent child x y")
        commandManager2.parseInput(DUMMY_COMMAND_SENDER, "parent a b")

        assertTrue(childExecuted)
        assertTrue(parentExecuted)
    }

    @ParameterizedTest
    @MethodSource("args")
    fun `should pass arguments`(input: String, expectedArgs: List<String>) {
        var actualArgs: List<String>? = null

        val command: Command = object : BaseCommand("c") {
            override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
                actualArgs = args
                return null
            }
        }

        commandManager.registerCommand(command)
        commandManager.parseInput(DUMMY_COMMAND_SENDER, input)

        assertNotNull(actualArgs)
        assertIterableEquals(expectedArgs, actualArgs)
    }

    companion object {
        @JvmStatic
        fun args() = listOf(
            Arguments.of("c", emptyList<String>()),
            Arguments.of("c ", listOf("")),
            Arguments.of("c   one", listOf("", "", "one")),
            Arguments.of("c one", listOf("one")),
            Arguments.of("c one two", listOf("one", "two")),
            Arguments.of("c one two three", listOf("one", "two", "three")),
            Arguments.of("c \"one two\" three", listOf("one two", "three")),
        )
    }
}
