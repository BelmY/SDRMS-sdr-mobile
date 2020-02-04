fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
## Android
### android internal
```
fastlane android internal
```
Submit a new Internal Build to Play Store
### android promote_internal_to_alpha
```
fastlane android promote_internal_to_alpha
```
Promote Internal to Alpha
### android promote_alpha_to_beta
```
fastlane android promote_alpha_to_beta
```
Promote Alpha to Beta
### android promote_beta_to_production
```
fastlane android promote_beta_to_production
```
Promote Beta to Production
### android validateJSON
```
fastlane android validateJSON
```
Check key
### android getNextStoreVersion
```
fastlane android getNextStoreVersion
```
Retrive latest version number from internal track

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
