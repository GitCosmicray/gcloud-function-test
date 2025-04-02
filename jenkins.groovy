pipeline {
    agent any

    environment {
        GCP_PROJECT_ID = 'consummate-rig-453502-q2'
        BUCKET_NAME = 'test-cloudrun-bucket'
        CREDENTIALS_ID = 'GCP-jenkins-json'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git 'https://github.com/GitCosmicray/gcloud-function-test.git'
            }
        }

        stage('Authenticate with GCP') {
            steps {
                withCredentials([file(credentialsId: "${CREDENTIALS_ID}", variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                    sh 'gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS'
                    sh 'gcloud config set project $GCP_PROJECT_ID'
                }
            }
        }

        stage('Upload Files to Cloud Storage') {
            steps {
                sh 'gsutil -m cp -r * gs://${BUCKET_NAME}/'
            }
        }

        stage('Set Bucket as a Public Website') {
            steps {
                sh """
                gsutil web set -m index.html gs://${BUCKET_NAME}/
                gsutil iam ch allUsers:objectViewer gs://${BUCKET_NAME}
                """
            }
        }
    }
}
