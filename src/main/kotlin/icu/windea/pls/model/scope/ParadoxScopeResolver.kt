package icu.windea.pls.model.scope

import icu.windea.pls.core.cache.CacheBuilder

object ParadoxScopeResolver {
    private val cache = CacheBuilder("maximumSize=1024, expireAfterAccess=30m").build<String, ParadoxScope> { ParadoxScope.Default(it) }

    fun get(id: String): ParadoxScope {
        return when {
            id == ParadoxScope.Any.id -> ParadoxScope.Any
            id == ParadoxScope.Unknown.id -> ParadoxScope.Unknown
            else -> cache.get(id)
        }
    }
}
