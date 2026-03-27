package icu.windea.pls.model

import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

enum class ParadoxMemberRole {
    PROPERTY,
    PROPERTY_VALUE,
    BLOCK_VALUE,
    OTHER,
    ;

    companion object {
        @JvmStatic
        fun resolve(element: ParadoxScriptMember): ParadoxMemberRole {
            return when {
                element is ParadoxScriptProperty -> PROPERTY
                element is ParadoxScriptValue -> if (element.parent is ParadoxScriptProperty) PROPERTY_VALUE else BLOCK_VALUE
                else -> OTHER
            }
        }
    }
}
