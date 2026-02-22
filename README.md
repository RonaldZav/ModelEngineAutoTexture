#  ModelEngineAutoTexture

MEAT es un plugin de Spigot/Paper diseñado para automatizar la entrega del paquete de recursos (resource pack) generado por ModelEngine a los jugadores cuando entran al servidor. Este no es un plugin oficial ni afiliado de ninguna forma al desarrollador original de ModelEngine.

Este plugin levanta un servidor HTTP ligero integrado que sirve el archivo `.zip` del paquete de recursos directamente desde la carpeta de tu servidor, eliminando la necesidad de subir el archivo manualmente a servicios de alojamiento externos como Dropbox o Google Drive cada vez que haces un cambio en tus modelos.

## Características

*   **Servidor HTTP Integrado:** Sirve el resource pack directamente desde tu servidor de Minecraft.
*   **Automatización:** Detecta y sirve el archivo generado por ModelEngine automáticamente.
*   **Hash SHA-1:** Calcula y envía el hash SHA-1 del archivo para asegurar que el cliente actualice el paquete si ha cambiado.
*   **Configurable:** Puerto, host, mensajes y rutas configurables.
*   **Comandos de Recarga:** Permite recargar la configuración sin reiniciar el servidor.
*   **Integración con ItemsAdder:** Extrae automáticamente el pack de ModelEngine en la carpeta de ItemsAdder, delegando la entrega del pack al propio ItemsAdder.

## Instalación

1.  Descarga el archivo `.jar` de la sección de [Releases](https://github.com/ronaldzav/ModelEngineAutoTexture/releases) (o compílalo tú mismo).
2.  Coloca el archivo en la carpeta `plugins` de tu servidor.
3.  Reinicia el servidor.
4.  Configura el plugin en `plugins/ModelEngineAutoTexture/config.yml`.

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

integrations:
  itemsadder:
    enabled: false
    target-path: "plugins/ItemsAdder/contents/modelengine/resourcepack"

debug: false
```

### Importante sobre Puertos y Firewall

Para que los jugadores puedan descargar el paquete, el puerto configurado (por defecto `8080`) debe estar **abierto** en tu firewall y redirigido (port forwarded) si estás detrás de un router, apuntando a la máquina donde corre el servidor de Minecraft.

La `external-url` debe ser accesible desde internet. Si usas una IP doméstica, será tu IP pública.

> Esta sección no aplica si usas la integración con ItemsAdder, ya que el servidor HTTP interno se desactiva completamente.

## Integración con ItemsAdder

Si ya tienes ItemsAdder instalado, puedes activar la integración para que ambos plugins compartan el mismo resource pack sin conflictos.

### ¿Cómo funciona?

Cuando la integración está activa, MEAT **no levanta un servidor HTTP** ni envía el pack a los jugadores al unirse. En su lugar, extrae el contenido del zip de ModelEngine directamente en la carpeta de namespace de ItemsAdder, y deja que ItemsAdder se encargue del resto.

### Activación

En `config.yml`, establece:

```yaml
integrations:
  itemsadder:
    enabled: true
    target-path: "plugins/ItemsAdder/contents/modelengine/resourcepack"
```

### Flujo de trabajo recomendado

Una vez que hayas hecho cambios en tus modelos de ModelEngine:

1.  Ejecuta `/meat zip` — este comando:
    *   Recarga ModelEngine para regenerar el zip.
    *   Extrae el zip en la carpeta de ItemsAdder.
    *   Ejecuta `/iareload` para que ItemsAdder procese los nuevos assets.
    *   Ejecuta `/iazip` para que ItemsAdder genere el resource pack final.

Si solo quieres actualizar los archivos sin regenerar el pack de ItemsAdder, usa `/meat reload`.

## Comandos

*   `/meat reload`: Recarga ModelEngine y la configuración de MEAT.
    *   En modo normal: reinicia el servidor HTTP.
    *   En modo ItemsAdder: extrae el pack de ModelEngine en la carpeta de ItemsAdder.
    *   Permiso: `meat.admin`

*   `/meat zip`: Como `reload`, pero además envía el pack actualizado a los jugadores.
    *   En modo normal: reenvía el resource pack a todos los jugadores conectados.
    *   En modo ItemsAdder: ejecuta `/iareload` y luego `/iazip`.
    *   Permiso: `meat.admin`

## Compilación

Para compilar el proyecto, necesitas Java 21 y Maven.

```bash
mvn clean package
```

El archivo compilado se encontrará en la carpeta `target`.

## Dependencias

*   Paper API (1.21)
*   ItemsAdder (opcional, para la integración)

## Problemas Comunes

1. Si la configuracion del plugin es correcta pero en el cliente de minecraft sale error al descargar, establece el parametro `send-hash` como `false`. Este error suele suceder porque el cliente tiene guardado una version antigua del texturepack que choca con el nuevo hash.

2. Si usas la integración con ItemsAdder y el pack no se actualiza tras `/meat zip`, verifica que el tiempo de espera entre `/iareload` y `/iazip` sea suficiente para tu servidor. ItemsAdder puede tardar más en servidores con muchos assets.

---
Desarrollado por RonaldZav. Se aceptan Pull Requests, solo se pide codigo limpio, claro y estructurado.
