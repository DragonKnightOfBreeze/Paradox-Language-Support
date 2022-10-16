package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptStringStubElementType: IStubElementType<ParadoxScriptStringStub, ParadoxScriptString>(
	"STRING",
	ParadoxScriptLanguage
) {
	private const val externalId = "paradoxScript.string"
	
	override fun getExternalId() = externalId
	
	override fun createPsi(stub: ParadoxScriptStringStub): ParadoxScriptString {
		return SmartParadoxScriptString(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptString, parentStub: StubElement<*>): ParadoxScriptStringStub {
		val valueSetInfo = ParadoxValueSetValueInfoHandler.resolve(psi, parentStub)
		val gameType = psi.fileInfo?.rootInfo?.gameType
		return ParadoxScriptStringStubImpl(parentStub, valueSetInfo, gameType)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//only for string
		if(node.elementType != ParadoxScriptElementTypes.STRING) return false
		//skip if may contain parameter
		val isParameterAware = node.processChild { it.elementType != ParadoxScriptElementTypes.PARAMETER }
		if(isParameterAware) return false
		return true
	}
	
	override fun indexStub(stub: ParadoxScriptStringStub, sink: IndexSink) {
		stub.valueSetValueInfo?.let { info -> sink.occurrence(ParadoxValueSetValueIndex.key, info.valueSetName) }
	}
	
	override fun serialize(stub: ParadoxScriptStringStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.valueSetValueInfo?.name)
		dataStream.writeName(stub.valueSetValueInfo?.valueSetName)
		dataStream.writeName(stub.gameType?.id)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptStringStub {
		val valueSetInfo = run {
			val name = dataStream.readNameString()
			val valueSetName = dataStream.readNameString()
			if(name == null || valueSetName == null) null else ParadoxValueSetValueInfo(name, valueSetName)
		}
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		return ParadoxScriptStringStubImpl(parentStub, valueSetInfo, gameType)
	}
}