package icu.windea.pls.base.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import icu.windea.pls.model.constants.ChronicleConstants
import icu.windea.pls.model.policies.ImageCacheCleanupPolicy
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import icu.windea.pls.model.policies.ImageRenderPolicy

/**
 * 插件的图片处理设置。
 */
@Service
@State(name = "PlsImageSettings", storages = [Storage(ChronicleConstants.pluginSettingsFileName)])
class ChronicleImageSettings : SimplePersistentStateComponent<ChronicleImageSettings.State>(State()) {
    companion object {
        const val DEFAULT_CACHE_TTL_DAYS = 30
        const val DEFAULT_CACHE_MAX_SIZE_MB = 1024
        const val DEFAULT_CACHE_MAX_FILES = 10_000

        @JvmStatic
        fun getInstance(): ChronicleImageSettings = service()
    }

    /**
     * @property cacheCleanupPolicy 图片磁盘缓存的清理策略。
     * @property cacheTtlDays 图片磁盘缓存的 TTL（天）。仅当清理策略包含 TTL 时生效。
     * @property cacheMaxSizeMb 图片磁盘缓存的最大大小（MB）。仅当清理策略包含大小限制时生效。
     * @property cacheMaxFiles 图片磁盘缓存的最大文件数量。仅当清理策略包含大小限制时生效。
     * @property renderPolicy 没有渲染会话且缓存未命中时默认使用的渲染策略。
     * @property renderFailurePolicy 图片渲染失败时的默认处理策略。
     */
    class State : BaseState() {
        var cacheCleanupPolicy by enum(ImageCacheCleanupPolicy.ByTtlAndSize)
        var cacheTtlDays by property(DEFAULT_CACHE_TTL_DAYS)
        var cacheMaxSizeMb by property(DEFAULT_CACHE_MAX_SIZE_MB)
        var cacheMaxFiles by property(DEFAULT_CACHE_MAX_FILES)
        var renderPolicy by enum(ImageRenderPolicy.Sync)
        var renderFailurePolicy by enum(ImageRenderFailurePolicy.AcceptBroken)
    }
}
