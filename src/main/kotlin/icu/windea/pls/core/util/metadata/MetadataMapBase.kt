package icu.windea.pls.core.util.metadata

import com.intellij.openapi.util.UserDataHolderBase

/**
 * 基于 [UserDataHolderBase] 实现的元数据映射。
 */
abstract class MetadataMapBase : UserDataHolderBase(), MetadataMap
