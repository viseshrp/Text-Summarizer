 
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;


public class SortComparator extends WritableComparator {

	protected SortComparator() {
		super(IntWritable.class, true);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int compare(WritableComparable o1, WritableComparable o2) {
		IntWritable k1 = (IntWritable) o1;
		IntWritable k2 = (IntWritable) o2;
		int cmp = k1.compareTo(k2);
		return -1 * cmp;
	}
}
