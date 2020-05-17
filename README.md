# WINESAPS
Winesaps client

![Winesaps](https://winesaps.com/img/winesaps.png)

**Winesaps client** is an official open source GUI for **Winesaps** - online multiplayer game for Desktop and Android.

Web page: <https://winesaps.com>

## Information ##
Tools:
* Server is written in [Go](https://golang.org)
* Client is written in [Java](https://www.java.com) with [LibGDX](https://libgdx.badlogicgames.com) game library

Supported platforms:
* Android (v.2.2+)
* Windows (x86, x64)
* Linux (x86, x64)
* Mac OS (x86, x64)

Supported languages:
* English
* Русский
* Español
* Português
* Français

Please contact us if you find any typos or translation mistakes.

Technical details and further information please see on [Wiki Page](https://github.com/mitrakov/winesaps/wiki)

## Contributing ##
For those who want to become a contributor.

We consider the following applicants on nonprofit basis:
* Programmer
* Artist (with spritesheet animation skills)
* GUI designer
* Game designer
* Promoter

## Contacts ##
Feel free to contact us via E-Mail: `mitrakov-artem@yandex.ru`

## How to compile
- important! add `keystore.properties` file in the project root (data must contain keyAlias, keyPassword, storeFile, storePassword)
- use `gradlew compile` for compilation
- use `desktop:other:run` gradle task for Desktop deb build
- use `desktop:other:dist` gradle task for Desktop release build
- use `android:other:run` gradle task for Android dev build
- use `android:build:assembleRelease` gradle task for Android release build

---
© 2017-2020, Mitrakov Artem, Russian Federation
