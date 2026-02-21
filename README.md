#  ModelEngineAutoTexture

MEAT es un plugin de Spigot/Paper diseñado para automatizar la entrega del paquete de recursos (resource pack) generado por ModelEngine a los jugadores cuando entran al servidor. Este no es un plugin oficial ni afiliado de ninguna forma al desarrollador original de ModelEngine.

Este plugin levanta un servidor HTTP ligero integrado que sirve el archivo `.zip` del paquete de recursos directamente desde la carpeta de tu servidor, eliminando la necesidad de subir el archivo manualmente a servicios de alojamiento externos como Dropbox o Google Drive cada vez que haces un cambio en tus modelos.

## Características

*   **Servidor HTTP Integrado:** Sirve el resource pack directamente desde tu servidor de Minecraft.
*   **Automatización:** Detecta y sirve el archivo generado por ModelEngine automáticamente.
*   **Hash SHA-1:** Calcula y envía el hash SHA-1 del archivo para asegurar que el cliente actualice el paquete si ha cambiado.
*   **Configurable:** Puerto, host, mensajes y rutas configurables.
*   **Comandos de Recarga:** Permite recargar la configuración sin reiniciar el servidor.

## Instalación

1.  Descarga el archivo `.jar` de la sección de [Releases](https://github.com/ronaldzav/ModelEngineAutoTexture/releases) (o compílalo tú mismo).
2.  Coloca el archivo en la carpeta `plugins` de tu servidor.
3.  Reinicia el servidor.
4.  Configura el plugin en `plugins/meat/config.yml`.

## Configuración

El archivo `config.yml` permite ajustar el comportamiento del plugin.

```yaml
server:
  host: "0.0.0.0"
  port: 8080
  external-url: "http://tu-ip-publica:8080/resourcepack.zip"

resource-pack:
  path: "plugins/ModelEngine/resource pack.zip"
  force: true
  prompt: "Este paquete de recursos es necesario para ver los modelos personalizados."
  send-hash: false
  delay-ticks: 40

debug: false
```

### Importante sobre Puertos y Firewall

Para que los jugadores puedan descargar el paquete, el puerto configurado (por defecto `8080`) debe estar **abierto** en tu firewall y redirigido (port forwarded) si estás detrás de un router, apuntando a la máquina donde corre el servidor de Minecraft.

La `external-url` debe ser accesible desde internet. Si usas una IP doméstica, será tu IP pública.

## Comandos

*   `/meat reload`: Recarga la configuración y reinicia el servidor HTTP interno.
    *   Permiso: `meat.admin`

## Compilación

Para compilar el proyecto, necesitas Java 21 y Maven.

```bash
mvn clean package
```

El archivo compilado se encontrará en la carpeta `target`.

## Dependencias

*   Paper API (1.21)

## Problemas Comunes
1. Si la configuracion del plugin es correcta pero en el cliente de minecraft sale error al descargar establece el paraemtro `send-hash` como `false`. Este error suele suceder porque el cliente tiene guardado una version antigua del texturepack que choca con el nuevo hash.

---
Desarrollado por RonaldZav. Se aceptan Pull Requests, solo se pide codigo limpio, claro y estructurado.
