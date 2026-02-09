pipeline {
    agent none

    stages {
        stage('Build All Platforms') {
            parallel {

                /*stage('Mac AMD') {
                    agent { label 'mac && amd' }
                    steps {
                        sh 'mvn clean package jpackage:jpackage@mac'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'target/**/*', fingerprint: true
                        }
                    }
                }

                stage('Mac ARM') {
                    agent { label 'mac && arm' }
                    steps {
                        sh 'mvn clean package jpackage:jpackage@mac'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'target/**/*', fingerprint: true
                        }
                    }
                }*/

                stage('Win AMD') {
                    agent { label 'windows && amd' }
                    steps {
                        bat 'mvn clean package jpackage:jpackage@win'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'target/**/*', fingerprint: true
                        }
                    }
                }

                /*stage('Win ARM') {
                    agent { label 'windows && arm' }
                    steps {
                        bat 'mvn clean package jpackage:jpackage@win'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'target/**/*', fingerprint: true
                        }
                    }
                }

                stage('Linux AMD') {
                    agent { label 'linux && amd' }
                    steps {
                        sh 'mvn clean package jpackage:jpackage@linux'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'target/**/*', fingerprint: true
                        }
                    }
                }

                stage('Linux ARM') {
                    agent { label 'linux && arm' }
                    steps {
                        sh 'mvn clean package jpackage:jpackage@linux'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'target/**/*', fingerprint: true
                        }
                    }
                }*/
            }
        }
    }
}
