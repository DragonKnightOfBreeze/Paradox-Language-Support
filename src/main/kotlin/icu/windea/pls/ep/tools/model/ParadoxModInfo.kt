package icu.windea.pls.ep.tools.model

import icu.windea.pls.model.ParadoxModSource
import java.nio.file.Path

/**
 * 模组信息（平台无关）。
 *
 * 用于导入/导出模组列表的通用数据模型，避免耦合 IDE 设置对象。
 *
 * @property name 显示名称（可为空）
 * @property modDirectory 模组根目录（如存在）
 * @property remoteId 远端ID，例如 Steam/Paradox 平台上的ID
 * @property source 模组来源（本地、Steam、Paradox Launcher 等）
 * @property enabled 是否启用
 * @property version 模组版本（可为空）
 * @property supportedVersion 兼容的游戏版本（可为空）
 */
 data class ParadoxModInfo(
     val name: String? = null,
     val modDirectory: Path? = null,
     val remoteId: String? = null,
     val source: ParadoxModSource = ParadoxModSource.Local,
     val enabled: Boolean = true,
     val version: String? = null,
     val supportedVersion: String? = null,
 )
