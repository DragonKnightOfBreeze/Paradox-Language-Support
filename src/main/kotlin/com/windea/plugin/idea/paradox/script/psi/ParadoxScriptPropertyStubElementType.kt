package com.windea.plugin.idea.paradox.script.psi

import com.intellij.psi.stubs.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.*
import com.windea.plugin.idea.paradox.script.psi.impl.*

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
		val definition = psi.paradoxDefinitionInfoNoCheck
		return ParadoxScriptPropertyStubImpl(parentStub, definition?.name ?: "@", definition?.type?.name ?: "")
	}
	
	//override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
	//	val keyNode = LightTreeUtil.firstChildOfType(tree, node, ParadoxScriptTypes.PROPERTY_KEY_ID)
	//	val key = intern(tree.charTable, keyNode)
	//	return ParadoxScriptPropertyStubImpl(parentStub, key)
	//}
	
	override fun serialize(stub: ParadoxScriptPropertyStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
		dataStream.writeName(stub.type)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
		return ParadoxScriptPropertyStubImpl(parentStub, dataStream.readNameString()!!, dataStream.readNameString()!!)
	}
	
	override fun indexStub(stub: ParadoxScriptPropertyStub, sink: IndexSink) {
		sink.occurrence(ParadoxDefinitionNameIndex.key, stub.name)
		sink.occurrence(ParadoxDefinitionTypeIndex.key, stub.type)
		if(stub.type == "scripted_loc") sink.occurrence(ParadoxScriptLocalisationNameIndex.key, stub.name)
	}
	
	//companion object {
	//	fun intern(table: CharTable, node: LighterASTNode?): String {
	//		return table.intern((node as LighterASTTokenNode).text).toString()
	//	}
	//}
}
