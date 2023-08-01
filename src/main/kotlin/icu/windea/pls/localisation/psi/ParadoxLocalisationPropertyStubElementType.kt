package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.impl.*
import icu.windea.pls.model.constraints.*

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
        return ParadoxLocalisationPropertyStubImpl(parentStub, "", ParadoxLocalisationCategory.Localisation, null, null)
    }
    
    override fun shouldCreateStub(node: ASTNode): Boolean {
        return ParadoxLocalisationHandler.shouldCreateStub(node)
    }
    
    override fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        return ParadoxLocalisationHandler.shouldCreateStub(tree, node, parentStub)
    }
    
    override fun indexStub(stub: ParadoxLocalisationPropertyStub, sink: IndexSink) {
        if(stub.gameType == null) return
        when(stub.category) {
            ParadoxLocalisationCategory.Localisation -> {
                //sink.occurrence(ParadoxLocalisationNameIndexKey, stub.name)
                ParadoxLocalisationConstraint.values.forEachFast { constraint -> 
                    if(constraint.predicate(stub.name)) {
                        val name = if(constraint.ignoreCase) stub.name.lowercase() else stub.name
                        sink.occurrence(constraint.indexKey, name)
                    }
                }
            }
            ParadoxLocalisationCategory.SyncedLocalisation -> {
                sink.occurrence(ParadoxSyncedLocalisationNameIndexKey, stub.name)
            }
        }
    }
    
    override fun serialize(stub: ParadoxLocalisationPropertyStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeBoolean(stub.category.flag)
        dataStream.writeName(stub.locale)
        dataStream.writeName(stub.gameType?.id)
    }
    
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
        val name = dataStream.readNameString().orEmpty()
        val category = ParadoxLocalisationCategory.resolve(dataStream.readBoolean())
        val locale = dataStream.readNameString()
        val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
        return ParadoxLocalisationPropertyStubImpl(parentStub, name, category, locale, gameType)
    }
}
