package testcode.zipslip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;

public class ZipSlipBad {
    void writeZipEntry(ZipEntry entry, File destinationDir) throws FileNotFoundException {
        File file = new File(destinationDir, entry.getName());
        FileOutputStream fos = new FileOutputStream(file); // $ZipSlip
        // ... write entry to fos ...
    }
}
