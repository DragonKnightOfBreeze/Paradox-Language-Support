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
		val file = parentStub.psi.containingFile
		val gameType = selectGameType(file)
		val complexEnumInfo = ParadoxComplexEnumValueHandler.resolveInfo(psi, file)
		return ParadoxScriptStringStubImpl(parentStub, complexEnumInfo, gameType)
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
			sink.occurrence(ParadoxComplexEnumIndex.KEY, info.enumName)
			sink.occurrence(ParadoxComplexEnumValueIndex.KEY, info.name)
		}
	}
	
	override fun serialize(stub: ParadoxScriptStringStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.gameType?.id)
		val complexEnumValueInfo = stub.complexEnumValueInfo
		dataStream.writeName(complexEnumValueInfo?.name)
		dataStream.writeName(complexEnumValueInfo?.enumName)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptStringStub {
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		val complexEnumValueInfo = run {
			val name = dataStream.readNameString().orEmpty()
			val enumName = dataStream.readNameString().orEmpty()
			ParadoxComplexEnumValueInfo(name, enumName, gameType)
		}
		return ParadoxScriptStringStubImpl(parentStub, complexEnumValueInfo, gameType)
	}
}
