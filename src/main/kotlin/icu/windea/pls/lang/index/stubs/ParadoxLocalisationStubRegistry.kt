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
import icu.windea.pls.core.children
import icu.windea.pls.core.firstChild
import icu.windea.pls.core.internNode
import icu.windea.pls.core.pass
import icu.windea.pls.core.writeByte
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LOCALE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LOCALE_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_KEY
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_LIST
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyImpl
import icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyListImpl
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationFileStub
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyListStub
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue

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
            dataStream.writeByte(stub.localisationType.optimizeValue())
            dataStream.writeByte(stub.gameType.optimizeValue())
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ParadoxLocalisationFileStub {
            val localisationType = dataStream.readByte().deoptimizeValue<ParadoxLocalisationType>()
            val gameType = dataStream.readByte().deoptimizeValue<ParadoxGameType>()
            return ParadoxLocalisationFileStub.Impl(null, localisationType, gameType)
        }

        override fun indexStub(stub: ParadoxLocalisationFileStub, sink: IndexSink) {
            pass() // do nothing
        }
    }

    class PropertyListFactory : LightStubElementFactory<ParadoxLocalisationPropertyListStub, ParadoxLocalisationPropertyList> {
        override fun createStub(psi: ParadoxLocalisationPropertyList, parentStub: StubElement<out PsiElement>?): ParadoxLocalisationPropertyListStub {
            val locale = selectLocale(psi)?.id
            return ParadoxLocalisationPropertyListStub.Impl(parentStub, locale)
        }

        override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationPropertyListStub {
            val locale = getLocaleFromNode(node, tree)
            return ParadoxLocalisationPropertyListStub.Impl(parentStub, locale)
        }

        private fun getLocaleFromNode(node: LighterASTNode, tree: LighterAST): String? {
            return node.firstChild(tree, LOCALE)
                ?.firstChild(tree, LOCALE_TOKEN)
                ?.internNode(tree)?.toString()
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
            return ParadoxLocalisationPropertyListStub.Impl(parentStub, locale)
        }

        override fun indexStub(stub: ParadoxLocalisationPropertyListStub, sink: IndexSink) {
            pass() // do nothing
        }
    }

    class PropertyFactory : LightStubElementFactory<ParadoxLocalisationPropertyStub, ParadoxLocalisationProperty> {
        override fun createStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<out PsiElement>?): ParadoxLocalisationPropertyStub {
            val name = psi.name.takeIf { it.isNotEmpty() } ?: return createDefaultStub(parentStub)
            return ParadoxLocalisationPropertyStub.Impl(parentStub, name)
        }

        override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
            val name = getNameFromNode(node, tree)?.takeIf { it.isNotEmpty() } ?: return createDefaultStub(parentStub)
            return ParadoxLocalisationPropertyStub.Impl(parentStub, name)
        }

        private fun getNameFromNode(node: LighterASTNode, tree: LighterAST): String? {
            // 名字不能带有参数
            return node.firstChild(tree, PROPERTY_KEY)
                ?.children(tree)
                ?.takeIf { it.size == 1 && it.first().tokenType == PROPERTY_KEY_TOKEN }
                ?.last()
                ?.internNode(tree)?.toString()
        }

        private fun createDefaultStub(parentStub: StubElement<out PsiElement>?): ParadoxLocalisationPropertyStub {
            return ParadoxLocalisationPropertyStub.Dummy(parentStub)
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
            if (name.isEmpty()) return ParadoxLocalisationPropertyStub.Dummy(parentStub)
            return ParadoxLocalisationPropertyStub.Impl(parentStub, name)
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
