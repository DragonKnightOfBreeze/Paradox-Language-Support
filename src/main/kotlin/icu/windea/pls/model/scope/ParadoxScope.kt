package icu.windea.pls.model.scope

sealed interface ParadoxScope {
    val id: String
    
    object Any : ParadoxScope {
        override val id: String = "any"
        
        override fun toString() = id
    }
    
    object Unknown : ParadoxScope {
        override val id: String = "?"
        
        override fun toString() = id
    }
    
    class Default(override val id: String) : ParadoxScope {
        override fun toString() = id
    }
    
    companion object {
        @JvmStatic
        fun of(id: String): ParadoxScope {
            return when {
                id == Any.id -> Any
                id == Unknown.id -> Unknown
                else -> Default(id)
            }
        }
    }
}

infix fun ParadoxScope.matches(other: ParadoxScope): Boolean {
    if(this.id == other.id) return true
    return false
}