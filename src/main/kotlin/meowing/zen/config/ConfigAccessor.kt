package meowing.zen.config

import meowing.zen.config.ui.ConfigUI
import java.awt.Color

class ConfigAccessor(val configUI: ConfigUI) {
    private var _blockoverlayfill = false
    private var _blockoverlaycolor = Color(0, 255, 255, 127)
    private var _blockoverlaywidth = 2.0
    private var _arrowpoison = false
    private var _slayertimer = false
    private var _slayerhighlightcolor = Color(0, 255, 255, 127)
    private var _slayerhighlightwidth = 2.0
    private var _slayerstats = false
    private var _lasertimer = false
    private var _vengdmg = false
    private var _carrycounter = false
    private var _carryvalue = "1.3"
    private var _carrybosshighlight = false
    private var _carrybosscolor = Color(0, 255, 255, 127)
    private var _carrybosswidth = 2.0
    private var _carryclienthighlight = false
    private var _carryclientcolor = Color(0, 255, 255, 127)
    private var _carryclientwidth = 2.0
    private var _carrycountsend = false
    private var _keyhighlightcolor = Color(0, 255, 255, 127)
    private var _keyhighlightwidth = 2.0
    private var _cryptreminderdelay = 2.0
    private var _draftself = false
    private var _autogetdraft = false
    private var _leapmessage = "Leaping to"
    private var _boxstarmobscolor = Color(0, 255, 255, 127)
    private var _boxstarmobswidth = 2.0
    private var _entityhighlightplayercolor = Color(0, 255, 255, 255)
    private var _entityhighlightmobcolor = Color(255, 0, 0, 255)
    private var _entityhighlightanimalcolor = Color(0, 255, 0, 255)
    private var _entityhighlightothercolor = Color(255, 255, 255, 255)
    private var _entityhighlightwidth = 2f
    private var _highlightlividcolor = Color(0, 255, 255, 127)
    private var _highlightlividwidth = 2f
    private var _hidewronglivid = false
    private var _highlightlividline = false

    val blockoverlayfill get() = _blockoverlayfill
    val blockoverlaycolor get() = _blockoverlaycolor
    val blockoverlaywidth get() = _blockoverlaywidth
    val slayertimer get() = _slayertimer
    val slayerhighlightcolor get() = _slayerhighlightcolor
    val slayerhighlightwidth get() = _slayerhighlightwidth
    val slayerstats get() = _slayerstats
    val lasertimer get() = _lasertimer
    val vengdmg get() = _vengdmg
    val carrycounter get() = _carrycounter
    val carryvalue get() = _carryvalue
    val carrybosshighlight get() = _carrybosshighlight
    val carrybosscolor get() = _carrybosscolor
    val carrybosswidth get() = _carrybosswidth
    val carryclienthighlight get() = _carryclienthighlight
    val carryclientcolor get() = _carryclientcolor
    val carryclientwidth get() = _carryclientwidth
    val carrycountsend get() = _carrycountsend
    val keyhighlightcolor get() = _keyhighlightcolor
    val keyhighlightwidth get() = _keyhighlightwidth
    val cryptreminderdelay get() = _cryptreminderdelay
    val draftself get() = _draftself
    val autogetdraft get() = _autogetdraft
    val leapmessage get() = _leapmessage
    val boxstarmobscolor get() = _boxstarmobscolor
    val boxstarmobswidth get() = _boxstarmobswidth
    val entityhighlightplayercolor get() = _entityhighlightplayercolor
    val entityhighlightmobcolor get() = _entityhighlightmobcolor
    val entityhighlightanimalcolor get() = _entityhighlightanimalcolor
    val entityhighlightothercolor get() = _entityhighlightothercolor
    val entityhighlightwidth get() = _entityhighlightwidth
    val highlightlividcolor get() = _highlightlividcolor
    val highlightlividwidth get() = _highlightlividwidth
    val hidewronglivid get() = _hidewronglivid
    val highlightlividline get() = _highlightlividline

    init {
        configUI.registerListener("blockoverlayfill") { _blockoverlayfill = it as Boolean }
        configUI.registerListener("blockoverlaycolor") { _blockoverlaycolor = it as Color }
        configUI.registerListener("blockoverlaywidth") { _blockoverlaywidth = it as Double }
        configUI.registerListener("arrowpoison") { _arrowpoison = it as Boolean }
        configUI.registerListener("slayertimer") { _slayertimer = it as Boolean }
        configUI.registerListener("slayerhighlightcolor") { _slayerhighlightcolor = it as Color }
        configUI.registerListener("slayerhighlightwidth") { _slayerhighlightwidth = it as Double }
        configUI.registerListener("slayerstats") { _slayerstats = it as Boolean }
        configUI.registerListener("lasertimer") { _lasertimer = it as Boolean }
        configUI.registerListener("vengdmg") { _vengdmg = it as Boolean }
        configUI.registerListener("carrycounter") { _carrycounter = it as Boolean }
        configUI.registerListener("carryvalue") { _carryvalue = it as String }
        configUI.registerListener("carrybosshighlight") { _carrybosshighlight = it as Boolean }
        configUI.registerListener("carrybosscolor") { _carrybosscolor = it as Color }
        configUI.registerListener("carrybosswidth") { _carrybosswidth = it as Double }
        configUI.registerListener("carryclienthighlight") { _carryclienthighlight = it as Boolean }
        configUI.registerListener("carryclientcolor") { _carryclientcolor = it as Color }
        configUI.registerListener("carryclientwidth") { _carryclientwidth = it as Double }
        configUI.registerListener("carrycountsend") { _carrycountsend = it as Boolean }
        configUI.registerListener("keyhighlightcolor") { _keyhighlightcolor = it as Color }
        configUI.registerListener("keyhighlightwidth") { _keyhighlightwidth = it as Double }
        configUI.registerListener("cryptreminderdelay") { _cryptreminderdelay = it as Double }
        configUI.registerListener("draftself") { _draftself = it as Boolean }
        configUI.registerListener("autogetdraft") { _autogetdraft = it as Boolean }
        configUI.registerListener("leapmessage") { _leapmessage = it as String }
        configUI.registerListener("boxstarmobscolor") { _boxstarmobscolor = it as Color }
        configUI.registerListener("boxstarmobswidth") { _boxstarmobswidth = it as Double }
        configUI.registerListener("entityhighlightplayercolor") { _entityhighlightplayercolor = it as Color }
        configUI.registerListener("entityhighlightmobcolor") { _entityhighlightmobcolor = it as Color }
        configUI.registerListener("entityhighlightanimalcolor") { _entityhighlightanimalcolor = it as Color }
        configUI.registerListener("entityhighlightothercolor") { _entityhighlightothercolor = it as Color }
        configUI.registerListener("entityhighlightwidth") { _entityhighlightwidth = (it as Double).toFloat() }
        configUI.registerListener("highlightlividcolor") { _highlightlividcolor = it as Color }
        configUI.registerListener("highlightlividwidth") { _highlightlividwidth = (it as Double).toFloat() }
        configUI.registerListener("hidewronglivid") { _hidewronglivid = it as Boolean }
        configUI.registerListener("highlightlividline") { _highlightlividline = it as Boolean }
    }

    fun getValue(key: String): Any? = configUI.getConfigValue(key)
    inline fun <reified T> getValue(key: String, default: T): T = configUI.getConfigValue(key) as? T ?: default
}