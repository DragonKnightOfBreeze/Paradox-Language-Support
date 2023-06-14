package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.impl.*

object ParadoxLocalisationPropertyStubElementType : ILightStubElementType<ParadoxLocalisationPropertyStub, ParadoxLocalisationProperty>(
    "PROPERTY",
    ParadoxLocalisationLanguage
) {
    private const val externalId = "paradoxLocalisation.property"
    
    override fun getExternalId() = externalId
    
    override fun createPsi(stub: ParadoxLocalisationPropertyStub): ParadoxLocalisationProperty {
        return ParadoxLocalisationPropertyImpl(stub, this)
    }
    
    override fun createStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
        return ParadoxLocalisationHandler.createStub(psi, parentStub) ?: createDefaultStub(parentStub)
    }
    
    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
        return ParadoxLocalisationHandler.createStub(tree, node, parentStub) ?: createDefaultStub(parentStub)
    }
    
    private fun createDefaultStub(parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
        return ParadoxLocalisationPropertyStubImpl(parentStub, "", null, ParadoxLocalisationCategory.Localisation, null, null)
    }
    
    override fun shouldCreateStub(node: ASTNode): Boolean {
        return ParadoxLocalisationHandler.shouldCreateStub(node)
    }
    
    override fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        return ParadoxLocalisationHandler.shouldCreateStub(tree, node, parentStub)
    }
    
    override fun indexStub(stub: ParadoxLocalisationPropertyStub, sink: IndexSink) {
        //根据分类索引localisation和localisation_synced的name
        if(stub.name.isNotEmpty() && stub.gameType != null) {
            when(stub.category) {
                ParadoxLocalisationCategory.Localisation -> {
                    //sink.occurrence(ParadoxLocalisationNameIndex.KEY, stub.name)
                    
                    ParadoxLocalisationConstraint.values.forEachFast { constraint -> 
                        if(constraint.predicate(stub.name)) {
                            val key = if(constraint.ignoreCase) stub.name.lowercase() else stub.name
                            sink.occurrence(ParadoxLocalisationNameIndex.ModifierIndex.KEY, key)
                        }
                    }
                }
                ParadoxLocalisationCategory.SyncedLocalisation -> {
                    sink.occurrence(ParadoxSyncedLocalisationNameIndex.KEY, stub.name)
                }
            }
        }
    }
    
    override fun serialize(stub: ParadoxLocalisationPropertyStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeName(stub.text)
        dataStream.writeBoolean(stub.category.flag)
        dataStream.writeName(stub.locale)
        dataStream.writeName(stub.gameType?.id)
    }
    
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
        val name = dataStream.readNameString().orEmpty()
        val text = dataStream.readNameString()
        val category = ParadoxLocalisationCategory.resolve(dataStream.readBoolean())
        val locale = dataStream.readNameString()
        val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
        return ParadoxLocalisationPropertyStubImpl(parentStub, name, text, category, locale, gameType)
    }
}
