package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
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
		val file = psi.containingFile
		val gameType = file.fileInfo?.rootInfo?.gameType
		val complexEnumInfo = ParadoxComplexEnumValueInfoHandler.resolve(psi, file)
		val valueSetInfo = if(complexEnumInfo == null) ParadoxValueSetValueInfoHandler.resolve(psi) else null
		return ParadoxScriptStringStubImpl(parentStub, complexEnumInfo, valueSetInfo, gameType)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//skip if it may contain parameter
		if(node.isParameterAwareExpression()) return false
		return true
	}
	
	override fun indexStub(stub: ParadoxScriptStringStub, sink: IndexSink) {
		stub.complexEnumValueInfo?.let { info -> sink.occurrence(ParadoxComplexEnumIndex.key, info.enumName) }
		stub.valueSetValueInfo?.let { info -> sink.occurrence(ParadoxValueSetValueIndex.key, info.valueSetName) }
	}
	
	override fun serialize(stub: ParadoxScriptStringStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.gameType?.id)
		val complexEnumValueInfo = stub.complexEnumValueInfo
		val valueSetValueInfo = stub.valueSetValueInfo
		when {
			complexEnumValueInfo != null -> {
				dataStream.writeByte(1)
				dataStream.writeName(complexEnumValueInfo.name)
				dataStream.writeName(complexEnumValueInfo.enumName)
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
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptStringStub {
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		val flag = dataStream.readByte()
		val complexEnumValueInfo = if(flag != 1.toByte()) null else {
			val name = dataStream.readNameString().orEmpty()
			val enumName = dataStream.readNameString().orEmpty()
			ParadoxComplexEnumValueInfo(name, enumName, gameType)
		}
		val valueSetValueInfo = if(flag != 2.toByte()) null else {
			val name = dataStream.readNameString().orEmpty()
			val valueSetName = dataStream.readNameString().orEmpty()
			ParadoxValueSetValueInfo(name, valueSetName, gameType)
		}
		return ParadoxScriptStringStubImpl(parentStub, complexEnumValueInfo, valueSetValueInfo, gameType)
	}
}