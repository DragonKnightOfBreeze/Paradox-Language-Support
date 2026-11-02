package icu.windea.pls.model.policies

import icu.windea.pls.ChronicleBundle

/**
 * 图片磁盘缓存的清理策略。
 */
enum class ImageCacheCleanupPolicy(val text: String) {
    None(ChronicleBundle.message("policy.imageCacheCleanup.0")),
    ByTtl(ChronicleBundle.message("policy.imageCacheCleanup.1")),
    BySize(ChronicleBundle.message("policy.imageCacheCleanup.2")),
    ByTtlAndSize(ChronicleBundle.message("policy.imageCacheCleanup.3")),
    ;
}
