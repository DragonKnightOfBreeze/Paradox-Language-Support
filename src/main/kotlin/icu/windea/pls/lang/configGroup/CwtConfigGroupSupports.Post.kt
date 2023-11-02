package icu.windea.pls.lang.configGroup

import icu.windea.pls.config.configGroup.*

/**
 * 用于在完成对CWT规则分组的初始化之前，对其进行一些必要的处理。
 */
abstract class PostCwtConfigGroupSupport: CwtConfigGroupSupport

/**
 * 用于初始化需要经过计算的那些数据。
 */
class BasePostCwtConfigGroupSupport: PostCwtConfigGroupSupport() {
    override fun process(configGroup: CwtConfigGroup): Boolean {
        //TODO
        return true
    }
}