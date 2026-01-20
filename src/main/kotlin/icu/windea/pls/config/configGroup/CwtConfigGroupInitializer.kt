package icu.windea.pls.config.configGroup

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import icu.windea.pls.model.ParadoxGameType

/**
 * 规则分组的初始化器。
 *
 * 用于初始化规则数据，包括后续的处理和优化。
 */
class CwtConfigGroupInitializer(
    override val project: Project,
    override val gameType: ParadoxGameType,
) : CwtConfigGroupDataHolderBase(), CwtConfigGroup {
    override var initialized: Boolean
        get() = throw UnsupportedOperationException()
        set(_) = throw UnsupportedOperationException()
    override var changed: Boolean
        get() = throw UnsupportedOperationException()
        set(_) = throw UnsupportedOperationException()
    override val initializer: CwtConfigGroupInitializer get() = throw UnsupportedOperationException()
    override val modificationTracker: SimpleModificationTracker get() = throw UnsupportedOperationException()

    override suspend fun init() = throw UnsupportedOperationException()
}
