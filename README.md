# Microcord
Microcord is a new Discord client for Java devices, currently in early development. Its goal is to be an evolution of [Discord J2ME](https://github.com/gtrxAC/discord-j2me) and [Droidcord](https://github.com/leap0x7b/Droidcord) with a focus on proxyless connectivity, user-friendly UI, and maintainability.

This repo is for the J2ME version. There is a separate repo for the [Android version](https://github.com/gtrxAC/Microcord-Android), but the plan is to eventually merge these into one project and potentially add support for other Java-based platforms like desktop.

![](/img/screenshots.png)

## Setup
The build system is based on my [j2me-template](https://github.com/gtrxAC/j2me-template/).

## Status
### Working
* Main menu with servers, DMs, and inbox
* Channel list
* Chat screen with pagination
* Token login
* Sending messages
* Sending replies
* Message timestamps
* Viewing replies
* Password login
* Profile pictures
* Theme settings (light/dark)
* Viewing attachments
* Message deleting

### Work in progress
* HTTP connection over BlueWAP
* Message editing

### Planned for first release
* Token file import

### Planned for later
* Threads
* Forums
* Emojis
* Reactions
* Embeds
* Text formatting (plan is to use [nnmarkdown](https://github.com/shinovon/nnmarkdown))
* Custom themes

### Not planned
* Gateway (not practical without a proxy, which is unsafe)
* Sending attachments (unsafe for third-party clients)
* Friend requests (unsafe)
* Joining servers (unsafe)

## Officially supported devices
Any device meeting the [system requirements](#system-requirements) should be able to run the app, but technical support will mainly be provided for these officially supported devices, and the app will be developed mainly with them in mind.
* Symbian devices with the [nnproject TLS patch](https://nnproject.cc/tls/) installed
* Sony Ericsson devices with DB2020 chipset or newer
  * This includes most devices from 2006 onwards; check your device [here](https://lpcwiki.miraheze.org/wiki/Sony_Ericsson_platform#List_of_phones)
  * Newer DB2010 devices may also work well (e.g. W300, W810, Z530)
* Nokia devices running Series 40 3rd Edition FP1 or later
  * TLS on these phones is slow, so it's recommended to find one with a faster CPU or use a Symbian/Sony Ericsson device instead
  * Series 40 Lite devices are not supported

## System requirements
| Specification | Minimum | Recommended |
|-|-|-|
| **Java ME support** | MIDP 2.0, CLDC 1.0 | MIDP 2.0, CLDC 1.0
| **Extra APIs** | none | JSR-75 FileConnection<br>JSR-82 Bluetooth API
| **JAR size limit** | 200 kB (may increase) | 1 MB
| **Heap size (RAM)** | 1 MB (may increase) | 2 MB
| **Display** | any color/grayscale screen | 128 × 160 or higher
| **Others** | none | TLS 1.2 support

## Build variants
Official builds are not yet available, this is preliminary information.

| Build | Description |
|-|-|
| **microcord** | Generic build for most devices
| **microcord_samsung** | For **Samsung and LG** touchscreen phones; hides virtual keypad
| **microcord_lite** | For devices that cannot run the generic build; lacks BT and file system functionality