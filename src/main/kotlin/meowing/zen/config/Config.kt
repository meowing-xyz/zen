package meowing.zen.config

import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import java.awt.Color

fun ZenConfig(): ConfigUI {
    return ConfigUI("ZenConfig")
        .addElement("General", "Clean chat", ConfigElement(
            "guildmsg",
            "Clean guild messages",
            "Reformats the Guild messages that you receive.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Clean chat", ConfigElement(
            "partymsg",
            "Clean party messages",
            "Reformats the party messages that you receive.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Clean chat", ConfigElement(
            "guildjoinleave",
            "Clean guild join messages",
            "Reformats the Guild join/leave message that you receive.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Clean chat", ConfigElement(
            "friendjoinleave",
            "Clean friend join messages",
            "Reformats the Friend join/leave message that you receive.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Clean chat", ConfigElement(
            "betterah",
            "Better AH messages",
            "Reformats the auction house messages that you receive.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Clean chat", ConfigElement(
            "betterbz",
            "Better BZ messages",
            "Reformats the bazaar messages that you receive.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Custom models", ConfigElement(
            "customsize",
            "Custom player size",
            "Custom model size for your player.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Custom models", ConfigElement(
            "customX",
            "Custom X size",
            "Custom model X value for your player.",
            ElementType.Slider(0.1, 10.0, 1.0, true),
            { config -> config["customsize"] as? Boolean == true }
        ))
        .addElement("General", "Custom models", ConfigElement(
            "customY",
            "Custom Y size",
            "Custom model Y value for your player.",
            ElementType.Slider(0.1, 10.0, 1.0, true),
            { config -> config["customsize"] as? Boolean == true }
        ))
        .addElement("General", "Custom models", ConfigElement(
            "customZ",
            "Custom Z size",
            "Custom model Z value for your player.",
            ElementType.Slider(0.1, 10.0, 1.0, true),
            { config -> config["customsize"] as? Boolean == true }
        ))
        .addElement("General", "Misc", ConfigElement(
            "nohurtcam",
            "Disable hurt camera",
            "Disables the camera bob when you get hit",
            ElementType.Switch(false)
        ))
        .addElement("General", "Block overlay", ConfigElement(
            "blockoverlay",
            "Block overlay",
            "Highlights your block with custom color",
            ElementType.Switch(false)
        ))
        .addElement("General", "Block overlay", ConfigElement(
            "blockoverlayfill",
            "Filled block overlay",
            "Enable to render filled block overlay",
            ElementType.Switch(false),
            { config -> config["blockoverlay"] as? Boolean == true }
        ))
        .addElement("General", "Block overlay", ConfigElement(
            "blockoverlaycolor",
            "Block overlay color",
            null,
            ElementType.ColorPicker(Color(0, 255, 255, 127)),
            { config -> config["blockoverlay"] as? Boolean == true }
        ))
        .addElement("General", "Block overlay", ConfigElement(
            "blockoverlaywidth",
            "Block overlay width",
            null,
            ElementType.Slider(1.0, 10.0, 2.0, false),
            { config -> config["blockoverlay"] as? Boolean == true }
        ))
        .addElement("General", "Entity highlight", ConfigElement(
            "entityhighlight",
            "Entity highlight",
            "Highlights the entity you are looking at",
            ElementType.Switch(false)
        ))
        .addElement("General", "Entity highlight", ConfigElement(
            "entityhighlightplayercolor",
            "Player color",
            "Color for highlighted players",
            ElementType.ColorPicker(Color(0, 255, 255, 255)),
            { config -> config["entityhighlight"] as? Boolean == true }
        ))
        .addElement("General", "Entity highlight", ConfigElement(
            "entityhighlightmobcolor",
            "Mob color",
            "Color for highlighted mobs",
            ElementType.ColorPicker(Color(255, 0, 0, 255)),
            { config -> config["entityhighlight"] as? Boolean == true }
        ))
        .addElement("General", "Entity highlight", ConfigElement(
            "entityhighlightanimalcolor",
            "Animal color",
            "Color for highlighted animals",
            ElementType.ColorPicker(Color(0, 255, 0, 255)),
            { config -> config["entityhighlight"] as? Boolean == true }
        ))
        .addElement("General", "Entity highlight", ConfigElement(
            "entityhighlightothercolor",
            "Other entity color",
            "Color for other highlighted entities",
            ElementType.ColorPicker(Color(255, 255, 255, 255)),
            { config -> config["entityhighlight"] as? Boolean == true }
        ))
        .addElement("General", "Entity highlight", ConfigElement(
            "entityhighlightwidth",
            "Entity highlight width",
            "Width of the entity highlight outline",
            ElementType.Slider(1.0, 10.0, 2.0, false),
            { config -> config["entityhighlight"] as? Boolean == true }
        ))
        .addElement("General", "World age", ConfigElement(
            "worldagechat",
            "World age message",
            "Sends the world age in your chat.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Arrow poison tracker", ConfigElement(
            "arrowpoison",
            "Arrow poison tracker",
            "Tracks the arrow poisons inside your inventory.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "General", ConfigElement(
            "slayertimer",
            "Slayer timer",
            "Slayer kill and spawn timer",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "General", ConfigElement(
            "slayerhighlight",
            "Slayer highlight",
            "Highlights your slayer boss",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "General", ConfigElement(
            "slayerhighlightcolor",
            "Slayer highlight color",
            "Slayer highlight color",
            ElementType.ColorPicker(Color(0, 255, 255, 127)),
            { config -> config["slayerhighlight"] as? Boolean == true }
        ))
        .addElement("Slayers", "General", ConfigElement(
            "slayerhighlightwidth",
            "Slayer highlight width",
            null,
            ElementType.Slider(1.0, 10.0, 2.0, false),
            { config -> config["slayerhighlight"] as? Boolean == true }
        ))
        .addElement("Slayers", "General", ConfigElement(
            "slayerstats",
            "Slayer stats",
            "Shows your average kill time, bosses/hr and total kills for the session.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Blaze", ConfigElement(
            "vengdmg",
            "Vengeance damage",
            "Sends your vengeance damage to the chat.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Blaze", ConfigElement(
            "vengtimer",
            "Vengeance timer",
            "Timer until vengeance procs.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carrycounter",
            "Carry counter",
            "Counts the carries automatically",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carrycountsend",
            "Send count",
            "Sends the count in party chat",
            ElementType.Switch(true)
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carryvalue",
            "Carry value",
            "The values for the auto-add from trade in carry counter",
            ElementType.TextInput("1.3", "10")
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carrybosshighlight",
            "Carry boss highlight",
            "Highlights your client's slayer boss",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carrybosscolor"
            , "Carry boss highlight color",
            null,
            ElementType.ColorPicker(Color(0, 255, 255, 127))
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carrybosswidth",
            "Carry boss highlight width",
            "Width for the carry boss outline",
            ElementType.Slider(2.0, 1.0, 10.0, false)
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carryclienthighlight",
            "Carry client highlight",
            "Highlights your client",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carryclientcolor",
            "Carry client highlight color",
            null,
            ElementType.ColorPicker(Color(0, 255, 255, 127))
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carryclientwidth",
            "Carry client highlight width",
            "Width for the carry client outline",
            ElementType.Slider(2.0, 1.0, 10.0, false)
        ))
        .addElement("Meowing", "Auto meow", ConfigElement(
            "automeow",
            "Auto meow",
            "Automatically responds with a random meow message when someone says \"meow\".",
            ElementType.Switch(false)
        ))
        .addElement("Meowing", "Meow Sounds", ConfigElement(
            "meowdeathsounds",
            "Meow death sounds",
            "Plays cat sounds when a mob dies and spawns some particles.",
            ElementType.Switch(false)
        ))
        .addElement("Meowing", "Meow Sounds", ConfigElement(
            "meowsounds",
            "Meow sounds",
            "Plays cat sounds when someone's message includes \"meow\".",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Blood helper", ConfigElement(
            "bloodtimer",
            "Blood camp helper",
            "Sends information related to blood camping.",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Terminals", ConfigElement(
            "termtracker",
            "Terminal tracker",
            "Tracks the terminals/levers/devices that your party does.",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Keys", ConfigElement(
            "keyalert",
            "Key spawn alert",
            "Displays a title when the wither/blood key spawns",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Keys", ConfigElement(
            "keyhighlight",
            "Key highlight",
            "Highlights the wither/blood key",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Keys", ConfigElement(
            "keyhighlightcolor",
            "Key highlight color",
            null,
            ElementType.ColorPicker(Color(0, 255, 255, 127)),
            { config -> config["keyhighlight"] as? Boolean == true }
        ))
        .addElement("Dungeons", "Keys", ConfigElement(
            "keyhighlightwidth",
            "Key highlight width",
            "Width for the key highlight",
            ElementType.Slider(2.0, 1.0, 10.0, false),
            { config -> config["keyhighlight"] as? Boolean == true }
        ))
        .addElement("Dungeons", "Party finder", ConfigElement(
            "partyfindermsgs",
            "Party finder messages",
            "Custom party finder messages.",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Misc", ConfigElement(
            "serverlagtimer",
            "Server lag timer",
            "Amount of difference between the client ticks and the server ticks",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Crypt reminder", ConfigElement(
            "cryptreminder",
            "Crypt reminder",
            "Shows a notification about the current crypt count if all 5 aren't done",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Crypt reminder", ConfigElement(
            "cryptreminderdelay",
            "Crypt reminder delay",
            "Time in minutes",
            ElementType.Slider(1.0, 5.0, 2.0, false),
            { config -> config["cryptreminderdelay"] as? Boolean == true }
        ))
        .addElement("Dungeons", "Fire freeze", ConfigElement(
            "firefreeze",
            "Fire freeze timer",
            "Time until you should activate fire freeze",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Architect Draft", ConfigElement(
            "architectdraft",
            "Architect draft message",
            "Automatically sends a message in your chat that you can click to get a draft from your sacks on puzzle fail",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Architect Draft", ConfigElement(
            "selfdraft",
            null,
            "Only send when you fail a puzzle",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Architect Draft", ConfigElement(
            "autogetdraft",
            "Auto Architect draft",
            "Automatically runs the command to get a draft into your inventory on puzzle fail",
            ElementType.Switch(false)
        ))
        .addElement("No clutter", "Dungeons", ConfigElement(
            "hidedamage",
            "Hide damage in dungeons",
            "Hides the damage nametag in dungeons.",
            ElementType.Switch(false)
        ))
        .addElement("No clutter", "Dungeons", ConfigElement(
            "hidenonstarmobs",
            "Hide non-starred mob nametags",
            "Hides non-starred mob's nametags in Dungeons",
            ElementType.Switch(false)
        ))
        .addElement("No clutter", "General", ConfigElement(
            "hidedeathanimation",
            "Hide death animation",
            "Cancels the death animation of mobs.",
            ElementType.Switch(false)
        ))
        .addElement("No clutter", "General", ConfigElement(
            "hidefallingblocks",
            "Hide falling blocks",
            "Cancels the animation of the blocks falling",
            ElementType.Switch(false)
        ))
        .addElement("No clutter", "General", ConfigElement(
            "nothunder",
            "Hide thunder",
            "Cancels thunder animation and sound.",
            ElementType.Switch(false)
        ))
        .addElement("No clutter", "General", ConfigElement(
            "noendermantp",
            "No enderman TP",
            "Disables endermen visually teleporting around.",
            ElementType.Switch(false)
        ))
}