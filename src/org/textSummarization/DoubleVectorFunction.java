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
 * A function that can be applied to a double vector
 */
public interface DoubleVectorFunction {

	/**
	 * Calculates the result with a given index and value of a vector.
	 */
	public double calculate(int index, double value);

}