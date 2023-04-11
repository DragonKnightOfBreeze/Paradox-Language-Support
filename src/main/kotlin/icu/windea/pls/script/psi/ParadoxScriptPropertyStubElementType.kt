package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptPropertyStubElementType : ILightStubElementType<ParadoxScriptPropertyStub, ParadoxScriptProperty>(
    "PROPERTY",
    ParadoxScriptLanguage
) {
    private const val externalId = "paradoxScript.property"
    
    override fun getExternalId() = externalId
    
    override fun createPsi(stub: ParadoxScriptPropertyStub): ParadoxScriptProperty {
        return SmartParadoxScriptProperty(stub, this)
    }
    
    override fun createStub(psi: ParadoxScriptProperty, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        return ParadoxDefinitionHandler.createStub(psi, parentStub) ?: createDefaultStub(parentStub)
    }
    
    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        return ParadoxDefinitionHandler.createStub(tree, node, parentStub) ?: createDefaultStub(parentStub)
    }
    
    private fun createDefaultStub(parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        return ParadoxScriptPropertyStubImpl(parentStub, "", "", "", EmptyParadoxElementPath, null)
    }
    
    override fun shouldCreateStub(node: ASTNode): Boolean {
        return ParadoxDefinitionHandler.shouldCreateStub(node)
    }
    
    override fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        return ParadoxDefinitionHandler.shouldCreateStub(tree, node, parentStub)
    }
    
    override fun indexStub(stub: ParadoxScriptPropertyStub, sink: IndexSink) {
        //索引definition的name和type
        if(stub.name.isNotEmpty() && stub.type.isNotEmpty() && stub.gameType != null) {
            sink.occurrence(ParadoxDefinitionNameIndex.KEY, stub.name)
            sink.occurrence(ParadoxDefinitionTypeIndex.KEY, stub.type)
        }
    }
    
    override fun serialize(stub: ParadoxScriptPropertyStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeName(stub.type)
        //dataStream.writeName(stub.subtypes.toCommaDelimitedString())
        dataStream.writeName(stub.rootKey)
        dataStream.writeName(stub.elementPath.path)
        dataStream.writeName(stub.gameType?.id)
    }
    
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        val name = dataStream.readNameString().orEmpty()
        val type = dataStream.readNameString().orEmpty()
        //val subtypes = dataStream.readNameString()?.toCommaDelimitedStringList().orEmpty()
        val rootKey = dataStream.readNameString().orEmpty()
        val elementPath = dataStream.readNameString().orEmpty().let { ParadoxElementPath.resolve(it) }
        val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
        //return ParadoxScriptPropertyStubImpl(parentStub, name, type, subtypes, rootKey, elementPath, gameType)
        return ParadoxScriptPropertyStubImpl(parentStub, name, type, rootKey, elementPath, gameType)
    }
}
