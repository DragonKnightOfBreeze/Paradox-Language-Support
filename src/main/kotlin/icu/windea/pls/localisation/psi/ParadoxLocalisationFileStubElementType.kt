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

class ParadoxLocalisationFileStubElementType : ILightStubFileElementType<PsiFileStub<*>>(ParadoxLocalisationLanguage) {
    override fun getExternalId() = "paradoxLocalisation.FILE"

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

    companion object {
        private const val VERSION = 60 //1.4.0

        @JvmField
        val INSTANCE = ParadoxLocalisationFileStubElementType()
    }
}
