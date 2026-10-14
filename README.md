# Finora

**A calm, private cash book and shared bachelor-mess ledger for Android.**

Finora keeps everyday money and shared-household accounting in one native app. It is designed around quick entry, readable monthly totals, and clear member settlements.

## Features

- Multiple independent cash books with running balances.
- Cash-in and cash-out entries with categories, notes, dates, search, and type filters.
- Monthly cash-flow and category summaries.
- Bachelor-mess members, deposits, daily meal units, shared bazar purchases, and personal expenses.
- Half meals and guest meals are entered as meal units.
- Monthly meal rate is calculated as shared bazar spending divided by all recorded meal units.
- Member statement includes meals, meal cost, deposits, bazar spending, and the resulting balance.
- Share monthly reports as CSV or PDF using Android’s share sheet.
- Dark, light, and system appearance, English and বাংলা UI, and an optional Bengali-numeral display.
- Offline-first Room storage. No sign-in or network access is required.

## Technology

- Kotlin, Jetpack Compose, Material 3, Navigation Compose
- Room, Coroutines and Flow
- Hilt dependency injection
- Minimum Android API 24; target API 36
- Manrope Latin and Noto Sans Bengali fonts; their SIL Open Font Licenses are in `app/src/main/assets/licenses/`

## Build and test

Use JDK 17 or newer and install the Android SDK components listed in the project’s Gradle configuration. Then run:

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Install it on a connected device with:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Mess settlement notes

- Money is stored as integer paisa to avoid floating-point rounding for cash entries.
- Meal units accept fractional values such as `0.5`.
- A positive member balance is shown as an amount to receive back; a negative balance is shown as an amount owed.
- Personal expenses are kept outside shared bazar and meal-rate calculations.
- Deactivating a member prevents selecting them for new records but preserves their history and any activity in the selected month.

## Project structure

```text
app/src/main/java/com/finora/
  data/       Room entities, DAO, repository, preferences, Hilt modules
  domain/     Settlement calculations, date helpers, report exports
  ui/         Compose screens, components, and theme
```

## বাংলা

**Finora হলো অ্যান্ড্রয়েডের জন্য একটি ব্যক্তিগত ক্যাশবুক ও যৌথ ব্যাচেলর-মেস হিসাবের অ্যাপ।**

### সুবিধা

- একাধিক আলাদা ক্যাশবুক এবং চলমান ব্যালেন্স।
- জমা ও খরচের হিসাব; বিভাগ, নোট, তারিখ, খোঁজ ও ফিল্টার।
- মাসিক ক্যাশ ফ্লো ও খরচের বিভাগের সারাংশ।
- মেস সদস্য, জমা, দৈনিক মিল, যৌথ বাজার এবং ব্যক্তিগত খরচের হিসাব।
- অর্ধেক মিল ও অতিথির মিল ভগ্নাংশ মিল ইউনিট হিসেবে যোগ করা যায়।
- মিল রেট = মোট যৌথ বাজারের খরচ ÷ মোট মিল ইউনিট।
- সদস্যভিত্তিক মিল, মিলের খরচ, জমা, বাজার ও পাওনা/দেনার বিবরণ।
- Android শেয়ার মেনু দিয়ে CSV বা PDF রিপোর্ট পাঠানো যায়।
- ডার্ক, লাইট ও সিস্টেম থিম; ইংরেজি ও বাংলা ভাষা; ঐচ্ছিক বাংলা অঙ্ক।
- অফলাইন Room ডেটাবেস। সাইন-ইন বা ইন্টারনেট সংযোগ দরকার নেই।

### তৈরি ও পরীক্ষা

JDK 17 বা নতুন সংস্করণ এবং Android SDK ইনস্টল করে চালান:

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

ডিবাগ APK পাওয়া যাবে `app/build/outputs/apk/debug/app-debug.apk`-এ।

### হিসাবের নিয়ম

- টাকার পরিমাণ পূর্ণ পয়সা হিসেবে সংরক্ষিত হয়।
- মিলের পরিমাণে `0.5`-এর মতো ভগ্নাংশ দেওয়া যায়।
- সদস্যের ধনাত্মক ব্যালেন্স ফেরত পাওয়ার এবং ঋণাত্মক ব্যালেন্স পরিশোধ করার পরিমাণ।
- ব্যক্তিগত খরচ যৌথ বাজার ও মিল রেটের হিসাবের বাইরে থাকে।
- সদস্য নিষ্ক্রিয় করলেও আগের তথ্য এবং সেই মাসের কার্যকলাপ সংরক্ষিত থাকে।

## License

MIT. See [LICENSE](LICENSE).
