package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*
import icu.windea.pls.localisation.psi.impl.*

class ParadoxLocalisationPropertyStubElementType : ILightStubElementType<ParadoxLocalisationPropertyStub, ParadoxLocalisationProperty>(
	"PARADOX_LOCALISATION_PROPERTY",
	ParadoxLocalisationLanguage
) {
	override fun getExternalId(): String {
		return "paradoxLocalisation.property"
	}
	
	override fun createPsi(stub: ParadoxLocalisationPropertyStub): ParadoxLocalisationProperty {
		return ParadoxLocalisationPropertyImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
		return ParadoxLocalisationPropertyStubImpl(parentStub, psi.name)
	}
	
	override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
		val keyNode = LightTreeUtil.firstChildOfType(tree, node, PROPERTY_KEY)!!
		val kenTokenNode = LightTreeUtil.firstChildOfType(tree, keyNode, PROPERTY_KEY_ID) as LighterASTTokenNode
		val key = intern(tree.charTable, kenTokenNode)
		return ParadoxLocalisationPropertyStubImpl(parentStub, key)
	}
	
	override fun indexStub(stub: ParadoxLocalisationPropertyStub, sink: IndexSink) {
		sink.occurrence(ParadoxLocalisationNameIndex.key, stub.key)
	}
	
	override fun serialize(stub: ParadoxLocalisationPropertyStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.key)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
		return ParadoxLocalisationPropertyStubImpl(parentStub, dataStream.readNameString()!!)
	}
}
