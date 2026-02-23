
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
                        sh 'mvn clean package jpackage:jpackage@mac'

                        withCredentials([
                            file(credentialsId: 'Apple-Dev-ID-Cert-2026', variable: 'CERT'),
                            string(credentialsId: 'mac-cert-pass', variable: 'P12_PASSWORD')
                        ]) {
                            sh '''
                            KEYCHAIN=build.keychain
                            KEYCHAIN_PASSWORD=buildpass

                            security delete-keychain $KEYCHAIN || true

                            security create-keychain -p $KEYCHAIN_PASSWORD $KEYCHAIN
                            security set-keychain-settings -lut 21600 $KEYCHAIN
                            security unlock-keychain -p $KEYCHAIN_PASSWORD $KEYCHAIN

                            security list-keychains -d user -s $KEYCHAIN
                            security default-keychain -s $KEYCHAIN

                            security import "$CERT" -k $KEYCHAIN -P "$P12_PASSWORD" \
                              -T /usr/bin/codesign -T /usr/bin/security

                            security set-key-partition-list -S apple-tool:,apple: \
                              -s -k $KEYCHAIN_PASSWORD $KEYCHAIN

                            security find-identity -v -p codesigning $KEYCHAIN

                            APP_PATH=$(find target -name "*.app" -type d | head -n 1)

                            echo "Signing app at $APP_PATH"

                            codesign --deep --force --options runtime \
                              --verify --verbose \
                              --sign "Developer ID Application: Steven Backenstoes (6J8PZ8D7V4)" \
                              "$APP_PATH"

                            codesign --verify --deep --strict --verbose=2 "$APP_PATH"

                            DMG_PATH=$(find target -name "*.dmg" | head -n 1)

                            echo "Signing DMG at $DMG_PATH"

                            codesign --force --verify --verbose \
                              --sign "Developer ID Application: Steven Backenstoes (6J8PZ8D7V4)" \
                              "$DMG_PATH"

                            codesign --verify --verbose=2 "$DMG_PATH"

                            xcrun notarytool submit "$DMG_PATH" \
                              --keychain-profile "FG_SIGNING_PROFILE" \
                              --wait

                            xcrun stapler staple "$DMG_PATH"
                            '''
                        }

                        sh 'mkdir -p artifacts/mac-arm64'
                        sh 'cp -r target/* artifacts/mac-arm64/'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'artifacts/mac-arm64/**/*', fingerprint: true
                        }
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