package testcode.pathtraversal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class UploadRecord {
    private Map<Long, UploadFile> uploadedFiles = new LinkedHashMap<Long, UploadFile>();

    private static UploadRecord instance = null;

    public static class UploadFile {
        private Long id;
        private File file;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public UploadFile(Long id, File file) {
            this.id = id;
            this.file = file;
        }

        public Boolean exists() {
            return file.exists();
        }

        public InputStream getInputStream() throws FileNotFoundException {
            return new FileInputStream(file);
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDisplayName() {
            String filename = getFilename();
            return filename.substring(0, filename.lastIndexOf('.'));
        }

        public String getFilename() {
            return file.getName();
        }

        public String getFilePath() {
            return file.getAbsolutePath();
        }

        public Long getLength() {
            return file.length();
        }
    }

    public static synchronized UploadRecord getInstance() {
        if (instance == null) {
            instance = new UploadRecord();
        }
        return instance;
    }

    public static void add(UploadFile uploadFile) {
        getInstance().uploadedFiles.put(uploadFile.getId(), uploadFile);
    }

    public static UploadFile findById(Long id) {
        return getInstance().uploadedFiles.get(id);
    }

    public static void removeById(Long id) {
        UploadFile uploadFile = findById(id);
        if (uploadFile != null && uploadFile.exists()) {
            uploadFile.file.delete();
        }
        getInstance().uploadedFiles.remove(id);
    }

    public static Map<Long, UploadFile> getUploadedFiles() {
        return getInstance().uploadedFiles;
    }
}