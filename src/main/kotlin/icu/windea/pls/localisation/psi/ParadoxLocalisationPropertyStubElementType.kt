package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.impl.*
import icu.windea.pls.model.*

class ParadoxLocalisationPropertyStubElementType : IStubElementType<ParadoxLocalisationPropertyStub, ParadoxLocalisationProperty>(
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
		val localisationInfo = psi.paradoxLocalisationInfo
		val name = psi.name //psi.name == localisationInfo.name
		val category = localisationInfo?.category ?: ParadoxLocalisationCategory.Localisation
		return ParadoxLocalisationPropertyStubImpl(parentStub, name,category)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//仅当是localisation或localisation_synced时才创建索引
		val element = node.psi as? ParadoxLocalisationProperty ?: return false
		return element.paradoxLocalisationInfo != null
	}
	
	override fun indexStub(stub: ParadoxLocalisationPropertyStub, sink: IndexSink) {
		//根据分类索引localisation和localisation_synced的name
		val category = stub.category
		when(category) {
			ParadoxLocalisationCategory.Localisation -> sink.occurrence(ParadoxLocalisationNameIndex.key,stub.name)
			ParadoxLocalisationCategory.SyncedLocalisation -> sink.occurrence(ParadoxSyncedLocalisationNameIndex.key,stub.name)
		}
		sink.occurrence(ParadoxLocalisationNameIndex.key, stub.name)
	}
	
	override fun serialize(stub: ParadoxLocalisationPropertyStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
		dataStream.writeBoolean(stub.category.flag)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
		val key = dataStream.readNameString().orEmpty()
		val category = ParadoxLocalisationCategory.resolve(dataStream.readBoolean())
		return ParadoxLocalisationPropertyStubImpl(parentStub, key, category)
	}
}
