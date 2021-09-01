package net.axay.fabrik.core.sideboard

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.axay.fabrik.core.Fabrik
import net.axay.fabrik.core.sideboard.internal.FabrikSideboardScoreboard
import net.axay.fabrik.core.task.fabrikCoroutineScope
import net.axay.fabrik.core.task.initWithServerAsync
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

/**
 * A sideboard which can be displayed to a variable collection of players
 * using the [displayToPlayer] function. A sideboard is an abstraction of Mincraft's
 * server side scoreboards displayed on the right-hand side of the screen.
 *
 * **Note:** You probably want to build this class using the sideboard builder API. See [sideboard]!
 */
class Sideboard(
    name: String,
    displayName: Text,
    lines: List<SideboardLine>,
) {
    private val scoreboardDeferred = initWithServerAsync { FabrikSideboardScoreboard(name, displayName) }

    private val initLock = CompletableDeferred<Boolean>()

    init {
        fabrikCoroutineScope.launch {
            val scoreboard = scoreboardDeferred.await()

            lines.forEachIndexed { index, line ->
                val team = scoreboard.addTeam("team_$index")
                scoreboard.addPlayerToTeam("§$index", team)
                scoreboard.setPlayerScore("§$index", lines.size - index)

                fabrikCoroutineScope.launch {
                    line.textFlow.collect {
                        team.prefix = it
                    }
                }
            }

            initLock.complete(true)
        }
    }

    internal fun displayToPlayer(player: ServerPlayerEntity) {
        if (Fabrik.currentServer?.isRunning == true)
            fabrikCoroutineScope.launch {
                initLock.await()
                scoreboardDeferred.await().displayToPlayer(player)
            }
    }
}
