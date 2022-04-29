package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.impl.*

class ParadoxLocalisationPropertyStubElementType : IStubElementType<ParadoxLocalisationStub, ParadoxLocalisationProperty>(
	"PARADOX_LOCALISATION_PROPERTY",
	ParadoxLocalisationLanguage
) {
	override fun getExternalId(): String {
		return "paradoxLocalisation.property"
	}
	
	override fun createPsi(stub: ParadoxLocalisationStub): ParadoxLocalisationProperty {
		return ParadoxLocalisationPropertyImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<*>): ParadoxLocalisationStub {
		val localisationInfo = psi.localisationInfo
		val name = localisationInfo?.name
		val category = localisationInfo?.category ?: ParadoxLocalisationCategory.Localisation
		return ParadoxLocalisationStubImpl(parentStub, name, category)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//仅当是localisation或localisation_synced时才创建索引
		val element = node.psi as? ParadoxLocalisationProperty ?: return false
		return element.localisationInfo != null
	}
	
	override fun indexStub(stub: ParadoxLocalisationStub, sink: IndexSink) {
		//根据分类索引localisation和localisation_synced的name
		stub.name?.let { name ->
			when(stub.category) {
				ParadoxLocalisationCategory.Localisation -> sink.occurrence(ParadoxLocalisationNameIndex.key, name)
				ParadoxLocalisationCategory.SyncedLocalisation -> sink.occurrence(ParadoxSyncedLocalisationNameIndex.key, name)
			}
		}
	}
	
	override fun serialize(stub: ParadoxLocalisationStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
		dataStream.writeBoolean(stub.category.flag)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxLocalisationStub {
		val key = dataStream.readNameString().orEmpty()
		val category = ParadoxLocalisationCategory.resolve(dataStream.readBoolean())
		return ParadoxLocalisationStubImpl(parentStub, key, category)
	}
}
