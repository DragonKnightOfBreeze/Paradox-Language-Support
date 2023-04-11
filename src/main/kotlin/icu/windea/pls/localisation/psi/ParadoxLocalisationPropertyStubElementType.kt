package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.impl.*

object ParadoxLocalisationPropertyStubElementType : ILightStubElementType<ParadoxLocalisationStub, ParadoxLocalisationProperty>(
	"PROPERTY",
	ParadoxLocalisationLanguage
) {
	private const val externalId = "paradoxLocalisation.property"
	
	override fun getExternalId() = externalId
	
	override fun createPsi(stub: ParadoxLocalisationStub): ParadoxLocalisationProperty {
		return ParadoxLocalisationPropertyImpl(stub, this)
	}
	
	override fun createStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<*>): ParadoxLocalisationStub {
		return ParadoxLocalisationHandler.createStub(psi, parentStub)
	}
	
	override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationStub {
		return ParadoxLocalisationHandler.createStub(tree, node, parentStub)
	}
	
	override fun shouldCreateStub(node: ASTNode): Boolean {
		//仅当是localisation或localisation_synced时才创建索引
		val file = selectFile(node.psi) ?: return false
		return ParadoxLocalisationCategory.resolve(file) != null
	}
	
	override fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
		//仅当是localisation或localisation_synced时才创建索引
		val file = selectFile(parentStub.psi) ?: return false
		return ParadoxLocalisationCategory.resolve(file) != null
	}
	
	override fun indexStub(stub: ParadoxLocalisationStub, sink: IndexSink) {
		//根据分类索引localisation和localisation_synced的name
		stub.name.takeIfNotEmpty()?.let { name ->
			when(stub.category) {
				ParadoxLocalisationCategory.Localisation -> sink.occurrence(ParadoxLocalisationNameIndex.KEY, name)
				ParadoxLocalisationCategory.SyncedLocalisation -> sink.occurrence(ParadoxSyncedLocalisationNameIndex.KEY, name)
			}
		}
	}
	
	override fun serialize(stub: ParadoxLocalisationStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
		dataStream.writeBoolean(stub.category.flag)
		dataStream.writeName(stub.locale)
		dataStream.writeName(stub.gameType?.id)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxLocalisationStub {
		val key = dataStream.readNameString().orEmpty()
		val category = ParadoxLocalisationCategory.resolve(dataStream.readBoolean())
		val locale = dataStream.readNameString()
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		return ParadoxLocalisationStubImpl(parentStub, key, category, locale, gameType)
	}
}
