package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxExpressionPath
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY
import icu.windea.pls.script.psi.ParadoxScriptProperty

@Suppress("UnstableApiUsage")
/**
 * 脚本属性的存根。
 *
 * 注意：为了更清晰地区分职责，定义相关的字段仅存在于子接口 [Definition] 中；
 * 顶层接口不再暴露任何与定义有关的属性（包括 `rootKey` 与 `elementPath`）。
 * 这是 INLINE_DEFINITION 重构的一部分。
 */
interface ParadoxScriptPropertyStub : ParadoxStub<ParadoxScriptProperty> {
    // 子类型：定义 vs 内联脚本使用
    interface Definition : ParadoxScriptPropertyStub {
        /** 定义名（可为空字符串表示匿名）。 */
        val definitionName: String
        /** 定义类型（非空）。 */
        val definitionType: String
        /** 定义子类型（可能为 null，表示需延后解析）。 */
        val definitionSubtypes: List<String>?
        /** 定义所在的根键。 */
        val rootKey: String
        /** 定义元素路径（不含参数）。 */
        val elementPath: ParadoxExpressionPath

        val isValidDefinition: Boolean get() = definitionType.isNotEmpty()
    }

    interface InlineScriptUsage : ParadoxScriptPropertyStub {
        /** `inline_script` 的路径表达式（不含参数）。 */
        val inlineScriptExpression: String
        /** 是否带入参（block 形态）。 */
        val hasArguments: Boolean
    }

    interface InlineScriptArgument : ParadoxScriptPropertyStub {
        /** 传参名（不含 `script`）。 */
        val argumentName: String
        /** 所属的 `inline_script` 的路径表达式（不含参数）。 */
        val inlineScriptExpression: String
    }

    abstract class Base(
        parent: StubElement<*>?
    ) : StubBase<ParadoxScriptProperty>(parent, PROPERTY), ParadoxScriptPropertyStub {
        override val gameType: ParadoxGameType get() = parentStub?.gameType ?: ParadoxGameType.Core

        override fun getParentStub(): ParadoxStub<*>? {
            return super.getParentStub() as? ParadoxStub<*>
        }

        override fun toString(): String {
            return "ParadoxScriptPropertyStub(gameType=$gameType)"
        }
    }

    class Impl(
        parent: StubElement<*>?,
        override val definitionName: String,
        override val definitionType: String,
        override val definitionSubtypes: List<String>?,
        override val rootKey: String,
        override val elementPath: ParadoxExpressionPath,
    ) : Base(parent), Definition {
        override fun toString(): String {
            return "ParadoxScriptPropertyStub.Definition(name=$definitionName, type=$definitionType, gameType=$gameType)"
        }
    }

    class InlineScriptUsageImpl(
        parent: StubElement<*>?,
        override val inlineScriptExpression: String,
        override val hasArguments: Boolean,
    ) : Base(parent), InlineScriptUsage {
        override fun toString(): String {
            return "ParadoxScriptPropertyStub.InlineScriptUsage(expression=$inlineScriptExpression, hasArgs=$hasArguments, gameType=$gameType)"
        }
    }

    class InlineScriptArgumentImpl(
        parent: StubElement<*>?,
        override val argumentName: String,
        override val inlineScriptExpression: String,
    ) : Base(parent), InlineScriptArgument {
        override fun toString(): String {
            return "ParadoxScriptPropertyStub.InlineScriptArgument(name=$argumentName, expression=$inlineScriptExpression, gameType=$gameType)"
        }
    }

    class Dummy(
        parent: StubElement<*>?
    ) : Base(parent) {
        override fun toString(): String {
            return "ParadoxScriptPropertyStub.Dummy(gameType=$gameType)"
        }
    }
}

