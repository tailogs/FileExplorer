# FileExplorer

![image](https://github.com/tailogs/FileExplorer/assets/69743960/5edba3eb-c3a2-4bcf-ad79-78ea924681bf)

*Рисунок 1. Интерфейс приложения File Explorer*

---

## Описание

Простой файловый менеджер, написанный на Java с использованием Swing для графического интерфейса. Позволяет просматривать файловую систему, открывать папки, просматривать содержимое и возвращаться к предыдущим директориям.

---

Вы можете скачать `jar` версию (тоже что и `exe` грубо говоря) приложения из репозитория релизов по этой [ссылке](https://github.com/tailogs/FileExplorer/releases) или скомпилировать самому из исходных кодов.

---
## Возможности

- Просмотр файловой системы и содержимого папок.
- Навигация по истории посещенных папок с помощью кнопки "Назад".
- Поддержка темной темы с использованием Nimbus LookAndFeel.
- Отображение имени файла, типа, размера и даты изменения в таблице.

---

## Установка и запуск

### Предварительные требования

- Java Development Kit (JDK) 8 или выше.
- Среда разработки Java (например, IntelliJ IDEA или Eclipse) или консоль для компиляции и запуска.

### Компиляция из исходных кодов

1. Склонируйте проект:

    ```sh
    git clone https://github.com/tailogs/FileExplorer.git
    ```
    
    ```sh
    cd FileExplorer
    ```

2. Компилируйте исходные коды:

    ```sh
    javac -d out src/Main.java
    ```

3. Упакуйте приложение в JAR файл:

    ```sh
    jar cvfm FileExplorer.jar Manifest.txt -C out . -C src resources
    ```

### Запуск приложения

Для запуска приложения используйте двойной клик по `FileExplorer.jar` файлу или же команду:

```sh
java -jar FileExplorer.jar
```
