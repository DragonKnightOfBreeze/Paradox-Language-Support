package icu.windea.pls.lang.model

import com.intellij.psi.*
import icu.windea.pls.core.psi.*

data class ParadoxParameterInfo(
    val name: String,
    val pointers: MutableList<SmartPsiElementPointer<ParadoxParameter>> = mutableListOf(),
    var conditional: Boolean = false 
)