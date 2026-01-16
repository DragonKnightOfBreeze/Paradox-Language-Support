package icu.windea.pls.model

import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptValue

enum class ParadoxExpressionRole(val id: String) {
    Key("key"),
    Value("value"),
    Other("other"),
    ;

    companion object {
        @JvmStatic
        fun resolve(element: ParadoxExpressionElement): ParadoxExpressionRole {
            return when {
                element is ParadoxScriptPropertyKey -> Key
                element is ParadoxScriptValue -> Value
                else -> Other
            }
        }
    }
}
