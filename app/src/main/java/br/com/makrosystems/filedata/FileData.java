package br.com.makrosystems.filedata;

import java.io.File;
import java.text.DateFormat;

public class FileData {

    public static String fileLastData(String path){

        File file;
        String filePath = path;
        file = new File(filePath);
        Long lastModified = file.lastModified();
        DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();
        String newDate = dateTimeInstance.format(lastModified);

        return newDate;
    }
}
