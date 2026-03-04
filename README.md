# SMS → Telegram Forwarder (Android)

This Android app:
- Receives incoming SMS instantly
- Stores SMS locally when offline
- Sends stored SMS to a Telegram group when internet is available
- Ensures each SMS is sent only once (unique hash key + sent flag)

## IMPORTANT (Security)
Do **not** hardcode your Telegram bot token in code.
Put it in `local.properties` and keep that file private.

If you already shared your bot token publicly, **rotate/regenerate** it with @BotFather.

## 1) Telegram setup
1. Create bot via @BotFather → get BOT_TOKEN
2. Add bot to your group
3. Make bot admin (or allow sending messages)
4. Get your Group chat id (usually starts with `-100...`)

## 2) Put secrets in local.properties (project root)
Create / edit `local.properties` and add:

TG_BOT_TOKEN=YOUR_BOT_TOKEN
TG_GROUP_ID=YOUR_GROUP_ID

Example:
TG_BOT_TOKEN=123456:ABCDEF...
TG_GROUP_ID=-1001234567890

## 3) Build & run
- Open project in Android Studio
- Sync Gradle
- Run on your phone
- Grant SMS permission in app

## Notes
- Google Play has strict policies for SMS permissions. Best for private APK / internal use.
- WorkManager handles retry automatically when internet comes back.
