# 探索_15_pls-inject.xml

> 文件：`src/main/resources/META-INF/pls-inject.xml`
> 主题：PLS 代码注入（Code Injection）基础设施与具体注入器注册

---

## 总览

- 定义并实现了“代码注入”相关的动态扩展点与监听器，用于在 IDE 与插件运行期向平台/插件内部类注入行为。
- 通过多组 `CodeInjector` 覆盖“核心能力修正/增强、图像读写优化、附加能力、性能优化（PSI 级优化）”。
- 与 `pls-images.xml` 与 `pls-integrations.xml` 的图像处理生态形成联动，利用外部工具提升 DDS/TGA 读取性能与兼容性。

---

## 扩展点与监听器

- `extensionPoints`（dynamic=true）
  - `icu.windea.pls.inject.codeInjectorSupport` → 接口 `CodeInjectorSupport`
  - `icu.windea.pls.inject.codeInjector` → 接口 `CodeInjector`
- `applicationListeners`
  - `CodeInjectorService$Listener` 订阅 `com.intellij.ide.AppLifecycleListener`
  - 用途：在应用生命周期阶段装配/撤销注入（推测），实现按启动时机注册/清理。

---

## 扩展实现：Support

- `defaultExtensionNs="icu.windea.pls.inject"`
  - `BaseCodeInjectorSupport`
  - `FieldBasedCacheCodeInjectorSupport`
  - 作用：为注入器提供通用支撑（反射/缓存/容错），并以字段级缓存等手段降低注入成本。

---

## 扩展实现：Injectors（分类）

- 核心能力修正/增强（core）
  - `LowLevelSearchUtilCodeInjector`
  - `RefManagerImplCodeInjector`
  - `SymbolNavigationServiceImplCodeInjector`
  - `LineCommentCopyPastePreProcessorCodeInjector`
  - `CommentByLineCommentHandlerCodeInjector`
  - `PathReferenceManagerImplCodeInjector`
  - 说明：覆盖查找/引用/导航/复制粘贴/路径引用等平台关键点，定制对 PLS 语言的更好支持。

- 图像读取优化（delegate to image tools）
  - `DDSImageReaderCodeInjector`
  - `TGAImageReaderCodeInjector`
  - 说明：将 DDS/TGA 解析委托给外部工具链（如 texconv/ImageMagick），与 `pls-images.xml` 动作与 `pls-integrations.xml` 的工具提供者配合。

- Bug 修复
  - `ImageDescriptorKtCodeInjector`（已注释）
  - 说明：作为兼容修复位，按需开启以应对特定版本问题。

- 附加能力（additional features）
  - `FileRenderCodeInjector`
  - `InjectionRegistrarImplCodeInjector`
  - 说明：可能扩展文件渲染/注入注册器行为，提供更灵活的语言片段注入与渲染能力。

- 性能优化（PSI 级）
  - Script：`ParadoxScriptPsiCodeInjectors$Property/$PropertyKey/$Boolean/$Int/$Float/$String/$Color/$ParameterConditionParameter/$Parameter/$InlineMathParameter`
  - Localisation：`ParadoxLocalisationPsiCodeInjectors$Locale/$Property/$PropertyKey/$PropertyValue/$Parameter/$Icon/$TextFormat/$TextIcon`
  - CWT：`CwtPsiCodeInjectors$Property/$PropertyKey/$Option/$OptionKey/$Boolean/$Int/$Float/$String`
  - 说明：对频繁创建/遍历/解析的 PSI 节点注入专门加速路径，降低索引与编辑时延。

- 其它扩展点
  - `icu.windea.pls.inject.injectedFileProcessor` → 接口 `InjectedFileProcessor`（dynamic=true）
  - 作用：为“注入产生的文件/片段”提供后处理钩子（语义修补/索引告知/缓存等）。

---

## 观察与推断

- 注入风险与版本兼容：注入平台内部类可能随平台版本变动而脆弱，建议做版本门槛或安全回退。
- 与语言/索引的协作：PSI 注入器表明其对解析与索引性能有直接影响，应在大型 Mod 项目中验证收益。
- 图像链路闭环：注入器与外部工具提供者、图像动作形成闭环，满足预览/转换/存储的不同场景需求。

---

## 展望：可实现的功能方向（建议）

- 配置与开关
  - 在设置页提供“注入器分组开关”（核心/图像/附加/性能），便于定位问题并按需启用。
  - 提供“安全模式”：注入失败或冲突时自动降级，写入日志并提示用户。
  - 版本门槛：按 IDE build range 启用特定注入器，避免不兼容。

- 可观测与诊断
  - 注入健康面板：实时展示启用的注入器、命中次数、失败统计与耗时指标。
  - 日志与告警：为关键注入点添加限频日志与错误聚合，便于快速回溯。

- 性能与质量
  - 基准测试：提供“索引/导航/搜索/渲染”基准任务，对比启用/禁用注入的耗时。
  - 细粒度启用：按语言/文件类型/目录作用域生效，降低全局注入副作用。

- 扩展生态
  - 对外 EP：开放自定义 `CodeInjector`/`InjectedFileProcessor` 的第三方接入与示例模板。
  - 与 AI/翻译联动：在注入的渲染/预览管线中插入“AI 预览处理器”（如占位符保护、术语高亮）。

- 测试与回归
  - 各注入器的单元/集成测试；覆盖回滚/禁用路径。
  - 兼容性预检：CI 中对多版本 IDE 运行注入自检。

---

## 关联与后续

- 直接关联：
  - `src/main/resources/META-INF/pls-images.xml`（图像动作/编辑器/转换）
  - `src/main/resources/META-INF/pls-integrations.xml`（图像工具/翻译/Lint 提供者）
  - `src/main/resources/META-INF/pls-ep.xml`（更通用的语义扩展位）
- 建议下一步：阅读 `src/main/resources/META-INF/pls-extension-translation.xml`，梳理与翻译插件生态的对接方式。
