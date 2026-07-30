# Исправление сборки GitHub Actions

В логе сборка завершалась до компиляции исходников:

`Plugin [id: 'com.gtnewhorizons.retrofuturagradle', version: '1.4.0'] was not found`.

В port.3 выполнены изменения:

1. RetroFuturaGradle заменён на `1.4.9`.
2. `settings.gradle` приведён к официальной схеме репозитория GTNH Maven.
3. GitHub Actions запускает Gradle 8.8 на Java 21 и оставляет Java 8 для компиляции Minecraft-мода.

После загрузки файлов в репозиторий откройте **Actions → Build → Run workflow** либо сделайте обычный commit — сборка запустится автоматически.
