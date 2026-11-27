package icu.windea.pls.images

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.images.dds.DdsDocumentationProvider
import icu.windea.pls.images.tga.TgaDocumentationProvider
import icu.windea.pls.test.ChronicleTestScope
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path

/**
 * 验证扩展图片文件在快速文档中展示的尺寸提示（tailText）。
 *
 * @see getExtendedImageDimensionTailText
 * @see DdsDocumentationProvider
 * @see TgaDocumentationProvider
 */
@RunWith(JUnit4::class)
class ImageDocumentationDimensionTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        // 注册内置的 DDS/TGA ImageIO SPI，确保测试不依赖插件启动生命周期
        ImageContext.init()
    }

    @Test
    fun extendedImageDimensionTailText_formatsAsSpaceParenthesizedWidthHeight() {
        val dds = findImage("images/mod_unknown.dds")
        val tga = findImage("images/mod_unknown.tga")

        Assert.assertEquals(" (25x25)", dds.virtualFile.getExtendedImageDimensionTailText(project))
        Assert.assertEquals(" (25x25)", tga.virtualFile.getExtendedImageDimensionTailText(project))
    }

    @Test
    fun ddsDocumentation_showsDimensionTailText() {
        val file = findImage("images/mod_unknown.dds")

        val doc = DdsDocumentationProvider().generateDoc(file, null)

        Assert.assertNotNull(doc)
        Assert.assertTrue(doc, doc!!.contains("mod_unknown.dds"))
        Assert.assertTrue(doc, doc.contains(" (25x25)"))
    }

    @Test
    fun tgaDocumentation_showsDimensionTailText() {
        val file = findImage("images/mod_unknown.tga")

        val doc = TgaDocumentationProvider().generateDoc(file, null)

        Assert.assertNotNull(doc)
        Assert.assertTrue(doc, doc!!.contains("mod_unknown.tga"))
        Assert.assertTrue(doc, doc.contains(" (25x25)"))
    }

    @Test
    fun normalImage_doesNotAppendDimensionTailText() {
        val png = findImage("images/mod_unknown.png")

        // PNG 等普通图片由 IntelliJ 官方处理，不应在这里重复追加尺寸
        Assert.assertNull(png.virtualFile.getExtendedImageDimensionTailText(project))
        // DDS/TGA 的快速文档提供器也不应处理普通图片
        Assert.assertNull(DdsDocumentationProvider().generateDoc(png, null))
        Assert.assertNull(TgaDocumentationProvider().generateDoc(png, null))
    }

    private fun findImage(relativePath: String): PsiFile {
        val path = Path.of("src/test/testData", relativePath)
        addAdditionalAllowedRoots(path.parent)
        val virtualFile = VfsUtil.findFileByIoFile(path.toFile(), true) ?: error("Cannot find file: $path")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        return PsiManager.getInstance(project).findFile(virtualFile)!!
    }
}
