package icu.windea.pls.config

import icu.windea.pls.config.option.CwtOptionMetadataService

object CwtConfigThreadContext {
    /**
     * 标记是否要跳过对选项元数据的处理，直接保留所有选项规则列表。
     *
     * @see CwtOptionMetadataService.process
     */
    val skipProcessingOptionMetadata = ThreadLocal<Boolean>()
}
