package icu.windea.pls.csv.psi

import com.intellij.lang.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.csv.*
import icu.windea.pls.lang.*

class ParadoxCsvFileStubElementType : ILightStubFileElementType<PsiFileStub<*>>(ParadoxCsvLanguage) {
    override fun getExternalId() = "paradoxCsv.FILE"

    override fun getStubVersion() = VERSION

    override fun getBuilder(): LightStubBuilder? {
        return Builder()
    }

    override fun shouldBuildStubFor(file: VirtualFile): Boolean {
        try {
            //不索引内存中的文件
            if (PlsFileManager.isLightFile(file)) return false
            //仅索引有根目录的文件
            return file.fileInfo != null
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            return false
        }
    }

    class Builder : LightStubBuilder() {
        override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
            //包括：row
            return true
        }

        override fun skipChildProcessingWhenBuildingStubs(tree: LighterAST, parent: LighterASTNode, node: LighterASTNode): Boolean {
            //包括：row
            return true
        }
    }

    companion object {
        private const val VERSION = 70 //2.0.0-dev

        @JvmField
        val INSTANCE = ParadoxCsvFileStubElementType()
    }
}
