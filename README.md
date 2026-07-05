# ldtageditor (WrathLab fork)

Lego Dimensions Tag Editor — read and write Lego Dimensions characters/vehicles to NTAG21x NFC tags from an Android phone.

Fork of [naleo/ldtageditor-v2](https://github.com/naleo/ldtageditor-v2), updated to build and run on modern Android devices (Android 12–16).

## What this fork changes

- **Runs on newer Android.** `targetSdk` raised 23 → 30; drops the old-target install warnings on Android 12+.
- **Builds for anyone.** Removed the original author's hardcoded keystore path and passwords; debug builds now auto-sign with the standard local debug key.
- **Single universal APK.** Dropped the arm64-only ABI split so one APK installs on every device.
- **No crash on Android 12+.** `PendingIntent` now uses `FLAG_MUTABLE` (required from API 31; old `flag 0` throws).
- **Reliable writing.** Writing a full character/vehicle used to re-open the NFC connection for every page — newer phones (e.g. ROG 6) dropped the tag mid-batch and the write silently failed while still reporting success. The connection is now held open across the whole page batch, and `writeTag` returns real success/failure.
- **Clean overwrite.** Writing a new token first zeroes the tag's user pages (0x04–0x27), so old character/save data can't bleed into the new write.
- **On-screen hex dump.** Every Read shows the raw tag bytes (green overlay, top of screen) so you can confirm exactly what's on the tag — no PC needed.

Original v2 fix (kept): all `MifareUltralight` API calls were replaced with `NfcA`, which every NFC-capable Android phone must support.

## How it works

1. **Read** — tap Read, then touch a blank/target NTAG21x tag. The UID fills in and the raw bytes dump on screen.
2. Pick **Character** or **Vehicle/Gadget** and select the token.
3. **Write** — appears after a UID is read. Tap it, hold the disc still (~2s) until the dialog closes.
4. On the toypad: after writing, **lift the disc and place it again** — the game only reads a token when it's placed, so an in-place rewrite won't reload. If it still shows the old character, **restart the game** — Lego Dimensions caches token data in memory until relaunched.

Tag notes:
- Use blank **NTAG213** (stable). NTAG215/216 are flagged experimental in the UI.
- Genuine (store-bought) Lego discs are locked and cannot be rewritten — use blank tags.

## How to get / build it

No PC required — GitHub builds the APK for you:

1. Go to the **Actions** tab of this repo.
2. Open the latest **build** run (or run it via **Run workflow**).
3. When it finishes green, download the **app-debug** artifact.
4. Extract the `.zip`, tap the `.apk`, allow install from unknown sources, install.
5. Grant NFC when asked.

To build locally instead: open the project in Android Studio (JDK 11), `Build > Build APK(s)`.

## Troubleshooting

- **No Write button** — you must Read a tag first; Write only appears once a UID is loaded.
- **Read shows stale / nothing** — press Read *first*, then tap the tag fresh (lift and re-tap); a tag already resting on the phone won't trigger a new read.
- **Write fails** — hold the disc still until the dialog closes; confirm it's a blank NTAG213. For details: `adb logcat | grep JSAPI` shows `write failed pXX` with the reason.
- **Game loads old character** — lift the disc off the pad and place it again. If it persists, **restart the game** (or reload the level); Lego Dimensions caches tokens until relaunched.

## Credits

- Original app by its respective author; v2 NfcA fix by [naleo](https://github.com/naleo/ldtageditor-v2).
- This fork: modern-Android build + write-reliability fixes.
