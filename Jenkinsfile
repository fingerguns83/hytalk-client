
pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
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
                    cleanWs()
                        checkout scm
                        sh 'mvn clean package javafx:jlink io.github.fvarrui:javapackager:package@package-mac-arm'
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
                        bat 'attrib -r "target/dist/*.exe"'
                        withCredentials([string(credentialsId: 'CERTUM_CERT_THUMBPRINT', variable: 'CERT_THUMB')]) {
                            bat 'signtool sign /v /debug /sha1 "%CERT_THUMB%" /fd sha256 /tr http://time.certum.pl /td sha256 target\\dist\\Hytalk-*.exe'
                        }
                        bat 'signtool verify /pa /all target\\dist\\Hytalk-*.exe'
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
                        cleanWs()
                        bat 'mvn package jpackage:jpackage@win'
                        bat 'attrib -r "target/dist/*.exe"'
                        withCredentials([string(credentialsId: 'CERTUM_CERT_THUMBPRINT', variable: 'CERT_THUMB')]) {
                            bat 'signtool sign /v /debug /sha1 "%CERT_THUMB%" /fd sha256 /tr http://time.certum.pl /td sha256 target\\dist\\Hytalk-*.exe'
                        }
                        bat 'signtool verify /pa /all target\\dist\\Hytalk-*.exe'
                        bat 'if not exist artifacts\\windows-arm64 mkdir artifacts\\windows-arm64'
                        bat 'xcopy /E /I /Y target artifacts\\windows-arm64'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'artifacts/windows-arm64/**/*', fingerprint: true
                        }
                    }
                }

                //stage('Linux AMD') {
                //    agent { label 'linux && amd' }
                //    steps {
                //        sh 'mvn clean package javafx:jlink io.github.fvarrui:javapackager:package@package-linux'
                //        sh 'mkdir -p artifacts/linux-amd64'
                //        sh 'cp -r target/* artifacts/linux-amd64/'
                //    }
                //    post {
                //        success {
                //            archiveArtifacts artifacts: 'artifacts/linux-amd64/**/*', fingerprint: true
                //        }
                //    }
                //}

                //stage('Linux ARM') {
                //    agent { label 'linux && arm' }
                //    steps {
                //        sh 'mvn clean package io.github.fvarrui:javapackager:package@package-linux'
                //        sh 'mkdir -p artifacts/linux-arm64'
                //        sh 'cp -r target/* artifacts/linux-arm64/'
                //    }
                //    post {
                //        success {
                //            archiveArtifacts artifacts: 'artifacts/linux-arm64/**/*', fingerprint: true
                //        }
                //    }
                //}
            }
        }
    }
}