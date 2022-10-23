package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptPropertyKeyStubElementType: IStubElementType<ParadoxScriptPropertyKeyStub, ParadoxScriptPropertyKey>(
	"PROPERTY_KEY",
	ParadoxScriptLanguage
) {
	private const val externalId = "paradoxScript.propertyKey"
	
	override fun getExternalId() = externalId
	
	override fun createPsi(stub: ParadoxScriptPropertyKeyStub): ParadoxScriptPropertyKey {
		return SmartParadoxScriptPropertyKey(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptPropertyKey, parentStub: StubElement<*>): ParadoxScriptPropertyKeyStub {
		//accept only one info
		val complexEnumInfo = ParadoxComplexEnumValueInfoHandler.resolve(psi, parentStub)
		val gameType = psi.fileInfo?.rootInfo?.gameType
		return ParadoxScriptPropertyKeyStubImpl(parentStub, complexEnumInfo, gameType)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//skip if it may contain parameter
		if(node.isParameterAwareExpression()) return false
		return true
	}
	
	override fun indexStub(stub: ParadoxScriptPropertyKeyStub, sink: IndexSink) {
		stub.complexEnumValueInfo?.let { info -> sink.occurrence(ParadoxComplexEnumIndex.key, info.enumName) }
	}
	
	override fun serialize(stub: ParadoxScriptPropertyKeyStub, dataStream: StubOutputStream) {
		val complexEnumValueInfo = stub.complexEnumValueInfo
		when {
			complexEnumValueInfo != null -> {
				dataStream.writeByte(1)
				dataStream.writeName(complexEnumValueInfo.name)
				dataStream.writeName(complexEnumValueInfo.enumName)
			}
			else -> {
				dataStream.writeByte(0)
			}
		}
		dataStream.writeName(stub.gameType?.id)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptPropertyKeyStub {
		val flag = dataStream.readByte()
		val complexEnumValueInfo = if(flag != 1.toByte()) null else {
			val name = dataStream.readNameString().orEmpty()
			val enumName = dataStream.readNameString().orEmpty()
			ParadoxComplexEnumValueInfo(name, enumName)
		}
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		return ParadoxScriptPropertyKeyStubImpl(parentStub, complexEnumValueInfo, gameType)
	}
}