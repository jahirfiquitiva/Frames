<img src="https://github.com/jahirfiquitiva/Frames/raw/master/art/app_logo.png" width="192" align="right" hspace="20" />

Frames
======

![API](https://img.shields.io/badge/API-21%2B-34bf49.svg)
[![GitHub Release](https://img.shields.io/github/v/release/jahirfiquitiva/Frames?label=Frames&sort=semver)](https://github.com/jahirfiquitiva/Frames/releases/latest)
[![Build Status](https://github.com/jahirfiquitiva/Frames/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/jahirfiquitiva/Frames/actions/workflows/build.yml)
[![Crowdin](https://badges.crowdin.net/Frames/localized.svg)](https://crowdin.com/project/Frames/invite)
[![GitHub Sponsor](https://img.shields.io/static/v1?label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=ff69b4)](https://github.com/sponsors/jahirfiquitiva)

Free, feature-rich, easily customizable Android dashboard for wallpapers apps

<a target="_blank" href="http://jahir.dev/gh-releases/frames">
<img src="https://jahir.dev/share/download_sample.svg?maxAge=432000" width="200"/>
</a>

<a target="_blank" href="https://jahir.dev/donate/">
<img src="https://jahir.dev/share/support_my_work.svg?maxAge=432000" width="200"/>
</a>

## Show some  :blue_heart:

[![GitHub stars](https://img.shields.io/github/stars/jahirfiquitiva/Frames.svg?style=social&label=Star)](https://github.com/jahirfiquitiva/Frames)
[![GitHub forks](https://img.shields.io/github/forks/jahirfiquitiva/Frames.svg?style=social&label=Fork)](https://github.com/jahirfiquitiva/Frames/fork)
[![GitHub watchers](https://img.shields.io/github/watchers/jahirfiquitiva/Frames.svg?style=social&label=Watch)](https://github.com/jahirfiquitiva/Frames)

[![Follow on GitHub](https://img.shields.io/github/followers/jahirfiquitiva.svg?style=social&label=Follow)](https://github.com/jahirfiquitiva)
[![Follow on Twitter](https://img.shields.io/twitter/follow/jahirfiquitiva.svg?style=social)](https://twitter.com/jahirfiquitiva)

---

# Previews

### Customizable styles

<p align="center">
<img src="/art/1.png" width="162" />
<img src="/art/2.png" width="162" />
<img src="/art/3.png" width="162" />
<img src="/art/4.png" width="162" />
<img src="/art/5.png" width="162" />
</p>

### Full-screen Viewer / Multiple themes

<p align="center">
<img src="/art/6.png" width="162" />
<img src="/art/7.png" width="162" />
<img src="/art/8.png" width="162" />
<img src="/art/9.png" width="162" />
</p>

### Credits / Settings

<p align="center">
<img src="/art/10.png" width="162" />
<img src="/art/11.png" width="162" />
</p>

---

# Features:

- Material Design dashboard.
- License Checker.
- Donations.
- [OneSignal](https://onesignal.com/) notifications ready.
- Cloud based (only) wallpapers.
- App can work offline.
- Categories section.
- Favorites section.
- Wallpapers can be applied and downloaded.
- Wallpapers include a full-screen viewer with zooming capabilities and detailed info viewer.
- Credits section.
- Settings section with these options:
	- Option to change app theme (Light, Dark, System, AMOLED).
	- Option to color navigation bar (Lollipop+).
	- Option to clear app cache.
- Optional [One4Home](https://one4home.app/developers/wallpaper-bridge) launcher integration.
- Tablet layouts.
- Lots of customizations.
- Works with Android 5.0 and newer.

## Help translating

:page_facing_up: Help making Frames available in more languages. [Click here to go to the translation site](https://crowdin.com/project/Frames/invite)

## Changelog

:radio_button: You can find it in the [Releases page](https://github.com/jahirfiquitiva/Frames/releases)

---

# Including in your project

Frames is available via Maven, so getting it as simple as adding it as a dependency, like this:

1. Add `MavenCentral` and `Jitpack` to your root `build.gradle` file
   
   ```gradle
   allprojects {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
   }
   ```
2. Add the dependency in your project `build.gradle` file
   
   ```gradle
   dependencies {
    compile('dev.jahir:Frames:{latest version}@aar') {
        transitive = true
    }
   }
   ```
   
   where `{latest version}` corresponds to published version in   [![GitHub Release](https://img.shields.io/github/v/release/jahirfiquitiva/Frames?label=Frames&sort=semver)](https://github.com/jahirfiquitiva/Frames/releases/latest)

## How to implement

:page_with_curl: Everything you need to know can be found in the **[Wiki Docs](https://github.com/jahirfiquitiva/Frames/wiki/)**

### Optional One4Home launcher integration

When the [One4Home](https://one4home.app/developers/wallpaper-bridge) launcher is installed, Settings shows a "Share with One4Home" switch. Once the user turns it on and approves the connection in One4Home, every wallpaper applied to the Home Screen (or Home and Lock Screens) is also handed to One4Home, so its effects can match the wallpaper. Nothing is shared before that, and never for Lock Screen-only applies. It uses a `FileProvider` limited to `cache/one4home-bridge/`, without a network call or a background service.

To turn it off in your dashboard, add this to `frames_setup.xml`:

```xml
<bool name="enable_one4home_bridge">false</bool>
```

## Still need help :question:
Just join our Discord Server and make a post. We'll help you as soon as possible. [![Join chat on Discord](https://badgen.net/badge/discord/join%20chat/7289DA?icon=discord)](https://discord.gg/78h7xgj)

---

# Developed by

### [Jahir Fiquitiva](https://jahir.dev/)

[![Follow on GitHub](https://img.shields.io/github/followers/jahirfiquitiva.svg?style=social&label=Follow)](https://github.com/jahirfiquitiva)
[![Follow on Twitter](https://img.shields.io/twitter/follow/jahirfiquitiva.svg?style=social)](https://twitter.com/jahirfiquitiva)

If you found this app/library helpful and want to thank me, you can:

<a target="_blank" href="https://jahir.dev/donate/">
<img src="https://jahir.dev/share/support_my_work.svg?maxAge=432000" width="200"/>
</a>

**Thanks in advance!** :pray:

## Special thanks 🙌

- [Eduardo Pratti](https://pratti.design/) 🎨
- [Sherry Sabatine](https://www.photography-by-sherry.com/) 💵
- [Allan Wang](https://www.allanwang.ca/) 💻
- [James Fenn](https://theandroidmaster.github.io/) 🔌
- [Maximilian Keppeler](https://twitter.com/maxKeppeler) 🔌
- [Sasi Kanth](https://twitter.com/its_sasikanth) 🔌
- [Alexandre Piveteau](https://github.com/alexandrepiveteau) 💻
- [Lukas Koller](https://github.com/kollerlukas) 🔌
- [Patryk Goworowski](https://twitter.com/pgoworowski) 🎨
- [Patryk Michalik](https://patrykmichalik.com) 💻
- [Jackson Hayes](https://jacksonhayes.xyz/) 📖
- [Kevin Aguilar](https://twitter.com/kevttob) 🎨
- [Anthony Nguyen](https://twitter.com/link6155) 🎨

---

# License

This app is shared under the CreativeCommons Attribution-ShareAlike license.

    Copyright © 2020 Jahir Fiquitiva
    
    Licensed under the CreativeCommons Attribution-ShareAlike 
    4.0 International License. You may not use this file except in compliance 
    with the License. You may obtain a copy of the License at
    
       http://creativecommons.org/licenses/by-sa/4.0/legalcode
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## Library source

As you may know, the [library source](https://github.com/jahirfiquitiva/Frames/tree/master) is open-source. This means that you can fork it and do your own modifications, but it has some conditions:

When using the [library source](https://github.com/jahirfiquitiva/Frames/tree/master), anything from it: errors, crashes, issues, etc. including successful builds, must be done completely by yourself and under your own risk and responsibility. I **will not** provide any help/support when using the [library source](https://github.com/jahirfiquitiva/Frames/tree/master).

Finally, be sure your projects comply with the [license previously mentioned](https://github.com/jahirfiquitiva/Frames#license). Otherwise I will be taking the required legal actions. I hope you understand.
