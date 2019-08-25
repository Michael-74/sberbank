package ru.michael74;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FileService implements IFileService {

    private static String basePath = new File("").getAbsolutePath();
    private static final String ERROR = "error";
    private static final String NOTIFY = "notify";
    private static final String WARNING = "warning";

    private AtomicInteger notify = new AtomicInteger();
    private AtomicInteger error = new AtomicInteger();
    private AtomicInteger warning = new AtomicInteger();
    private String fileName;
    private ReadWriteLock lock = new ReentrantReadWriteLock();


    /**
     * Находим все файлы в папке
     * @return List
     * @throws IOException
     */
    static List<File> getFileLogs() throws IOException {
        String pathDir = "\\src\\main\\resources\\log";

        if (!Files.exists(Paths.get(basePath + pathDir))) {
            return null;
        }

        return Files.walk(Paths.get(basePath + pathDir))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    /**
     * Путь до файла записи результата
     * @return string
     */
    private static String pathOuter() {
        String path = "\\src\\main\\resources\\out\\Statistics.txt";

        return basePath + path;
    }

    /**
     * Ищем совпадения в файле
     * @param file
     * @return
     */
    public FileService parseFile(File file) {

        this.fileName = file.getName();
        Stream<String> stream = null;
        try {
            stream = Files.lines(Paths.get(file.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        stream
                .flatMap(line -> Arrays.stream(line.split(" ")))
                .parallel()
                .map(String::toLowerCase)
                .filter(w -> w.matches(ERROR + "|" + NOTIFY + "|" + WARNING))
                .forEach(this::counter);

        return this;
    }

    /**
     * Производим запись в файл
     */
    public void writeFile() {
        lock.writeLock().lock();
        FileWriter writer = null;
        try {
            writer = new FileWriter(FileService.pathOuter(), true);
            String text = "File: " + this.fileName + " " + NOTIFY + ":" + notify + " " + ERROR + ":" + error + " " + WARNING + ":" + warning ;
            writer.write(text);
            writer.append('\n');
            writer.close();
        } catch(IOException ex){
            System.out.println(ex.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Производим очистку в файла
     */
    static void clearFile() {
        try {
            PrintWriter writer = new PrintWriter(FileService.pathOuter());
            writer.print("");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Счетчик совпадений
     * @param str
     */
    private void counter(String str) {
        switch (str) {
            case NOTIFY:
                notify.getAndIncrement();
                break;
            case ERROR:
                error.getAndIncrement();
                break;
            case WARNING:
                warning.getAndIncrement();
                break;
        }
    }
}
