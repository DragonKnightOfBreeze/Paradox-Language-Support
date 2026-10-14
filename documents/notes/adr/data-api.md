# 引入 `ParadoxData` 和 `ParadoxDataSource`

> - 模块：后端 > 其他语义相关
> - 版本：v3.0.3+
> - 日期：2026-09-15+
> - 状态：TODO

提供“数据”的API，以便按需获取便于序列化的结构化数据。

说明：
- 这类数据是基于上下文的，dataSource 作为数据源提供元数据、上下文信息、查询方法等，data 则是各种特定上下文中的数据模型类（比如事件的数据模型）。
- 可以用来在为前后端代码提供限定的、仅必要的上下文数据。
- dataSource 不应在接口层面绑定PSI，但是默认实现自然是基于PSI的。查询方法得到的可以是切换后的 dataSource，或者具体的 data，或者可直接序列化的数据。
- data 应尽可能地作为纯粹的数据类，通过手动方式或者基于注解的方式来创建（排除基于属性委托的方式，因为这不怎么适用于数据类）。
- data 的字段除了可直接序列化的数据之外，也可以考虑包括一个可选且只读的 metadata 字段（需要为其设计序列化和反序列化的方式）。
- 主要面向脚本文件提供，但也要考虑为本地化文件和 CSV文件提供（尽管使用场景相对受限）。
- 通常不需要在创建时严格检查游戏类型/文件信息/定义信息等，而是直接假设正确，应在调用处保证。

说明（代码位置）：
- 模型代码位于 `icu.windea.pls.model.data` 中。
- 其他相关代码位于 `icu.windea.pls.lang.data` 中。

说明（RWA 相关）：
- 仅在通过 dataSource 查询数据时需要 readAction，但是，目不前考虑在必要时自动加上（通过 `runSmartReadAction`）。
- data 自身不应通过普通字段绑定 PSI（或者 VirtualFile 等），不过，目前可以考虑在 metadata 字段中通过 `com.intellij.model.Pointer` 绑定 PSI。

相关代码位置：
- [ ] `icu.windea.pls.model.data.ParadoxData`
- [ ] `icu.windea.pls.model.data.ParadoxDataSource`
- [ ] `icu.windea.pls.model.lang.ParadoxDataFactory`
- [ ] `icu.windea.pls.model.lang.ParadoxDataImpls.kt` - 一组预定义且按需提供的模型实现
