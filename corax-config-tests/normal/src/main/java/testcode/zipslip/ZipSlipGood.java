package testcode.zipslip;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;

public class ZipSlipGood {
    void writeZipEntry(ZipEntry entry, File destinationDir) throws Exception {
        File file = new File(destinationDir, entry.getName());
        if (!file.toPath().normalize().startsWith(destinationDir.toPath()))
            throw new Exception("Bad zip entry");
        FileOutputStream fos = new FileOutputStream(file); // !$ZipSlip
        // ... write entry to fos ...
    }
}
