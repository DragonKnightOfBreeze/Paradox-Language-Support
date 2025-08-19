package icu.windea.pls.lang.index.stubs

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.stubs.*
import com.intellij.util.diff.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.lexer.*
import icu.windea.pls.script.parser.*
import icu.windea.pls.script.psi.*

@Suppress("UnstableApiUsage")
class ParadoxScriptStubDefinition : LightLanguageStubDefinition {
    override val stubVersion: Int = 72 // VERSION for 2.0.2
    override val builder = ParadoxScriptStubBuilder()

    override fun shouldBuildStubFor(file: VirtualFile): Boolean {
        // 不索引内存中的文件
        // 不索引在游戏或模组目录下的文件

        if (PlsVfsManager.isLightFile(file)) return false
        val fileInfo = runCatchingCancelable { file.fileInfo }.getOrNull()
        if (fileInfo == null) return false
        val path = fileInfo.path
        if (path.matches(ParadoxPathMatcher.InLocalisationPath)) return true
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
