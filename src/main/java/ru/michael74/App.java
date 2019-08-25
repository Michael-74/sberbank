package ru.michael74;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class App {

    static Callable callable(File file) {
        return () -> {
            FileService fileService = new FileService();
            fileService.parseFile(file).writeFile();

            return true;
        };
    }

    public static void main(String[] args) {

        ExecutorService executor = Executors.newWorkStealingPool();

        try {
            FileService.clearFile();
            List<File> files = FileService.getFileLogs();
            if(files != null) {
                List<Callable<File>> callables = new ArrayList<>();
                for (File file : files) {
                    callables.add(callable(file));
                }
                executor.invokeAll(callables);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
