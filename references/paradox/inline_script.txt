# Inline Scripts
# -----------------------
# Inline scripts gives you the ability to write a block of script in one file 
# and then include that block of script into other scripts as if it would have
# been written directly in those other scripts. This can prevent a huge amount
# of copy paste. It is especially useful when you may want to update something
# inside of the block of script since updating the inline_script will
# automatically update all scripts that are using that inline_script, while
# otherwise you would need to go through all those scripts yourself and update
# them one by one.
# 
# This is very similar to how scripted effects and scripted triggers work. But
# inline scripts can be used for things that are not effects or triggers.
# 
# HOW TO USE
# -----------------------
# Create a file in the "common/inline_scripts" folder with the script that you
# want to inline into other scripts. Note that you can only have one inline
# script per file and that the file name will be used when inlining the script
# into other scripts. You can organize the inline scripts by putting them
# inside subfolders.
# 
# --- common/inline_scripts/edicts/upkeep_low.txt ---
# resources = {
# 	category = edicts
# 	upkeep = {
# 		unity = 10
# 		multiplier = value:edict_size_effect
# 	}
# }
# --- file end ---
# 
# Then use inline_script = <file_name_without_extension> to inline it.
# --- 00_edicts.txt ---
# fortify_the_border = {
# 	length = @EdictPerpetual
# 	icon = "GFX_edict_type_policy"
# 
# 	inline_script = "edicts/upkeep_low" # Game replaces this statement with the contents of common/inline_scripts/edicts/upkeep_low.txt
# 
# 	modifier = {
# 		starbase_upgrade_speed_mult = 0.50
# 		country_starbase_capacity_add = 2
# ------
#
# PARAMETERS
# -----------------------
# You can use parameters to change parts of the inline script.
# 
# --- common/inline_scripts/test_basic_policy.txt ---
# $KEY$ = {
#	option = {
#		name = "$KEY$_a"
#		on_enabled = {
#			add_modifier = { modifier = $MODIFIER_A$ days = 360 }
#		}
#	}
#	option = {
#		name = "$KEY$_b"
#		on_enabled = {
#			add_modifier = { modifier = $MODIFIER_B$ days = 360 }
#		}
#	}
# }
# --- end file ---
#
# --- common/policies/00_policies.txt ---
# inline_script = {
#	script = test_basic_policy
#	KEY = test_1
#	MODIFIER_A = evermore_science
#	MODIFIER_B = fumongus_authoritarian
# }
# ------
#
# Inline script parameters accept strings so it's possible to swap in a
# full statement or even several statements by enclosing it in ""
# inline_script = {
#	script = just_an_example
#	MODIFIER = "num_tech_alternatives_add = 1 scientist_skill_levels = 2" # <--- like this
# }
#
# GOOD TO KNOW
# -----------------------
# Support for inline_script hasn't been added everywhere, so if you get an
# error that inline_script is an unexpected token or that inline_script is
# encountered before init then it's likely that you can't use inline_scripts
# there. You also can't use inline scripts within a list of items such as
# "examples = { abc test2 test3 object }".
