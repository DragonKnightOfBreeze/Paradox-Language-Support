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
import icu.windea.pls.core.firstChild
import icu.windea.pls.core.internNode
import icu.windea.pls.core.pass
import icu.windea.pls.core.writeByte
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue
import icu.windea.pls.model.paths.ParadoxExpressionPath
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.AT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY
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
            val name = psi.name?.takeIf { it.isNotEmpty() } ?: return createDefaultStub(parentStub)
            val gameType = selectGameType(parentStub) ?: return createDefaultStub(parentStub)
            return ParadoxScriptScriptedVariableStub.Impl(parentStub, name, gameType)
        }

        override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub {
            val name = getNameFromNode(node, tree)?.takeIf { it.isNotEmpty() } ?: return createDefaultStub(parentStub)
            val gameType = selectGameType(parentStub) ?: return createDefaultStub(parentStub)
            return ParadoxScriptScriptedVariableStub.Impl(parentStub, name, gameType)
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
            dataStream.writeByte(stub.gameType.optimizeValue())
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<out PsiElement>?): ParadoxScriptScriptedVariableStub {
            val name = dataStream.readNameString().orEmpty()
            if (name.isEmpty()) return ParadoxScriptScriptedVariableStub.Dummy(parentStub)
            val gameType = dataStream.readByte().deoptimizeValue<ParadoxGameType>()
            return ParadoxScriptScriptedVariableStub.Impl(parentStub, name, gameType)
        }

        override fun indexStub(stub: ParadoxScriptScriptedVariableStub, sink: IndexSink) {
            if (stub.name.isEmpty()) return
            sink.occurrence(ParadoxIndexKeys.ScriptedVariableName, stub.name)
        }
    }

    class PropertyFactory : LightStubElementFactory<ParadoxScriptPropertyStub, ParadoxScriptProperty> {
        override fun createStub(psi: ParadoxScriptProperty, parentStub: StubElement<out PsiElement>?): ParadoxScriptPropertyStub {
            return ParadoxDefinitionManager.createStub(psi, parentStub) ?: createDefaultStub(parentStub)
        }

        override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
            return ParadoxDefinitionManager.createStub(tree, node, parentStub) ?: createDefaultStub(parentStub)
        }

        private fun createDefaultStub(parentStub: StubElement<out PsiElement>?): ParadoxScriptPropertyStub {
            return ParadoxScriptPropertyStub.Dummy(parentStub)
        }

        override fun createPsi(stub: ParadoxScriptPropertyStub): ParadoxScriptProperty {
            return ParadoxScriptPropertyImpl(stub, PROPERTY)
        }
    }

    class PropertySerializer : ObjectStubSerializer<ParadoxScriptPropertyStub, StubElement<out PsiElement>> {
        override fun getExternalId(): String {
            return "paradox.script.property"
        }

        override fun serialize(stub: ParadoxScriptPropertyStub, dataStream: StubOutputStream) {
            dataStream.writeName(stub.definitionName)
            dataStream.writeName(stub.definitionType)
            if (stub.definitionType.isEmpty()) return
            val subtypes = stub.definitionSubtypes
            if (subtypes == null) {
                dataStream.writeInt(-1)
            } else {
                dataStream.writeInt(subtypes.size)
                subtypes.forEach { subtype -> dataStream.writeName(subtype) }
            }
            dataStream.writeName(stub.rootKey)
            dataStream.writeName(stub.elementPath.path)
            dataStream.writeByte(stub.gameType.optimizeValue())
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<out PsiElement>?): ParadoxScriptPropertyStub {
            val definitionName = dataStream.readNameString().orEmpty()
            val definitionType = dataStream.readNameString().orEmpty()
            if (definitionType.isEmpty()) return ParadoxScriptPropertyStub.Dummy(parentStub)
            val definitionSubtypesSize = dataStream.readInt()
            val definitionSubtypes = if (definitionSubtypesSize == -1) null else MutableList(definitionSubtypesSize) { dataStream.readNameString().orEmpty() }
            val rootKey = dataStream.readNameString().orEmpty()
            val elementPath = dataStream.readNameString().orEmpty().let { ParadoxExpressionPath.resolve(it) }
            val gameType = dataStream.readByte().deoptimizeValue<ParadoxGameType>()
            return ParadoxScriptPropertyStub.Impl(parentStub, definitionName, definitionType, definitionSubtypes, rootKey, elementPath, gameType)
        }

        override fun indexStub(stub: ParadoxScriptPropertyStub, sink: IndexSink) {
            if (stub.definitionType.isEmpty()) return
            // Note that definition name can be empty (aka anonymous)
            sink.occurrence(ParadoxIndexKeys.DefinitionName, stub.definitionName)
            ParadoxIndexConstraint.Definition.entries.forEach { constraint ->
                if (constraint.supports(stub.definitionType)) {
                    val name = if (constraint.ignoreCase) stub.definitionName.lowercase() else stub.definitionName
                    sink.occurrence(constraint.indexKey, name)
                }
            }
            sink.occurrence(ParadoxIndexKeys.DefinitionType, stub.definitionType)
        }
    }
}
