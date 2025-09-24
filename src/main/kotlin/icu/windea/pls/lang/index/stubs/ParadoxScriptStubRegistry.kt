package icu.windea.pls.lang.index.stubs

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.LightStubElementFactory
import com.intellij.psi.stubs.ObjectStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import com.intellij.psi.stubs.StubSerializer
import icu.windea.pls.core.children
import icu.windea.pls.core.childrenOfType
import icu.windea.pls.core.firstChild
import icu.windea.pls.core.internNode
import icu.windea.pls.core.orNull
import icu.windea.pls.core.pass
import icu.windea.pls.core.unquote
import com.intellij.psi.util.elementType
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.constants.PlsPatternConstants
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue
import icu.windea.pls.model.paths.ParadoxExpressionPath
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.AT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.BLOCK
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY_KEY
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.STRING
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.STRING_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE_NAME
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl
import icu.windea.pls.script.psi.impl.ParadoxScriptScriptedVariableImpl
import icu.windea.pls.script.psi.stubs.ParadoxScriptFileStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptScriptedVariableStub

@Suppress("UnstableApiUsage")
class ParadoxScriptStubRegistry : StubRegistryExtension {
    override fun register(registry: StubRegistry) {
        registry.registerStubSerializer(ParadoxScriptFile.ELEMENT_TYPE, FileSerializer())
        registry.registerLightStubFactory(SCRIPTED_VARIABLE, ScriptedVariableFactory())
        registry.registerStubSerializer(SCRIPTED_VARIABLE, ScriptedVariableSerializer())
        registry.registerLightStubFactory(PROPERTY, PropertyFactory())
        registry.registerStubSerializer(PROPERTY, PropertySerializer())
    }

    class FileSerializer : StubSerializer<ParadoxScriptFileStub> {
        override fun getExternalId(): String {
            return "paradox.script.file"
        }

        override fun serialize(stub: ParadoxScriptFileStub, dataStream: StubOutputStream) {
            dataStream.writeByte(stub.gameType.optimizeValue())
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ParadoxScriptFileStub {
            val gameType = dataStream.readByte().deoptimizeValue<ParadoxGameType>()
            return ParadoxScriptFileStub.Impl(null, gameType)
        }

        override fun indexStub(stub: ParadoxScriptFileStub, sink: IndexSink) {
            pass() // do nothing
        }
    }

    class ScriptedVariableFactory : LightStubElementFactory<ParadoxScriptScriptedVariableStub, ParadoxScriptScriptedVariable> {
        override fun createStub(psi: ParadoxScriptScriptedVariable, parentStub: StubElement<out PsiElement>?): ParadoxScriptScriptedVariableStub {
            val name = psi.name?.orNull() ?: return createDefaultStub(parentStub)
            return ParadoxScriptScriptedVariableStub.Impl(parentStub, name)
        }

        override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub {
            val name = getNameFromNode(node, tree)?.orNull() ?: return createDefaultStub(parentStub)
            return ParadoxScriptScriptedVariableStub.Impl(parentStub, name)
        }

        private fun getNameFromNode(node: LighterASTNode, tree: LighterAST): String? {
            // 这里认为名字是不带参数的
            return node.firstChild(tree, SCRIPTED_VARIABLE_NAME)
                ?.children(tree)
                ?.takeIf { it.size == 2 && it.first().tokenType == AT }
                ?.last()
                ?.internNode(tree)?.toString()
        }

        private fun createDefaultStub(parentStub: StubElement<out PsiElement>?): ParadoxScriptScriptedVariableStub {
            return ParadoxScriptScriptedVariableStub.Dummy(parentStub)
        }

        override fun createPsi(stub: ParadoxScriptScriptedVariableStub): ParadoxScriptScriptedVariable {
            return ParadoxScriptScriptedVariableImpl(stub, SCRIPTED_VARIABLE)
        }

        // since 1.3.24, also index local scripted variables, not only global scripted variables
    }

    class ScriptedVariableSerializer : ObjectStubSerializer<ParadoxScriptScriptedVariableStub, StubElement<out PsiElement>> {
        override fun getExternalId(): String {
            return "paradox.script.scriptedVariable"
        }

        override fun serialize(stub: ParadoxScriptScriptedVariableStub, dataStream: StubOutputStream) {
            dataStream.writeName(stub.name)
            if (stub.name.isEmpty()) return
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<out PsiElement>?): ParadoxScriptScriptedVariableStub {
            val name = dataStream.readNameString().orEmpty()
            if (name.isEmpty()) return ParadoxScriptScriptedVariableStub.Dummy(parentStub)
            return ParadoxScriptScriptedVariableStub.Impl(parentStub, name)
        }

        override fun indexStub(stub: ParadoxScriptScriptedVariableStub, sink: IndexSink) {
            if (stub.name.isEmpty()) return
            sink.occurrence(ParadoxIndexKeys.ScriptedVariableName, stub.name)
        }
    }

    class PropertyFactory : LightStubElementFactory<ParadoxScriptPropertyStub, ParadoxScriptProperty> {
        override fun createStub(psi: ParadoxScriptProperty, parentStub: StubElement<out PsiElement>?): ParadoxScriptPropertyStub {
            // 1) inline_script 使用（PSI 阶段完整，解析表达式与是否带参数）
            if (psi.name.equals(ParadoxInlineScriptManager.inlineScriptKey, true)) {
                val info = ParadoxInlineScriptManager.getUsageInfo(psi)
                if (info != null) {
                    val hasArguments = psi.propertyValue?.elementType == BLOCK
                    return ParadoxScriptPropertyStub.InlineScriptUsageImpl(parentStub, info.expression, hasArguments)
                }
            }
            // 1.1) inline_script 的传参（仅在 PSI 阶段处理，索引传参名）
            run {
                val parentBlock = psi.parent
                if (parentBlock != null && parentBlock.elementType == BLOCK) {
                    val inlineProp = parentBlock.parent as? ParadoxScriptProperty
                    if (inlineProp != null && inlineProp.name.equals(ParadoxInlineScriptManager.inlineScriptKey, true)) {
                        val argumentName = psi.propertyKey.name
                        if (!argumentName.equals("script", true) && PlsPatternConstants.parameterName.matches(argumentName)) {
                            val usageInfo = ParadoxInlineScriptManager.getUsageInfo(inlineProp)
                            if (usageInfo != null) {
                                return ParadoxScriptPropertyStub.InlineScriptArgumentImpl(parentStub, argumentName, usageInfo.expression)
                            }
                        }
                    }
                }
            }
            // 2) 其他：尝试作为定义
            return ParadoxDefinitionManager.createStub(psi, parentStub) ?: createDefaultStub(parentStub)
        }

        override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
            // 1) inline_script 使用（仅处理字符串形式 inline_script = "..."）
            getPropertyKeyFromNode(node, tree)?.let { key ->
                if (key.equals(ParadoxInlineScriptManager.inlineScriptKey, true)) {
                    val expr = getStringValueFromNode(node, tree)
                    if (expr != null) {
                        return ParadoxScriptPropertyStub.InlineScriptUsageImpl(parentStub, expr, false)
                    }
                }
            }
            // 2) 其他：尝试作为定义
            return ParadoxDefinitionManager.createStub(tree, node, parentStub) ?: createDefaultStub(parentStub)
        }

        private fun createDefaultStub(parentStub: StubElement<out PsiElement>?): ParadoxScriptPropertyStub {
            return ParadoxScriptPropertyStub.Dummy(parentStub)
        }

        override fun createPsi(stub: ParadoxScriptPropertyStub): ParadoxScriptProperty {
            return ParadoxScriptPropertyImpl(stub, PROPERTY)
        }

        private fun getPropertyKeyFromNode(node: LighterASTNode, tree: LighterAST): String? {
            return node.firstChild(tree, PROPERTY_KEY)
                ?.childrenOfType(tree, PROPERTY_KEY_TOKEN)?.singleOrNull()
                ?.internNode(tree)?.toString()?.unquote()
        }

        private fun getStringValueFromNode(node: LighterASTNode, tree: LighterAST): String? {
            return node.firstChild(tree, STRING)
                ?.childrenOfType(tree, STRING_TOKEN)?.singleOrNull()
                ?.internNode(tree)?.toString()?.unquote()
        }
    }

    class PropertySerializer : ObjectStubSerializer<ParadoxScriptPropertyStub, StubElement<out PsiElement>> {
        override fun getExternalId(): String {
            return "paradox.script.property"
        }

        override fun serialize(stub: ParadoxScriptPropertyStub, dataStream: StubOutputStream) {
            val isInlineUsage = stub is ParadoxScriptPropertyStub.InlineScriptUsage
            dataStream.writeBoolean(isInlineUsage)
            val isInlineArgument = stub is ParadoxScriptPropertyStub.InlineScriptArgument
            dataStream.writeBoolean(isInlineArgument)
            if (isInlineUsage) {
                val s = stub as ParadoxScriptPropertyStub.InlineScriptUsage
                dataStream.writeName(s.inlineScriptExpression)
                dataStream.writeBoolean(s.hasArguments)
                return
            }
            if (isInlineArgument) {
                val s = stub as ParadoxScriptPropertyStub.InlineScriptArgument
                dataStream.writeName(s.argumentName)
                dataStream.writeName(s.inlineScriptExpression)
                return
            }
            // definition or dummy
            if (stub is ParadoxScriptPropertyStub.Definition) {
                val d = stub
                dataStream.writeName(d.definitionName)
                dataStream.writeName(d.definitionType)
                if (d.definitionType.isEmpty()) return
                val subtypes = d.definitionSubtypes
                if (subtypes == null) {
                    dataStream.writeInt(-1)
                } else {
                    dataStream.writeInt(subtypes.size)
                    subtypes.forEach { subtype -> dataStream.writeName(subtype) }
                }
                dataStream.writeName(d.rootKey)
                dataStream.writeName(d.elementPath.path)
            } else {
                // Dummy: 写入空的 name/type，让反序列化返回 Dummy
                dataStream.writeName("")
                dataStream.writeName("")
            }
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<out PsiElement>?): ParadoxScriptPropertyStub {
            val isInlineUsage = dataStream.readBoolean()
            val isInlineArgument = dataStream.readBoolean()
            if (isInlineUsage) {
                val expr = dataStream.readNameString().orEmpty()
                val hasArgs = dataStream.readBoolean()
                return ParadoxScriptPropertyStub.InlineScriptUsageImpl(parentStub, expr, hasArgs)
            }
            if (isInlineArgument) {
                val name = dataStream.readNameString().orEmpty()
                val expr = dataStream.readNameString().orEmpty()
                return ParadoxScriptPropertyStub.InlineScriptArgumentImpl(parentStub, name, expr)
            }
            val definitionName = dataStream.readNameString().orEmpty()
            val definitionType = dataStream.readNameString().orEmpty()
            if (definitionType.isEmpty()) return ParadoxScriptPropertyStub.Dummy(parentStub)
            val definitionSubtypesSize = dataStream.readInt()
            val definitionSubtypes = if (definitionSubtypesSize == -1) null else MutableList(definitionSubtypesSize) { dataStream.readNameString().orEmpty() }
            val rootKey = dataStream.readNameString().orEmpty()
            val elementPath = dataStream.readNameString().orEmpty().let { ParadoxExpressionPath.resolve(it) }
            return ParadoxScriptPropertyStub.Impl(parentStub, definitionName, definitionType, definitionSubtypes, rootKey, elementPath)
        }

        override fun indexStub(stub: ParadoxScriptPropertyStub, sink: IndexSink) {
            when (stub) {
                is ParadoxScriptPropertyStub.InlineScriptUsage -> {
                    if (stub.inlineScriptExpression.isNotEmpty()) {
                        sink.occurrence(ParadoxIndexKeys.InlineScriptUsageByExpression, stub.inlineScriptExpression)
                    }
                }
                is ParadoxScriptPropertyStub.InlineScriptArgument -> {
                    if (stub.argumentName.isNotEmpty()) {
                        sink.occurrence(ParadoxIndexKeys.InlineScriptArgumentByName, stub.argumentName)
                    }
                }
                is ParadoxScriptPropertyStub.Definition -> {
                    val d = stub
                    if (d.definitionType.isEmpty()) return
                    // Note that definition name can be empty (aka anonymous)
                    sink.occurrence(ParadoxIndexKeys.DefinitionName, d.definitionName)
                    ParadoxIndexConstraint.Definition.entries.forEach { constraint ->
                        if (constraint.supports(d.definitionType)) {
                            val name = if (constraint.ignoreCase) d.definitionName.lowercase() else d.definitionName
                            sink.occurrence(constraint.indexKey, name)
                        }
                    }
                    sink.occurrence(ParadoxIndexKeys.DefinitionType, d.definitionType)
                }
                else -> return // Dummy，不索引
            }
        }
    }
}
