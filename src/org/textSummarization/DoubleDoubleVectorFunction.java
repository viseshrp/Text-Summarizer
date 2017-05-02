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

/**
 * A function that can be applied to two double vectors.
 * 
 */
public interface DoubleDoubleVectorFunction {
	/**
	 * Calculates the result of the left and right value of two vectors at a
	 * given index.
	 */
	public double calculate(int index, double left, double right);
}