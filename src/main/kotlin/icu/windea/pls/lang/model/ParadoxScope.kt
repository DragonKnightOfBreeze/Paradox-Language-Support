package icu.windea.pls.lang.model

import icu.windea.pls.lang.*

sealed class ParadoxScope private constructor(val id: String) {
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxScope && id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return id
    }
    
    object AnyScope : ParadoxScope(ParadoxScopeHandler.anyScopeId)
    object UnknownScope : ParadoxScope(ParadoxScopeHandler.unknownScopeId)
    class Scope(id: String) : ParadoxScope(id)
    class InferredScope(id: String) : ParadoxScope(id) {
        override fun toString(): String {
            return "$id!"
        }
    }
    
    companion object {
        @JvmStatic fun of(id: String): ParadoxScope {
            return when {
                id == ParadoxScopeHandler.anyScopeId -> AnyScope
                id == ParadoxScopeHandler.unknownScopeId -> UnknownScope
                else -> Scope(id)
            }
        }
        
        @JvmStatic fun inferred(id: String): ParadoxScope {
            return when {
                id == ParadoxScopeHandler.anyScopeId -> AnyScope
                id == ParadoxScopeHandler.unknownScopeId -> UnknownScope
                else -> InferredScope(id)
            }
        }
    }
}

fun ParadoxScope?.isUnsure() = this == null || this == ParadoxScope.AnyScope || this == ParadoxScope.UnknownScope