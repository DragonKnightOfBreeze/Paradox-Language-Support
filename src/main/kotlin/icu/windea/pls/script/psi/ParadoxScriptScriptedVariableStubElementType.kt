package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptScriptedVariableStubElementType : ILightStubElementType<ParadoxScriptScriptedVariableStub, ParadoxScriptScriptedVariable>(
    "SCRIPTED_VARIABLE",
    ParadoxScriptLanguage
) {
    private const val externalId = "paradoxScript.scriptedVariable"
    
    override fun getExternalId() = externalId
    
    override fun createPsi(stub: ParadoxScriptScriptedVariableStub): ParadoxScriptScriptedVariable {
        return ParadoxScriptScriptedVariableImpl(stub, this)
    }
    
    override fun createStub(psi: ParadoxScriptScriptedVariable, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub {
        return ParadoxScriptedVariableHandler.createStub(psi, parentStub) ?: createDefaultStub(parentStub)
    }
    
    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub {
        return ParadoxScriptedVariableHandler.createStub(tree, node, parentStub) ?: createDefaultStub(parentStub)
    }
    
    private fun createDefaultStub(parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub {
        return ParadoxScriptScriptedVariableStubImpl(parentStub, "", ParadoxGameType.placeholder())
    }
    
    override fun shouldCreateStub(node: ASTNode): Boolean {
        return ParadoxScriptedVariableHandler.shouldCreateStub(node)
    }
    
    override fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        return ParadoxScriptedVariableHandler.shouldCreateStub(tree, node, parentStub)
    }
    
    override fun indexStub(stub: ParadoxScriptScriptedVariableStub, sink: IndexSink) {
        sink.occurrence(ParadoxScriptedVariableNameIndexKey, stub.name)
    }
    
    override fun serialize(stub: ParadoxScriptScriptedVariableStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeByte(stub.gameType.toByte())
    }
    
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub {
        val name = dataStream.readNameString().orEmpty()
        val gameType = dataStream.readByte().toGameType()
        return ParadoxScriptScriptedVariableStubImpl(parentStub, name, gameType)
    }
}
