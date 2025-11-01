package icu.windea.pls.lang.index.stubs

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.LightStubElementFactory
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import com.intellij.psi.stubs.StubSerializer
import icu.windea.pls.core.pass
import icu.windea.pls.core.writeByte
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyImpl
import icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyListImpl
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationFileStub
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyListStub
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyStub
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.ValueOptimizers.ForParadoxGameType
import icu.windea.pls.model.ValueOptimizers.ForParadoxLocalisationType
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.model.deoptimized
import icu.windea.pls.model.optimized

@Suppress("UnstableApiUsage")
class ParadoxLocalisationStubRegistry : StubRegistryExtension {
    override fun register(registry: StubRegistry) {
        registry.registerStubSerializer(ParadoxLocalisationFile.ELEMENT_TYPE, FileSerializer())
        registry.registerLightStubFactory(PROPERTY_LIST, PropertyListFactory())
        registry.registerStubSerializer(PROPERTY_LIST, PropertyListSerializer())
        registry.registerLightStubFactory(PROPERTY, PropertyFactory())
        registry.registerStubSerializer(PROPERTY, PropertySerializer())
    }

    class FileSerializer : StubSerializer<ParadoxLocalisationFileStub> {
        override fun getExternalId(): String {
            return "paradox.localisation.file"
        }

        override fun serialize(stub: ParadoxLocalisationFileStub, dataStream: StubOutputStream) {
            dataStream.writeByte(stub.localisationType.optimized(ForParadoxLocalisationType))
            dataStream.writeByte(stub.gameType.optimized(ForParadoxGameType))
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ParadoxLocalisationFileStub {
            val localisationType = dataStream.readByte().deoptimized(ForParadoxLocalisationType)
            val gameType = dataStream.readByte().deoptimized(ForParadoxGameType)
            return ParadoxLocalisationFileStub.create(null, localisationType, gameType)
        }

        override fun indexStub(stub: ParadoxLocalisationFileStub, sink: IndexSink) {
            pass() // do nothing
        }
    }

    class PropertyListFactory : LightStubElementFactory<ParadoxLocalisationPropertyListStub, ParadoxLocalisationPropertyList> {
        override fun createStub(psi: ParadoxLocalisationPropertyList, parentStub: StubElement<out PsiElement>?): ParadoxLocalisationPropertyListStub {
            return ParadoxLocalisationStubManager.createPropertyListStub(psi, parentStub)
        }

        override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationPropertyListStub {
            return ParadoxLocalisationStubManager.createPropertyListStub(node, tree, parentStub)
        }

        override fun createPsi(stub: ParadoxLocalisationPropertyListStub): ParadoxLocalisationPropertyList {
            return ParadoxLocalisationPropertyListImpl(stub, PROPERTY_LIST)
        }
    }

    class PropertyListSerializer : StubSerializer<ParadoxLocalisationPropertyListStub> {
        override fun getExternalId(): String {
            return "paradox.localisation.propertyList"
        }

        override fun serialize(stub: ParadoxLocalisationPropertyListStub, dataStream: StubOutputStream) {
            dataStream.writeName(stub.locale)
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<out PsiElement>?): ParadoxLocalisationPropertyListStub {
            val locale = dataStream.readNameString()
            return ParadoxLocalisationPropertyListStub.create(parentStub, locale)
        }

        override fun indexStub(stub: ParadoxLocalisationPropertyListStub, sink: IndexSink) {
            pass() // do nothing
        }
    }

    class PropertyFactory : LightStubElementFactory<ParadoxLocalisationPropertyStub, ParadoxLocalisationProperty> {
        override fun createStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<out PsiElement>?): ParadoxLocalisationPropertyStub {
            return ParadoxLocalisationStubManager.createPropertyStub(psi, parentStub)
        }

        override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
            return ParadoxLocalisationStubManager.createPropertyStub(tree, node, parentStub)
        }

        override fun createPsi(stub: ParadoxLocalisationPropertyStub): ParadoxLocalisationProperty {
            return ParadoxLocalisationPropertyImpl(stub, PROPERTY)
        }
    }

    class PropertySerializer : StubSerializer<ParadoxLocalisationPropertyStub> {
        override fun getExternalId(): String {
            return "paradox.localisation.property"
        }

        override fun serialize(stub: ParadoxLocalisationPropertyStub, dataStream: StubOutputStream) {
            dataStream.writeName(stub.name)
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<out PsiElement>?): ParadoxLocalisationPropertyStub {
            val name = dataStream.readNameString().orEmpty()
            if (name.isEmpty()) return ParadoxLocalisationPropertyStub.createDummy(parentStub)
            return ParadoxLocalisationPropertyStub.create(parentStub, name)
        }

        override fun indexStub(stub: ParadoxLocalisationPropertyStub, sink: IndexSink) {
            if (stub.name.isEmpty()) return
            when (stub.type) {
                ParadoxLocalisationType.Normal -> {
                    sink.occurrence(ParadoxIndexKeys.LocalisationName, stub.name)
                    ParadoxIndexConstraint.Localisation.entries.forEach { constraint ->
                        if (constraint.supports(stub.name)) {
                            val name = if (constraint.ignoreCase) stub.name.lowercase() else stub.name
                            sink.occurrence(constraint.indexKey, name)
                        }
                    }
                }
                ParadoxLocalisationType.Synced -> {
                    sink.occurrence(ParadoxIndexKeys.SyncedLocalisationName, stub.name)
                }
            }
        }
    }
}
