package icu.windea.pls.extensions.diagram

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.extensions.diagram.provider.StellarisEventTreeDiagramProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.atomic.AtomicReference

/**
 * 验证 [ParadoxDiagramDataModel.refreshDataModel] 不再在外部持有读锁。
 *
 * 这是消除 UI 冻结/死锁的关键：外层的 `runSmartReadAction` 包裹已被移除，
 * 因而各具体数据模型可以在读锁之外执行无锁的后台任务（如物化图片）。
 *
 * @see ParadoxDiagramDataModel
 */
@RunWith(JUnit4::class)
class ParadoxDiagramDataModelTest : BasePlatformTestCase() {
    @Test
    fun refreshDataModel_shouldNotAcquireReadActionExternally() {
        val provider = StellarisEventTreeDiagramProvider()
        val readAccessAllowed = AtomicReference<Boolean?>(null)
        val model = object : ParadoxDiagramDataModel(project, null, provider) {
            override fun updateDataModel() {
                readAccessAllowed.set(ApplicationManager.getApplication().isReadAccessAllowed)
            }
        }

        // 在池化线程中执行，确保没有环境读锁；刷新本身不应再获取读锁。
        ApplicationManager.getApplication().executeOnPooledThread { model.refreshDataModel() }.get()

        assertEquals(false, readAccessAllowed.get())
    }
}
