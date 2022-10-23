package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptStringStubElementType : IStubElementType<ParadoxScriptStringStub, ParadoxScriptString>(
	"STRING",
	ParadoxScriptLanguage
) {
	private const val externalId = "paradoxScript.string"
	
	override fun getExternalId() = externalId
	
	override fun createPsi(stub: ParadoxScriptStringStub): ParadoxScriptString {
		return SmartParadoxScriptString(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptString, parentStub: StubElement<*>): ParadoxScriptStringStub {
		//accept only one info
		val complexEnumInfo = ParadoxComplexEnumInfoHandler.resolve(psi, parentStub)
		val valueSetInfo = if(complexEnumInfo == null) ParadoxValueSetValueInfoHandler.resolve(psi, parentStub) else null
		val gameType = psi.fileInfo?.rootInfo?.gameType
		return ParadoxScriptStringStubImpl(parentStub, complexEnumInfo, valueSetInfo, gameType)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//skip if it may contain parameter
		if(node.isParameterAwareExpression()) return false
		return true
	}
	
	override fun indexStub(stub: ParadoxScriptStringStub, sink: IndexSink) {
		stub.complexEnumInfo?.let { info -> sink.occurrence(ParadoxComplexEnumIndex.key, info.enumName) }
		stub.valueSetValueInfo?.let { info -> sink.occurrence(ParadoxValueSetValueIndex.key, info.valueSetName) }
	}
	
	override fun serialize(stub: ParadoxScriptStringStub, dataStream: StubOutputStream) {
		val complexEnumInfo = stub.complexEnumInfo
		val valueSetValueInfo = stub.valueSetValueInfo
		when {
			complexEnumInfo != null -> {
				dataStream.writeByte(1)
				dataStream.writeName(complexEnumInfo.name)
				dataStream.writeName(complexEnumInfo.enumName)
			}
			valueSetValueInfo != null -> {
				dataStream.writeByte(2)
				dataStream.writeName(valueSetValueInfo.name)
				dataStream.writeName(valueSetValueInfo.valueSetName)
			}
			else -> {
				dataStream.writeByte(0)
			}
		}
		dataStream.writeName(stub.gameType?.id)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptStringStub {
		val flag = dataStream.readByte()
		val complexEnumInfo = if(flag != 1.toByte()) null else {
			val name = dataStream.readNameString().orEmpty()
			val enumName = dataStream.readNameString().orEmpty()
			ParadoxComplexEnumInfo(name, enumName)
		}
		val valueSetValueInfo = if(flag != 2.toByte()) null else {
			val name = dataStream.readNameString().orEmpty()
			val valueSetName = dataStream.readNameString().orEmpty()
			ParadoxValueSetValueInfo(name, valueSetName)
		}
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		return ParadoxScriptStringStubImpl(parentStub, complexEnumInfo, valueSetValueInfo, gameType)
	}
}