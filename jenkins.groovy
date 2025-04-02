pipeline {
    agent any

    environment {
        // Set environment variables
        GOOGLE_APPLICATION_CREDENTIALS = credentials('GCP-jenkins-json') 
        BUCKET_NAME = 'test-cloudrun-bucket' // Replace with your bucket name
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout code from GitHub or GitLab
                checkout scm
            }
        }

        stage('Upload Website Files to GCS') {
            steps {
                script {
                    // Install GCloud SDK (if not already installed in Jenkins)
                    sh 'curl https://sdk.cloud.google.com | bash'

                    // Authenticate with GCP
                    sh 'gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS'

                    // Set the project
                    sh 'gcloud config set project consummate-rig-453502-q2'  
                    
                    // Upload website files to GCS
                    sh 'gsutil -m cp -r * gs://$BUCKET_NAME/'
                }
            }
        }

        stage('Set Bucket as Website') {
            steps {
                script {
                    // Set the bucket as a static website
                    sh 'gsutil web set -m index.html gs://$BUCKET_NAME'
                }
            }
        }

        stage('Set Bucket Permissions') {
            steps {
                script {
                    // Make the bucket publicly accessible
                    sh 'gsutil iam ch allUsers:objectViewer gs://$BUCKET_NAME'
                }
            }
        }
    }

    post {
        success {
            echo 'Website deployment completed successfully!'
        }
        failure {
            echo 'Deployment failed. Please check the logs.'
        }
    }
}
