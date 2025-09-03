package icu.windea.pls.config.configGroup

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.withLock
import icu.windea.pls.ep.configGroup.CwtConfigGroupDataProvider
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.id
import kotlinx.coroutines.sync.Mutex
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

private val logger = logger<CwtConfigGroup>()

/**
 * 规则分组。
 * @property gameType 对应的游戏类型。如果为null，则会得到共享的规则分组。
 * @property project 对应的项目。如果不需要访问PSI，可以直接传入默认项目。
 */
class CwtConfigGroup(
    val gameType: ParadoxGameType?,
    val project: Project,
) : UserDataHolderBase() {
    private val initialized = AtomicBoolean()
    private val mutex = Mutex()

    val changed = AtomicBoolean()
    val modificationTracker = SimpleModificationTracker()

    suspend fun init() {
        initialized.withLock(mutex) {
            // 按需加载数据（但是项目启动时会自动在后台预加载）
            doInit()
        }
    }

    private suspend fun doInit() {
        val projectTitle = if (project.isDefault) "default project" else "project '${project.name}'"
        logger.info("Initializing config group '${gameType.id}' for $projectTitle...")
        val start = System.currentTimeMillis()
        val configGroupOnInit = CwtConfigGroup(gameType, project)
        val dataProviders = CwtConfigGroupDataProvider.EP_NAME.extensionList
        dataProviders.all { dataProvider -> dataProvider.process(configGroupOnInit) }
        configGroupOnInit.copyUserDataTo(this) // 直接一次性替换规则数据
        modificationTracker.incModificationCount() // 显式增加修改计数
        val end = System.currentTimeMillis()
        logger.info("Initialized config group '${gameType.id}' for $projectTitle in ${end - start} ms.")
    }

    fun clear() {
        synchronized(this) {
            val projectName = if (project.isDefault) "default project" else project.name
            logger.info("Clear config group '${gameType.id}' for project '$projectName'.")
            clearUserData()
            initialized.set(false)
        }
    }

    override fun <T : Any?> getUserData(key: Key<T?>): T? {
        // 这里不要保证规则数据已全部加载完毕（手动重新解析已打开的文件，或者让 IDE 自动感知）
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
