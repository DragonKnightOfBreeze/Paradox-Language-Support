package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

class ParadoxScriptPropertyStubElementType : ILightStubElementType<ParadoxScriptPropertyStub, ParadoxScriptProperty>("PROPERTY", ParadoxScriptLanguage) {
    override fun getExternalId() = "paradoxScript.PROPERTY"

    override fun createPsi(stub: ParadoxScriptPropertyStub): ParadoxScriptProperty {
        return ParadoxScriptPropertyImpl(stub, this)
    }

    override fun createStub(psi: ParadoxScriptProperty, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        return ParadoxDefinitionManager.createStub(psi, parentStub) ?: createDefaultStub(parentStub)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        return ParadoxDefinitionManager.createStub(tree, node, parentStub) ?: createDefaultStub(parentStub)
    }

    private fun createDefaultStub(parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        return ParadoxScriptPropertyStub.Dummy(parentStub)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        return true
    }

    override fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        return true
    }

    override fun indexStub(stub: ParadoxScriptPropertyStub, sink: IndexSink) {
        //Note that definition name can be empty (aka anonymous)
        sink.occurrence(ParadoxIndexManager.DefinitionNameKey, stub.name)
        ParadoxIndexConstraint.Definition.entries.forEach { constraint ->
            if (constraint.supports(stub.type)) {
                val name = if (constraint.ignoreCase) stub.name.lowercase() else stub.name
                sink.occurrence(constraint.indexKey, name)
            }
        }
        sink.occurrence(ParadoxIndexManager.DefinitionTypeKey, stub.type)
    }

    override fun serialize(stub: ParadoxScriptPropertyStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeName(stub.type)
        val subtypes = stub.subtypes
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

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        val name = dataStream.readNameString().orEmpty()
        val type = dataStream.readNameString().orEmpty()
        val subtypesSize = dataStream.readInt()
        val subtypes = if (subtypesSize == -1) null else MutableList(subtypesSize) { dataStream.readNameString().orEmpty() }
        val rootKey = dataStream.readNameString().orEmpty()
        val elementPath = dataStream.readNameString().orEmpty().let { ParadoxExpressionPath.resolve(it) }
        val gameType = dataStream.readByte().deoptimizeValue<ParadoxGameType>()
        return ParadoxScriptPropertyStub.Impl(parentStub, name, type, subtypes, rootKey, elementPath, gameType)
    }

    companion object {
        @JvmField
        val INSTANCE = ParadoxScriptPropertyStubElementType()
    }
}
