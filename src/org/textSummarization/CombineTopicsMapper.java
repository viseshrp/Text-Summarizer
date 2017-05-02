 

import 
java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import edu.smu.tspell.wordnet.*;
import java.text.ParseException;

public class CombineTopicsMapper extends Mapper<LongWritable ,  Text ,  Text ,  Text > {
    private final static IntWritable one  = new IntWritable(1);
    //private static final Pattern WORD_BOUNDARY = Pattern .compile("\\s*\\b\\s*");

    public void map( LongWritable offset,  Text lineText,  Context context)
      throws  IOException,  InterruptedException {
       try{
       String term  = lineText.toString();
       Text currlines = new Text(term);
       System.out.println("terms"+term);
       //WordNetDatabase database = WordNetDatabase.getFileInstance();
       
       context.write(new Text("Key"),currlines);
    }catch(Exception ex){
    	System.out.println(ex);
    }
}
}