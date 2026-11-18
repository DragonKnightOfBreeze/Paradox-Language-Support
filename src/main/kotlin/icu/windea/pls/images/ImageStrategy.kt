package icu.windea.pls.images

/**
 * 图片处理策略。
 */
enum class ImageStrategy {
    /** 直接使用源图片，无需切分或格式转换。 */
    Direct,

    /** 需要经由渲染管道派生到目标缓存路径（切分或格式转换）。 */
    Delegated,
}
