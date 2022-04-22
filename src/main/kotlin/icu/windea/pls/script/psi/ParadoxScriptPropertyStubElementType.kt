package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

//注意：这里的node和psi会变更，因此无法从userDataMap直接获取数据！
//这之后，virtualFile会变为LightVirtualFIle，无法从virtualFile获取数据！psiFile也会变更
//检查createStub的psi和parentStub，尝试获取definition
//或者：尝试直接从shouldCreateStub的node获取definition，缓存同步到createStub
//或者：重写com.intellij.psi.stubs.DefaultStubBuilder.StubBuildingWalkingVisitor.createStub，尝试从更多信息中获取
//要求：必须能够获取paradoxPath和paradoxPropertyPath！即使psiFIle在内存中也要缓存信息

class ParadoxScriptPropertyStubElementType : IStubElementType<ParadoxScriptDefinitionStub, ParadoxScriptProperty>(
	"PARADOX_SCRIPT_PROPERTY",
	ParadoxScriptLanguage
) {
	override fun getExternalId(): String {
		return "paradoxScript.property"
	}
	
	override fun createPsi(stub: ParadoxScriptDefinitionStub): ParadoxScriptProperty {
		return ParadoxScriptPropertyImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptProperty, parentStub: StubElement<*>): ParadoxScriptDefinitionStub {
		//这里使用scriptProperty.definitionInfo.name而非scriptProperty.name
		val definitionInfo = psi.definitionInfo
		val name = definitionInfo?.name ?: ""
		val type = definitionInfo?.type ?: ""
		val subtypes = definitionInfo?.subtypes ?: emptyList()
		val rootKey = definitionInfo?.rootKey ?: ""
		return ParadoxScriptDefinitionStubImpl(parentStub, name, type, subtypes, rootKey)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//仅当是definition时才会创建索引
		val element = node.psi as? ParadoxScriptProperty ?: return false
		return element.definitionInfo != null
	}
	
	override fun indexStub(stub: ParadoxScriptDefinitionStub, sink: IndexSink) {
		//索引definition的name和type
		sink.occurrence(ParadoxDefinitionNameIndex.key, stub.name)
		sink.occurrence(ParadoxDefinitionTypeIndex.key, stub.type)
	}
	
	override fun serialize(stub: ParadoxScriptDefinitionStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
		dataStream.writeName(stub.type)
		dataStream.writeName(stub.subtypes.toCommaDelimitedString())
		dataStream.writeName(stub.rootKey)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptDefinitionStub {
		val name = dataStream.readNameString().orEmpty()
		val type = dataStream.readNameString().orEmpty()
		val subtypes = dataStream.readNameString().orEmpty().toCommaDelimitedStringList()
		val rootKey = dataStream.readNameString().orEmpty()
		return ParadoxScriptDefinitionStubImpl(parentStub, name, type, subtypes, rootKey)
	}
}
