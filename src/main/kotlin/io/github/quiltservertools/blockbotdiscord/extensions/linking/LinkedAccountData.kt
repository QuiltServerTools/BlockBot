package io.github.quiltservertools.blockbotdiscord.extensions.linking

import dev.kord.common.entity.Snowflake
import java.util.*

interface LinkedAccountData {
    fun get(id: UUID): Snowflake?
    fun get(id: Snowflake): Set<UUID>?

    fun add(id: Snowflake, uuid: UUID)

    fun load()

    fun save()
}
