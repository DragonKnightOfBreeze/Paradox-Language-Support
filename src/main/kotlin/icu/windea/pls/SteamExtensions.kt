package icu.windea.pls

fun getSteamWorkshopLink(steamId:String): String{
	return "https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
}

fun getSteamWorkshopLinkOnSteam(steamId: String): String{
	return "steam://openurl/https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
}