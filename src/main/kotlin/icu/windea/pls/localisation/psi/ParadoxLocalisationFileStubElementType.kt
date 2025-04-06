package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

object ParadoxLocalisationFileStubElementType : ILightStubFileElementType<PsiFileStub<*>>(ParadoxLocalisationLanguage) {
    private const val ID = "paradoxLocalisation.file"
    private const val VERSION = 58 //1.3.27

    override fun getExternalId() = ID

    override fun getStubVersion() = VERSION

    override fun getBuilder(): LightStubBuilder {
        return Builder()
    }

    override fun shouldBuildStubFor(file: VirtualFile): Boolean {
        try {
            //不索引内存中的文件
            if (ParadoxFileManager.isLightFile(file)) return false
            //仅索引有根目录的文件
            return file.fileInfo != null
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            return false
        }
    }

    class Builder : LightStubBuilder() {
        override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
            //包括：propertyList、property
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
            //包括：propertyList、property
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
