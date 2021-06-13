package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

class ParadoxScriptVariableStubElementType : IStubElementType<ParadoxScriptVariableStub, ParadoxScriptVariable>(
	"PARADOX_SCRIPT_VARIABLE",
	ParadoxScriptLanguage
) {
	override fun getExternalId(): String {
		return "paradoxScript.variable"
	}
	
	override fun createPsi(stub: ParadoxScriptVariableStub): ParadoxScriptVariable {
		return ParadoxScriptVariableImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptVariable, parentStub: StubElement<*>): ParadoxScriptVariableStub {
		return ParadoxScriptVariableStubImpl(parentStub, psi.name)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//仅当是scripted_variable才创建索引
		if(node.treeParent.elementType != ParadoxScriptTypes.ROOT_BLOCK) return false
		val file = node.psi.containingFile
		val parentPath = file.paradoxFileInfo?.path?.parent ?: return false
		return "common/scripted_variables".matchesPath(parentPath)
	}
	
	override fun indexStub(stub: ParadoxScriptVariableStub, sink: IndexSink) {
		//索引scripted_variable的name
		sink.occurrence(ParadoxScriptVariableNameIndex.key, stub.name)
	}
	
	override fun serialize(stub: ParadoxScriptVariableStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptVariableStub {
		val readNameString = dataStream.readNameString().toStringOrEmpty()
		return ParadoxScriptVariableStubImpl(parentStub, readNameString)
	}
}
