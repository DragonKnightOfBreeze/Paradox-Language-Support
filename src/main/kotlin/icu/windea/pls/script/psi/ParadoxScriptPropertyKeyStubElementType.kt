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
        val complexEnumInfo = ParadoxComplexEnumValueHandler.resolveInfo(psi, file)
        val valueSetInfo =  ParadoxValueSetValueHandler.resolveInfo(psi)
        val inlineScriptInfo = ParadoxInlineScriptHandler.resolveInfo(psi, file)
        return ParadoxScriptPropertyKeyStubImpl(parentStub, complexEnumInfo, valueSetInfo, inlineScriptInfo, gameType)
    }
    
    override fun shouldCreateStub(node: ASTNode): Boolean {
        //skip if it may contain parameters
        if(node.isParameterAwareExpression()) return false
        return true
    }
    
    override fun indexStub(stub: ParadoxScriptPropertyKeyStub, sink: IndexSink) {
        stub.complexEnumValueInfo?.let { info ->
            sink.occurrence(ParadoxComplexEnumIndex.KEY, info.enumName)
            sink.occurrence(ParadoxComplexEnumValueIndex.KEY, info.name)
        }
        stub.valueSetValueInfo?.let { info ->
            sink.occurrence(ParadoxValueSetIndex.KEY, info.valueSetName)
            sink.occurrence(ParadoxValueSetValueIndex.KEY, info.name)
        }
        stub.inlineScriptInfo?.let { info ->
            sink.occurrence(ParadoxInlineScriptIndex.KEY, info.expression)
        }
    }
    
    override fun serialize(stub: ParadoxScriptPropertyKeyStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.gameType?.id)
        val complexEnumValueInfo = stub.complexEnumValueInfo
        val valueSetValueInfo = stub.valueSetValueInfo
        val inlineScriptInfo = stub.inlineScriptInfo
        dataStream.writeName(complexEnumValueInfo?.name)
        dataStream.writeName(complexEnumValueInfo?.enumName)
        dataStream.writeName(valueSetValueInfo?.name)
        dataStream.writeName(valueSetValueInfo?.valueSetName)
        dataStream.writeBoolean(valueSetValueInfo?.read ?: false)
        dataStream.writeName(inlineScriptInfo?.expression)
    }
    
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptPropertyKeyStub {
        val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
        val complexEnumValueInfo = run {
            val name = dataStream.readNameString().orEmpty()
            val enumName = dataStream.readNameString().orEmpty()
            ParadoxComplexEnumValueInfo(name, enumName, gameType)
        }
        val valueSetValueInfo = run {
            val name = dataStream.readNameString().orEmpty()
            val valueSetName = dataStream.readNameString().orEmpty()
            val read = dataStream.readBoolean()
            ParadoxValueSetValueInfo(name, valueSetName, gameType, read)
        }
        val inlineScriptInfo = run {
            val expression = dataStream.readNameString().orEmpty()
            ParadoxInlineScriptInfo(expression, gameType)
        }
        return ParadoxScriptPropertyKeyStubImpl(parentStub, complexEnumValueInfo, valueSetValueInfo, inlineScriptInfo, gameType)
    }
}
