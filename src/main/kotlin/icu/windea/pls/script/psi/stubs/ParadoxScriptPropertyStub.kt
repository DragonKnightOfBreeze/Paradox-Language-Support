package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyListStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxElementPath
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 脚本属性的存根。
 *
 * @property name 脚本属性的名字。注意这不一定是定义的名字。
 */
@Suppress("UnstableApiUsage")
interface ParadoxScriptPropertyStub : ParadoxStub<ParadoxScriptProperty> {
    val name: String

    /**
     * （作为脚本属性的）定义的存根。
     *
     * @property definitionName 定义的名字。可以为空，表示匿名。可以为 null，表示需延后解析。
     * @property definitionType 定义的类型。
     * @property definitionSubtypes 定义的子类型。可以为 null，表示需延后解析。
     * @property typeKey 定义的类型键。等同于 [ParadoxScriptPropertyStub.name]。
     * @property elementPath 定义的元素路径（不含参数）。
     *
     */
    interface Definition : ParadoxScriptPropertyStub {
        val definitionName: String?
        val definitionType: String
        val definitionSubtypes: List<String>?
        val typeKey: String
        val elementPath: ParadoxElementPath
    }

    /**
     * （作为脚本属性的）内联脚本使用的存根。
     *
     * @property inlineScriptExpression 内联脚本表达式。用于定位内联脚本文件，例如，`test` 对应路径为 `common/inline_scripts/test.txt` 的内联脚本文件。
     */
    interface InlineScriptUsage : ParadoxScriptPropertyStub {
        val inlineScriptExpression: String
    }

    /**
     * （作为脚本属性的）内联脚本的传入参数的存根。
     *
     * @property argumentName 传入参数的名字。等同于 [ParadoxScriptPropertyStub.name]。
     * @property inlineScriptExpression 内联脚本表达式。来自父存根（[ParadoxLocalisationPropertyListStub]）。
     */
    interface InlineScriptArgument : ParadoxScriptPropertyStub {
        val argumentName: String
        val inlineScriptExpression: String

        override fun getParentStub(): InlineScriptUsage
    }

    private sealed class Base(
        parent: StubElement<*>?,
    ) : StubBase<ParadoxScriptProperty>(parent, PROPERTY), ParadoxScriptPropertyStub {
        override val gameType get() = parentStub?.gameType ?: ParadoxGameType.Core

        override fun getParentStub() = super.getParentStub() as? ParadoxStub<*>

        override fun toString(): String {
            return "ParadoxScriptPropertyStub(name=$name, gameType=$gameType)"
        }
    }

    private class Impl(parent: StubElement<*>?, override val name: String) : Base(parent)

    private class Dummy(parent: StubElement<*>?) : Base(parent) {
        override val name: String get() = ""

        override fun toString(): String {
            return "ParadoxScriptPropertyStub.Dummy(gameType=$gameType)"
        }
    }

    private class DefinitionImpl(
        parent: StubElement<*>?,
        override val definitionName: String?,
        override val definitionType: String,
        override val definitionSubtypes: List<String>?,
        override val typeKey: String,
        override val elementPath: ParadoxElementPath,
    ) : Base(parent), Definition {
        override val name get() = typeKey

        override fun toString(): String {
            return "ParadoxScriptPropertyStub.Definition(name=$definitionName, type=$definitionType, gameType=$gameType)"
        }
    }

    private class InlineScriptUsageImpl(
        parent: StubElement<*>?,
        override val name: String,
        override val inlineScriptExpression: String,
    ) : Base(parent), InlineScriptUsage {
        override fun toString(): String {
            return "ParadoxScriptPropertyStub.InlineScriptUsage(expression=$inlineScriptExpression, gameType=$gameType)"
        }
    }

    private class InlineScriptArgumentImpl(
        parent: StubElement<*>?,
        override val name: String,
    ) : Base(parent), InlineScriptArgument {
        override val argumentName get() = name
        override val inlineScriptExpression get() = parentStub.inlineScriptExpression

        override fun getParentStub() = super.getParentStub() as InlineScriptUsage

        override fun toString(): String {
            return "ParadoxScriptPropertyStub.InlineScriptArgument(name=$argumentName, expression=$inlineScriptExpression, gameType=$gameType)"
        }
    }

    companion object {
        fun create(parent: StubElement<*>?, name: String): ParadoxScriptPropertyStub {
            return Impl(parent, name)
        }

        fun createDummy(parent: StubElement<*>?): ParadoxScriptPropertyStub {
            return Dummy(parent)
        }

        fun createDefinition(
            parent: StubElement<*>?,
            definitionName: String?,
            definitionType: String,
            definitionSubtypes: List<String>?,
            typeKey: String,
            elementPath: ParadoxElementPath,
        ): Definition {
            return DefinitionImpl(parent, definitionName, definitionType, definitionSubtypes, typeKey, elementPath)
        }

        fun createInlineScriptUsage(
            parent: StubElement<*>?,
            name: String,
            inlineScriptExpression: String,
        ): InlineScriptUsage {
            return InlineScriptUsageImpl(parent, name, inlineScriptExpression)
        }

        fun createInlineScriptArgument(
            parent: StubElement<*>?,
            name: String,
        ): InlineScriptArgument {
            return InlineScriptArgumentImpl(parent, name)
        }
    }
}

