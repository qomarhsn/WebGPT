# WebGPT

**WebGPT** is a lightweight, open source WebView-based Android app for [ChatGPT](https://chatgpt.com), forked from the original project [gptAssist](https://github.com/woheller69/gptAssist).

![Release workflow](https://github.com/qomarhsn/WebGPT/actions/workflows/main.yml/badge.svg)

It is designed for users who face issues using the official ChatGPT app due to device compatibility (e.g., Play Integrity), or who prefer a minimal interface.
This version is customized for public use and released under the **GNU General Public License v3.0**.

---

## ✨ Features

* Minimal, fast WebView wrapper (APK size < 150 KB)
* Light/dark mode support based on system theme
* No tracking, analytics, or background activity

---

## 🔧 Modifications

Compared to the original **gptAssist**:

* Renamed app to **WebGPT**
* Changed package name to `com.qomarhsn.webgpt`
* Replaced the original app icon with a new one
* Removed third-party URL restriction icon and related logic
* Configured WebView to open only ChatGPT and login pages internally; all other links open in the external browser

---

## 📜 License

**WebGPT** is free software: you can redistribute it and/or modify
it under the terms of the [GNU General Public License](https://www.gnu.org/licenses/gpl-3.0.en.html)
as published by the Free Software Foundation, either **version 3 of the License**, or
(at your option) **any later version**.

This program is distributed in the hope that it will be useful,
but **WITHOUT ANY WARRANTY**; without even the implied warranty of
**MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE**. See the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgment

This project is a minimal modification of [gptAssist](https://github.com/woheller69/gptAssist) by [@woheller69](https://github.com/woheller69).
Full credit goes to the original developer.