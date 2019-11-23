import java.util.ArrayList;

/**
 * This class provides a method to randomize the contents of an array
 * @author Salindor. Modified by O.Gonzalez-Recio in January 2010, to make 'temp' to be a list, instead a vector
 *
 */
public class Permutator{
     
     /**
      * This function does all the work.  It randomizes the array into the new
      * array it returns.  Granted if you were using this for real, you would
      * most likely want to use something other than the default java randomizer.
      * or you would at least want to seed it properly
      * @param a the original array
      * @return the new shuffled array
      */
     public static int[] permute (int a[])
     {
          //temp object we are going to use to return
          int ret[] = new int[a.length];
          
          //going to use a vector because they have element remove pre-implmented which
          //makes it easy for us
          //int temp[] = new int[a.length];
          ArrayList<Integer> temp = new ArrayList<Integer>();
          
          //copy the contents of the array into the vector, 
          for (int i=0; i<a.length; i++)
               temp.add(i,a[i]);
          
          //now that all the prework is done, here is the beautiful part
          int i=0; //index we are writting to
          while (i < ret.length)
          {
               int v = (int)(Math.random()* temp.size()); //generate a random number from 0- (size-1)
               if (v==temp.size()) continue;              //just in case, paranoid
               ret[i] = temp.remove(v);                 //uncomment for sampling w/o replacement
               //ret[i] = temp.get(v);                  //uncomment for sampling w replacement
//               System.out.println(ret[i]);
               i++;                                                 
          }
          
          return ret;
     }
     
     /**
      * Test driver -- generate an array of counting integers, then shuffle.
      * If the class works correctly, then the array should have all unique
      * randomly shuffled integers between 0 and v
      * @param args ignored
      */

}