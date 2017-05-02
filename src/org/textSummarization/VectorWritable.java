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
 

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

/**
 * Writable and comparable wrapper for dense vectors.
 * 
 * 
 */
public final class VectorWritable implements WritableComparable<VectorWritable> {

	private DoubleVector vector;

	public VectorWritable() {
		super();
	}

	public VectorWritable(VectorWritable v) {
		this.vector = v.getVector();
	}

	public VectorWritable(DenseDoubleVector v) {
		this.vector = v;
	}

	public VectorWritable(double x) {
		this.vector = new DenseDoubleVector(new double[] { x });
	}

	public VectorWritable(double x, double y) {
		this.vector = new DenseDoubleVector(new double[] { x, y });
	}

	public VectorWritable(double[] arr) {
		this.vector = new DenseDoubleVector(arr);
	}

	@Override
	public final void write(DataOutput out) throws IOException {
		writeVector(this.vector, out);
	}

	@Override
	public final void readFields(DataInput in) throws IOException {
		this.vector = readVector(in);
	}

	@Override
	public final int compareTo(VectorWritable o) {
		return compareVector(this, o);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((vector == null) ? 0 : vector.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VectorWritable other = (VectorWritable) obj;
		if (vector == null) {
			if (other.vector != null)
				return false;
		} else if (!vector.equals(other.vector))
			return false;
		return true;
	}

	/**
	 * @return the vector
	 */
	public DoubleVector getVector() {
		return vector;
	}

	@Override
	public String toString() {
		return vector.toString();
	}

	public static void writeVector(DoubleVector vector, DataOutput out) throws IOException {
		out.writeInt(vector.getLength());
		for (int i = 0; i < vector.getDimension(); i++) {
			out.writeDouble(vector.get(i));
		}
	}

	public static DoubleVector readVector(DataInput in) throws IOException {
		final int length = in.readInt();
		DoubleVector vector = new DenseDoubleVector(length);
		for (int i = 0; i < length; i++) {
			vector.set(i, in.readDouble());
		}
		return vector;
	}

	public static int compareVector(VectorWritable a, VectorWritable o) {
		return compareVector(a.getVector(), o.getVector());
	}

	public static int compareVector(DoubleVector a, DoubleVector o) {
		DoubleVector subtract = a.subtract(o);
		return (int) subtract.sum();
	}

	public static VectorWritable wrap(DenseDoubleVector a) {
		return new VectorWritable(a);
	}

}
