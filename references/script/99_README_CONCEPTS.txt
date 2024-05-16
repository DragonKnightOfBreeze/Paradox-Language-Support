#####################
#	CONCEPT TYPES	#
#####################
#concept_id = {													# ID used for specifying the concept in loc strings.
#																# If it's used without a replacement string, there must be a loc with an identical key.
#																# If tooltip_override is not used, there must be a loc with a key suffixed with "_desc" (e.g. concept_id_desc).
#	alias = { concept_alias_1 concept_alias_2 }					# List of aliases that may be used instead of the ID.
#																# Any that are used without a replacement string must have a loc with an identical key.
#	icon = "gfx/interface/icons/concepts/concept_icon.dds"		# Icon shown in concept tooltip.
#																# If not specified, it will use NUncheckedDefines::NInterface::GAME_CONCEPT_ICON_DEFAULT from unchecked_defines/00_interface.txt.
#	texture = "gfx/interface/icons/concepts/concept_icon.dds"	# Alternative for "icon".
#	tooltip_override = "civic:civic_imperial_cult"				# Tooltip to use instead of the default. Can be either a loc key or a database object (see DATABASE CONCEPTS below).
#}
#
#############################
#	USING CONCEPTS IN LOC	#
#############################
# EXAMPLE_LOC_1: "Example of a ['concept_id'] with a tooltip"
#	This will replace the square bracketed text with the "concept_id" loc.
#	The tooltip will use the key "concept_id_desc" unless tooltip_override is specified.
#
# EXAMPLE_LOC_2: "Example of a ['concept_id', replacement text] with a tooltip"
#	This will replace the square bracketed text with "replacement text".
#	Unlike EXAMPLE_LOC_1, there doesn't need to be a loc with the key "concept_id".
#
#########################
#	DATABASE CONCEPTS	#
#########################
# For some types of database objects, we support automatically generating concept tooltips without having to manually create them in loc. Objects are specified using the following syntax:
# civic:civic_imperial_cult
# In this example, "civic" is the object type and "civic_imperial_cult" is the object ID.
#
# There are two ways to use database concepts:
# 1) Specify a database object instead of a concept ID.
#		EXAMPLE_LOC_1: "Example of ['civic:civic_imperial_cult'] with a tooltip"
#		This method does not require creating a concept in game_concepts or any concept loc.
#		Tooltips shown this way will not have a concept icon.
#		If a replacement string is not specified, this will use the object ID as the loc key for the concept text.
#			This only works for object types whose loc keys are the same as their object IDs, otherwise a replacement string should be used.
#			It also doesn't support objects using different names based on triggers (e.g. tradition swaps); it will always use the default name (only in the concept text; the tooltip should apply triggers correctly).
#    Specify a specific database swap.
#        This can be used if you want to override any triggers and show one specific swap or the pre-swapped version of the database object.
#        EXAMPLE AUTHORITY SWAP
#        1: authority:auth_democratic:auth_cyber_creed_democratic
#            Always show the Cyber Creed authority swap for Democracy
#        2: authority:auth_democratic:
#            Always show the base Democratic authority
# 2) Create a concept with the database object as its tooltip_override, then use that concept in loc.
#		concept_id = {
#			tooltip_override = "civic:civic_imperial_cult"
#		}
#		Like other concepts, this requires a loc with the same key as the concept unless a replacement string is used. It also supports concept icons and aliases.
#
# List of currently supported object types:
#	ascension_perk
#	authority
#	authority_swaps
#	building
#	civic (includes origins)
#	district
#	edict
#	ethic
#	starbase_building
#	starbase_module
#	technology (note: the tooltip currently only shows the name and description)
#	tradition
#	tradition_category
#	trait (both species and leader traits)
#
# In GUI, instantTextBoxType has a "concepts_show_missing_dlc" parameter. If set to "yes", database concept tooltips inside the text box will show unmet DLC requirements for the database object.
# Currently, this only works for authorities, civics, ethics and species traits.
