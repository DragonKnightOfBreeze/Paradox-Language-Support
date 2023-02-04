package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptScriptedVariableStubElementType : IStubElementType<ParadoxScriptScriptedVariableStub, ParadoxScriptScriptedVariable>(
	"SCRIPTED_VARIABLE",
	ParadoxScriptLanguage
) {
	private const val externalId = "paradoxScript.variable"
	
	override fun getExternalId() = externalId
	
	override fun createPsi(stub: ParadoxScriptScriptedVariableStub): ParadoxScriptScriptedVariable {
		return ParadoxScriptScriptedVariableImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptScriptedVariable, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub {
		val name = psi.name
		val gameType = psi.fileInfo?.rootInfo?.gameType
		return ParadoxScriptScriptedVariableStubImpl(parentStub, name, gameType)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//仅当是全局的scripted_variable时才创建stub
		if(node.treeParent.elementType != ParadoxScriptElementTypes.ROOT_BLOCK) return false
		val file = node.psi.containingFile
		val path = file.fileInfo?.path?.path ?: return false
		return "common/scripted_variables".matchesPath(path, acceptSelf = false)
	}
	
	override fun indexStub(stub: ParadoxScriptScriptedVariableStub, sink: IndexSink) {
		//索引scripted_variable的name
		stub.name?.takeIfNotEmpty()?.let { name -> sink.occurrence(ParadoxScriptedVariableNameIndex.key, name) }
	}
	
	override fun serialize(stub: ParadoxScriptScriptedVariableStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
		dataStream.writeName(stub.gameType?.id)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub {
		val name = dataStream.readNameString()
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		return ParadoxScriptScriptedVariableStubImpl(parentStub, name, gameType)
	}
}
