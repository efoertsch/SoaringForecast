package com.fisincorporated.aviationweather.task;

import android.os.Environment;

import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Turnpoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import timber.log.Timber;

public class TurnpointProcessor {

    AppRepository appRepository;

    @Inject
    public TurnpointProcessor( AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    public List<File> getCupFileList() {
        ArrayList<File> cupFileList = new ArrayList<>();
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File filesInDirectory[] = path.listFiles(new ImageFileFilter());
        cupFileList.addAll(new ArrayList<>(Arrays.asList(filesInDirectory)));
        return cupFileList;
    }

    public class ImageFileFilter implements FileFilter {
        private final String[] cupFileExtensions = new String[]{"cup"};

        public boolean accept(File file) {
            for (String extension : cupFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    // make sure not version with control number placed before name.
                    if (file.getName().substring(0, file.getName().indexOf(".cup")).endsWith("_nm")) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public Completable importTurnpointFileCompletable(final String fileName) {
        return new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                try{
                    importTurnpointFile(fileName);
                    s.onComplete();
                } catch (IOException e) {
                    s.onError(e);
                }
            }
        };
    }

    /**
     *
     * @param fileName SeeYou cup file name
     * @return  success if import went OK
     * @throws IOException
     */
    public boolean importTurnpointFile(final String fileName) throws IOException {
        int linesRead = 0;
        int numberTurnpoints = 0;
        String turnpointLine;
        Turnpoint turnpoint;
        BufferedReader reader = null;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String cupFilePath = path.getAbsoluteFile() + "/" + fileName;
        try {
            Timber.d("Turnpoint file: %1$s", fileName);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cupFilePath)));
            turnpointLine = reader.readLine();
            while (turnpointLine != null && !turnpointLine.isEmpty()) {
                linesRead++;
                if (linesRead > 1) {
                    turnpoint = Turnpoint.createTurnpointFromCSVDetail(turnpointLine);
                    if (turnpoint != null) {
                        appRepository.insertTurnpoint(turnpoint);
                    }
                    numberTurnpoints++;
                }
                turnpointLine = reader.readLine();
            }
            Timber.d("Lines read: %1$d   Lines written %2$d", linesRead, numberTurnpoints);
            Timber.d("File saved to DB successfully!");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Timber.d("Failed to import the file!");
            throw (e);
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }

}
