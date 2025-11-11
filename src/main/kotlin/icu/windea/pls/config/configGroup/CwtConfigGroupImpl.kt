package icu.windea.pls.config.configGroup

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SimpleModificationTracker
import icu.windea.pls.config.data.CwtDataProviderBase
import icu.windea.pls.ep.configGroup.CwtConfigGroupDataProvider
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

private val logger = logger<CwtConfigGroupImpl>()

class CwtConfigGroupImpl(
    override val project: Project,
    override val gameType: ParadoxGameType,
) : CwtDataProviderBase(), CwtConfigGroup {
    private val mutex = Mutex()

    override val initialized = AtomicBoolean()
    override val changed = AtomicBoolean()
    override val modificationTracker = SimpleModificationTracker()

    override suspend fun init() {
        // 即使规则数据已全部加载完毕，也可能需要再次重新加载
        mutex.withLock {
            initInLock()
        }
    }

    private suspend fun initInLock() {
        try {
            val start = System.currentTimeMillis()
            val initializer = CwtConfigGroupInitializer(project, gameType)
            val dataProviders = CwtConfigGroupDataProvider.EP_NAME.extensionList
            dataProviders.all { dataProvider -> dataProvider.process(initializer, this) }
            initializer.copyUserDataTo(this) // 直接一次性替换规则数据
            initializer.clear() // 清空以避免内存泄露
            modificationTracker.incModificationCount() // 显式增加修改计数
            initialized.set(true) // 标记规则数据已全部加载完毕
            val end = System.currentTimeMillis()
            val targetName = if (project.isDefault) "application" else "project '${project.name}'"
            logger.info("Initialized config group '${gameType.id}' for $targetName in ${end - start} ms.")
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            if (e is CancellationException) throw e
            logger.error(e) // 不期望在这里出现常规异常
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
        return "CwtConfigGroupImpl(gameType=${gameType.id}, project=$project)"
    }
}
