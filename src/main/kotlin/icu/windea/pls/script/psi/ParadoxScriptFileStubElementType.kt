package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*
import icu.windea.pls.tool.*

object ParadoxScriptFileStubElementType : ILightStubFileElementType<PsiFileStub<*>>(ParadoxScriptLanguage) {
    private const val ID = "paradoxScript.file"
    private const val VERSION = 22 //1.0.0
    
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
            //索引definition的name和type
            if(stub.name.isNotEmpty() && stub.type.isNotEmpty() && stub.gameType != null) {
                sink.occurrence(ParadoxDefinitionNameIndex.KEY, stub.name)
                sink.occurrence(ParadoxDefinitionTypeIndex.KEY, stub.type)
            }
        }
        super.indexStub(stub, sink)
    }
    
    override fun serialize(stub: PsiFileStub<*>, dataStream: StubOutputStream) {
        if(stub is ParadoxScriptFileStub) {
            dataStream.writeName(stub.name)
            dataStream.writeName(stub.type)
            //dataStream.writeName(stub.subtypes.toCommaDelimitedString())
            dataStream.writeName(stub.gameType?.id)
        }
        super.serialize(stub, dataStream)
    }
    
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): PsiFileStub<*> {
        val name = dataStream.readNameString().orEmpty()
        val type = dataStream.readNameString().orEmpty()
        //val subtypes = dataStream.readNameString()?.toCommaDelimitedStringList().orEmpty()
        val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
        //return ParadoxScriptFileStubImpl(null, name, type, subtypes, gameType)
        return ParadoxScriptFileStubImpl(null, name, type, gameType)
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
            //需要包括scripted_variable, property, key/string (作为：complexEnum, valueSetValue)
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
