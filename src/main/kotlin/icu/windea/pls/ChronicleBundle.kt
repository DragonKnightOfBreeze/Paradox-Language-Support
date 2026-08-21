package icu.windea.pls

import com.intellij.DynamicBundle
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptValue
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

object ChronicleBundle {
    @NonNls
    private const val BUNDLE = "messages.ChronicleBundle"
    private val INSTANCE = DynamicBundle(ChronicleBundle::class.java, BUNDLE)

    @JvmStatic
    @Nls
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
        return INSTANCE.getMessage(key, *params)
    }

    @JvmStatic
    @Nls
    fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<String> {
        return INSTANCE.getLazyMessage(key, *params)
    }

    // region methods to get specific messages

    @JvmStatic
    @Nls
    fun errorDetails(message: String?): String {
        if (message.isNullOrEmpty()) return ""
        return INSTANCE.getMessage("error.details")
    }

    @JvmStatic
    @Nls
    fun expressionType(value: ParadoxExpressionElement) : String {
        return when(value) {
            is ParadoxScriptPropertyKey -> INSTANCE.getMessage("expression.type.key")
            is ParadoxScriptValue -> INSTANCE.getMessage("expression.type.value")
            is ParadoxCsvColumn -> INSTANCE.getMessage("expression.type.column")
            else -> INSTANCE.getMessage("expression.type.other")
        }
    }

    @JvmStatic
    @Nls
    fun expressionType(value: ParadoxExpression) : String {
        return when(value.role) {
            ParadoxExpressionRole.Key -> INSTANCE.getMessage("expression.type.key")
            ParadoxExpressionRole.Value -> INSTANCE.getMessage("expression.type.value")
            else -> INSTANCE.getMessage("expression.type.other")
        }
    }

    @JvmStatic
    @Nls
    fun expressionType(value: CwtDataExpression) : String {
        return when(value.role) {
            CwtDataExpressionRole.Key -> INSTANCE.getMessage("expression.type.key")
            CwtDataExpressionRole.Value -> INSTANCE.getMessage("expression.type.value")
            else -> INSTANCE.getMessage("expression.type.other")
        }
    }

    // endregion
}
