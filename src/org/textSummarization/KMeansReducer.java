
/*=============================================================================
|   Assignment:  Final Project - Multiple Document Summarization
|       Author:  Group7 - (Sampath, Ajay, Visesh)
|       Grader:  Walid Shalaby
|
|       Course:  ITCS 6190
|   Instructor:  Srinivas Akella
|
|     Language:  Java 
|     Version :  1.8.0_101
|                
| Deficiencies:  No logical errors.
*===========================================================================*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.Text;

/*calculate a new cluster center for these vertices
 * 
 */
public class KMeansReducer extends Reducer<ClusterCenter, Text, ClusterCenter, Text> {

	public static enum Counter {
		CONVERGED
	}

	private final List<ClusterCenter> centers = new ArrayList<>();

	@Override
	protected void reduce(ClusterCenter key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		// List<VectorWritable> vectorList = new ArrayList<>();
		List<String> fileNameVectorList = new ArrayList<>();
		DoubleVector newCenter = null;
		String fileName = null;
		String vectors;
		for (Text value : values) {
			String[] fileNameandVec = value.toString().split("=");
			fileName = fileNameandVec[0];
			vectors = fileNameandVec[1];
			Pattern p = Pattern.compile("\\[(.*?)\\]");
			Matcher m = p.matcher(vectors);
			String v = null;
			while (m.find()) {
				v = m.group(1);
			}
			String[] vec = v.split(",");
			double[] vecArray = new double[vec.length];
			for (int i = 0; i < vec.length; i++) {
				String trim = vec[i].replaceAll("\\s+", "");
				vecArray[i] = Double.parseDouble(trim);
			}
			VectorWritable dv = new VectorWritable(vecArray);
			fileNameVectorList.add(fileName + "#" + dv.toString());
			// find new cluster centers
			if (newCenter == null)
				newCenter = dv.getVector().deepCopy();
			else
				newCenter = newCenter.add(dv.getVector());
		}

		newCenter = newCenter.divide(fileNameVectorList.size());
		ClusterCenter center = new ClusterCenter(newCenter);
		centers.add(center);
		for (String vector : fileNameVectorList) {
			String[] fileVector = vector.split("#");
			context.write(center, new Text(fileVector[0] + "=" + fileVector[1]));
		}

		if (center.converged(key))
			context.getCounter(Counter.CONVERGED).increment(1);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void cleanup(Context context) throws IOException, InterruptedException {
		super.cleanup(context);
		Configuration conf = context.getConfiguration();
		Path outPath = new Path(conf.get("centroid.path"));
		FileSystem fs = FileSystem.get(conf);
		fs.delete(outPath, true);
		try (SequenceFile.Writer out = SequenceFile.createWriter(fs, context.getConfiguration(), outPath,
				ClusterCenter.class, IntWritable.class)) {
			final IntWritable value = new IntWritable(0);
			for (ClusterCenter center : centers) {
				out.append(center, value);
			}
		}
	}
}
