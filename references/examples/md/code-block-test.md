# Code Block Test

```paradox_script path=stellaris:common/armies/injected_defence_armies.txt
defense_army = {
    defensive = yes
    is_pop_spawned = yes
    health = 1.25
    damage = 1.50
    morale = 1.25
    collateral_damage = 0.0
    war_exhaustion = 0.0 # No WE from defense armies
    icon = GFX_army_type_defensive

    resources = {
        category = armies
        produces = {
            trigger = {
                exists = owner
                owner = { has_active_tradition = tr_unyielding_resistance_is_frugal }
            }
            unity = 0.5
        }
    }

    potential = {
        from = {
            NOR = {
                has_trait = "trait_mechanical"
                has_trait = "trait_machine_unit"
                is_sapient = no
            }
        }
        owner = {
            is_primitive = no
            OR = {
                is_machine_empire = no
                has_valid_civic = civic_machine_assimilator
            }
        }

    }
}
```