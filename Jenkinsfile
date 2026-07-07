pipeline {
    agent any

    environment {
        APP_NAME = "cicd-web-demo"
        STAGING_PORT = "8081"
        PROD_PORT = "8082"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Descargando el código..."
            }
        }
        stage('Lint / Validación') {
            steps {
                echo "Validando estructura mínima..."
                sh 'test -f Dockerfile'
                sh 'test -f docker-compose.yml'
                sh 'test -f app/index.html'
                sh 'test -x scripts/test.sh'
                echo "Validación OK"
            }
        }
        stage('Test') {
            steps {
                echo "Ejecutando pruebas..."
                sh './scripts/test.sh'
            }
        }
        stage('Build Imagen (staging)') {
            steps {
                echo "Construyendo imagen para staging..."
                sh "docker build -t ${APP_NAME}:staging ."
            }
        }
        stage('Deploy a Staging') {
            steps {
                echo "Desplegando en STAGING (puerto ${STAGING_PORT})..."
                sh 'docker compose up -d web-staging'
                echo "Staging actualizado. Verifica en: http://localhost:8081"
            }
        }
        stage('Aprobación para Producción') {
            steps {
                input message: '¿Aprobar despliegue a PRODUCCIÓN?', ok: 'Sí, desplegar'
            }
        }
        stage('Promover Imagen a Producción') {
            steps {
                echo "Promoviendo imagen a producción..."
                sh "docker tag ${APP_NAME}:staging ${APP_NAME}:production"
            }
        }
        stage('Deploy a Producción') {
            steps {
                echo "Desplegando en PRODUCCIÓN (puerto ${PROD_PORT})..."
                sh 'docker compose up -d web-production'
                echo "Producción actualizada. Verifica en: http://localhost:8082"
            }
        }
    }
    post {
        success {
            echo "CI/CD completado con éxito."
        }
        failure {
            echo "CI/CD falló. Revisar logs del build."
        }
        always {
            sh 'docker ps --format "table {{.Names}}\\t{{.Image}}\\t{{.Status}}\\t{{.Ports}}" || true'
        }
    }
}
