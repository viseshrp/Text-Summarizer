 

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class TextPreprocessor {

	public static void main(String[] args) {
		try {

			FileSystem fs = FileSystem.get(new Configuration());
			FileStatus[] status = fs.listStatus(new Path("/home/gowtham/Desktop/input")); //input path containing file collection
			for (int i = 0; i < status.length; i++) {
				BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(status[i].getPath())));
				String lineRead;
				lineRead = br.readLine();
				String doc = "";

				boolean value = false;
				while (lineRead != null) {

					if (value) {
						doc += lineRead;

					}
					value = true;
					lineRead = br.readLine();
				}

				String[] linesArray = doc.split("\\.");

				Path pt2 = new Path("/home/gowtham/Desktop/output/"+"file"+i+".txt"); //output file path
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fs.create(pt2, true)));
				// TO append data to a file, use fs.append(Path f)

				for (String line : linesArray) {
					bw.write(line + ".");
					bw.newLine();
				}
				bw.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
