package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyListStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 脚本属性的存根。
 *
 * @property name 脚本属性的名字（不一定是定义的名字）。
 */
@Suppress("UnstableApiUsage")
sealed interface ParadoxScriptPropertyStub : ParadoxStub<ParadoxScriptProperty> {
    val name: String

    /**
     * （作为脚本属性的）定值（命名空间/变量）的存根。
     *
     * @property namespace 命名空间。
     * @property variable 变量名。如果当前存根对应命名空间，则为 `null`。
     */
    sealed interface Define : ParadoxScriptPropertyStub {
        val namespace: String
        val variable: String?
    }

    /**
     * （作为脚本属性的）定值的命名空间的存根。
     *
     * @property namespace 命名空间。等同于 [ParadoxScriptPropertyStub.name]。
     */
    sealed interface DefineNamespace : Define {
        override val namespace: String
        override val variable: String? get() = null
    }

    /**
     * （作为脚本属性的）定值的变量的存根。
     *
     * @property namespace 命名空间。来自父存根（[DefineNamespace]）。
     * @property variable 变量名。等同于 [ParadoxScriptPropertyStub.name]。
     */
    sealed interface DefineVariable : Define {
        override val namespace: String
        override val variable: String

        override fun getParentStub(): DefineNamespace
    }

    /**
     * （作为脚本属性的）内联脚本用法的存根。
     *
     * @property expression 内联脚本表达式。用于定位内联脚本文件，例如，`test` 对应路径为 `common/inline_scripts/test.txt` 的内联脚本文件。
     */
    sealed interface InlineScriptUsage : ParadoxScriptPropertyStub {
        val expression: String
    }

    /**
     * （作为脚本属性的）内联脚本的传入参数的存根。
     *
     * @property argumentName 传入参数的名字。等同于 [ParadoxScriptPropertyStub.name]。
     * @property expression 内联脚本表达式。来自父存根（[ParadoxLocalisationPropertyListStub]）。
     */
    sealed interface InlineScriptArgument : ParadoxScriptPropertyStub {
        val argumentName: String
        val expression: String

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

    private class DefineNamespaceImpl(
        parent: StubElement<*>?,
        override val name: String,
    ) : Base(parent), DefineNamespace {
        override val namespace get() = name

        override fun toString(): String {
            return "ParadoxScriptPropertyStub.DefineNamespace(" +
                "namespace=$namespace, " +
                "gameType=$gameType)"
        }
    }

    private class DefineVariableImpl(
        parent: StubElement<*>?,
        override val name: String,
    ) : Base(parent), DefineVariable {
        override val namespace get() = parentStub.namespace
        override val variable get() = name

        override fun getParentStub() = super.getParentStub() as DefineNamespace

        override fun toString(): String {
            return "ParadoxScriptPropertyStub.DefineVariable(" +
                "namespace=$namespace, " +
                "variable=$variable, " +
                "gameType=$gameType)"
        }
    }

    private class InlineScriptUsageImpl(
        parent: StubElement<*>?,
        override val name: String,
        override val expression: String,
    ) : Base(parent), InlineScriptUsage {
        override fun toString(): String {
            return "ParadoxScriptPropertyStub.InlineScriptUsage(" +
                "expression=$expression, " +
                "gameType=$gameType)"
        }
    }

    private class InlineScriptArgumentImpl(
        parent: StubElement<*>?,
        override val name: String,
    ) : Base(parent), InlineScriptArgument {
        override val argumentName get() = name
        override val expression get() = parentStub.expression

        override fun getParentStub() = super.getParentStub() as InlineScriptUsage

        override fun toString(): String {
            return "ParadoxScriptPropertyStub.InlineScriptArgument(" +
                "argumentName=$argumentName, " +
                "expression=$expression, " +
                "gameType=$gameType)"
        }
    }

    companion object {
        fun create(parent: StubElement<*>?, name: String): ParadoxScriptPropertyStub {
            return Impl(parent, name)
        }

        fun createDummy(parent: StubElement<*>?): ParadoxScriptPropertyStub {
            return Dummy(parent)
        }

        fun createDefineNamespace(
            parent: StubElement<*>?,
            name: String,
        ): DefineNamespace {
            return DefineNamespaceImpl(parent, name)
        }

        fun createDefineVariable(
            parent: StubElement<*>?,
            name: String,
        ): DefineVariable {
            return DefineVariableImpl(parent, name)
        }

        fun createInlineScriptUsage(
            parent: StubElement<*>?,
            name: String,
            expression: String,
        ): InlineScriptUsage {
            return InlineScriptUsageImpl(parent, name, expression)
        }

        fun createInlineScriptArgument(
            parent: StubElement<*>?,
            name: String,
        ): InlineScriptArgument {
            return InlineScriptArgumentImpl(parent, name)
        }
    }
}

