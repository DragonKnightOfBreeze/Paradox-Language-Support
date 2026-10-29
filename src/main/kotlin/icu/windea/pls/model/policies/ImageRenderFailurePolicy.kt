package icu.windea.pls.model.policies

import icu.windea.pls.ChronicleBundle

/**
 * 图片渲染失败时的处理策略。
 */
enum class ImageRenderFailurePolicy(val text: String) {
    AcceptBroken(ChronicleBundle.message("policy.imageRenderFailure.0")),
    RemoveImage(ChronicleBundle.message("policy.imageRenderFailure.1")),
    Placeholder(ChronicleBundle.message("policy.imageRenderFailure.2")),
    ;
}
