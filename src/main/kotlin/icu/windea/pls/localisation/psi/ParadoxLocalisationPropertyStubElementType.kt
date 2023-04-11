package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
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
        return ParadoxLocalisationHandler.createStub(psi, parentStub) ?: createDefaultStub(parentStub)
    }
    
    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationStub {
        return ParadoxLocalisationHandler.createStub(tree, node, parentStub) ?: createDefaultStub(parentStub)
    }
    
    private fun createDefaultStub(parentStub: StubElement<*>): ParadoxLocalisationStub {
        return ParadoxLocalisationStubImpl(parentStub, "", ParadoxLocalisationCategory.Localisation, null, null)
    }
    
    override fun shouldCreateStub(node: ASTNode): Boolean {
        return ParadoxLocalisationHandler.shouldCreateStub(node)
    }
    
    override fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        return ParadoxLocalisationHandler.shouldCreateStub(tree, node, parentStub)
    }
    
    override fun indexStub(stub: ParadoxLocalisationStub, sink: IndexSink) {
        //根据分类索引localisation和localisation_synced的name
        if(stub.name.isNotEmpty() && stub.gameType != null) {
            when(stub.category) {
                ParadoxLocalisationCategory.Localisation -> sink.occurrence(ParadoxLocalisationNameIndex.KEY, stub.name)
                ParadoxLocalisationCategory.SyncedLocalisation -> sink.occurrence(ParadoxSyncedLocalisationNameIndex.KEY, stub.name)
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
