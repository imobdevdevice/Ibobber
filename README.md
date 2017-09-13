iBobber should be built using IntelliJ GUI using Build / Generate Signed APK.

Key store info can be found in signing.properties file.

"chineseLanguage" build variant overrides language detection - and forces default to Chinese (this deals with issues of language not being successfully detected on Chinese phones).

"allLanguage" build variant allows normal language detection.

//INSTRUCTIONS BELOW ARE DEPRECATED.  SEE ABOVE.

## Release build
1. Bump the versionCode and/or versionName in build.gradle
1. Copy signing.properties.template to signing.properties, and fill in the values with the path to the keystore, and the keystore and key passwords
1. Run "./gradlew assembleRelease"
1. The signed APK will be at app/build/apk/app-release.apk 
