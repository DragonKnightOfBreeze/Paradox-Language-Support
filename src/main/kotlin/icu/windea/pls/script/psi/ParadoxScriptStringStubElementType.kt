package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
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
		val file = psi.containingFile
		val gameType = file.fileInfo?.rootInfo?.gameType
		val complexEnumInfo = ParadoxComplexEnumValueHandler.resolveInfo(psi, file)
		val valueSetInfo = if(complexEnumInfo != null) null else ParadoxValueSetValueHandler.resolveInfo(psi)
		return ParadoxScriptStringStubImpl(parentStub, complexEnumInfo, valueSetInfo, gameType)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//skip if it may contain parameters
		if(node.isParameterAwareExpression()) return false
		//skip if it is not a property value or block value
		val parentType = node.treeParent.elementType
		if(parentType != PROPERTY && parentType != BLOCK && parentType != ROOT_BLOCK) return false
		return true
	}
	
	override fun indexStub(stub: ParadoxScriptStringStub, sink: IndexSink) {
		stub.complexEnumValueInfo?.let { info ->
			sink.occurrence(ParadoxComplexEnumIndex.key, info.enumName)
			sink.occurrence(ParadoxComplexEnumValueIndex.key, info.name)
		}
		stub.valueSetValueInfo?.let { info -> 
			sink.occurrence(ParadoxValueSetIndex.key, info.valueSetName)
		}
	}
	
	override fun serialize(stub: ParadoxScriptStringStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.gameType?.id)
		val complexEnumValueInfo = stub.complexEnumValueInfo
		val valueSetValueInfo = stub.valueSetValueInfo
		dataStream.writeName(complexEnumValueInfo?.name)
		dataStream.writeName(complexEnumValueInfo?.enumName)
		dataStream.writeName(valueSetValueInfo?.name)
		dataStream.writeName(valueSetValueInfo?.valueSetName)
		dataStream.writeBoolean(valueSetValueInfo?.read ?: false)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptStringStub {
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		val complexEnumValueInfo = run {
			val name = dataStream.readNameString().orEmpty()
			val enumName = dataStream.readNameString().orEmpty()
			icu.windea.pls.lang.model.ParadoxComplexEnumValueInfo(name, enumName, gameType)
		}
		val valueSetValueInfo = run {
			val name = dataStream.readNameString().orEmpty()
			val valueSetName = dataStream.readNameString().orEmpty()
			val read = dataStream.readBoolean()
			ParadoxValueSetValueInfo(name, valueSetName, gameType, read)
		}
		return ParadoxScriptStringStubImpl(parentStub, complexEnumValueInfo, valueSetValueInfo, gameType)
	}
}
