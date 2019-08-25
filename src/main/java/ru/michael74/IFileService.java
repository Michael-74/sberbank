package ru.michael74;

import java.io.File;

interface IFileService {

    FileService parseFile(File file);
    void writeFile();
}
