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
		val definition = psi.paradoxDefinitionInfo
		val name = definition?.name ?: ""
		val typeKey = definition?.typeKey ?: ""
		val type = definition?.type ?: ""
		val subtypes = definition?.subtypes ?: emptyList()
		return ParadoxScriptPropertyStubImpl(parentStub, name, typeKey, type, subtypes)
	}
	
	//override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
	//	val keyNode = LightTreeUtil.firstChildOfType(tree, node, ParadoxScriptTypes.PROPERTY_KEY_ID)
	//	val key = intern(tree.charTable, keyNode)
	//	return ParadoxScriptPropertyStubImpl(parentStub, key)
	//}
	
	override fun serialize(stub: ParadoxScriptPropertyStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
		dataStream.writeName(stub.typeKey)
		dataStream.writeName(stub.type)
		dataStream.writeName(stub.subtypes.joinToString(","))
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
		val name = dataStream.readNameString()!!
		val typeKey = dataStream.readNameString()!!
		val type = dataStream.readNameString()!!
		val subtypes = dataStream.readNameString()!!.let { if(it.isEmpty()) emptyList() else it.split(',') }
		return ParadoxScriptPropertyStubImpl(parentStub, name, typeKey, type, subtypes)
	}
	
	override fun indexStub(stub: ParadoxScriptPropertyStub, sink: IndexSink) {
		//索引definition的名称和类型
		sink.occurrence(ParadoxDefinitionNameIndex.key, stub.name)
		sink.occurrence(ParadoxDefinitionTypeIndex.key, stub.type)
		when {
			//索引scripted_loc的名字
			stub.type == "scripted_loc" -> {
				sink.occurrence(ParadoxScriptLocalisationNameIndex.key, stub.name)
			}
			//索引icon的名字
			(stub.type == "sprite" || stub.type == "spriteType") && stub.typeKey == "spriteType" -> {
				val iconName = when {
					stub.name.startsWith("GFX_text_") -> stub.name.substring(9)
					stub.name.startsWith("GFX_") -> stub.name.substring(4)
					else -> return
				}
				sink.occurrence(ParadoxIconNameIndex.key, iconName)
			}
		}
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//仅当是definition时才会创建索引
		val element = node.psi as? ParadoxScriptProperty ?: return false
		return element.paradoxDefinitionInfo != null
	}
	
	//companion object {
	//	fun intern(table: CharTable, node: LighterASTNode?): String {
	//		return table.intern((node as LighterASTTokenNode).text).toString()
	//	}
	//}
}
