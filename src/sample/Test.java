package sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SampleFileReader {

    public static void main(String[] args) {
        File file = new File("d:/temp/test.text");
        FileReader fr;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            _vf_safeClose(br);
        }
    }

    public static void _vf_safeClose(BufferedReader arg0) {
        if (arg0 != null) {
            try {
                arg0.close();
            } catch (IOException e) {
            // TODO VioFixer: handle your logging here
            }
        }
    }
}
