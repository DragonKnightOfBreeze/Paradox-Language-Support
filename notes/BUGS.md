# BUGS

## 0.4.0

* [ ] 是否缺失event namespace的检查对于所有paradoxScript文件都生效，而不是仅对于events目录下的paradoxScript文件
* [ ] definition和definitionLocalisation的gutterIcon未对齐
* [ ] definition没有definitionLocalisation时不要显示对应的gutterIcon
* [ ] 完善localisationCommand（即本地化文本中方括号中的内容）的解析规则（匹配正则`[\w.]+`？）
* [ ] 完善localisationText（即本地化文本）的解析规则，允许单独的美元符号（`$`）
* [ ] 完善script文件的解析规则：等号之前允许换行
* [ ] 对于scriptColor，颜色设置在第一次选择后便不再会同步更改到文件中
* [ ] 对于scriptColor，颜色设置应该保持颜色类型（`rgb` `hsv`等）
* [ ] 当项目中存在模组文件夹（基于`descriptor.mod`）时，弹出通知要求玩家选择对应的原版游戏文件夹（参照CwTools）