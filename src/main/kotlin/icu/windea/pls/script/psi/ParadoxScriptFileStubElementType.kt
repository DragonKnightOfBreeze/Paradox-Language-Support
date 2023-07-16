package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptFileStubElementType : ILightStubFileElementType<PsiFileStub<*>>(ParadoxScriptLanguage) {
    private const val ID = "paradoxScript.file"
    private const val VERSION = 31 //1.1.1
    
    override fun getExternalId() = ID
    
    override fun getStubVersion() = VERSION
    
    override fun getBuilder(): LightStubBuilder {
        return Builder()
    }
    
    override fun shouldBuildStubFor(file: VirtualFile): Boolean {
        try {
            //不索引内存中的文件
            if(ParadoxFileManager.isLightFile(file)) return false
            //仅索引有根目录的文件
            val fileInfo = file.fileInfo ?: return false
            val path = fileInfo.pathToEntry
            //要求不直接在根目录
            if(path.isEmpty()) return false
            //不索引内联脚本文件
            if("common/inline_scripts".matchesPath(path.path)) return false
            return true
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            return false
        }
    }
    
    override fun indexStub(stub: PsiFileStub<*>, sink: IndexSink) {
        if(stub is ParadoxScriptFileStub) {
            //Note that definition name can be empty (aka anonymous)
            if(stub.gameType == null) return
            sink.occurrence(ParadoxDefinitionNameIndex.KEY, stub.name)
            sink.occurrence(ParadoxDefinitionTypeIndex.KEY, stub.type)
        }
        super.indexStub(stub, sink)
    }
    
    override fun serialize(stub: PsiFileStub<*>, dataStream: StubOutputStream) {
        if(stub is ParadoxScriptFileStub) {
            dataStream.writeName(stub.name)
            dataStream.writeName(stub.type)
            val subtypes = stub.subtypes
            if(subtypes == null) {
                dataStream.writeInt(-1)
            } else {
                dataStream.writeInt(subtypes.size)
                subtypes.forEach { subtype -> dataStream.writeName(subtype) }
            }
            dataStream.writeName(stub.gameType?.id)
        }
        super.serialize(stub, dataStream)
    }
    
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): PsiFileStub<*> {
        val name = dataStream.readNameString().orEmpty()
        val type = dataStream.readNameString().orEmpty()
        val subtypesSize = dataStream.readInt()
        val subtypes = if(subtypesSize == -1) null else MutableList(subtypesSize) { dataStream.readNameString().orEmpty() }
        val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
        return ParadoxScriptFileStubImpl(null, name, type, subtypes, gameType)
    }
    
    class Builder : LightStubBuilder() {
        override fun createStubForFile(file: PsiFile, tree: LighterAST): StubElement<*> {
            return ParadoxDefinitionHandler.createStubForFile(file, tree) ?: super.createStubForFile(file, tree)
        }
        
        override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
            //需要包括scripted_variable, property
            val type = node.elementType
            val parentType = parent.elementType
            return when {
                type == SCRIPTED_VARIABLE -> parentType != ROOT_BLOCK
                type == PROPERTY -> false
                type == BLOCK -> false
                parentType == PROPERTY -> type != BLOCK
                parentType == BLOCK -> type != PROPERTY
                else -> false
            }
        }
        
        override fun skipChildProcessingWhenBuildingStubs(tree: LighterAST, parent: LighterASTNode, node: LighterASTNode): Boolean {
            //需要包括scripted_variable, property
            val type = node.tokenType
            val parentType = parent.tokenType
            return when {
                type == SCRIPTED_VARIABLE -> parentType != ROOT_BLOCK
                type == PROPERTY -> false
                type == BLOCK -> false
                parentType == PROPERTY -> type != BLOCK
                parentType == BLOCK -> type != PROPERTY
                else -> false
            }
        }
    }
}
