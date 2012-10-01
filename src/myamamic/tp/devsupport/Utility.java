package myamamic.tp.devsupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utility {
    public static String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        String date = sdf.format(Calendar.getInstance().getTime());
        return date;
    }

    public static boolean writeResultToFile(String outputFilePath, String result, boolean append) {
        File outputFile = new File(outputFilePath);
        OutputStream os = null;

        String line = System.getProperty("line.separator");
        try {
            os = new FileOutputStream(outputFile, append);
            os.write(result.getBytes(), 0, result.length());
            os.write(line.getBytes(), 0, line.length());
            os.close();
            os = null;
        } catch(IOException ioe) {
            return false;
        }
        return true;
    }

    public static boolean CopyFile(String srcFilePath, String dstFilePath) {
        File srcFile = new File(srcFilePath);
        File dstFile = new File(dstFilePath);
        return CopyFileProc(srcFile, dstFile);
    }

    public static boolean CopyFile(File srcFile, File dstFile) {
        return CopyFileProc(srcFile, dstFile);
    }

    private static boolean CopyFileProc(File srcFile, File dstFile) {
        // Copy file
        InputStream is = null;
        OutputStream os = null;
    
        try {
            is = new FileInputStream(srcFile);
            os = new FileOutputStream(dstFile);

            byte[] buf = new byte[4096];
            int n = 0;
            while (-1 != (n = is.read(buf))) {
                os.write(buf, 0, n);
            }
            is.close();
            is = null;
            os.close();
            os = null;
        } catch(IOException ioe) {
            return false;
        }
        return true;
    }
}
