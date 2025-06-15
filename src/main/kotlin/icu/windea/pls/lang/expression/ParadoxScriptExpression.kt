package icu.windea.pls.lang.expression

import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * 脚本表达式，
 *
 * @see ParadoxScriptExpressionElement
 */
interface ParadoxScriptExpression {
    val value: String
    val type: ParadoxType
    val quoted: Boolean
    val isKey: Boolean?

    fun isParameterized(): Boolean

    fun isFullParameterized(): Boolean

    fun matchesConstant(v: String): Boolean

    companion object Resolver {
        val BlockExpression: ParadoxScriptExpression = Impl(PlsStringConstants.blockFolder, ParadoxType.Block, false, false)
        val UnknownExpression: ParadoxScriptExpression = Impl(PlsStringConstants.unknown, ParadoxType.Unknown, false, false)

        fun resolve(value: String, quoted: Boolean, isKey: Boolean? = null): ParadoxScriptExpression {
            return Impl(value, ParadoxTypeManager.resolve(value), quoted, isKey)
        }

        fun resolve(text: String, isKey: Boolean? = null): ParadoxScriptExpression {
            return Impl(text.unquote(), ParadoxTypeManager.resolve(text), text.isLeftQuoted(), isKey)
        }

        fun resolve(element: ParadoxScriptExpressionElement, matchOptions: Int = ParadoxExpressionMatcher.Options.Default): ParadoxScriptExpression {
            return when {
                element is ParadoxScriptBlock -> Impl(PlsStringConstants.blockFolder, ParadoxType.Block, false, false)
                element is ParadoxScriptScriptedVariableReference -> LazyImpl(element, matchOptions, false)
                else -> Impl(element.value, element.type, element.text.isLeftQuoted(), element is ParadoxScriptPropertyKey)
            }
        }
    }

    //region Implementations

    private sealed class Base : ParadoxScriptExpression {
        private val regex by lazy { ParadoxExpressionManager.toRegex(value) }

        override fun isParameterized(): Boolean {
            return type == ParadoxType.String && value.isParameterized()
        }

        override fun isFullParameterized(): Boolean {
            return type == ParadoxType.String && value.isParameterized(full = true)
        }

        override fun matchesConstant(v: String): Boolean {
            if (value.isParameterized()) {
                //兼容带参数的情况（此时先转化为正则表达式，再进行匹配）
                return regex.matches(v)
            }
            return value.equals(v, true) //忽略大小写
        }

        override fun equals(other: Any?): Boolean {
            return other is ParadoxScriptExpression && (value == other.value && quoted == other.quoted)
        }

        override fun hashCode(): Int {
            return Objects.hash(value, type)
        }

        override fun toString(): String {
            return value
        }
    }

    private class Impl(
        override val value: String,
        override val type: ParadoxType,
        override val quoted: Boolean,
        override val isKey: Boolean?
    ) : Base()

    private class LazyImpl(
        private val element: ParadoxScriptScriptedVariableReference,
        private val matchOptions: Int,
        override val isKey: Boolean?
    ) : Base() {
        //1.3.28 lazy resolve scripted variable value for data expressions to optimize config resolving (and also indexing) logic
        val valueElement by lazy {
            when {
                BitUtil.isSet(matchOptions, ParadoxExpressionMatcher.Options.SkipIndex) -> null
                else -> element.referenceValue
            }
        }

        override val value get() = valueElement?.value ?: PlsStringConstants.unknown
        override val type get() = valueElement?.type ?: ParadoxType.Unknown
        override val quoted get() = valueElement?.text?.isLeftQuoted() ?: false
    }

    //endregion
}
