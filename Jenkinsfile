
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
                       withCredentials([string(credentialsId: 'CERTUM_CERT_NAME', variable: 'CERT_NAME')]) {
                          bat 'signtool sign /n "%CERT_NAME%" /t http://time.certum.pl/ /fd sha256 /v target\\dist\\Hytalk-*.exe'
                      }
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
                        withCredentials([string(credentialsId: 'CERTUM_CERT_NAME', variable: 'CERT_NAME')]) {
                            bat 'signtool sign /n "%CERT_NAME%" /t http://time.certum.pl/ /fd sha256 /v target\\dist\\Hytalk-*.exe'
                        }
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
//
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