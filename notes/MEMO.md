# Memo

## To check

```
# Cannot resolve 'faill_breach_shroud_owner' and no exception thrown???
event_target:faill_breach_shroud_owner
无法提示普通枚举？


option = {
    name = "diplo_stance_cooperative" # 为什么这里的"diplo_stance_cooperative"使用定义或复杂枚举的高亮？？
    icon = "GFX_diplomatic_stance_cooperative"
    potential = {
        OR = {
            is_country_type = default
            is_country_type = awakened_fallen_empire
        }
        is_unfriendly = no			# Not homicidal OR barbaric despoilers
        NOR = {
            has_valid_civic = civic_inwards_perfection
            has_valid_civic = civic_isolationism
        }
    }
    policy_flags = {
        diplo_stance_cooperative
    }
```