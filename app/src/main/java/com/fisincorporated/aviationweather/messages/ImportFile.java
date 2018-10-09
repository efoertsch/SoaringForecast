package com.fisincorporated.aviationweather.messages;

import java.io.File;

public final class ImportFile {

    private File file;

    public ImportFile(File file) {
        this.file  = file;
    }

    public File getFile() {
        return file;
    }

}
