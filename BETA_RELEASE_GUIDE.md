# Beta Testing Release Guide

## Step 1: Create a Keystore (First Time Only)

If you don't have a keystore yet, create one:

```bash
keytool -genkey -v -keystore connect-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias connect-key
```

**Important:** 
- Store the keystore file and password securely
- You'll need this for all future releases
- Never commit the keystore file to git!

## Step 2: Configure Signing in build.gradle.kts

The signing configuration has been added to `app/build.gradle.kts`. You'll need to:

1. Create the keystore file (see Step 1)
2. Create a `keystore.properties` file in the project root with:
   ```
   storePassword=your_store_password
   keyPassword=your_key_password
   keyAlias=connect-key
   storeFile=connect-release-key.jks
   ```

3. Add `keystore.properties` to `.gitignore`:
   ```
   keystore.properties
   *.jks
   ```

## Step 3: Update Version Information

Before building, update the version in `app/build.gradle.kts`:
- `versionCode`: Increment for each release (e.g., 1, 2, 3...)
- `versionName`: User-facing version (e.g., "1.3.9", "1.4.0-beta")

## Step 4: Build the App Bundle

Run this command to build the release bundle:

```bash
./gradlew bundleRelease
```

The bundle will be created at:
`app/build/outputs/bundle/release/app-release.aab`

## Step 5: Upload to Google Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app (or create it if first time)
3. Go to **Testing** → **Internal testing** (or **Closed testing** / **Open testing**)
4. Click **Create new release**
5. Upload the `.aab` file from Step 4
6. Add release notes
7. Review and roll out to testers

## Step 6: Add Testers

- **Internal testing**: Up to 100 testers (email addresses)
- **Closed testing**: Up to 20,000 testers (email addresses or Google Groups)
- **Open testing**: Public beta (anyone can join)

## Troubleshooting

- **"App not signed"**: Make sure signing config is set up correctly
- **"Version code already exists"**: Increment versionCode in build.gradle.kts
- **"Bundle too large"**: Enable ProGuard/R8 (currently disabled in your config)
