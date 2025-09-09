package icu.windea.pls.config.configGroup

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.ep.configGroup.CwtConfigGroupDataProvider
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

private val logger = logger<CwtConfigGroup>()

/**
 * 规则分组。
 * @property gameType 对应的游戏类型。
 * @property project 对应的项目。如果不需要访问PSI，可以直接传入默认项目。
 */
class CwtConfigGroup(
    val gameType: ParadoxGameType,
    val project: Project,
) : UserDataHolderBase() {
    private val mutex = Mutex()

    val changed = AtomicBoolean()
    val modificationTracker = SimpleModificationTracker()

    suspend fun init() {
        // 即使规则数据已全部加载完毕，也可能需要再次重新加载
        mutex.withLock {
            doInit()
        }
    }

    private suspend fun doInit() {
        val projectTitle = if (project.isDefault) "default project" else "project '${project.name}'"
        logger.info("Initializing config group '${gameType.id}' for $projectTitle...")
        val start = System.currentTimeMillis()
        try {
            val configGroupOnInit = CwtConfigGroup(gameType, project)
            val dataProviders = CwtConfigGroupDataProvider.EP_NAME.extensionList
            dataProviders.all { dataProvider -> dataProvider.process(configGroupOnInit) }
            configGroupOnInit.copyUserDataTo(this) // 直接一次性替换规则数据
            modificationTracker.incModificationCount() // 显式增加修改计数
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            if (e is CancellationException) throw e
            logger.error(e) // 不期望在这里出现常规异常
        } finally {
            val end = System.currentTimeMillis()
            logger.info("Initialized config group '${gameType.id}' for $projectTitle in ${end - start} ms.")
        }
    }

    override fun <T : Any?> getUserData(key: Key<T?>): T? {
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
}
