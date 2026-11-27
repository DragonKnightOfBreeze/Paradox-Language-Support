@file:Suppress("UnstableApiUsage", "JUnitMixedFramework")

package icu.windea.pls.extensions.markdown

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleExtensionSettings
import icu.windea.pls.base.settings.ChronicleSettings
import icu.windea.pls.extensions.markdown.psi.MarkdownInlineCodeReferenceProvider
import icu.windea.pls.extensions.markdown.psi.MarkdownInlineCodeSymbol
import icu.windea.pls.extensions.markdown.psi.MarkdownInlineCodeSymbolReference
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFence
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeSpan
import org.junit.After
import org.junit.Before

/**
 * Markdown 扩展测试的公共基类。
 *
 * 提供 `chronicle` 测试数据的根目录与规则目录，并统一管理扩展设置的状态恢复。
 */
abstract class MarkdownTestBase : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    protected open val gameType: ParadoxGameType get() = ParadoxGameType.Stellaris
    protected open val rootDirectory: String get() = "chronicle"
    protected open val configDirectory: String get() = "chronicle/.config"

    private var oldResolveLinks = true
    private var oldResolveInlineCodes = true
    private var oldInjectCodeBlocks = true
    private var oldPreferredLocale: String? = null

    @Before
    fun setUpMarkdown() {
        val markdownState = ChronicleExtensionSettings.getInstance().state.markdown
        oldResolveLinks = markdownState.resolveLinks
        oldResolveInlineCodes = markdownState.resolveInlineCodes
        oldInjectCodeBlocks = markdownState.injectCodeBlocks
        oldPreferredLocale = ChronicleSettings.getInstance().state.preferredLocale

        markIntegrationTest()
        addAdditionalAllowedRoots(testDataPath)
        markRootDirectory(rootDirectory)
        markConfigDirectory(configDirectory)
        initConfigGroups(project, gameType)
    }

    @After
    fun tearDownMarkdown() {
        val markdownState = ChronicleExtensionSettings.getInstance().state.markdown
        markdownState.resolveLinks = oldResolveLinks
        markdownState.resolveInlineCodes = oldResolveInlineCodes
        markdownState.injectCodeBlocks = oldInjectCodeBlocks
        ChronicleSettings.getInstance().state.preferredLocale = oldPreferredLocale

        clearIntegrationTest()
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        FileDocumentManager.getInstance().saveAllDocuments()
    }

    protected fun copyFixture(relativePath: String): VirtualFile {
        val vFile = myFixture.copyFileToProject("$rootDirectory/$relativePath", relativePath)
        vFile.injectFileInfo(gameType, relativePath)
        return vFile
    }

    @Suppress("SameParameterValue")
    protected fun addScriptedVariableFile(path: String, text: String): VirtualFile {
        val vFile = myFixture.addFileToProject(path, text).virtualFile
        vFile.injectFileInfo(gameType, path)
        return vFile
    }

    protected fun configureMarkdown(text: String, path: String = "common/tests/test.md"): PsiFile {
        val vFile = myFixture.addFileToProject(path, text).virtualFile
        vFile.injectFileInfo(gameType, path, group = ParadoxFileGroup.Other)
        myFixture.configureFromExistingVirtualFile(vFile)
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        return myFixture.file
    }

    protected fun configureMarkdownWithoutFileInfo(text: String, name: String = "test.md"): PsiFile {
        myFixture.configureByText(name, text)
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        return myFixture.file
    }

    protected fun findCodeFence(): MarkdownCodeFence {
        return PsiTreeUtil.findChildOfType(myFixture.file, MarkdownCodeFence::class.java)
            ?: error("Expected a Markdown code fence")
    }

    protected fun findCodeSpan(): MarkdownCodeSpan {
        return PsiTreeUtil.findChildOfType(myFixture.file, MarkdownCodeSpan::class.java)
            ?: error("Expected a Markdown code span")
    }

    protected fun findInjectedFile(codeFence: MarkdownCodeFence = findCodeFence()): PsiFile {
        val injectedFile = InjectedLanguageManager.getInstance(project)
            .getInjectedPsiFiles(codeFence)
            ?.mapNotNull { it.first as? PsiFile }
            ?.singleOrNull()
        return injectedFile ?: error("Expected an injected PSI file")
    }

    protected fun findInlineCodeReference(): MarkdownInlineCodeSymbolReference {
        val codeSpan = findCodeSpan()
        val provider = MarkdownInlineCodeReferenceProvider()
        val reference = provider.getImplicitReference(codeSpan, codeSpan.textLength / 2)
        return reference as? MarkdownInlineCodeSymbolReference
            ?: error(
                "Expected an implicit inline code symbol reference: text=${codeSpan.text}, " +
                "class=${codeSpan.javaClass.name}, node=${codeSpan.node.elementType}, " +
                "nodeChildren=${codeSpan.node.getChildren(null).joinToString { "${it.elementType}:${it.text}" }}, " +
                "children=${codeSpan.children.joinToString { "${it.elementType}:${it.text}" }}, " +
                "identifier=${MarkdownExtensionManager.getIdentifierFromInlineCode(codeSpan)}, reference=$reference"
            )
    }

    protected fun findInlineCodeSymbol(): MarkdownInlineCodeSymbol {
        return findInlineCodeReference().resolveReference().single() as? MarkdownInlineCodeSymbol
            ?: error("Expected an inline code symbol")
    }

    protected fun textOf(vFile: VirtualFile): String {
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        return PsiManager.getInstance(project).findFile(vFile)?.text ?: error("File not found: ${vFile.path}")
    }
}
