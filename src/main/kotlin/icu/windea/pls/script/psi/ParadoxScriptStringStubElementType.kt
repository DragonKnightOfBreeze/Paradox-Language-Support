package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptStringStubElementType: IStubElementType<ParadoxScriptValueStub, ParadoxScriptString>(
	"STRING",
	ParadoxScriptLanguage
) {
	private const val externalId = "paradoxScript.string"
	
	override fun getExternalId() = externalId
	
	override fun createPsi(stub: ParadoxScriptValueStub): ParadoxScriptString {
		return ParadoxScriptStringImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptString, parentStub: StubElement<*>): ParadoxScriptValueStub {
		val valueSetInfo = ParadoxValueSetValueInfoHandler.resolve(psi, parentStub) ?: throw InternalError()
		return ParadoxScriptValueStubImpl(parentStub, valueSetInfo)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		if(node.elementType != ParadoxScriptElementTypes.STRING) return false
		return ParadoxValueSetValueInfoHandler.resolve(node) != null
	}
	
	override fun indexStub(stub: ParadoxScriptValueStub, sink: IndexSink) {
		stub.valueSetValueInfo?.let { info -> sink.occurrence(ParadoxValueSetValueIndex.key, info.valueSetName) }
	}
	
	override fun serialize(stub: ParadoxScriptValueStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.valueSetValueInfo?.name)
		dataStream.writeName(stub.valueSetValueInfo?.valueSetName)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptValueStub {
		val valueSetInfo = run {
			val name = dataStream.readNameString()
			val valueSetName = dataStream.readNameString()
			if(name == null || valueSetName == null) null else ParadoxValueSetValueInfo(name, valueSetName)
		}
		return ParadoxScriptValueStubImpl(parentStub, valueSetInfo)
	}
}