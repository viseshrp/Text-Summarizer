 

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.Text;

// first iteration, k-random centers, in every follow-up iteration we have new calculated centers
public class KMeansMapper extends Mapper<LongWritable, Text, ClusterCenter, Text> {

	private final List<ClusterCenter> centers = new ArrayList<>();
	private DistanceMeasurer distanceMeasurer;

	@SuppressWarnings("deprecation")
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
			super.setup(context);
			Configuration conf = context.getConfiguration();
			Path centroids = new Path(conf.get("centroid.path"));
			FileSystem fs = FileSystem.get(conf);

			try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, centroids, conf)) {
				ClusterCenter key = new ClusterCenter();
				IntWritable value = new IntWritable();
				int index = 0;
				while (reader.next(key, value)) {
					ClusterCenter clusterCenter = new ClusterCenter(key);
					clusterCenter.setClusterIndex(index++);
					centers.add(clusterCenter);
				}
			}
			distanceMeasurer = new ManhattanDistance();
		}

	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException,
			InterruptedException {
		ClusterCenter nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		String line = value.toString();
		String fileName, vectorValues;
		String[] fileNameAndValues;
		String[] sec = line.split("\t");
		if(sec.length==1){
			fileNameAndValues = line.split("=");
			fileName = fileNameAndValues[0];
			vectorValues = fileNameAndValues[1];
			//System.out.println("mapper filename"+fileName+"values"+vectorValues);
		}else{
			fileNameAndValues = sec[1].split("=");
			fileName = fileNameAndValues[0];
			vectorValues = fileNameAndValues[1];
			//System.out.println("mapper filename"+fileName+"values"+vectorValues);
		}
		Pattern p = Pattern.compile("\\[(.*?)\\]");
		Matcher m = p.matcher(vectorValues);
		String v=null;
		while(m.find()) {
		    v = m.group(1);
		}
		//String[] vec1 = vectors.split("[(.*?)]");
		//System.out.println("brackets content"+v);
		String[] vec = v.split(",");
		double[] vecArray = new double[vec.length];		
		for(int i=0;i<vec.length;i++){
			String trim = vec[i].replaceAll("\\s+","");
			vecArray[i] = Double.parseDouble(trim);
		}
		VectorWritable vw = new VectorWritable(vecArray);
		System.out.println("mapper VW"+vw+"dim"+vw.getVector().getDimension());
		for (ClusterCenter c : centers) {
			double dist = distanceMeasurer.measureDistance(c.getCenterVector(), vw.getVector());
			if (nearest == null) {
				nearest = c;
				nearestDistance = dist;
			} else {
				if (nearestDistance > dist) {
					nearest = c;
					nearestDistance = dist;
				}
			}
		}
		String finalValue = fileName +"="+ vw;
		context.write(nearest, new Text(finalValue));
	}

}
