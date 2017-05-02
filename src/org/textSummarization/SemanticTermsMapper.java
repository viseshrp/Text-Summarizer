 

import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.Synset;
import java.text.ParseException;

public class SemanticTermsMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
	private final static IntWritable one = new IntWritable(1);

	public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {
		try {
			String term = lineText.toString();
			Text currTopic = new Text(term);

			WordNetDatabase database = WordNetDatabase.getFileInstance();
			if (term != null) {
				Synset[] similarTerms = database.getSynsets(term);

				if (similarTerms.length > 0) {
					for (int i = 0; i < similarTerms.length; i++) {

						String[] wordForms = similarTerms[i].getWordForms();
						for (int j = 0; j < wordForms.length; j++) {
							context.write(new Text(wordForms[j]), one);
						}
					}
				}
			}
			context.write(currTopic, one);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}