package com.presidentsimulator.game.data

import java.util.Locale

/** Offline country profiles. Statistics are reference-year snapshots; game capacities are derived. */
internal data class RealCountryProfile(
    val code: String,
    val name: String,
    val officialName: String,
    val region: String,
    val governmentSystem: GovernmentSystem,
    val governmentLabel: String,
    val population: Long,
    val populationYear: Int,
    val gdpUsd: Long,
    val gdpYear: Int,
    val statusNote: String,
) {
    val flagEmoji: String
        get() = code.uppercase(Locale.ROOT).take(2).map { char ->
            String(Character.toChars(0x1F1E6 + (char.code - 'A'.code)))
        }.joinToString("")
}

internal object RealCountryCatalog {
    private val rawProfiles = """
AF|Afghanistan|Islamic Republic of Afghanistan|Asia|TRANSITIONAL|Transitional system|41454761|2023|17152234636|2023|UN member state
AL|Albania|Republic of Albania|Europe|PARLIAMENTARY|Parliamentary system|2411658|2023|23491242727|2023|UN member state
DZ|Algeria|People's Democratic Republic of Algeria|Middle East & North Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|46164219|2023|247923887214|2023|UN member state
AD|Andorra|Principality of Andorra|Europe|PARLIAMENTARY|Parliamentary system|80856|2023|3785067331|2023|UN member state
AO|Angola|Republic of Angola|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|36749906|2023|106042349567|2023|UN member state
AG|Antigua and Barbuda|Antigua and Barbuda|Americas|PARLIAMENTARY|Parliamentary system|93316|2023|2054096296|2023|UN member state
AR|Argentina|Argentine Republic|Americas|PRESIDENTIAL|Presidential system|45538401|2023|649461687959|2023|UN member state
AM|Armenia|Republic of Armenia|Europe|PARLIAMENTARY|Parliamentary system|2964300|2023|24185982215|2023|UN member state
AU|Australia|Commonwealth of Australia|Pacific|PARLIAMENTARY|Parliamentary system|26659922|2023|1734451264655|2023|UN member state
AT|Austria|Republic of Austria|Europe|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|9131761|2023|516670509628|2023|UN member state
AZ|Azerbaijan|Republic of Azerbaijan|Europe|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|10153958|2023|72428470588|2023|UN member state
BS|Bahamas|Commonwealth of the Bahamas|Americas|PARLIAMENTARY|Parliamentary system|399440|2023|15271300000|2023|UN member state
BH|Bahrain|Kingdom of Bahrain|Middle East & North Africa|MONARCHY|Monarchy|1577059|2023|46192260638|2023|UN member state
BD|Bangladesh|People's Republic of Bangladesh|Asia|PARLIAMENTARY|Parliamentary system|171466990|2023|437415333018|2023|UN member state
BB|Barbados|Barbados|Americas|PARLIAMENTARY|Parliamentary system|282336|2023|7223248150|2023|UN member state
BY|Belarus|Republic of Belarus|Europe|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|9178298|2023|72478760370|2023|UN member state
BE|Belgium|Kingdom of Belgium|Europe|PARLIAMENTARY|Parliamentary system|11779946|2023|650779364363|2023|UN member state
BZ|Belize|Belize|Americas|PARLIAMENTARY|Parliamentary system|411106|2023|3052362650|2023|UN member state
BJ|Benin|Republic of Benin|Africa|PRESIDENTIAL|Presidential system|14111034|2023|19673291004|2023|UN member state
BT|Bhutan|Kingdom of Bhutan|Asia|MONARCHY|Monarchy|786385|2023|3012896789|2023|UN member state
BO|Bolivia (Plurinational State of)|Plurinational State of Bolivia|Americas|PRESIDENTIAL|Presidential system|12244159|2023|52340206946|2023|UN member state
BA|Bosnia and Herzegovina|Bosnia and Herzegovina|Europe|PARLIAMENTARY|Parliamentary system|3185073|2023|27592361498|2023|UN member state
BW|Botswana|Republic of Botswana|Africa|PARLIAMENTARY|Parliamentary system|2480244|2023|19413614554|2023|UN member state
BR|Brazil|Federative Republic of Brazil|Americas|PRESIDENTIAL|Presidential system|211140729|2023|2191131765684|2023|UN member state
BN|Brunei Darussalam|Negara Brunei Darussalam|Asia|MONARCHY|Monarchy|458949|2023|15095084656|2023|UN member state
BG|Bulgaria|Republic of Bulgaria|Europe|PARLIAMENTARY|Parliamentary system|6446596|2023|102204457334|2023|UN member state
BF|Burkina Faso|Burkina Faso|Africa|TRANSITIONAL|Transitional system|23025776|2023|20106062971|2023|UN member state
BI|Burundi|Republic of Burundi|Africa|PRESIDENTIAL|Presidential system|13689450|2023|3419558408|2023|UN member state
CV|Cabo Verde|Republic of Cape Verde|Africa|PARLIAMENTARY|Parliamentary system|522331|2023|2504525537|2023|UN member state
KH|Cambodia|Kingdom of Cambodia|Asia|PARLIAMENTARY|Parliamentary system|17423880|2023|42335646895|2023|UN member state
CM|Cameroon|Republic of Cameroon|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|28372687|2023|48814501547|2023|UN member state
CA|Canada|Canada|Americas|PARLIAMENTARY|Parliamentary system|40049088|2023|2196593836347|2023|UN member state
CF|Central African Republic|Central African Republic|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|5152421|2023|2555492085|2023|UN member state
TD|Chad|Republic of Chad|Africa|PRESIDENTIAL|Presidential system|19319064|2023|18352937976|2023|UN member state
CL|Chile|Republic of Chile|Americas|PRESIDENTIAL|Presidential system|19658835|2023|335802745366|2023|UN member state
CN|China|People's Republic of China|Asia|COMMUNIST|Communist one-party system|1410710000|2023|18270356654533|2023|UN member state
CO|Colombia|Republic of Colombia|Americas|PRESIDENTIAL|Presidential system|52321152|2023|366901643683|2023|UN member state
KM|Comoros|Union of the Comoros|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|850387|2023|1465977684|2023|UN member state
CG|Congo|Republic of the Congo|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|6182885|2023|15321055822|2023|UN member state
CR|Costa Rica|Republic of Costa Rica|Americas|PRESIDENTIAL|Presidential system|5105525|2023|87512637055|2023|UN member state
HR|Croatia|Republic of Croatia|Europe|PARLIAMENTARY|Parliamentary system|3859686|2023|85621337533|2023|UN member state
CU|Cuba|Republic of Cuba|Americas|COMMUNIST|Communist one-party system|11019931|2023|107352000000|2020|UN member state
CY|Cyprus|Republic of Cyprus|Europe|PRESIDENTIAL|Presidential system|1344976|2023|35075440602|2023|UN member state
CZ|Czechia|Czechia|Europe|PARLIAMENTARY|Parliamentary system|10864042|2023|345059295659|2023|UN member state
CI|Côte d'Ivoire|Republic of Côte d'Ivoire|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|31165654|2023|80780312569|2023|UN member state
KP|Democratic People's Republic of Korea|Democratic People's Republic of Korea|Asia|COMMUNIST|Communist one-party system|26418204|2023|0|0|UN member state
CD|Democratic Republic of the Congo|Democratic Republic of the Congo|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|105789731|2023|69843655425|2023|UN member state
DK|Denmark|Kingdom of Denmark|Europe|PARLIAMENTARY|Parliamentary system|5946952|2023|404651706117|2023|UN member state
DJ|Djibouti|Republic of Djibouti|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|1152944|2023|3898447007|2023|UN member state
DM|Dominica|Commonwealth of Dominica|Americas|PARLIAMENTARY|Parliamentary system|66510|2023|659311111|2023|UN member state
DO|Dominican Republic|Dominican Republic|Americas|PRESIDENTIAL|Presidential system|11331265|2023|120456239153|2023|UN member state
EC|Ecuador|Republic of Ecuador|Americas|PRESIDENTIAL|Presidential system|17980083|2023|120792801000|2023|UN member state
EG|Egypt|Arab Republic of Egypt|Middle East & North Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|114535772|2023|395926071447|2023|UN member state
SV|El Salvador|Republic of El Salvador|Americas|PRESIDENTIAL|Presidential system|6309624|2023|33565430000|2023|UN member state
GQ|Equatorial Guinea|Republic of Equatorial Guinea|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|1847549|2023|12541814272|2023|UN member state
ER|Eritrea|State of Eritrea|Africa|PRESIDENTIAL|Presidential system|3470390|2023|0|0|UN member state
EE|Estonia|Republic of Estonia|Europe|PARLIAMENTARY|Parliamentary system|1370286|2023|41470344395|2023|UN member state
SZ|Eswatini|Kingdom of Eswatini|Africa|MONARCHY|Monarchy|1230506|2023|4621191261|2023|UN member state
ET|Ethiopia|Federal Democratic Republic of Ethiopia|Africa|PARLIAMENTARY|Parliamentary system|128691692|2023|135874093203|2023|UN member state
FJ|Fiji|Republic of Fiji|Pacific|PARLIAMENTARY|Parliamentary system|924145|2023|5476673518|2023|UN member state
FI|Finland|Republic of Finland|Europe|PARLIAMENTARY|Parliamentary system|5583911|2023|295191741637|2023|UN member state
FR|France|French Republic|Europe|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|68372286|2023|3056250648138|2023|UN member state
GA|Gabon|Gabonese Republic|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|2484789|2023|19388372070|2023|UN member state
GM|Gambia (The)|Republic of The Gambia|Africa|PRESIDENTIAL|Presidential system|2697845|2023|2382262185|2023|UN member state
GE|Georgia|Georgia|Europe|PARLIAMENTARY|Parliamentary system|3715483|2023|30777833600|2023|UN member state
DE|Germany|Federal Republic of Germany|Europe|PARLIAMENTARY|Parliamentary system|83287273|2023|4562207532490|2023|UN member state
GH|Ghana|Republic of Ghana|Africa|PRESIDENTIAL|Presidential system|33787914|2023|80547146875|2023|UN member state
GR|Greece|Hellenic Republic|Europe|PARLIAMENTARY|Parliamentary system|10407351|2023|242946187738|2023|UN member state
GD|Grenada|Grenada|Americas|PARLIAMENTARY|Parliamentary system|117081|2023|1336418518|2023|UN member state
GT|Guatemala|Republic of Guatemala|Americas|PRESIDENTIAL|Presidential system|18124838|2023|104298081428|2023|UN member state
GN|Guinea|Republic of Guinea|Africa|PRESIDENTIAL|Presidential system|14405468|2023|22407615556|2023|UN member state
GW|Guinea-Bissau|Republic of Guinea-Bissau|Africa|TRANSITIONAL|Transitional system|2153339|2023|2076748678|2023|UN member state
GY|Guyana|Co-operative Republic of Guyana|Americas|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|826353|2023|16918503597|2023|UN member state
HT|Haiti|Republic of Haiti|Americas|PRESIDENTIAL|Presidential system|11637398|2023|19572288894|2023|UN member state
VA|Holy See (Vatican City)|The Holy See|Europe|THEOCRATIC_MONARCHY|Elective theocratic monarchy|764|2024|0|0|UN non-member observer state · GDP unavailable
HN|Honduras|Republic of Honduras|Americas|PRESIDENTIAL|Presidential system|10644851|2023|34355805527|2023|UN member state
HU|Hungary|Hungary|Europe|PARLIAMENTARY|Parliamentary system|9592186|2023|213029511028|2023|UN member state
IS|Iceland|Republic of Iceland|Europe|PARLIAMENTARY|Parliamentary system|385663|2023|31701996433|2023|UN member state
IN|India|Republic of India|Asia|PARLIAMENTARY|Parliamentary system|1438069596|2023|3500906030644|2023|UN member state
ID|Indonesia|Republic of Indonesia|Asia|PRESIDENTIAL|Presidential system|281190067|2023|1371166925749|2023|UN member state
IR|Iran (Islamic Republic of)|Islamic Republic of Iran|Asia|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|90608707|2023|457510482317|2023|UN member state
IQ|Iraq|Republic of Iraq|Middle East & North Africa|PARLIAMENTARY|Parliamentary system|45074049|2023|268881051643|2023|UN member state
IE|Ireland|Ireland|Europe|PARLIAMENTARY|Parliamentary system|5311538|2023|567372737459|2023|UN member state
IL|Israel|State of Israel|Middle East & North Africa|PARLIAMENTARY|Parliamentary system|9849000|2023|513393395491|2023|UN member state
IT|Italy|Italian Republic|Europe|PARLIAMENTARY|Parliamentary system|58984216|2023|2316882296366|2023|UN member state
JM|Jamaica|Jamaica|Americas|PARLIAMENTARY|Parliamentary system|2839786|2023|21418797832|2023|UN member state
JP|Japan|Japan|Asia|PARLIAMENTARY|Parliamentary system|124516650|2023|4384854269961|2023|UN member state
JO|Jordan|Hashemite Kingdom of Jordan|Middle East & North Africa|MONARCHY|Monarchy|11439213|2023|56123472112|2023|UN member state
KZ|Kazakhstan|Republic of Kazakhstan|Asia|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|20330104|2023|261840101059|2023|UN member state
KE|Kenya|Republic of Kenya|Africa|PRESIDENTIAL|Presidential system|55339003|2023|107500884685|2023|UN member state
KI|Kiribati|Republic of Kiribati|Pacific|PARLIAMENTARY|Parliamentary system|132530|2023|291657337|2023|UN member state
XK|Kosovo|Republic of Kosovo|Europe|PARLIAMENTARY|Parliamentary republic|1682668|2023|10466753839|2023|Additional self-governing entity · status disputed
KW|Kuwait|State of Kuwait|Middle East & North Africa|MONARCHY|Monarchy|4853420|2023|165462656226|2023|UN member state
KG|Kyrgyzstan|Kyrgyz Republic|Asia|PRESIDENTIAL|Presidential system|7099750|2023|15180842369|2023|UN member state
LA|Lao People's Democratic Republic|Lao People's Democratic Republic|Asia|COMMUNIST|Communist one-party system|7664993|2023|15843155731|2023|UN member state
LV|Latvia|Republic of Latvia|Europe|PARLIAMENTARY|Parliamentary system|1883710|2023|42779550936|2023|UN member state
LB|Lebanon|Republic of Lebanon|Middle East & North Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|5773493|2023|20078620356|2023|UN member state
LS|Lesotho|Kingdom of Lesotho|Africa|PARLIAMENTARY|Parliamentary system|2311472|2023|2129871529|2023|UN member state
LR|Liberia|Republic of Liberia|Africa|PRESIDENTIAL|Presidential system|5493031|2023|4390000000|2023|UN member state
LY|Libya|Libya|Middle East & North Africa|TRANSITIONAL|Transitional system|7305659|2023|44027664915|2023|UN member state
LI|Liechtenstein|Principality of Liechtenstein|Europe|PARLIAMENTARY|Parliamentary system|39846|2023|8239379403|2023|UN member state
LT|Lithuania|Republic of Lithuania|Europe|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|2871585|2023|80356613554|2023|UN member state
LU|Luxembourg|Grand Duchy of Luxembourg|Europe|PARLIAMENTARY|Parliamentary system|666430|2023|88788881539|2023|UN member state
MG|Madagascar|Republic of Madagascar|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|31195932|2023|16036707975|2023|UN member state
MW|Malawi|Republic of Malawi|Africa|PRESIDENTIAL|Presidential system|21104482|2023|13363669672|2023|UN member state
MY|Malaysia|Federation of Malaysia|Asia|PARLIAMENTARY|Parliamentary system|35126298|2023|399949418752|2023|UN member state
MV|Maldives|Republic of Maldives|Asia|PRESIDENTIAL|Presidential system|525994|2023|6621008703|2023|UN member state
ML|Mali|Republic of Mali|Africa|TRANSITIONAL|Transitional system|23769127|2023|24777941860|2023|UN member state
MT|Malta|Republic of Malta|Europe|PARLIAMENTARY|Parliamentary system|552747|2023|22625329775|2023|UN member state
MH|Marshall Islands|Republic of the Marshall Islands|Pacific|PRESIDENTIAL|Presidential system|38827|2023|264000000|2023|UN member state
MR|Mauritania|Islamic Republic of Mauritania|Middle East & North Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|5022441|2023|10687541640|2023|UN member state
MU|Mauritius|Republic of Mauritius|Africa|PARLIAMENTARY|Parliamentary system|1248647|2023|14072212290|2023|UN member state
MX|Mexico|United Mexican States|Americas|PRESIDENTIAL|Presidential system|129739759|2023|1794410347718|2023|UN member state
FM|Micronesia (Federated States of)|Federated States of Micronesia|Pacific|PRESIDENTIAL|Presidential system|112630|2023|442975600|2023|UN member state
MC|Monaco|Principality of Monaco|Europe|PARLIAMENTARY|Parliamentary system|38956|2023|10003897341|2023|UN member state
MN|Mongolia|Mongolia|Asia|PARLIAMENTARY|Parliamentary system|3481145|2023|20325121393|2023|UN member state
ME|Montenegro|Montenegro|Europe|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|623529|2023|7643318276|2023|UN member state
MA|Morocco|Kingdom of Morocco|Middle East & North Africa|MONARCHY|Monarchy|37712505|2023|146036093666|2023|UN member state
MZ|Mozambique|Republic of Mozambique|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|33635160|2023|20927498317|2023|UN member state
MM|Myanmar|Republic of the Union of Myanmar|Asia|TRANSITIONAL|Transitional system|54133798|2023|66757619000|2023|UN member state
NA|Namibia|Republic of Namibia|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|2963095|2023|12522012873|2023|UN member state
NR|Naoero|Republic of Naoero|Pacific|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|11875|2023|161531316|2023|UN member state
NP|Nepal|Federal Democratic Republic of Nepal|Asia|PARLIAMENTARY|Parliamentary system|29694614|2023|41049329851|2023|UN member state
NL|Netherlands|Kingdom of the Netherlands|Europe|PARLIAMENTARY|Parliamentary system|17877117|2023|1135475867551|2023|UN member state
NZ|New Zealand|New Zealand|Pacific|PARLIAMENTARY|Parliamentary system|5200000|2023|256372177757|2023|UN member state
NI|Nicaragua|Republic of Nicaragua|Americas|PRESIDENTIAL|Presidential system|6823613|2023|17805842220|2023|UN member state
NE|Niger|Republic of Niger|Africa|TRANSITIONAL|Transitional system|26159867|2023|16949765463|2023|UN member state
NG|Nigeria|Federal Republic of Nigeria|Africa|PRESIDENTIAL|Presidential system|227882945|2023|487387801877|2023|UN member state
MK|North Macedonia|The Republic of North Macedonia|Europe|PARLIAMENTARY|Parliamentary system|1827816|2023|15855131188|2023|UN member state
NO|Norway|Kingdom of Norway|Europe|PARLIAMENTARY|Parliamentary system|5519601|2023|502197633322|2023|UN member state
OM|Oman|Sultanate of Oman|Middle East & North Africa|MONARCHY|Monarchy|5049269|2023|106174707932|2023|UN member state
PK|Pakistan|Islamic Republic of Pakistan|Asia|PARLIAMENTARY|Parliamentary system|247504495|2023|336686348609|2023|UN member state
PW|Palau|Republic of Palau|Pacific|PRESIDENTIAL|Presidential system|17727|2023|276157406|2023|UN member state
PA|Panama|Republic of Panama|Americas|PRESIDENTIAL|Presidential system|4458759|2023|83812155200|2023|UN member state
PG|Papua New Guinea|Independent State of Papua New Guinea|Pacific|PARLIAMENTARY|Parliamentary system|10389635|2023|30816328065|2023|UN member state
PY|Paraguay|Republic of Paraguay|Americas|PRESIDENTIAL|Presidential system|6844146|2023|43140465595|2023|UN member state
PE|Peru|Republic of Peru|Americas|PRESIDENTIAL|Presidential system|33845617|2023|268025009709|2023|UN member state
PH|Philippines|Republic of the Philippines|Asia|PRESIDENTIAL|Presidential system|114891199|2023|437055627245|2023|UN member state
PL|Poland|Republic of Poland|Europe|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|36687353|2023|812451193396|2023|UN member state
PT|Portugal|Portuguese Republic|Europe|PARLIAMENTARY|Parliamentary system|10578174|2023|292323800948|2023|UN member state
QA|Qatar|State of Qatar|Middle East & North Africa|MONARCHY|Monarchy|2656032|2023|213002809340|2023|UN member state
KR|Republic of Korea|Republic of Korea|Asia|PRESIDENTIAL|Presidential system|51712619|2023|1844800934391|2023|UN member state
MD|Republic of Moldova|Republic of Moldova|Europe|PARLIAMENTARY|Parliamentary system|2457783|2023|16711906746|2023|UN member state
RO|Romania|Romania|Europe|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|19061062|2023|347757995758|2023|UN member state
RU|Russian Federation|Russian Federation|Europe|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|143826130|2023|2046284838151|2023|UN member state
RW|Rwanda|Republic of Rwanda|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|13954471|2023|14806501437|2023|UN member state
KN|Saint Kitts and Nevis|Federation of Saint Christopher and Nevis|Americas|PARLIAMENTARY|Parliamentary system|46758|2023|1055651851|2023|UN member state
LC|Saint Lucia|Saint Lucia|Americas|PARLIAMENTARY|Parliamentary system|179285|2023|2441259259|2023|UN member state
VC|Saint Vincent and the Grenadines|Saint Vincent and the Grenadines|Americas|PARLIAMENTARY|Parliamentary system|101323|2023|1072237037|2023|UN member state
WS|Samoa|Independent State of Samoa|Pacific|PARLIAMENTARY|Parliamentary system|216663|2023|1044909500|2023|UN member state
SM|San Marino|Republic of San Marino|Europe|PARLIAMENTARY|Parliamentary system|33860|2023|2027243193|2023|UN member state
ST|Sao Tome and Principe|Democratic Republic of São Tomé and Príncipe|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|230871|2023|690456806|2023|UN member state
SA|Saudi Arabia|Kingdom of Saudi Arabia|Middle East & North Africa|MONARCHY|Monarchy|33702731|2023|1218584800000|2023|UN member state
SN|Senegal|Republic of Senegal|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|18077573|2023|30696331295|2023|UN member state
RS|Serbia|Republic of Serbia|Europe|PARLIAMENTARY|Parliamentary system|6623183|2023|81343999280|2023|UN member state
SC|Seychelles|Republic of Seychelles|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|119773|2023|2171547935|2023|UN member state
SL|Sierra Leone|Republic of Sierra Leone|Africa|PRESIDENTIAL|Presidential system|8460512|2023|6415852766|2023|UN member state
SG|Singapore|Republic of Singapore|Asia|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|5917648|2023|511181761243|2023|UN member state
SK|Slovakia|Slovak Republic|Europe|PARLIAMENTARY|Parliamentary system|5426740|2023|133578518423|2023|UN member state
SI|Slovenia|Republic of Slovenia|Europe|PARLIAMENTARY|Parliamentary system|2120461|2023|69255264238|2023|UN member state
SB|Solomon Islands|Solomon Islands|Pacific|PARLIAMENTARY|Parliamentary system|800005|2023|1506124566|2023|UN member state
SO|Somalia|Federal Republic of Somalia|Africa|PRESIDENTIAL|Presidential system|18358615|2023|10958000000|2023|UN member state
ZA|South Africa|Republic of South Africa|Africa|PARLIAMENTARY|Parliamentary system|63212384|2023|381440724491|2023|UN member state
SS|South Sudan|Republic of South Sudan|Africa|TRANSITIONAL|Transitional system|11483374|2023|0|0|UN member state
ES|Spain|Kingdom of Spain|Europe|PARLIAMENTARY|Parliamentary system|48352528|2023|1619481980719|2023|UN member state
LK|Sri Lanka|Democratic Socialist Republic of Sri Lanka|Asia|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|22037000|2023|84080307355|2023|UN member state
PS|State of Palestine|State of Palestine|Middle East & North Africa|TRANSITIONAL|Transitional institutions|5165775|2023|18635100000|2023|UN non-member observer state
SD|Sudan|Republic of the Sudan|Middle East & North Africa|TRANSITIONAL|Transitional system|50042791|2023|39898289820|2023|UN member state
SR|Suriname|Republic of Suriname|Americas|PRESIDENTIAL|Presidential system|628886|2023|3472693412|2023|UN member state
SE|Sweden|Kingdom of Sweden|Europe|PARLIAMENTARY|Parliamentary system|10536632|2023|578990915246|2023|UN member state
CH|Switzerland|Switzerland|Europe|PARLIAMENTARY|Parliamentary system|8888822|2023|928435275851|2023|UN member state
SY|Syrian Arab Republic|Syrian Arab Republic|Middle East & North Africa|TRANSITIONAL|Transitional system|23594623|2023|23737634644|2022|UN member state
TW|Taiwan|Taiwan|Asia|PRESIDENTIAL|Presidential system|23420000|2023|0|0|Additional self-governing entity · status disputed · GDP profile unavailable
TJ|Tajikistan|Republic of Tajikistan|Asia|PRESIDENTIAL|Presidential system|10389799|2023|12244169292|2023|UN member state
TH|Thailand|Kingdom of Thailand|Asia|PARLIAMENTARY|Parliamentary system|71702435|2023|517013369475|2023|UN member state
TL|Timor-Leste|Democratic Republic of Timor-Leste|Asia|PARLIAMENTARY|Parliamentary system|1384286|2023|2079767200|2023|UN member state
TG|Togo|Togolese Republic|Africa|PARLIAMENTARY|Parliamentary system|8223850|2023|9723300576|2023|UN member state
TO|Tonga|Kingdom of Tonga|Pacific|MONARCHY|Monarchy|104597|2023|591139733|2023|UN member state
TT|Trinidad and Tobago|Republic of Trinidad and Tobago|Americas|PARLIAMENTARY|Parliamentary system|1367510|2023|25036993423|2023|UN member state
TN|Tunisia|Republic of Tunisia|Middle East & North Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|12200431|2023|48205328303|2023|UN member state
TM|Turkmenistan|Turkmenistan|Asia|PRESIDENTIAL|Presidential system|7364438|2023|39020211239|2023|UN member state
TV|Tuvalu|Tuvalu|Pacific|PARLIAMENTARY|Parliamentary system|9816|2023|50491930|2023|UN member state
TR|Türkiye|Republic of Türkiye|Europe|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|85325965|2023|1141242864657|2023|UN member state
UG|Uganda|Republic of Uganda|Africa|PRESIDENTIAL|Presidential system|48656601|2023|48768955863|2023|UN member state
UA|Ukraine|Ukraine|Europe|PARLIAMENTARY|Parliamentary system|37732836|2023|181221517868|2023|UN member state
AE|United Arab Emirates|United Arab Emirates|Middle East & North Africa|MONARCHY|Monarchy|10483751|2023|522622268401|2023|UN member state
GB|United Kingdom|United Kingdom of Great Britain and Northern Ireland|Europe|PARLIAMENTARY|Parliamentary system|68526000|2023|3420796653789|2023|UN member state
TZ|United Republic of Tanzania|United Republic of Tanzania|Africa|PRESIDENTIAL|Presidential system|66617606|2023|79030935638|2023|UN member state
US|United States of America|United States of America|Americas|PRESIDENTIAL|Presidential system|336755052|2023|27811517000000|2023|UN member state
UY|Uruguay|Eastern Republic of Uruguay|Americas|PRESIDENTIAL|Presidential system|3388081|2023|79208725899|2023|UN member state
UZ|Uzbekistan|Republic of Uzbekistan|Asia|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|35652307|2023|107526539716|2023|UN member state
VU|Vanuatu|Republic of Vanuatu|Pacific|PARLIAMENTARY|Parliamentary system|320409|2023|1188137265|2023|UN member state
VE|Venezuela (Bolivarian Republic of)|Bolivarian Republic of Venezuela|Americas|PRESIDENTIAL|Presidential system|28300854|2023|102377501185|2023|UN member state
VN|Viet Nam|Socialist Republic of Viet Nam|Asia|COMMUNIST|Communist one-party system|100352192|2023|433805036898|2023|UN member state
YE|Yemen|Republic of Yemen|Middle East & North Africa|TRANSITIONAL|Transitional system|39390799|2023|0|0|UN member state
ZM|Zambia|Republic of Zambia|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|20723965|2023|27577956471|2023|UN member state
ZW|Zimbabwe|Republic of Zimbabwe|Africa|PRESIDENTIAL_PARLIAMENTARY|Presidential–parliamentary system|16340822|2023|35871781303|2023|UN member state
""".trimIndent()

    val all: List<RealCountryProfile> by lazy {
        rawProfiles.lineSequence()
            .filter(String::isNotBlank)
            .map { row ->
                val columns = row.split('|')
                require(columns.size == 11) { "Invalid country profile row: $row" }
                RealCountryProfile(
                    code = columns[0],
                    name = columns[1],
                    officialName = columns[2],
                    region = columns[3],
                    governmentSystem = GovernmentSystem.valueOf(columns[4]),
                    governmentLabel = columns[5],
                    population = columns[6].toLong(),
                    populationYear = columns[7].toInt(),
                    gdpUsd = columns[8].toLong(),
                    gdpYear = columns[9].toInt(),
                    statusNote = columns[10],
                )
            }.toList()
    }
}
