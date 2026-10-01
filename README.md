# Microcord
Microcord is a new Discord client for Java devices. Currently only J2ME is supported, but the plan is to add support for other Java platforms like Android and desktop.

## Officially supported devices
Any device meeting the [system requirements](#system-requirements) should be able to run the app, but technical support will mainly be provided for officially supported devices, and the app will be developed mainly with these devices in mind.
* Nokia devices running Series 40 3rd Edition FP1 or later
  * Series 40 Lite devices are not supported
* Symbian devices that support the [nnproject TLS patch](https://nnproject.cc/tls/)
  * Note that as of writing this, Symbian 8.0 (e.g. Nokia 6630/6680) is not supported.
* Sony Ericsson devices with DB3350 chipset (Aino, Cedar, Elm, Hazel, Xperia Pureness, Yari, Zylo)

## System requirements
| Specification | Minimum | Recommended |
|-|-|-|
| **Java ME support** | MIDP 2.0, CLDC 1.0 | MIDP 2.0, CLDC 1.0
| **Extra APIs** | none | JSR-75 FileConnection<br>JSR-82 Bluetooth API
| **JAR size limit** | 100 kB (may increase) | 1 MB
| **Heap size (RAM)** | untested | 2 MB
| **Display** | any color screen | 128 × 160 or higher
| **Others** | none | TLS 1.2 support