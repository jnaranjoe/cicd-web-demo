# CI/CD Completo con Jenkins (Build + Test + Deploy)

Este proyecto implementa un flujo de CI/CD (Integración y Despliegue Continuo) completamente automatizado usando **Jenkins** y **Docker**, como se especifica en la guía de laboratorio de Gestión de Configuración del Software, adaptada 100% para funcionar dentro de un entorno Docker.

---

## 🚀 Arquitectura del Proyecto

El laboratorio se ejecuta íntegramente sobre Docker utilizando la técnica **Docker-out-of-Docker (DooD)**:
1. **Jenkins Server (`jenkins/`)**:
   - Corre una imagen personalizada de Jenkins basada en la última LTS.
   - Instala las herramientas CLI de Docker y Docker-Compose internamente.
   - Automatiza la instalación de plugins clave (`git`, `github`, `workflow-aggregator`, `docker-workflow`, `credentials-binding`, `timestamper`).
   - Salta el asistente de instalación inicial de Jenkins.
   - Crea un usuario administrador por defecto (`admin` / `admin`).
   - Configura las credenciales personales de GitHub automáticamente.
   - Crea el Pipeline Job `CICD-Web-Demo` apuntando a este repositorio.
2. **Aplicación Web (`/`)**:
   - Una aplicación HTML ultra-mínima servida con Nginx Alpine (`app/index.html`).
   - Un script de prueba básico (`scripts/test.sh`).
   - El archivo `docker-compose.yml` que define los entornos de **Staging** (puerto `8081`) y **Producción** (puerto `8082`).

---

## 🛠️ Requisitos Previos

- Tener instalado **Docker** y **Docker Compose** en tu máquina (Windows/Linux/macOS).

---

## 🏁 Cómo Iniciar

### 1. Iniciar Jenkins
Para levantar el servidor Jenkins completamente configurado y con el pipeline listo, ejecuta el siguiente comando desde la raíz del proyecto:

```bash
docker compose -f jenkins/docker-compose.yml up -d --build
```

Esto compilará la imagen personalizada de Jenkins (instalando plugins y dependencias) y levantará el contenedor. El proceso de arranque inicial de Jenkins puede tardar aproximadamente 1-2 minutos.

### 2. Acceder a Jenkins
Una vez iniciado, abre tu navegador favorito y accede a:
👉 **URL:** [http://localhost:8080](http://localhost:8080)
- **Usuario:** `admin`
- **Contraseña:** `admin`

Verás que el Job **`CICD-Web-Demo`** ya está creado y listo para usarse.

---

## 🔄 El Pipeline de CI/CD

El flujo configurado en el `Jenkinsfile` automatiza las siguientes fases:
1. **Checkout**: Descarga el código desde el repositorio de GitHub.
2. **Lint / Validación**: Verifica la existencia y formato de los archivos necesarios.
3. **Test**: Ejecuta `./scripts/test.sh` para comprobar la integridad del HTML.
4. **Build Imagen (staging)**: Construye la imagen Docker de la aplicación etiquetada como `cicd-web-demo:staging`.
5. **Deploy a Staging**: Despliega el contenedor de Staging y lo publica en [http://localhost:8081](http://localhost:8081).
6. **Aprobación para Producción**: El pipeline se pausa pidiendo confirmación manual para continuar.
7. **Promover Imagen a Producción**: Re-etiqueta la imagen testeada en Staging como `cicd-web-demo:production`.
8. **Deploy a Producción**: Despliega el contenedor de Producción en [http://localhost:8082](http://localhost:8082).

---

## 🧪 Actividades del Laboratorio

### Actividad 1: Realizar un cambio y ver el Despliegue en Staging
1. Edita el archivo `app/index.html` (por ejemplo, cambia `<h1>Hola CI/CD con Jenkins</h1>` por `<h1>Hola CI/CD con Jenkins (v2)</h1>`).
2. Sube el cambio a GitHub:
   ```bash
   git add app/index.html
   git commit -m "Cambio visual v2"
   git push
   ```
3. En Jenkins, inicia el Job (`Build Now`). Verás que el cambio se despliega automáticamente en Staging ([http://localhost:8081](http://localhost:8081)) mientras que Producción ([http://localhost:8082](http://localhost:8082)) se mantiene intacto esperando aprobación.

### Actividad 2: Simular una falla en las pruebas
1. Modifica la prueba o elimina el texto esperado en `app/index.html` (provocando que el script `test.sh` falle).
2. Sube el cambio a GitHub y ejecuta el build.
3. El pipeline fallará en la fase de **Test**, bloqueando la creación de la imagen y protegiendo el entorno de Staging/Producción de un despliegue defectuoso.
