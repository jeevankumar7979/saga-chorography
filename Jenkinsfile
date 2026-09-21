pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timestamps()
    }

    environment {
        DOCKERHUB_NAMESPACE = 'jeevan7979'
        IMAGE_PREFIX = "${DOCKERHUB_NAMESPACE}/saga-chorography"
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        KUBECONFIG_CREDENTIALS = credentials('saga-kubeconfig')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.IMAGE_TAG = sh(
                        script: 'git rev-parse HEAD',
                        returnStdout: true
                    ).trim()
                }
            }
        }

        stage('Build application') {
            steps {
                sh '''
                    set -eu
                    ./order-service/mvnw -q -f order-service/pom.xml -DskipTests package
                    ./payment-service/mvnw -q -f payment-service/pom.xml -DskipTests package
                    ./inventory-service/mvnw -q -f inventory-service/pom.xml -DskipTests package
                '''
            }
        }

        stage('Build Docker images') {
            steps {
                sh '''
                    set -eu
                    docker build --tag "$IMAGE_PREFIX-order-service:$IMAGE_TAG" ./order-service
                    docker build --tag "$IMAGE_PREFIX-payment-service:$IMAGE_TAG" ./payment-service
                    docker build --tag "$IMAGE_PREFIX-inventory-service:$IMAGE_TAG" ./inventory-service
                '''
            }
        }

        stage('Push Docker images') {
            steps {
                sh '''
                    set -eu
                    printf '%s' "$DOCKERHUB_CREDENTIALS_PSW" | docker login --username "$DOCKERHUB_CREDENTIALS_USR" --password-stdin
                    docker push "$IMAGE_PREFIX-order-service:$IMAGE_TAG"
                    docker push "$IMAGE_PREFIX-payment-service:$IMAGE_TAG"
                    docker push "$IMAGE_PREFIX-inventory-service:$IMAGE_TAG"
                    docker logout
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    set -eu
                    export KUBECONFIG="$KUBECONFIG_CREDENTIALS"
                    kubectl apply --filename k8s
                    kubectl --namespace saga set image deployment/order-service \
                      order-service="$IMAGE_PREFIX-order-service:$IMAGE_TAG"
                    kubectl --namespace saga set image deployment/payment-service \
                      payment-service="$IMAGE_PREFIX-payment-service:$IMAGE_TAG"
                    kubectl --namespace saga set image deployment/inventory-service \
                      inventory-service="$IMAGE_PREFIX-inventory-service:$IMAGE_TAG"
                    kubectl --namespace saga rollout status deployment/order-service --timeout=180s
                    kubectl --namespace saga rollout status deployment/payment-service --timeout=180s
                    kubectl --namespace saga rollout status deployment/inventory-service --timeout=180s
                '''
            }
        }
    }

    post {
        always {
            sh 'rm -f "$KUBECONFIG_CREDENTIALS" 2>/dev/null || true'
        }
    }
}
