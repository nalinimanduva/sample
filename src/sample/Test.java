package sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SampleFileReader {
	public static void main(String[] args)  {
		static final String PASSWORD = "p3n13l";
		File file = new File("d:/temp/test.text");

			FileReader fr;
			try {
				fr = new FileReader(file);
				BufferedReader br =	
						new	
						BufferedReader(fr);
				String line;
				while((line = br.readLine()) != null){
					System.out.println(line);
				}
				br      
				.
				close()
				;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
	}
}
