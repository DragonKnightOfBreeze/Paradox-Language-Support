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
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.pass
import icu.windea.pls.core.writeByte
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.model.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.model.forGameType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
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
            dataStream.writeByte(stub.gameType.optimized(OptimizerRegistry.forGameType()))
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ParadoxScriptFileStub {
            val gameType = dataStream.readByte().deoptimized(OptimizerRegistry.forGameType())
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
            sink.occurrence(PlsIndexKeys.ScriptedVariableName, stub.name)
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
                    val named = stub.definitionName == stub.typeKey
                    if (named) {
                        dataStream.writeByte(Flags.definitionNamed)
                    } else {
                        dataStream.writeByte(Flags.definition)
                    }
                    dataStream.writeName(stub.definitionName)
                    dataStream.writeName(stub.definitionType)
                    if (stub.definitionType.isEmpty()) return
                    val definitionSubtypes = stub.definitionSubtypes
                    if (definitionSubtypes == null) {
                        dataStream.writeInt(-1)
                    } else {
                        dataStream.writeInt(definitionSubtypes.size)
                        definitionSubtypes.forEach { dataStream.writeName(it) }
                    }
                    if (!named) {
                        dataStream.writeName(stub.name)
                    }
                    val rootKeys = stub.rootKeys
                    dataStream.writeInt(rootKeys.size)
                    rootKeys.forEach { dataStream.writeName(it) }
                }
                is ParadoxScriptPropertyStub.DefineNamespace -> {
                    dataStream.writeByte(Flags.defineNamespace)
                    dataStream.writeName(stub.name)
                }
                is ParadoxScriptPropertyStub.DefineVariable -> {
                    dataStream.writeByte(Flags.defineVariable)
                    dataStream.writeName(stub.name)
                }
                is ParadoxScriptPropertyStub.InlineScriptUsage -> {
                    dataStream.writeByte(Flags.inlineScriptUsage)
                    dataStream.writeName(stub.name)
                    dataStream.writeName(stub.expression)
                }
                is ParadoxScriptPropertyStub.InlineScriptArgument -> {
                    dataStream.writeByte(Flags.inlineScriptArgument)
                    dataStream.writeName(stub.name)
                }
                is ParadoxScriptPropertyStub.DefinitionInjection -> { // #252
                    dataStream.writeByte(Flags.definitionInjection)
                    dataStream.writeName(stub.name)
                    dataStream.writeName(stub.mode)
                    dataStream.writeName(stub.target)
                    dataStream.writeName(stub.type)
                }
                else -> {
                    dataStream.writeByte(Flags.property)
                    dataStream.writeName(stub.name)
                }
            }
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<out PsiElement>?): ParadoxScriptPropertyStub {
            val flag = dataStream.readByte()
            return when (flag) {
                Flags.definition, Flags.definitionNamed -> {
                    val definitionName = dataStream.readNameString().orEmpty()
                    val definitionType = dataStream.readNameString().orEmpty()
                    if (definitionType.isEmpty()) return ParadoxScriptPropertyStub.createDummy(parentStub)
                    val definitionSubtypesSize = dataStream.readInt()
                    val definitionSubtypes = when (definitionSubtypesSize) {
                        -1 -> null
                        0 -> emptyList()
                        else -> MutableList(definitionSubtypesSize) { dataStream.readNameString().orEmpty() }
                    }
                    val name = if (flag == Flags.definitionNamed) definitionName else dataStream.readNameString().orEmpty()
                    val rootKeysSize = dataStream.readInt()
                    val rootKeys = when (rootKeysSize) {
                        0 -> emptyList()
                        else -> MutableList(rootKeysSize) { dataStream.readNameString().orEmpty() }
                    }
                    ParadoxScriptPropertyStub.createDefinition(parentStub, name, definitionName, definitionType, definitionSubtypes, rootKeys)
                }
                Flags.defineNamespace -> {
                    val name = dataStream.readNameString().orEmpty()
                    if (name.isEmpty()) return ParadoxScriptPropertyStub.createDummy(parentStub)
                    ParadoxScriptPropertyStub.createDefineNamespace(parentStub, name)
                }
                Flags.defineVariable -> {
                    val name = dataStream.readNameString().orEmpty()
                    if (name.isEmpty()) return ParadoxScriptPropertyStub.createDummy(parentStub)
                    ParadoxScriptPropertyStub.createDefineVariable(parentStub, name)
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
                Flags.definitionInjection -> { // #252
                    val name = dataStream.readNameString().orEmpty()
                    val mode = dataStream.readNameString().orEmpty()
                    val target = dataStream.readNameString()
                    val type = dataStream.readNameString()
                    ParadoxScriptPropertyStub.createDefinitionInjection(parentStub, name, mode, target, type)
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
                    if (!definitionName.isNullOrEmpty()) {
                        // notte that definition name can be empty (aka anonymous), and skipped for definition name indices here
                        sink.occurrence(PlsIndexKeys.DefinitionName, definitionName)
                        ParadoxDefinitionIndexConstraint.entries.forEach { constraint ->
                            if (constraint.test(definitionType)) {
                                val name = if (constraint.ignoreCase) definitionName.lowercase() else definitionName
                                sink.occurrence(constraint.indexKey, name)
                            }
                        }
                    }
                    if (definitionType.isNotEmpty()) {
                        sink.occurrence(PlsIndexKeys.DefinitionType, definitionType)
                    }
                }
                is ParadoxScriptPropertyStub.DefineNamespace -> {
                    if (stub.namespace.isEmpty()) return
                    sink.occurrence(PlsIndexKeys.DefineNamespace, stub.namespace)
                }
                is ParadoxScriptPropertyStub.DefineVariable -> {
                    if (stub.namespace.isEmpty()) return
                    if (stub.variable.isEmpty()) return
                    val key = stub.namespace + "\u0000" + stub.variable
                    sink.occurrence(PlsIndexKeys.DefineVariable, key)
                }
                is ParadoxScriptPropertyStub.InlineScriptUsage -> {
                    if (stub.expression.isEmpty()) return
                    sink.occurrence(PlsIndexKeys.InlineScriptUsage, stub.expression)
                }
                is ParadoxScriptPropertyStub.InlineScriptArgument -> {
                    if (stub.expression.isEmpty()) return
                    sink.occurrence(PlsIndexKeys.InlineScriptArgument, stub.expression)
                }
                is ParadoxScriptPropertyStub.DefinitionInjection -> { // #252
                    if (stub.target.isNullOrEmpty()) return
                    if (stub.type.isNullOrEmpty()) return
                    val targetKey = stub.type + "@" + stub.target
                    sink.occurrence(PlsIndexKeys.DefinitionInjectionTarget, targetKey)
                }
                else -> {}
            }
        }

        private object Flags {
            const val property: Byte = 0
            const val definition: Byte = 1
            const val defineNamespace: Byte = 6
            const val defineVariable: Byte = 7
            const val inlineScriptUsage: Byte = 2
            const val inlineScriptArgument: Byte = 3
            const val definitionNamed: Byte = 4
            const val definitionInjection: Byte = 5 // #252
        }
    }
}
