# Installation Guide

### <div style="text-align: center;">[Download (Codeberg)](https://codeberg.org/gtrxAC/Discofi/releases/latest) - [Download (GitHub)](https://github.com/gtrxAC/Discofi/releases/latest)</div>

This is a guide for installing Discofi (J2ME) on supported devices. Note that the setup is a bit more involved compared to other Java apps. The steps may be familiar to users of old Discord J2ME with Direct connection. Also see the list of [officially supported devices](README.md#officially-supported-devices).

> [!IMPORTANT]
> To avoid account restrictions, **use the "Direct" connection option and login with email and password whenever possible.** If you get a CAPTCHA when logging in, you may need to enable 2FA on your account and try the login again **while connected to a Wi-Fi network that you have previously used Discord on**.
> 
> *If you need to use Wi-Fi and your device does not support it, log in on a device that supports Wi-Fi (e.g. another J2ME phone or an Android device with [Discofi-Android](https://github.com/gtrxAC/Discofi-Android)) and then send the token to the other device.*
> 
> Do **not** follow old Discord J2ME setup guides that ask you to get your token from browser dev tools.

**Install the app by following the appropriate steps for your device below:**

## Symbian S60, S80, and Symbian^3
***Any Symbian (non-UIQ) phone with MIDP 2.0 support. S60v3 and Symbian^3 are recommended.***
1. If your device runs **Symbian 9.1 (S60v3) or above**, make sure it is jailbroken. To jailbreak the device, you can follow any online guide, such as the [LPCwiki guide](https://lpcwiki.miraheze.org/wiki/Jailbreaking_S60_devices).
2. Install the [nnproject TLS 1.2 patch](https://nnproject.cc/tls/) onto your device.
3. Make sure the phone's date and time are set correctly.
4. Install Discofi onto your device.
5. Recommended to prevent access point popups: go to the **App. mgr.** (depending on the phone, it may be in the Installations or Tools folder), select Discofi, and define an access point for it.

## Symbian UIQ3
***Any Symbian UIQ3 phone (mostly Sony Ericsson Symbian phones like the P990).***
1. Install the TLS 1.2 patch onto your device using [UIQ3 Hack Tool](https://www.avalibeyaz.com/uiq3/).
2. Install Discofi onto your device.

## Sony Ericsson
***Sony Ericsson phones running Java Platform 6 or newer. This includes most models released since 2006, but not low-end "ODM" models. (check your device's Java Platform [here](https://en.wikipedia.org/wiki/Sony_Ericsson_Java_Platform))***

To install Discofi on Sony Ericsson devices, a **data cable** for your device is required (usually DCU-60, DCU-65, or equivalent).

Make sure the cable is capable of transferring data. Some cables are only for charging as they only have two gold contacts on the phone connector's side. You'll need a cable that has at least four contacts.

Discofi requires a **certificate** to be installed onto the phone to allow its installation. The certificate must be installed with a PC program that can manage the phone's internal file system. In this guide, we will use Far Manager with SEFP2.

1. Download Far Manager [here](https://drive.google.com/file/d/13NFVz44Y6Z3NyKdByX5tvF0y6jm9wT1U/view?usp=sharing).
2. Extract the downloaded archive. Use the password **Far123**.
3. Open **Far.exe**.
4. Press **11 Plugin** on the bottom right.
5. Select **SEFP2**.
6. Follow the on-screen instructions to connect the phone. You may get a warning that the files on the phone cannot be read. This warning can be ignored.
7. Download [exp.cer](https://gtrxac.fi/fh/exp.cer) and [default_upgrade.xml](https://gtrxac.fi/default_upgrade.xml") to your computer.
8. Copy these two files to the **/tpa/preset/default** folder on the phone.
9. Disconnect and reboot the phone.
10. Make sure the phone's date and time are set correctly.
11. Download Discofi onto the phone. This *must* be done by downloading a **signed JAD (.sjad)** file via the phone's browser. You can get a signed JAD by uploading the JAR to [gtrxac.fi/fh](https://gtrxac.fi/fh).

## Nokia Series 40
***Nokia phones running S40 3rd Edition Feature Pack 1 or newer. This includes most models released since 2006, but not low-end "DCT4" models running S40 Lite. (check your device's S40 version [here](https://lpcwiki.miraheze.org/wiki/Series_40#List_of_phones))***

Discofi requires a **certificate** to be installed onto the phone to allow its installation. The certificate can be installed with an Android device using aNokiCert (recommended), a Windows PC using MobiMB, or a Linux/Windows PC using Gammu.

After installing the certificate, download Discofi onto the phone. This *must* be done by downloading a **signed JAD (.sjad)** file via the phone's browser. You can get a signed JAD by uploading the JAR to [gtrxac.fi/fh](https://gtrxac.fi/fh).

### aNokiCert method (Android)
1. Download the [certificate](https://gtrxac.fi/fh/exp.cer) onto your Android device.
2. Install [aNokiCert](https://gtrxac.fi/j2me/anokicert.apk) onto your Android device. Newer Android devices may restrict the installation of older apps. In such cases, you can try bypassing the restriction via ADB or by using an older device.
3. Make sure Bluetooth on the Nokia phone is enabled and set to visible.
4. Open aNokiCert and press the search icon on the top right. This will connect the Android device to your Nokia phone using Bluetooth.
5. Select **Choose certificate** and find the downloaded certificate file.
6. Press **Install**.

### MobiMB/Gammu method (PC)
1. Download the [exp.cer](https://gtrxac.fi/fh/exp.cer) and [ext_info.sys](https://gtrxac.fi/fh/ext_info.sys) files onto your PC.
2. Using MobiMB, Gammu, or another program that can manage the file system of Nokia phones, copy both downloaded files to the **C:/predefhiddenfolder/certificates/user** folder on the phone.

## Other/unsupported devices
Discofi may be directly installed on the device like any other Java app (JAR), though note that **Direct connection may be unavailable** in this case, so you may have to connect to Discord using **Bluetooth tethering** or proxy (not recommended).

**The Discofi team is not responsible for account restrictions, bans, or connection errors caused by using a proxy.**

Please make sure your device meets the [system requirements](/README.md#system-requirements). Meeting the minimum requirements means the app will probably install and launch without problems, but it will not guarantee a good user experience.

Make sure you **download the correct build** for your device:

| Build | Description |
|-|-|
| **discofi** | Generic build for most devices
| **discofi_samsung** | For **Samsung and LG** touchscreen phones; hides virtual keypad
| **discofi_lite** | For devices that cannot run the generic build; lacks BT and file system functionality
