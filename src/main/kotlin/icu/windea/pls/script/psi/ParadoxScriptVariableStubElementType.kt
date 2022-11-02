package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptVariableStubElementType : IStubElementType<ParadoxScriptVariableStub, ParadoxScriptVariable>(
	"VARIABLE",
	ParadoxScriptLanguage
) {
	private const val externalId = "paradoxScript.variable"
	
	override fun getExternalId() = externalId
	
	override fun createPsi(stub: ParadoxScriptVariableStub): ParadoxScriptVariable {
		return ParadoxScriptVariableImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptVariable, parentStub: StubElement<*>): ParadoxScriptVariableStub {
		val name = psi.name
		val gameType = psi.fileInfo?.rootInfo?.gameType
		return ParadoxScriptVariableStubImpl(parentStub, name, gameType)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//仅当是全局的scripted_variable时才创建stub
		if(node.treeParent.elementType != ParadoxScriptElementTypes.ROOT_BLOCK) return false
		val file = node.psi.containingFile
		val path = file.fileInfo?.path?.path ?: return false
		return "common/scripted_variables".matchesPath(path, acceptSelf = false)
	}
	
	override fun indexStub(stub: ParadoxScriptVariableStub, sink: IndexSink) {
		//索引scripted_variable的name
		stub.name?.takeIfNotEmpty()?.let { name -> sink.occurrence(ParadoxScriptedVariableNameIndex.key, name) }
	}
	
	override fun serialize(stub: ParadoxScriptVariableStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
		dataStream.writeName(stub.gameType?.id)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptVariableStub {
		val name = dataStream.readNameString()
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		return ParadoxScriptVariableStubImpl(parentStub, name, gameType)
	}
}
