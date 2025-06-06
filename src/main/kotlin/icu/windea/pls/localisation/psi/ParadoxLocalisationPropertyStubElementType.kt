package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.impl.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*

class ParadoxLocalisationPropertyStubElementType : ILightStubElementType<ParadoxLocalisationPropertyStub, ParadoxLocalisationProperty>("PROPERTY", ParadoxLocalisationLanguage) {
    override fun getExternalId() = "paradoxLocalisation.PROPERTY"

    override fun createPsi(stub: ParadoxLocalisationPropertyStub): ParadoxLocalisationProperty {
        return ParadoxLocalisationPropertyImpl(stub, this)
    }

    override fun createStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
        return ParadoxLocalisationManager.createStub(psi, parentStub) ?: createDefaultStub(parentStub)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
        return ParadoxLocalisationManager.createStub(tree, node, parentStub) ?: createDefaultStub(parentStub)
    }

    private fun createDefaultStub(parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
        return ParadoxLocalisationPropertyStub.Dummy(parentStub)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        return true
    }

    override fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        return true
    }

    override fun indexStub(stub: ParadoxLocalisationPropertyStub, sink: IndexSink) {
        when (stub.category) {
            ParadoxLocalisationCategory.Localisation -> {
                sink.occurrence(ParadoxIndexManager.LocalisationNameKey, stub.name)
                ParadoxIndexConstraint.Localisation.entries.forEach { constraint ->
                    if (constraint.predicate(stub.name)) {
                        val name = if (constraint.ignoreCase) stub.name.lowercase() else stub.name
                        sink.occurrence(constraint.indexKey, name)
                    }
                }
            }
            ParadoxLocalisationCategory.SyncedLocalisation -> {
                sink.occurrence(ParadoxIndexManager.SyncedLocalisationNameKey, stub.name)
            }
        }
    }

    override fun serialize(stub: ParadoxLocalisationPropertyStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeByte(stub.category.optimizeValue())
        dataStream.writeName(stub.locale)
        dataStream.writeByte(stub.gameType.optimizeValue())
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
        val name = dataStream.readNameString().orEmpty()
        val category = dataStream.readByte().deoptimizeValue<ParadoxLocalisationCategory>()
        val locale = dataStream.readNameString()
        val gameType = dataStream.readByte().deoptimizeValue<ParadoxGameType>()
        return ParadoxLocalisationPropertyStub.Impl(parentStub, name, category, locale, gameType)
    }

    override fun isAlwaysLeaf(root: StubBase<*>): Boolean {
        return true
    }

    companion object {
        @JvmField
        val INSTANCE = ParadoxLocalisationPropertyStubElementType()
    }
}
