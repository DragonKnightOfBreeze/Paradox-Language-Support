package icu.windea.pls

fun getWorkshopLink(steamId:String): String{
	return "https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
}

fun getWorkshopLinkInSteam(steamId: String): String{
	return "steam://openurl/https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
}