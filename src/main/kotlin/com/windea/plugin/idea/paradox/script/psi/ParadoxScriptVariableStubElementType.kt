package com.windea.plugin.idea.paradox.script.psi

import com.intellij.lang.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.stubs.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.script.*
import com.windea.plugin.idea.paradox.script.psi.impl.*

class ParadoxScriptVariableStubElementType : ILightStubElementType<ParadoxScriptVariableStub, ParadoxScriptVariable>(
	"PARADOX_SCRIPT_VARIABLE",
	ParadoxScriptLanguage
) {
	override fun createPsi(stub: ParadoxScriptVariableStub): ParadoxScriptVariable {
		return ParadoxScriptVariableImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptVariable, parentStub: StubElement<*>): ParadoxScriptVariableStub {
		return ParadoxScriptVariableStubImpl(parentStub, psi.name)
	}
	
	override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptVariableStub {
		val keyNode = LightTreeUtil.firstChildOfType(tree, node, ParadoxScriptTypes.VARIABLE_NAME_ID)
		val key = intern(tree.charTable, keyNode)
		return ParadoxScriptVariableStubImpl(parentStub, key)
	}
	
	override fun getExternalId(): String {
		return "paradoxScript.variable"
	}
	
	override fun serialize(stub: ParadoxScriptVariableStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.key)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptVariableStub {
		return ParadoxScriptVariableStubImpl(parentStub, dataStream.readNameString()!!)
	}
	
	override fun indexStub(stub: ParadoxScriptVariableStub, sink: IndexSink) {
		sink.occurrence(ParadoxScriptVariableKeyIndex.key,stub.key)
	}
	
	companion object{
		fun intern(table: CharTable,node: LighterASTNode?):String{
			return table.intern((node as LighterASTTokenNode).text).toString()
		}
	}
}
