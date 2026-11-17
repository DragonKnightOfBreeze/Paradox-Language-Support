package icu.windea.pls.lang.presentation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.FileContentUtilCore
import icu.windea.pls.core.select.one
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path

/**
 * 验证 [StellarisTechnologyCardBuilder] 的三段隔离：
 * - [StellarisTechnologyCardBuilder.loadModel]（读锁阶段加载纯数据）
 * - [StellarisTechnologyCardBuilder.materializeAssets]（无锁 `Dispatchers.IO` 阶段物化资产）
 * - [StellarisTechnologyCardBuilder.build]（纯内存、零锁、零 IO 地组装组件）
 *
 * @see StellarisTechnologyCardPresentationModel
 * @see StellarisTechnologyCardAssets
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class StellarisTechnologyCardBuilderTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris
    private var addedContentRoot: VirtualFile? = null

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/diagram")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() {
        addedContentRoot?.let { root ->
            ModuleRootModificationUtil.updateModel(myFixture.module) { model ->
                model.contentEntries.filter { it.file == root }.forEach { model.removeContentEntry(it) }
            }
            addedContentRoot = null
        }
        clearIntegrationTest()
    }

    @Test
    fun loadModel_materializeAssets_build() {
        val file = setUpData()
        val technology = selectScope { file.queryBy("tech_diagram_test").asProperty().one() }!!
        val builder = StellarisTechnologyCardBuilder(technology, project)

        // 阶段一：读锁阶段加载纯数据呈现模型
        val model = ReadAction.compute<StellarisTechnologyCardPresentationModel?, RuntimeException> { builder.loadModel() }
        assertNotNull(model)
        assertEquals("Diagram Test Tech", model!!.nameText)
        assertEquals(100, model.costNumber)

        // 阶段二：在非 EDT、非读锁的后台线程物化渲染资产
        val assets = ApplicationManager.getApplication().executeOnPooledThread<StellarisTechnologyCardAssets?> {
            ProgressManager.getInstance().runProcess<StellarisTechnologyCardAssets?>({
                runBlockingCancellable { builder.materializeAssets(model) }
            }, EmptyProgressIndicator())
        }.get()
        assertNotNull(assets)

        // 阶段三：纯内存组装组件
        val panel = builder.build(model, assets!!)
        assertNotNull(panel)
        assertEquals(452, panel.preferredSize.width)
        assertEquals(97, panel.preferredSize.height)
    }

    @Test
    fun loadModel_returnsNull_whenIconCannotResolve() {
        val file = setUpData()
        val technology = selectScope { file.queryBy("tech_diagram_missing_icon").asProperty().one() }!!
        val builder = StellarisTechnologyCardBuilder(technology, project)

        val model = ReadAction.compute<StellarisTechnologyCardPresentationModel?, RuntimeException> { builder.loadModel() }

        // 关键图标无法解析时应安全降级为 null，而非抛出异常
        assertNull(model)
    }

    // region Helpers

    private fun setUpData(): ParadoxScriptFile {
        // 使用真实本地文件系统的测试数据目录，使全部文件同处同一内容根且图片 VirtualFile 支持 `toNioPath()`
        val diagramRootPath = Path.of("src", "test", "testData", "features", "diagram").toAbsolutePath().normalize()
        addAdditionalAllowedRoots(diagramRootPath)
        val rootVFile = ApplicationManager.getApplication().runWriteAction<VirtualFile> {
            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(diagramRootPath)
                ?: error("Cannot find diagram test data root: $diagramRootPath")
        }

        val rootInfo = createRootInfo(gameType)
        val relativeFiles = listOf(
            "localisation/diagram_l_english.yml",
            "gfx/interface/icons/technologies/test_icon.png",
            "gfx/interface/technologies/test_bg.png",
            "gfx/interface/technologies/test_bottom_line.png",
            "interface/diagram_sprites.gfx",
            "common/technology/00_technology.txt",
        )
        val vFiles = relativeFiles.map { rel ->
            val vFile = rootVFile.findFileByRelativePath(rel) ?: error("Missing test data file: $rel")
            vFile.injectFileInfo(rootInfo, rel)
            vFile
        }

        PsiTestUtil.addContentRoot(myFixture.module, rootVFile)
        addedContentRoot = rootVFile
        FileContentUtilCore.reparseFiles(vFiles)
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val techVFile = rootVFile.findFileByRelativePath("common/technology/00_technology.txt")!!
        return ReadAction.compute<ParadoxScriptFile, RuntimeException> {
            PsiManager.getInstance(project).findFile(techVFile) as ParadoxScriptFile
        }
    }

    // endregion
}
