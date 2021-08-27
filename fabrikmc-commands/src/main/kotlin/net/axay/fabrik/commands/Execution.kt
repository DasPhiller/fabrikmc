package net.axay.fabrik.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import net.axay.fabrik.commands.internal.SimpleCommandContext

/**
 * Adds execution logic to this command. The place where this function
 * is called matters, as this defines for which path in the command tree
 * this executor should be called.
 *
 * @see com.mojang.brigadier.builder.ArgumentBuilder.executes
 */
inline infix fun <S> ArgumentBuilder<S, *>.runs(
    crossinline executor: SimpleCommandContext<S>.() -> Unit
) = this.apply {
    executes wrapped@{
        executor.invoke(SimpleCommandContext(it))
        return@wrapped 1
    }
}

/**
 * Adds execution logic to this command. The place where this function
 * is called matters, as this defines for which path in the command tree
 * this executor should be called.
 *
 * @see com.mojang.brigadier.builder.ArgumentBuilder.executes
 */
infix fun <S> ArgumentBuilder<S, *>.runs(executor: Command<S>) = this.apply {
    executes(executor)
}

/**
 * Add custom execution logic for this command.
 */
@Deprecated(
    "The name 'simpleExecutes' has been superseded by 'runs'.",
    ReplaceWith("runs { executor.invoke() }")
)
inline infix fun <S> ArgumentBuilder<S, *>.simpleExecutes(
    crossinline executor: SimpleCommandContext<S>.() -> Unit
) = runs(executor)
