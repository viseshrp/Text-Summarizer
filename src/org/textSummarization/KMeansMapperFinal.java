 

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.Text;


public class KMeansMapperFinal extends Mapper<LongWritable, Text, Text, Text> {
	
	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException,
			InterruptedException {
		String line = value.toString();
		String[] centerAndFile = line.split("\t");
		String clusterCenter = centerAndFile[0];
		String fileNameVector = centerAndFile[1];
		String[] fileNameSplit = fileNameVector.split("=");
		String fileName = fileNameSplit[0];
		context.write(new Text(clusterCenter), new Text(fileName));
	}

}
