LOCALIZATION GRAMMAR SYSTEM FOR NAMES
Since 3.6, Stellaris has a system in place to handle some aspects of grammar relating to names in the game.


This covers names that can be generated in-game, randomly or through script, including (not exclusive):
* Countries
* Species
* Leaders
* Systems
* Planets
* Federations
* Factions
* Wars
* Ships
* Fleets
* Armies
* ...


The main difficulties that the system is intended to solve are:
* Grammatical gender
* Grammatical case
* Variable word order


1. Organization of names
	a. In the game state, a name is stored as a localization key. Some names have sub-names inside them, 
		which have their own loc keys, and so on.
	b. Sub-names go into named "slots" in the localization string. Each slot is denoted by a slot name 
		surrounded by dollar signs $.
	c. For example,
		"Holy Provalguvoran Mandate" (in English) might be represented in the gamestate as
		"holy"
		which localizes to "Holy $1$"
		with slot "1" filled by "provalguvor_adj" 
		which localizes to "Provalguvoran $1$"
		with slot "1" filled by "mandate"
		which localizes to "Mandate".
	d. The gamestate representation is the same for all languages, e.g. "holy"->"provalguvor_adj"->"mandate", but 
		different languages can move the slots around as appropriate to control word order, and also use the 
		grammar tools described below to make the components of this name come out correctly.

2. Types of localization strings
	The grammatical system has various implications for different classes of localization strings:
	a. Nouns/proper nouns: Names of planets, stars, species, or words like "Empire", "Collective", "War", 
		"Faction". These don't have sub-names but can form the "core" of complex names.
		Nouns may need tags added (see (4)) to support grammatical gender. For languages where the 
		grammatical case is important, nouns may also need variant strings (see (5)). In some cases, nouns 
		may also require adjective overrides (see (9)).
	b. Attributes: Words or phrases that apply to one or more sub-names (which can be nouns or other attribute 
		phrases), e.g. "Panaxalan [Empire]", "Sacrosanct [Conclave]", "[Association] of Scientific Ascendancy".
		Attributes may need variant strings so that they take the form appropriate to their sub-name; they may also need 
		tags of their own.
	c. Game text: Text such as event texts. These are not names themselves, but may incorporate names using 
		properties (e.g. "Victory in [fromfrom.System.GetName]").
		These strings may, if so desired, utilize tag-sensitive text (see (7)) and/or specify the desired 
		form of a name using context tags (see (8)).
		The same applies to "[...GetAdjective]" properties.
	d. UI text: Text that goes in e.g. tooltips. Characterized by having dollar sign slots, that don't refer 
		to other localization keys (e.g. "$COUNTRY$ wins the game.").
		Such text does not yet support tag-sensitive text or context tags. This is ongoing work. (The name 
		will still obey its own internal grammatical rules when used like this).
	e. Special strings: These are new strings introduced with the grammar system and serve various special 
		purposes, including default adjective formats, default leader and ruler name formats, etc.

3. Localization files involved
	a. Special strings are located in the file "name_system_l_english.yml".
		This includes the default adjective format (see (11)), adjectivization patterns (see (10)), generic 
		formats for leader names, war names and other general name formats.
	b. "name_system_l_english.yml" also contains formats for numbers (see (13)).
	c. "localization/english/name_lists" contains large numbers of names specific to different civilizations. For 
		the most part, these are not combined with other name components in the game and so will not require 
		tagging. Certain leader name lists ("Red"+"Mary") may be exceptions to this (see (12)).
		Sequential names in these files may also be adjusted as outlined in (13).
	d. "prescripted_countries_names_l_english.yml" contains specific names for prescripted empires as well as 
		empire adjectives, species name, species adjective, species plural, and default leader.
	e. "initializers_names_l_english.yml" contains proper nouns for special planets and stars.
		The same is true for "event_names_l_english.yml" and "common_names_l_english.yml"
	f. "species_..._l_english.yml" contains species names, species plurals, and associated star and planet names.
	g. "localization/english/random_names" contains the bulk of strings that form components in the names and 
		where it makes most sense to make adjustments as outlined below.
		i. "formats_00_...yml" contains formats for randomly-generated empires, wars, factions etc. These strings 
			are deprecated and are no longer referenced in newly created games (but needed for backward 
			compatibility). These do not require any adjustments on account of the grammar system.
		ii. "name_parts_00_...yml" carries most of the building blocks for randomly generated names. These strings 
			take the form of nouns, adjectives, and other types of attributes.
		iii. "random_names_l_english.yml" contains a large number of proper nouns for things like stars, planets
			etc. Of these, stars, planets and black holes may be used as components in other names (e.g. "Empire 
			of Betelgeuse").
		iv. "00_random_names_l_english.yml" contains a smaller number of nouns and a handful of 
			adjectives/attributes. The same points apply here as above.

4. Tagging Localization Strings
	a. Each loc string can be "tagged" with one or more grammatical tags.
	b. This is done by adding an ampersand and an exclamation mark, followed by the tags, separated by commas.
	c. Example: 
		 a_tagged_string:0 "Empress&!fem,vowel"
	d. There is no specific set of tags available; you can come up with any set that serves your purposes.
	e. Each language can use entirely different tags (or none).
	f. As well, even for a given language, you can use different tags for different purposes.
	g. The number of tags that are applied to a single name should ideally not be more than 8.

5. String Variants
	a. A single localization string can contain multiple "variants". Which variant the game will use will depend
		on the tags that are applied to that name.
	b. Variants are separated by three pipe characters, followed by 0 or more tags that are required for this 
		variant to be used, followed by a colon and then the variant string itself.
	c. By default, a name that has sub-names gets all the tags from its sub-names applied to it.
	d. The system goes from right to left and picks the first variant it encounters whose tag requirements are all 
		satisfied. In other words, you should start with the most generic variant and proceed to more specific 
		ones.
	e. Example:
		 string_with_variants:0 "A $1$|||vowel:An $1$"
		This would yield "An Empress" (since Empress is tagged with "vowel", see (4)) but "A Queen" (provided 
		Queen is not)
	f. Variants can add their own tags as part of their string. This is different from the tag requirements that 
		determine which variant gets used.
	g. Tags added by variants are not used to determine which variant gets used; the variant is selected first, 
		then its own tags are added to the ones forwarded to the parent name.
	h. The main intended use for this feature is to allow adjectives etc. to be inflected based on a noun's 
		gender (which is not generally needed in English).

6. Tag forwarding
	a. By default, if name A contains name B and name B contains name C, then C's tags are applied to B and used 
		to select its variant string, and then both B's tags and C's are applied to A.
	b. If there are multiple subnames under a single name, then all tags from all sub-names are forwarded to the 
		parent in this fashion by default.
	c. If this is not desired, then the tags forwarded can be controlled by using the same syntax as when adding 
		tags (ampersand + exclamation mark), but with a number instead of a tag.
	d. If the name should only forward the tags from its first slot, use "&!1". If only the second, use "&!2". 
		If none, use "&!0".
	e. Example:
		 coalition:0 "Coalition of $1$&!0"
		This will prevent for example a "vowel" tag from being forwarded and causing e.g. "An Coalition of Empires" 
		(instead of "A Coalition of Empires")
	f. A number can be combined with the addition of new tags, using commas.
		Example:
		 organization:0 "Organization of $1$&!0,vowel"
		This will "override" whatever tags would come from the sub-name, and forward only "vowel", allowing for 
		e.g. "An Organization of Empires" (instead of "A Organization of Empires")
	g. It is possible to combine several of these numbers, using commas. (This only makes sense if a name has 3 or 
		more sub-names, which is not typically the case).

7. Tag-sensitive text
	a. When a name is used in a game text, such as "Declaration of war from [From.GetName]", the name gets 
		localized as outlined under (5), and inserted in the appropriate position. However, for many languages the 
		surrounding text, while not part of the name, needs to be made to vary according to the grammar of the 
		name.
	b. To accomplish this, it is possible to enclose a portion of the text along with the name, and provide 
		variants for that portion of text depending on the tags of the name.
		This is done by appending a colon to the property name, inside the brackets [], followed by variants 
		separated by ||| (and required tags) in the same way as described in (5).
	c. The position where the name itself goes is denoted by "<1>". (Dollar signs $ are not used here because they 
		already have a different use in game text, namely including other localization strings by key)
	d. Example:
		plot_string:0 "We have uncovered [From.GetAdj:a <1> plot|||vowel:an <1> plot]"
		might be used to yield "...a Human plot" but "...an Ofoxxer plot".
	e. The main use of this feature is once again to allow text to be inflected based on the grammatical gender 
		of a name.
	f. It is perfectly valid to omit the "<1>" from a tag-sensitive text, so that the name itself doesn't appear,
		but only influences the form the sentence takes. This can be useful if the text has name-dependent words 
		that are far apart from each other.
	g. It is also possible to use angle brackets <> to look up a different localization key and apply the tags from 
		the name to it. 
		Example:
		 plot_string:0 "We have uncovered [From.GetAdj:<a_an> <1> plot]"
		 a_an:0 "a|||vowel:an"
		This method can be useful for patterns that are repeated a lot in a given language's text.
	h. The difference between using $dollar_key$ and <angle_key> notation is that the former will include the target 
		string verbatim; the latter will attempt to extract a string variant using tags from the name whose 
		bracket [] context we are currently in.

8. Context tags
	a. In some cases, it is not the name that determines the form that its context will take, but the other way 
		around. This is especially the case with languages where context determines the grammatical case a word 
		should take.
	b. To support this, the system allows a localization string to specify required tags from the "outside in", 
		in addition to the default "outward" dependency described in (6).
	c. These tags will typically be a different set than the ones provided from "inside" by e.g. the noun - the 
		latter will typically be gender etc., the former things like case.
	d. To specify such a requirement, an ampersand is appended to either a property name, e.g.:
		"[From.GetName&dative]"
		or to a sub-name slot within a name string, e.g.:
		"Empire of $1&genitive$"
	e. The name, and all its sub-names, will receive these context tags as an (extra) requirement when selecting 
		variants.

9. Explicit adjectives
	a. Prescripted empires all have adjectives specified for both their species and their empire itself 
		("prescripted_countries_l_english.yml").
	b. Most species and empires do not have adjectives specified. Instead, they have adjectives generated from the 
		base name, using adjectivization patterns (see (10)).
	c. Whether or not English specifies an adjective for a species or empire, it is always possible to do so 
		explicitly where needed, by creating a new localization key that is the same as the name's key but with 
		"_adj" appended.
	d. Example:
		 SPEC_Mishar:0 "Mishar"
		 SPEC_Mishar_adj:0 "Mishish"
	e. You may or may not add a "$1$" slot to specify where the attributed phrase goes ("Mishish $1$"). If you do 
		not, the default adjective format will do so (see (11)).

10. Adjectivization patterns
	a. When an adjective is needed for a name that has not had one specified explicitly, the system will attempt 
		to apply a series of simple patterns to the name.
	b. These patterns are located in "name_system_l_english.yml" and take the form "adj_NN[x]...", where the [x] is 
		a word ending consisting of 0-2 letters.
	c. If there is a pattern with 2 letters whose ending matches, then that one is applied. Otherwise, it tries to 
		apply a 1-letter pattern. If neither is the case, and there is a 0-letter pattern ("adj_NN:"), then that 
		pattern is applied.
	d. When a pattern is applied, the matching ending is removed, and the remainder of the word is substituted for 
		the asterisk in the loc string.
	e. Example:
		 adj_NNus:0 "*an $1$"
		will turn "Ganvius" into "Ganvian $1$".
	f. If no patterns match (and there's no 0-letter pattern), the noun (or attributed phrase) will be used 
		unaltered, with the default adjective format applied (see (11)).
	g. Both tags and string variants can be used in adjectivization patterns, e.g. to make an adjective that 
		conforms to the gender of the noun.
		Example (French):
		 adj_NNus:0 "$1$ *enne|||masc:$1$ *en"
	g. Just because a pattern exists in English that doesn't mean it needs to exist in any other language: you may 
		add or remove patterns as needed for the language in question.
	h. Currently, you must place a "$1$" in each adjectivization pattern (default adjective formats are not 
		applied to the result). This may be amended in the future.

11. Default adjective format
	a. As it is tiresome to add " $1$" to every string, the system supports a default format for adjectives. Any 
		adjective that has no "$1$" slot in its localization will have one added at run-time, using a default 
		format.
	b. The default format has the special loc key "adj_format". The string for this key is used, with the 
		substring "adj" replaced by the adjective in question.
	c. For example, in English the default looks like this:
		 adj_format:0 "adj $1$"
		which will turn an adjective without "$1$" such as "Human" into "Human $1$".

12. Leader names
	a. Leaders can have two names - a first and second name - or only one name, which counts as a first name.
	b. When localizing the full name of a leader that has two names, the system performs some special treatment 
		depending on the localization string.
		i. If the first name contains a "$1$", then it is treated as an attribute of the second name.
		ii. If the second name contains a "$1$", then it is treated as an attribute of the first name.
		iii. Otherwise, the system localizes the special string "TWO_NAMES_FORMAT" and places the first name in the
			"$1$" slot and the second name in the "$2$" slot.
	c. The reason for this is that there are some names on the pattern "Red Mary", where "Red" needs to be 
			dependent on the gender of "Mary" in certain languages; and, there are some names on the pattern 
			"Plume of Blue", where "of Blue" needs to be dependent on "Plume".

13. Numerals
	a. Names of ships, armies and fleets may be created as "sequential names", meaning they contain a number. 
	b. Localization has full control over how numbers are displayed inside each name. For sequential names, use 
		dollar signs together with a key referring to a number format (located in "name_system_l_english.yml").
	c. Example:
		 AQUATIC1_SEASPRAYBATTALION:0 "$ORD$ Seaspray Battalion"
		which refers to the number format "ORD", which will show as "1st Seaspray Battalion", "2nd", "3rd" etc.
	d. Changing "$ORD$" to "$C$" (for "cardinal" numbers) will make it "1 Seaspray Battalion" etc.
	e. "$CC$" and "$CCC$" are the same as $C$ but will show "01" and "001" respectively.
	f. "$CC0$" is like $CC$ but starts counting at "00".
	g. "$R$" will show as Roman numerals
	h. "$HEX$" will show as hexadecimal numbers, and will start counting at "000"
	i. "$ORD0$" will show ordinal numbers starting with "0th"
	j. Each language may modify these formats or create new ones. Each name can be made to use any format 
		desired.
	k. More information on how new numerals can be created is available in the comments inside 
		"name_system_l_english.yml".
	l. Currently, variant strings can be used but will only work with context tags, not with tags from the 
		sequential name itself. In other words, grammatical case is supported, gender is not. It is to be hoped 
		this can be fixed in an upcoming release. In the meantime, it will also work to create e.g. a feminine 
		ordinal format and a masculine one, and simply refer to one or the other depending on the sequential name.