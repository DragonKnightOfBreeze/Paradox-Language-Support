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
import icu.windea.pls.core.pass
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue
import icu.windea.pls.model.paths.ParadoxExpressionPath
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE
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
            return ParadoxScriptFileStub.create(null, gameType)
        }

        override fun indexStub(stub: ParadoxScriptFileStub, sink: IndexSink) {
            pass() // do nothing
        }
    }

    class ScriptedVariableFactory : LightStubElementFactory<ParadoxScriptScriptedVariableStub, ParadoxScriptScriptedVariable> {
        // since 1.3.24, also index local scripted variables, not only global scripted variables

        override fun createStub(psi: ParadoxScriptScriptedVariable, parentStub: StubElement<out PsiElement>?): ParadoxScriptScriptedVariableStub {
            return ParadoxScriptStubManager.createScriptedVariableStub(psi, parentStub)
        }

        override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub {
            return ParadoxScriptStubManager.createScriptedVariableStub(tree, node, parentStub)
        }

        override fun createPsi(stub: ParadoxScriptScriptedVariableStub): ParadoxScriptScriptedVariable {
            return ParadoxScriptScriptedVariableImpl(stub, SCRIPTED_VARIABLE)
        }
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
            if (name.isEmpty()) return ParadoxScriptScriptedVariableStub.createDummy(parentStub)
            return ParadoxScriptScriptedVariableStub.create(parentStub, name)
        }

        override fun indexStub(stub: ParadoxScriptScriptedVariableStub, sink: IndexSink) {
            if (stub.name.isEmpty()) return
            sink.occurrence(ParadoxIndexKeys.ScriptedVariableName, stub.name)
        }
    }

    class PropertyFactory : LightStubElementFactory<ParadoxScriptPropertyStub, ParadoxScriptProperty> {
        override fun createStub(psi: ParadoxScriptProperty, parentStub: StubElement<out PsiElement>?): ParadoxScriptPropertyStub {
            return ParadoxScriptStubManager.createPropertyStub(psi, parentStub)
        }

        override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
            return ParadoxScriptStubManager.createPropertyStub(tree, node, parentStub)
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
            when (stub) {
                is ParadoxScriptPropertyStub.Definition -> {
                    dataStream.writeByte(Flags.definition)
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
                }
                is ParadoxScriptPropertyStub.InlineScriptUsage -> {
                    dataStream.writeByte(Flags.inlineScriptUsage)
                    dataStream.writeName(stub.name)
                    dataStream.writeName(stub.inlineScriptExpression)
                }
                is ParadoxScriptPropertyStub.InlineScriptArgument -> {
                    dataStream.writeByte(Flags.inlineScriptArgument)
                    dataStream.writeName(stub.name)
                }
                else -> {
                    dataStream.writeByte(Flags.property)
                    dataStream.writeName(stub.name)
                }
            }
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<out PsiElement>?): ParadoxScriptPropertyStub {
            val flag = dataStream.readByte()
            return when (flag.toInt()) {
                Flags.definition -> {
                    val definitionName = dataStream.readNameString().orEmpty()
                    val definitionType = dataStream.readNameString().orEmpty()
                    if (definitionType.isEmpty()) return ParadoxScriptPropertyStub.createDummy(parentStub)
                    val definitionSubtypesSize = dataStream.readInt()
                    val definitionSubtypes = if (definitionSubtypesSize == -1) null else MutableList(definitionSubtypesSize) { dataStream.readNameString().orEmpty() }
                    val rootKey = dataStream.readNameString().orEmpty()
                    val elementPath = dataStream.readNameString().orEmpty().let { ParadoxExpressionPath.resolve(it) }
                    ParadoxScriptPropertyStub.createDefinition(parentStub, definitionName, definitionType, definitionSubtypes, rootKey, elementPath)
                }
                Flags.inlineScriptUsage -> {
                    val name = dataStream.readNameString().orEmpty()
                    val inlineScriptExpression = dataStream.readNameString().orEmpty()
                    ParadoxScriptPropertyStub.createInlineScriptUsage(parentStub, name, inlineScriptExpression)
                }
                Flags.inlineScriptArgument -> {
                    val name = dataStream.readNameString().orEmpty()
                    ParadoxScriptPropertyStub.createInlineScriptArgument(parentStub, name)
                }
                else -> {
                    val name = dataStream.readNameString().orEmpty()
                    if (name.isEmpty()) return ParadoxScriptPropertyStub.createDummy(parentStub)
                    ParadoxScriptPropertyStub.create(parentStub, name)
                }
            }
        }

        override fun indexStub(stub: ParadoxScriptPropertyStub, sink: IndexSink) {
            when (stub) {
                is ParadoxScriptPropertyStub.Definition -> {
                    val definitionName = stub.definitionName
                    val definitionType = stub.definitionType
                    if (definitionName != null) {
                        // Note that definition name can be empty (aka anonymous)
                        sink.occurrence(ParadoxIndexKeys.DefinitionName, definitionName)
                        ParadoxIndexConstraint.Definition.entries.forEach { constraint ->
                            if (constraint.supports(definitionType)) {
                                val name = if (constraint.ignoreCase) definitionName.lowercase() else definitionName
                                sink.occurrence(constraint.indexKey, name)
                            }
                        }
                    }
                    if (definitionType.isNotEmpty()) {
                        sink.occurrence(ParadoxIndexKeys.DefinitionType, definitionType)
                    }
                }
                is ParadoxScriptPropertyStub.InlineScriptUsage -> {
                    sink.occurrence(ParadoxIndexKeys.InlineScriptUsage, stub.inlineScriptExpression)
                }
                is ParadoxScriptPropertyStub.InlineScriptArgument -> {
                    sink.occurrence(ParadoxIndexKeys.InlineScriptArgument, stub.argumentName)
                }
            }
        }

        private object Flags {
            const val definition = 1
            const val inlineScriptUsage = 2
            const val inlineScriptArgument = 3
            const val property = 0
        }
    }
}
