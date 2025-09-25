package icu.windea.pls.lang.index.stubs

import com.intellij.lang.ASTNode
import com.intellij.lang.LighterASTNode
import com.intellij.lang.PsiBuilderFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.stubs.LightLanguageStubDefinition
import com.intellij.util.diff.FlyweightCapableTreeStructure
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.PlsVfsManager
import icu.windea.pls.model.paths.ParadoxPathMatcher
import icu.windea.pls.model.paths.matches
import icu.windea.pls.script.lexer.ParadoxScriptLexerFactory
import icu.windea.pls.script.parser.ParadoxScriptParser
import icu.windea.pls.script.psi.ParadoxScriptFile

@Suppress("UnstableApiUsage")
class ParadoxScriptStubDefinition : LightLanguageStubDefinition {
    override val stubVersion: Int = 75 // VERSION for 2.0.5
    override val builder = ParadoxScriptStubBuilder()

    override fun shouldBuildStubFor(file: VirtualFile): Boolean {
        // 不索引内存中的文件
        // 不索引不在游戏或模组目录下的文件
        // 不索引在本地化目录下的文件

        if (PlsVfsManager.isLightFile(file)) return false
        val fileInfo = runCatchingCancelable { file.fileInfo }.getOrNull()
        if (fileInfo == null) return false
        if (fileInfo.path.matches(ParadoxPathMatcher.InLocalisationPath)) return false
        return true
    }

    override fun parseContentsLight(chameleon: ASTNode): FlyweightCapableTreeStructure<LighterASTNode> {
        val project = chameleon.psi.project
        val lexer = ParadoxScriptLexerFactory.createLexer(project)
        val builder = PsiBuilderFactory.getInstance().createBuilder(project, lexer, chameleon)
        val parser = ParadoxScriptParser()
        parser.parseLight(ParadoxScriptFile.ELEMENT_TYPE, builder)
        return builder.lightTree
    }
}
