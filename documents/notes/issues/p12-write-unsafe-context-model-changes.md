# P12 java.lang.Throwable: Write-unsafe context! Model changes are allowed from write-safe contexts only.

> - 状态：应当已解决

堆栈跟踪：[stack-trace-11](../assets/stack-trace-11.txt)

自插件版本2.1.8开始，由于解析 rootInfo 时不再依赖VFS，应当不会再复现了。
