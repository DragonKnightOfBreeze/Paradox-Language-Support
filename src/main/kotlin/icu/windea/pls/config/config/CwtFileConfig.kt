package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtFile

/**
 * CWT 文件级规则对象。
 *
 * 表示一个 CWT 规则文件在内存中的聚合视图，包含该文件下的顶层属性/值规则。
 *
 * @property pointer 指向源 [CwtFile] 的智能指针。
 * @property configGroup 所属的规则组 [CwtConfigGroup]。
 * @property properties 顶层属性规则列表。
 * @property values 顶层值规则列表。
 * @property name 规则文件名（逻辑名，用于展示/诊断）。
 */
class CwtFileConfig(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    val properties: List<CwtPropertyConfig>,
    val values: List<CwtValueConfig>,
    val name: String
) : UserDataHolderBase(), CwtConfig<CwtFile>
