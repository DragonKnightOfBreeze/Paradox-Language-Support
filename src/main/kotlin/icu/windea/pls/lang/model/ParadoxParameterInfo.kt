package icu.windea.pls.lang.model

import com.intellij.psi.*
import icu.windea.pls.core.psi.*

data class ParadoxParameterInfo(
    val name: String,
    val pointers: MutableList<SmartPsiElementPointer<ParadoxParameter>> = mutableListOf(),
    var optional: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        return other is ParadoxParameterInfo && name == other.name
    }
    
    override fun hashCode(): Int {
        return name.hashCode()
    }
}