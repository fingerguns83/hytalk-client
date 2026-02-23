
pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-25'
    }

    stages {
        stage('Build All Platforms') {
            parallel {
                //stage('Mac AMD') {
                //    agent { label 'mac && amd' }
                //    steps {
                //        sh 'mvn clean package jpackage:jpackage@mac'
                //        sh 'mkdir -p artifacts/mac-amd64'
                //        sh 'cp -r target/* artifacts/mac-amd64/'
                //    }
                //    post {
                //        success {
                //            archiveArtifacts artifacts: 'artifacts/mac-amd64/**/*', fingerprint: true
                //        }
                //    }
                //}

                stage('Mac ARM') {
                    agent { label 'mac && arm' }

                    steps {
                        // Build first
                        sh 'mvn clean package jpackage:jpackage@mac'

                        // Unlock the existing keychain that already has your .p12 certificate
                        xcodeUnlockKeychain(
                            keychainPath: '/Users/jenkins/Library/Keychains/jenkins.keychain-db', // path to your installed keychain
                            keychainPassword: 'jenkins' // password for that keychain
                        )

                        // Run Xcode build using the unlocked keychain
                        xcodeBuild(
                            xcodeProjectPath: 'Hytalk.xcodeproj',
                            xcodeSchema: 'Release',
                            configuration: 'Release',
                            unlockKeychain: true,
                            keychainPath: '/Users/jenkins/Library/Keychains/jenkins.keychain-db',
                            keychainPassword: 'jenkins'
                        )

                        // Locate the built app and DMG
                        sh '''
                            APP_PATH=$(find target -name "*.app" -type d | head -n 1)
                            DMG_PATH=$(find target -name "*.dmg" | head -n 1)

                            echo "Signing and verifying app at $APP_PATH"
                            codesign --deep --force --verbose -s "Developer ID Application: Steven Backenstoes (6J8PZ8D7V4)" "$APP_PATH"
                            codesign --verify --deep --strict --verbose=2 "$APP_PATH"

                            echo "Signing and verifying DMG at $DMG_PATH"
                            codesign --deep --force --verbose -s "Developer ID Application: Steven Backenstoes (6J8PZ8D7V4)" "$DMG_PATH"
                            codesign --verify --verbose=2 "$DMG_PATH"

                            # Notarize and staple
                            xcrun notarytool submit "$DMG_PATH" --keychain-profile "FG_SIGNING_PROFILE" --wait
                            xcrun stapler staple "$DMG_PATH"
                        '''

                        // Collect artifacts
                        sh 'mkdir -p artifacts/mac-arm64'
                        sh 'cp -r target/* artifacts/mac-arm64/'
                    }

                    post {
                        success {
                            archiveArtifacts artifacts: 'artifacts/mac-arm64/**/*', fingerprint: true
                        }
                    }



                stage('Win AMD') {
                    agent { label 'windows && amd' }
                    options {
                        skipDefaultCheckout()
                    }
                    steps {
                       cleanWs()
                       checkout scm
                       bat 'mvn package jpackage:jpackage@win'
                       bat 'if not exist artifacts\\windows-amd64 mkdir artifacts\\windows-amd64'
                       bat 'xcopy /E /I /Y target artifacts\\windows-amd64'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'artifacts/windows-amd64/**/*', fingerprint: true
                        }
                    }
                }

                stage('Win ARM') {
                    agent { label 'windows && arm' }
                    steps {
                        bat 'mvn -version'
                        bat 'mvn clean package jpackage:jpackage@win'
                        bat 'if not exist artifacts\\windows-arm64 mkdir artifacts\\windows-arm64'
                        bat 'xcopy /E /I /Y target artifacts\\windows-arm64'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'artifacts/windows-arm64/**/*', fingerprint: true
                        }
                    }
                }

                stage('Linux AMD') {
                    agent { label 'linux && amd' }
                    steps {
                        sh 'mvn clean package jpackage:jpackage@linux'
                        sh 'mkdir -p artifacts/linux-amd64'
                        sh 'cp -r target/* artifacts/linux-amd64/'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'artifacts/linux-amd64/**/*', fingerprint: true
                        }
                    }
                }

                stage('Linux ARM') {
                    agent { label 'linux && arm' }
                    steps {
                        sh 'mvn clean package jpackage:jpackage@linux'
                        sh 'mkdir -p artifacts/linux-arm64'
                        sh 'cp -r target/* artifacts/linux-arm64/'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'artifacts/linux-arm64/**/*', fingerprint: true
                        }
                    }
                }
            }
        }
    }
}