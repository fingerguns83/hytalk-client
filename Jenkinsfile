
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
                            def keychainName = "build-${env.BUILD_NUMBER}.keychain"
                            def keychainPassword = "buildpass"

                            sh '''
                            # Delete old keychain if it exists
                            security delete-keychain "${keychainName}" || true

                            # Create new keychain and unlock
                            security create-keychain -p "${keychainPassword}" "${keychainName}"
                            security set-keychain-settings -lut 21600 "${keychainName}"
                            security unlock-keychain -p "${keychainPassword}" "${keychainName}"

                            # Set as default and ensure it's in search list
                            security list-keychains -d user -s "${keychainName}"
                            security default-keychain -s "${keychainName}"

                            # Import certificate into keychain
                            security import "$CERT" -k "${keychainName}" -P "$P12_PASSWORD" -T /usr/bin/codesign -T /usr/bin/security

                            # Set key partition list for non-interactive codesigning
                            security set-key-partition-list -S apple-tool:,apple: -s -k "${keychainPassword}" "${keychainName}"

                            # Verify identities
                            security find-identity -v -p codesigning "${keychainName}"

                            APP_PATH=$(find target -name "*.app" -type d | head -n 1)

                            echo "Signing app at $APP_PATH"

                            CODESIGN_IDENTITY=\$(security find-identity -v -p codesigning "${keychainName}" | grep "Developer ID Application" | awk '{print substr(\$0, index(\$0, \\"Developer ID Application\\") )}')
                            echo "Signing with identity: \$CODESIGN_IDENTITY"
                            codesign --deep --force --verbose -s "\$CODESIGN_IDENTITY" "$APP_PATH"


                            codesign --verify --deep --strict --verbose=2 "$APP_PATH"

                            DMG_PATH=$(find target -name "*.dmg" | head -n 1)

                            echo "Signing DMG at $DMG_PATH"

                            CODESIGN_IDENTITY=\$(security find-identity -v -p codesigning "${keychainName}" | grep "Developer ID Application" | awk '{print substr(\$0, index(\$0, \\"Developer ID Application\\") )}')
                            echo "Signing with identity: \$CODESIGN_IDENTITY"
                            codesign --deep --force --verbose -s "\$CODESIGN_IDENTITY" "$DMG_PATH"


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