package test.asnTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.bouncycastle.asn1.DEROutputStream;

public class BerBillMain {
    private static final String filePath = "D:/work/IOT_TEMP/test";
    public static void main(String[] argv) throws IOException {
        long startTime;
        long parseTime;
        long encodeTime;
        File file_path = new File(filePath);
        File[] files = file_path.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith("DER") | fileName.endsWith("DB")) {
                continue;
            }
            System.out.println(fileName);
            String key = fileName.substring(0, fileName.indexOf("2017") - 1);
            startTime = System.currentTimeMillis();
            BillFile billFile = new BillFile(key);
            billFile.parse(new FileInputStream(file));
            parseTime = System.currentTimeMillis();
            FileOutputStream outFile = new FileOutputStream(file.getAbsolutePath() + ".DER");
            DEROutputStream out = new DEROutputStream(outFile);
            out.writeObject(billFile);
            out.flush();
            out.close();
            outFile.close();
            encodeTime = System.currentTimeMillis();
            System.out.println("parseTime=" + (parseTime - startTime));
            System.out.println("encodeTime=" + (encodeTime - parseTime));
        }
    }
}
