package icu.windea.pls.config.configGroup

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SimpleModificationTracker
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupPostProcessor
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupProcessor
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

/**
 * 规则分组。保存了处理后的所有规则数据。
 *
 * 规则分组会在获取时就保证已经被创建，而其中的规则数据的初始化是在打开 IDE 或项目时异步进行的。
 *
 * @property project 对应的项目。如果是默认项目，则不能用于访问 PSI。
 * @property gameType 对应的游戏类型。如果是 [ParadoxGameType.Core]，则为共享的规则分组。
 *
 * @see CwtConfigGroupDataHolder
 * @see CwtConfigGroupService
 */
class CwtConfigGroup(
    val project: Project,
    val gameType: ParadoxGameType,
) : CwtConfigGroupDataHolderBase() {
    private val mutex = Mutex()

    @Volatile var initialized: Boolean = false
    @Volatile var changed: Boolean = false
    val initializer = CwtConfigGroupInitializer(project, gameType)
    val modificationTracker = SimpleModificationTracker()

    @Optimized
    suspend fun init() {
        // 即使规则数据已全部加载完毕，也可能需要再次重新加载
        mutex.withLock { doInit() }
    }

    private suspend fun doInit() {
        try {
            val start = System.currentTimeMillis()
            initializer.clear() // 清空以避免数据残留
            doApplyProcessors() // 应用 processors
            initializer.copyUserDataTo(this) // 直接一次性替换规则数据
            initializer.clear() // 清空以避免内存泄露
            doApplyPostProcessors() // 应用 postProcessors
            modificationTracker.incModificationCount() // 显式增加修改计数
            initialized = true // 标记规则数据已全部加载完毕
            val end = System.currentTimeMillis()
            val targetName = if (project.isDefault) "application" else "project '${project.name}'"
            logger.info("Initialized config group '${gameType.id}' for $targetName in ${end - start} ms.")
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            if (e is CancellationException) throw e
            logger.error(e) // 不期望在这里出现常规异常
        }
    }

    private suspend fun doApplyProcessors() {
        val dataProviders = CwtConfigGroupProcessor.EP_NAME.extensionList
        dataProviders.forEachFast { it.process(this) }
    }

    private suspend fun doApplyPostProcessors() {
        val postProcessors = CwtConfigGroupPostProcessor.EP_NAME.extensionList
        postProcessors.forEachFast { it.postProcess(this) }
    }

    override fun <T> getUserData(key: Key<T?>): T? {
        // 这里不保证规则数据已全部加载完毕（手动重新解析已打开的文件，或者让 IDE 自动感知）
        return super.getUserData(key)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is CwtConfigGroup && gameType == other.gameType && project == other.project)
    }

    override fun hashCode(): Int {
        return Objects.hash(gameType, project)
    }

    override fun toString(): String {
        return "CwtConfigGroup(gameType=${gameType.id}, project=$project)"
    }

    object Keys : KeyRegistry()

    companion object {
        private val logger = logger<CwtConfigGroup>()
    }
}
