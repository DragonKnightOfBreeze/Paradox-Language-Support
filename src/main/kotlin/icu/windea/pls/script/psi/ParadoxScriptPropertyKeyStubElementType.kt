package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptPropertyKeyStubElementType : IStubElementType<ParadoxScriptPropertyKeyStub, ParadoxScriptPropertyKey>(
    "PROPERTY_KEY",
    ParadoxScriptLanguage
) {
    private const val externalId = "paradoxScript.propertyKey"
    
    override fun getExternalId() = externalId
    
    override fun createPsi(stub: ParadoxScriptPropertyKeyStub): ParadoxScriptPropertyKey {
        return SmartParadoxScriptPropertyKey(stub, this)
    }
    
    override fun createStub(psi: ParadoxScriptPropertyKey, parentStub: StubElement<*>): ParadoxScriptPropertyKeyStub {
        val file = parentStub.psi.containingFile
        val gameType = selectGameType(file)
        val inlineScriptInfo = ParadoxInlineScriptHandler.resolveInfo(psi, file)
        return ParadoxScriptPropertyKeyStubImpl(parentStub, inlineScriptInfo, gameType)
    }
    
    override fun shouldCreateStub(node: ASTNode): Boolean {
        //skip if it may contain parameters
        if(node.isParameterAwareExpression()) return false
        return true
    }
    
    override fun indexStub(stub: ParadoxScriptPropertyKeyStub, sink: IndexSink) {
        stub.inlineScriptInfo?.let { info ->
            sink.occurrence(ParadoxInlineScriptIndex.KEY, info.expression)
        }
    }
    
    override fun serialize(stub: ParadoxScriptPropertyKeyStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.gameType?.id)
        val inlineScriptInfo = stub.inlineScriptInfo
        dataStream.writeName(inlineScriptInfo?.expression)
    }
    
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptPropertyKeyStub {
        val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
        val inlineScriptInfo = run {
            val expression = dataStream.readNameString().orEmpty()
            ParadoxInlineScriptInfo(expression, gameType)
        }
        return ParadoxScriptPropertyKeyStubImpl(parentStub, inlineScriptInfo, gameType)
    }
}
