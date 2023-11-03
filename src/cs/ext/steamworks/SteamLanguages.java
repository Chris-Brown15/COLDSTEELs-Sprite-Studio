/**
 * 
 */
package cs.ext.steamworks;

/**
 * Contains constants representing languages that Steam has full support for, both for games and websites.
 */
public enum SteamLanguages {

	Bulgarian("bulgarian" , "bg") ,
	SimplifiedChinese("schinese" , "zh-CN") ,
	TraditionalChinese("tchinese" , "zh-TW") ,
	Czech("czech" , "cs") ,
	Danish("danish" , "da") ,
	Dutch("dutch" , "nl") ,
	English("english" , "en") ,
	Finnish("finnish" , "fi") ,
	French("french" , "fr") ,
	German("german" , "de") ,
	Greek("greek" , "el") ,
	Hungarian("hungarian" , "hu") ,
	Indonesian("indonesian" , "id") ,
	Italian("italian" , "it") ,
	Japanese("japanese" , "ja") ,
	Korean("koreana" , "ko") ,
	Norwegian("norwegian" , "no") ,
	Polish("polish" , "pl") ,
	Portuguese("portuguese" , "pt") ,
	BrazilianPortuguese("brazilian" , "pt-BR") ,
	Romanian("romanian" , "ro") ,
	Russian("russian" , "ru") ,
	SpainSpanish("spanish" , "es") ,
	LatinAmericanSpanish("latam" , "es-419") ,
	Swedish("swedish" , "sv") ,
	Thai("thai" , "th") ,
	Turkish("turkish" , "tr") ,
	Ukrainian("ukrainian" , "uk") ,
	Vietnamese("vietnamese" , "vn") ,
	;
	
	public final String APILanguageCode , webAPILanguageCode;
	
	SteamLanguages(String APILangaugeCode , String webAPILanguageCode) {
		
		this.APILanguageCode = APILangaugeCode;
		this.webAPILanguageCode = webAPILanguageCode;
			
	}
	
	
	
}
