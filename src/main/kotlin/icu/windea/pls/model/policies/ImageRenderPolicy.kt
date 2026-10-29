package icu.windea.pls.model.policies

import icu.windea.pls.ChronicleBundle

/**
 * 图片渲染策略。
 */
enum class ImageRenderPolicy(val text: String) {
    Sync(ChronicleBundle.message("policy.imageRender.0")),
    Async(ChronicleBundle.message("policy.imageRender.1")),
    ;
}
