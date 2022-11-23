package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

//注意：这里的node和psi会变更，因此无法从userDataMap直接获取数据！
//这之后，virtualFile会变为LightVirtualFIle，无法从virtualFile获取数据！psiFile也会变更
//检查createStub的psi和parentStub，尝试获取definition
//或者：尝试直接从shouldCreateStub的node获取definition，缓存同步到createStub
//或者：重写com.intellij.psi.stubs.DefaultStubBuilder.StubBuildingWalkingVisitor.createStub，尝试从更多信息中获取
//要求：必须能够获取paradoxPath和paradoxPropertyPath！即使psiFile在内存中也要缓存信息

object ParadoxScriptPropertyStubElementType : IStubElementType<ParadoxScriptPropertyStub, ParadoxScriptProperty>(
	"PROPERTY",
	ParadoxScriptLanguage
) {
	private const val externalId = "paradoxScript.property"
	
	override fun getExternalId() = externalId
	
	override fun createPsi(stub: ParadoxScriptPropertyStub): ParadoxScriptProperty {
		return SmartParadoxScriptProperty(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptProperty, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
		//这里使用scriptProperty.definitionInfo.name而非scriptProperty.name
		val definitionInfo = psi.definitionInfo?.takeUnless { it.shouldIndex }
		val name = definitionInfo?.name
		val type = definitionInfo?.type
		val subtypes = definitionInfo?.subtypes
		val rootKey = definitionInfo?.rootKey
		val gameType = definitionInfo?.gameType
		return ParadoxScriptPropertyStubImpl(parentStub, name, type, subtypes, rootKey, gameType)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//仅当是definition时才会创建stub
		val element = node.psi as? ParadoxDefinitionProperty ?: return false
		return element.definitionInfo?.takeUnless { it.shouldIndex } != null
	}
	
	override fun indexStub(stub: ParadoxScriptPropertyStub, sink: IndexSink) {
		//索引definition的name和type
		stub.name?.takeIfNotEmpty()?.let { name -> sink.occurrence(ParadoxDefinitionNameIndex.key, name) }
		stub.type?.takeIfNotEmpty()?.let { type -> sink.occurrence(ParadoxDefinitionTypeIndex.key, type) }
	}
	
	override fun serialize(stub: ParadoxScriptPropertyStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
		dataStream.writeName(stub.type)
		dataStream.writeName(stub.subtypes?.toCommaDelimitedString())
		dataStream.writeName(stub.rootKey)
		dataStream.writeName(stub.gameType?.id)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
		val name = dataStream.readNameString()
		val type = dataStream.readNameString()
		val subtypes = dataStream.readNameString()?.toCommaDelimitedStringList()
		val rootKey = dataStream.readNameString()
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		return ParadoxScriptPropertyStubImpl(parentStub, name, type, subtypes, rootKey, gameType)
	}
}
