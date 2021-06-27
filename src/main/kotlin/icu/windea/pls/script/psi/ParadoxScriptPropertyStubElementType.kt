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

class ParadoxScriptPropertyStubElementType : IStubElementType<ParadoxScriptPropertyStub, ParadoxScriptProperty>(
	"PARADOX_SCRIPT_PROPERTY",
	ParadoxScriptLanguage
) {
	override fun getExternalId(): String {
		return "paradoxScript.property"
	}
	
	override fun createPsi(stub: ParadoxScriptPropertyStub): ParadoxScriptProperty {
		return ParadoxScriptPropertyImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptProperty, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
		//这里使用scriptProperty.paradoxDefinitionInfo.name而非scriptProperty.name
		val definitionInfo = psi.paradoxDefinitionInfo
		val name = definitionInfo?.name ?: ""
		val type = definitionInfo?.type ?: ""
		return ParadoxScriptPropertyStubImpl(parentStub, name, type)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//仅当是definition时才会创建索引
		val element = node.psi as? ParadoxScriptProperty ?: return false
		return element.paradoxDefinitionInfo != null
	}
	
	override fun indexStub(stub: ParadoxScriptPropertyStub, sink: IndexSink) {
		//索引definition的name和type
		sink.occurrence(ParadoxDefinitionNameIndex.key, stub.name)
		sink.occurrence(ParadoxDefinitionTypeIndex.key, stub.type)
	}
	
	override fun serialize(stub: ParadoxScriptPropertyStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
		dataStream.writeName(stub.type)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
		val name = dataStream.readNameString().orEmpty()
		val type = dataStream.readNameString().orEmpty()
		return ParadoxScriptPropertyStubImpl(parentStub, name, type)
	}
}
