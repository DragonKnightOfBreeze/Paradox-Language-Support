# 不正确的文件编码的代码检查

> 所属模块：`后端 > 语言功能支持`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v2.1.10
> - 日期：2026-06-22
> - 状态：已实施
> - 相关问题：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/345

使用以下规则：
- Always use `UTF-8` for script/loc/csv files
- Always use `BOM` for loc files
- Always use `NO BOM` for csv files
- For Stellaris, Use `BOM` for script files under `common/name_list`, otherwise `NO BOM`
- For other game types, do not check bom for script files (maybe `BOM` or `NO BOM`, there is no ideal way to check)

相关代码位置：
- `icu.windea.pls.lang.analysis.ParadoxFileEncodingService`
- `icu.windea.pls.lang.inspections.ParadoxFileInspectionService`
- `icu.windea.pls.lang.inspections.script.common.IncorrectFileEncodingInspection`
- `icu.windea.pls.lang.inspections.localisation.common.IncorrectFileEncodingInspection`
- `icu.windea.pls.lang.inspections.csv.common.IncorrectFileEncodingInspection`
