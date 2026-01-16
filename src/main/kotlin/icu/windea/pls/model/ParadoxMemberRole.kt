package icu.windea.pls.model

import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

enum class ParadoxMemberRole(val id: String) {
    Property("property"),
    PropertyValue("property_value"),
    BlockValue("block_value"),
    Other("other"),
    ;

    companion object {
        @JvmStatic
        fun resolve(element: ParadoxScriptMember): ParadoxMemberRole {
            return when {
                element is ParadoxScriptProperty -> Property
                element is ParadoxScriptValue -> if (element.parent is ParadoxScriptProperty) PropertyValue else BlockValue
                else -> Other
            }
        }
    }
}
