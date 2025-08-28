# 报告_#3_项目探索_task-3_会话二

> 时间：2025-08-27 16:35（本地）
> 会话：继续 PLS 插件配置文件按序探索（task-3）
> 概述：本会话聚焦 `src/main/resources/META-INF/pls-lang.xml`，整理跨语言聚合与 IDE 集成功能；已生成对应探索笔记并同步更新前一份报告的“阶段更新”。

---

## 阶段更新：阅读 pls-lang.xml（探索_8）

- 笔记位置：`notes/ai-vision/task-3/探索_8_pls-lang.xml.md`
- 核心要点：
  - 跨语言聚合：统一注册索引/搜索（StubIndex/FileBasedIndex、QueryExecutor）、Inlay、行标记、参数信息、后缀模板、文档/链接处理。
  - 重构矩阵：rename 校验/建议、自动重命名、inline/extract、多类 refactoring handlers。
  - 导航与层级：Go To Files/Definitions/Localisations/Related*，Definition/Call 层级视图。
  - 动作体系：`Pls.Tools` 聚合与多处上下文菜单；Steam/路径/URL/复制等外部集成。
  - 运行闭环：Listener + LibraryProvider + FileListener + PsiTreeChangePreprocessor 协同触发刷新与通知。
  - Registry Keys：字体/图标/文本长度/深度/数量/图片尺寸等运行时调参。

---

## 阶段更新：阅读 pls-ep.xml（探索_9）

- 笔记位置：`notes/ai-vision/task-3/探索_9_pls-ep.xml.md`
- 核心要点：
  - 定义 PLS 扩展点：Color/QuickDoc/HintText/ReferenceLink、CWT 注入/覆盖/关联、Config Context/Declaration Context、Config Group Data/File、DataExpression Resolver/Merger/Priority、Expression Matchers/Supports（Script/Localisation/CSV/PathRef）、Icon/Index、检查与抑制、元数据与推断、修饰符/参数/作用域生态、Definition 呈现/继承/内联/优先级、Mod Import/Export；均 `dynamic="true"`。
  - 默认实现：覆盖上述各 EP 的基础/核心/模板/正则/Ant/常量等策略，以及 Stellaris 专项实现；多处使用 `order`（first/last/after）控制链顺序。
  - 设计定位：作为“语义与规则层”的抽象扩展面，为 `pls-lang.xml` 中的平台扩展提供语义数据源与行为策略。

---

## 待办与下一步

- 继续 include 文件（任选其一）：
  - `src/main/resources/META-INF/pls-images.xml`
  - `src/main/resources/META-INF/pls-integrations.xml`
  - `src/main/resources/META-INF/pls-ai.xml`
  - `src/main/resources/META-INF/pls-intentions.xml`
  - `src/main/resources/META-INF/pls-inspections.xml`
  - `src/main/resources/META-INF/pls-inject.xml`

---

## 会话总结

- 已完成：`pls-lang.xml` 阅读与记录，输出探索笔记与任务报告更新。
- 收获：明确 PLS 的跨语言能力注册面与运行时闭环；为后续 inspections/EP/intentions/integrations 的阅读奠定索引/搜索/动作/重构的全局背景。
