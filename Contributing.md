### Want to support the mod?

Zen is developed and maintained by dedicated volunteers who contribute their free time to improve and maintain the mod. This project is entirely non-profit—we don't seek monetary gain, and any revenue generated (such as from Modrinth) is donated to charities worldwide.

Here's how you can support us:
- **Star the repository** to show your appreciation
- **Contribute code** if you have development skills and want to help add features
- **Share feedback** and suggestions to help improve the mod

## How to Contribute

### Reporting Bugs

If you find a bug in the mod, please report it by pinging a maintainer in our [Discord](https://discord.com/invite/KPmHQUC97G) along with:
- A brief description of what happens
- What should happen instead
- Steps to reproduce (if known)

### Suggesting Features

Have an idea for a new feature? Share it in our [Discord](https://discord.com/invite/KPmHQUC97G) and include:
- A clear and descriptive title
- A detailed description of the proposed feature
- Potential use cases or benefits

### Contributing Code

We welcome contributions from everyone! Follow these steps to get started:

#### 1. Fork the Repository
Navigate to our [GitHub repository](https://github.com/meowing-xyz/zen) and click the **Fork** button to create a copy under your account.

#### 2. Clone Your Fork
```bash
git clone https://github.com/YOUR_USERNAME/zen.git
cd zen
```

#### 3. Create a Feature Branch
```bash
git checkout -b feature/your-feature-name
```
Use descriptive names like `feature/new-overlay` or `bugfix/memory-leak`.

#### 4. Make Your Changes
Edit the code, then stage and commit your changes:
```bash
git add .
git commit -m "Add: brief description of your changes"
```

#### 5. Push to GitHub
```bash
git push origin feature/your-feature-name
```

#### 6. Create a Pull Request
1. Go to the [Zen repository](https://github.com/meowing-xyz/zen)
2. Click **Pull requests** → **New pull request**
3. Select your branch and describe your changes
4. Submit the pull request

#### 7. Address Feedback
Maintainers may request changes. Update your branch and push new commits—the pull request will update automatically.

## Helpful Information for Contributors

There is a certain style of code that we prefer and many utilities exist to make your coding experience easier.

### EventBus
```kotlin
// Feature class specific
register<Event>(priority = 1000) {
    if (condition) {
        fn()
    }
}

createCustomEvent<RenderEvent>("hudRender", priority = 500) {
    // custom HUD rendering code
}

registerEvent("hudRender") // enable the custom event
unregisterEvent("hudRender") // disable the custom event

// Global config-based registration
configRegister<Event>("configKey", priority = 1000) {
    // only runs when config value (boolean) is enabled
}

configRegister<Event>("configKey", enabledIndices = setOf(1, 2, 3)) {
    // only runs when hotkeys config value (Dropdown) has indices 1, 2, or 3 selected
}

configRegister<Event>("configKey", requiredIndex = 2) {
    // only runs when config value (MultiCheckBox) contains index 2
}

EventBus.register<Event> (priority = 1000, {
    // add = false means it does not automatically get registered, and you have to register/unregister
}, add = false)
```

### Loops
```kotlin
// Feature class specific - Use setupLoops for automatic registration/unregistration
object MyFeature : Feature("configKey") {
    override fun initialize() {
        setupLoops {
            // Client tick loops (20 TPS)
            loop<ClientTick>(intervalTicks = 20) {
                player?.let { p ->
                    if (p.health < 10) ChatUtils.addMessage("Low health!")
                }
            }

            // Server tick loops
            loop<ServerTick>(intervalTicks = 100) { 
                fn()
            }

            // Timer loops (millisecond precision)
            loop<Timer>(intervalMs = 1000) {
                fn()
            }

            // Dynamic loops with variable delays - it checks delay on every loop
            loopDynamic<ClientTick>(
                delay = { if (variable) 1L else 20L },
                stop = { !isEnabled() }
            ) {
                fn()
            }
        }
    }
}
```
**Note:** Always use `setupLoops { }` in your feature's initialize block. This ensures loops are automatically started when the feature is enabled and stopped when disabled, preventing memory leaks and unnecessary processing.
### Config
```kotlin
object MyFeature : Feature("configKey") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Category", "Section/Feature", "Sub categories" /* OR null */, ConfigElement(
                "configKey",
                "Title" /* OR null */,
                ElementType
            ), isSectionToggle = Boolean)
    }
}
```