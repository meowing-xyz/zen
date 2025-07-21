package meowing.zen.config

import meowing.zen.config.ui.ConfigUI
import org.lwjgl.input.Keyboard
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
    private var _ragparty = false
    private var _armorhudvert = false
    private var _firefreezeoverlaycolor = Color(0, 255, 255, 127)
    private var _chatcleanerkey = Keyboard.KEY_H
    private var _customtintcolor = Color(0, 255, 255, 127)
    private var _carrysendmsg = false

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
    val ragparty get() = _ragparty
    val armorhudvert get() = _armorhudvert
    val firefreezeoverlaycolor get() = _firefreezeoverlaycolor
    val chatcleanerkey get() = _chatcleanerkey
    val customtintcolor get() = _customtintcolor
    val carrysendmsg get() = _carrysendmsg

    init {
        configUI
            .registerListener("blockoverlayfill") { _blockoverlayfill = it as Boolean }
            .registerListener("blockoverlaycolor") { _blockoverlaycolor = it as Color }
            .registerListener("blockoverlaywidth") { _blockoverlaywidth = it as Double }
            .registerListener("arrowpoison") { _arrowpoison = it as Boolean }
            .registerListener("slayertimer") { _slayertimer = it as Boolean }
            .registerListener("slayerhighlightcolor") { _slayerhighlightcolor = it as Color }
            .registerListener("slayerhighlightwidth") { _slayerhighlightwidth = it as Double }
            .registerListener("slayerstats") { _slayerstats = it as Boolean }
            .registerListener("lasertimer") { _lasertimer = it as Boolean }
            .registerListener("vengdmg") { _vengdmg = it as Boolean }
            .registerListener("carrycounter") { _carrycounter = it as Boolean }
            .registerListener("carryvalue") { _carryvalue = it as String }
            .registerListener("carrybosshighlight") { _carrybosshighlight = it as Boolean }
            .registerListener("carrybosscolor") { _carrybosscolor = it as Color }
            .registerListener("carrybosswidth") { _carrybosswidth = it as Double }
            .registerListener("carryclienthighlight") { _carryclienthighlight = it as Boolean }
            .registerListener("carryclientcolor") { _carryclientcolor = it as Color }
            .registerListener("carryclientwidth") { _carryclientwidth = it as Double }
            .registerListener("carrycountsend") { _carrycountsend = it as Boolean }
            .registerListener("keyhighlightcolor") { _keyhighlightcolor = it as Color }
            .registerListener("keyhighlightwidth") { _keyhighlightwidth = it as Double }
            .registerListener("cryptreminderdelay") { _cryptreminderdelay = it as Double }
            .registerListener("draftself") { _draftself = it as Boolean }
            .registerListener("autogetdraft") { _autogetdraft = it as Boolean }
            .registerListener("leapmessage") { _leapmessage = it as String }
            .registerListener("boxstarmobscolor") { _boxstarmobscolor = it as Color }
            .registerListener("boxstarmobswidth") { _boxstarmobswidth = it as Double }
            .registerListener("entityhighlightplayercolor") { _entityhighlightplayercolor = it as Color }
            .registerListener("entityhighlightmobcolor") { _entityhighlightmobcolor = it as Color }
            .registerListener("entityhighlightanimalcolor") { _entityhighlightanimalcolor = it as Color }
            .registerListener("entityhighlightothercolor") { _entityhighlightothercolor = it as Color }
            .registerListener("entityhighlightwidth") { _entityhighlightwidth = (it as Double).toFloat() }
            .registerListener("highlightlividcolor") { _highlightlividcolor = it as Color }
            .registerListener("highlightlividwidth") { _highlightlividwidth = (it as Double).toFloat() }
            .registerListener("hidewronglivid") { _hidewronglivid = it as Boolean }
            .registerListener("highlightlividline") { _highlightlividline = it as Boolean }
            .registerListener("ragparty") { _ragparty = it as Boolean }
            .registerListener("armorhudvert") { _armorhudvert = it as Boolean }
            .registerListener("firefreezeoverlaycolor") { _firefreezeoverlaycolor = it as Color }
            .registerListener("chatcleanerkey") { _chatcleanerkey = (it as Number).toInt() }
            .registerListener("customtintcolor") { _customtintcolor = it as Color }
            .registerListener("carrysendmsg") { _carrysendmsg = it as Boolean }
    }

    fun getValue(key: String): Any? = configUI.getConfigValue(key)
    inline fun <reified T> getValue(key: String, default: T): T = configUI.getConfigValue(key) as? T ?: default
}