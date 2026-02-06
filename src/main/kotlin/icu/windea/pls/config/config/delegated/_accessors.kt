package icu.windea.pls.config.config.delegated

// region CwtLinkConfig Accessors

/** 是否为静态链接。 */
val CwtLinkConfig.isStatic: Boolean get() = dataSources.isEmpty()

/** 使用函数调用形式时采用的前缀（作为函数名）。 */
val CwtLinkConfig.prefixFromArgument: String? get() = prefix?.removeSuffix(":")

// endregion
