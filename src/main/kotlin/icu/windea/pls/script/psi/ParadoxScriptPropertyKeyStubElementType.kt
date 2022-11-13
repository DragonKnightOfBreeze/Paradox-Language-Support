package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.model.*
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
		//accept only one info
		val file = psi.containingFile
		val gameType = file.fileInfo?.rootInfo?.gameType
		val complexEnumInfo = ParadoxComplexEnumValueInfoHandler.resolve(psi)
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
		dataStream.writeName(stub.gameType?.id)
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
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptPropertyKeyStub {
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		val flag = dataStream.readByte()
		val complexEnumValueInfo = if(flag != 1.toByte()) null else {
			val name = dataStream.readNameString().orEmpty()
			val enumName = dataStream.readNameString().orEmpty()
			ParadoxComplexEnumValueInfo(name, enumName, gameType)
		}
		return ParadoxScriptPropertyKeyStubImpl(parentStub, complexEnumValueInfo, gameType)
	}
}