# Microcord
Microcord is a new Discord client for Java devices. Currently only J2ME is supported, but the plan is to add support for other Java platforms like Android and desktop.

## Setup
The build system is based on my [j2me-template](https://github.com/gtrxAC/j2me-template/).

## Status
### Working
* Main menu with servers, DMs, and inbox
* Channel list
* Chat screen with pagination
* Token login
* Sending messages

### Planned for first release
* Sending replies
* Message editing/deleting
* Message timestamps
* Viewing replies/attachments
* Password login
* Token file import
* HTTP connection over BlueWAP

### Not implemented
* Emojis, reactions
* Gateway
* Sending attachments
* etc.

## Officially supported devices
Any device meeting the [system requirements](#system-requirements) should be able to run the app, but technical support will mainly be provided for these officially supported devices, and the app will be developed mainly with them in mind.
* Nokia devices running Series 40 3rd Edition FP1 or later
  * Series 40 Lite devices are not supported
* Symbian devices that support the [nnproject TLS patch](https://nnproject.cc/tls/)
  * Note that as of writing this, Symbian 8.0 (e.g. Nokia 6630/6680) is not supported.
* Sony Ericsson devices with DB2020 chipset or newer
  * This includes most devices from 2006 onwards; check your device [here](https://lpcwiki.miraheze.org/wiki/Sony_Ericsson_platform#List_of_phones)

## System requirements
| Specification | Minimum | Recommended |
|-|-|-|
| **Java ME support** | MIDP 2.0, CLDC 1.0 | MIDP 2.0, CLDC 1.0
| **Extra APIs** | none | JSR-75 FileConnection<br>JSR-82 Bluetooth API
| **JAR size limit** | 128 kB (may increase) | 1 MB
| **Heap size (RAM)** | untested | 2 MB
| **Display** | any color/grayscale screen | 128 × 160 or higher
| **Others** | none | TLS 1.2 support

## Build variants
Official builds are not yet available, this is preliminary information.

| Build | Description |
|-|-|
| **microcord** | Generic build for most devices
| **microcord_samsung** | For **Samsung and LG** touchscreen phones; hides virtual keypad
| **microcord_lite** | For devices that cannot run the generic build; lacks BT and file system functionality