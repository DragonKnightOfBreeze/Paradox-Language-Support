package com.windea.plugin.idea.paradox.localisation.psi

import com.intellij.lang.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.stubs.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.localisation.*
import com.windea.plugin.idea.paradox.localisation.psi.impl.*

class ParadoxLocalisationPropertyStubElementType : ILightStubElementType<ParadoxLocalisationPropertyStub, ParadoxLocalisationProperty>(
	"PARADOX_LOCALISATION_PROPERTY",
	ParadoxLocalisationLanguage
) {
	override fun createPsi(stub: ParadoxLocalisationPropertyStub): ParadoxLocalisationProperty {
		return ParadoxLocalisationPropertyImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
		return ParadoxLocalisationPropertyStubImpl(parentStub, psi.name)
	}
	
	override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
		val keyNode = LightTreeUtil.firstChildOfType(tree, node, ParadoxLocalisationTypes.PROPERTY_KEY_ID)
		val key = intern(tree.charTable, keyNode)
		return ParadoxLocalisationPropertyStubImpl(parentStub, key)
	}
	
	override fun getExternalId(): String {
		return "paradoxLocalisation.property"
	}
	
	override fun serialize(stub: ParadoxLocalisationPropertyStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.key)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
		return ParadoxLocalisationPropertyStubImpl(parentStub, dataStream.readNameString()!!)
	}
	
	override fun indexStub(stub: ParadoxLocalisationPropertyStub, sink: IndexSink) {
		sink.occurrence(ParadoxLocalisationPropertyKeyIndex.key,stub.key)
	}
	
	companion object{
		fun intern(table: CharTable,node: LighterASTNode?):String{
		return table.intern((node as LighterASTTokenNode).text).toString()
		}
	}
}
