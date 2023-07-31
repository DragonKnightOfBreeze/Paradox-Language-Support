package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

object ParadoxLocalisationFileStubElementType : ILightStubFileElementType<PsiFileStub<*>>(ParadoxLocalisationLanguage) {
    private const val ID = "paradoxLocalisation.file"
    private const val VERSION = 32 //1.1.5
    
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
            return true
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            return false
        }
    }
    
    class Builder : LightStubBuilder() {
        override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
            //仅包括propertyList和property
            val type = node.elementType
            return when {
                type == LOCALE -> true
                type == PROPERTY_LIST -> false
                type == PROPERTY -> false
                parent.elementType == PROPERTY -> true
                else -> true
            }
        }
        
        override fun skipChildProcessingWhenBuildingStubs(tree: LighterAST, parent: LighterASTNode, node: LighterASTNode): Boolean {
            //仅包括propertyList和property
            val type = node.tokenType
            return when {
                type == LOCALE -> true
                type == PROPERTY_LIST -> false
                type == PROPERTY -> false
                parent.tokenType == PROPERTY -> true
                else -> true
            }
        }
    }
}
