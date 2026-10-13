# 规则数据可能会被不定期清空的问题

> 所属类别：`后端`
> 上级导航：[问题解决与避坑索引 (Solutions Index)](README.md)

- 用户反映内嵌提示中的中文本地化文本会被不定期回退为英文
- 这里使用的语言环境设置（`CwtLocaleConfig`）来自默认项目的规则数据
- 研究发现默认项目目前基于 `TimedReference`
  - 参见：`com.intellij.openapi.project.impl.DefaultProject`
  - 参见：`com.intellij.openapi.project.impl.DefaultProjectTimed`
  - 参见：`com.intellij.util.TimedReference`
- `DefaultProject` 本身不会被清空，其生命周期是整个应用（`Application`），但 `DefaultProject.delegate` 会被定期清空
- 这说明默认项目的规则数据可能会被不定期清空，需要考虑其他的实现方案，而非直接从默认项目获取服务对象，再获取服务对象上的缓存数据

最终的解决方案：

改为缓存到应用（默认项目的规则数据）或项目（对应项目的规则数据）的用户数据中
