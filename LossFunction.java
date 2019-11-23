/**
 * This class provides a method to calculate the Loss function of a given attribute.
 * The method implements two sort of loss functions: 
 *  	Info Gain (type=1) for classified covariates
 *  	MSE (type=2)for continuous covariates.
 *  	Pseudo-Huber Loss function (type=3).
 *  	Cost function on misclassification (type=4) for classification problems.
 *  	Gini index (type=5).
 *  
 *  More Loss functions can be added in the future.
 *
 */

public class LossFunction{

     
     /**
      * This class calculates the loss function
      * @param an integer number with the type of loss function
      * @return the value of the chosen loss function in a given node 
      */

	String type;

	public static double getLossFunctionNode (String type, Branch a, double phenotype[], double Genotype [][]){
		int i=0;
		double LF_val=0,mean=0;
		int nn=0;
		switch (Integer.parseInt(type)) {
			case 1: //Information gain
				LF_val=0.d;
				double IO=0.d;			
	    		IO=0.d;
	    		int nIO[] =new int [2];
	    		for (i=0;i<a.list.size();i++){
	    			nIO[(int)phenotype[a.list.get(i)]]++;
	    		}
	    		for (i=0;i<2;i++){
	    			if (nIO[i]>0){IO=IO-(nIO[i]/(float)a.list.size())*(Math.log(nIO[i]/(float)a.list.size())/Math.log(2));}
	    		}
				LF_val=IO;
				break;	
			case 2: //L2
				//read the IG for each SNPs in the sequences
				LF_val=0.d; 
	    		//Calculate mean for SNP j
        		mean=a.getMean(phenotype);
        		//Calculate mean squared error
        		for (i=0;i<a.list.size();i++){
        			LF_val=LF_val+(phenotype[a.list.get(i)]-mean)*(phenotype[a.list.get(i)]-mean);
        			nn++;
        		}    
        		LF_val=LF_val/(float)nn;
				break;				
			case 3: //pseudo-Huber loss function
				//read the IG for each SNPs in the sequences
				LF_val=0.d; 
	    		//Calculate mean for SNP j
        		mean=a.getMean(phenotype);
        		//Calculate huber loss function
        		for (i=0;i<a.list.size();i++){
        			LF_val=LF_val+Math.log(Math.cosh(phenotype[a.list.get(i)]-mean));
        			nn++;
        		}    
        		LF_val=LF_val/(float)nn;
				break;	
			case 4: //False Positive and False Negative cost function
				//read the IG for each SNPs in the sequences
				LF_val=0.d; 
				
				//This is the cost variable of incorrectly classify individuals
					double [] cost= new double[2];
					cost[0]=2; //Cost of a false positive (individual incorrectly assigned y_hat=1  
					cost[1]=1; //Cost of a false negative (individual incorrectly assigned y_hat=0
					
	    		//Calculate mean for SNP j
        		mean=0.d;
        		for (i=0;i<a.list.size();i++){
        			mean=mean+phenotype[a.list.get(i)];
        			nn++;
        		}
        		mean=mean/(float)nn;
        		for (i=0;i<a.list.size();i++){
       				if ((int)phenotype[a.list.get(i)] != (int)mean){;
        				LF_val=LF_val+cost[(int)phenotype[a.list.get(i)]]; 
        			}
				}
        		//LF_val=LF_val/(float)nn;
				break;
			case 5: //Gini index
				//read the IG for each SNPs in the sequences
				LF_val=0.d;
				double GI=0.d;
				int nGI[] =new int [3]; //nIG[phenotype_group]
				nn=0;
           		//Calculate Gini index
        		for (i=0;i<a.list.size();i++){
        				nGI[(int)phenotype[a.list.get(i)]]++;
        				nn++;
        		}
        		GI=(nGI[0])*(nGI[1])+
        		   (nGI[0])*(nGI[2])+
        		   (nGI[1])*(nGI[2]);
        		LF_val=GI/(float)(nn*nn);
				break;			
			default: //Wrong entered number 
				System.out.println("Error!  Illegal option number of Loss Function type!  I quit!");
				System.exit(1);
		} //end of switch statement
		return LF_val;
	}
	public static double getLossFunctionSplit (String type, int snp, Branch a, double phenotype[], double Genotype [][]){
		int i=0;
		double LF_val=0,mean=0;
		double mean_right=0.0d,mean_left=0.0d;
		int n_right=0, n_left=0;
		switch (Integer.parseInt(type)) {
			case 1: //Information gain
				LF_val=0.d;
				double IO=0.d, Ij=0.d;			
				int nIG[] []=new int [3][3]; //nIG[genotype_group][phenotype_group]
	    		IO=0.d;
	    		int nIO[] =new int [2];
	    		for (i=0;i<a.list.size();i++){
	    			nIO[(int)phenotype[a.list.get(i)]]++;
	    		}
	    		for (i=0;i<2;i++){
	    			if (nIO[i]>0){IO=IO-(nIO[i]/(float)a.list.size())*(Math.log(nIO[i]/(float)a.list.size())/Math.log(2));}
	    		}
   		//	Calculate Information gain for SNP j
				for (i=0;i<a.list.size();i++){
					nIG[(int)Genotype[a.list.get(i)][snp]][(int)phenotype[a.list.get(i)]]++;
				}
				LF_val=0.d;//I0;
				for (i=0; i<3; i++){
					Ij=0.0d;
					if (nIG[i][0] != 0){
						Ij=Ij-(nIG[i][0]/(float)(nIG[i][0]+nIG[i][1]+nIG[i][2]))*(Math.log(nIG[i][0]/(float)(nIG[i][0]+nIG[i][1]+nIG[i][2]))/Math.log(2));
					}
					if (nIG[i][1] != 0){			
						Ij=Ij-(nIG[i][1]/(float)(nIG[i][0]+nIG[i][1]+nIG[i][2]))*(Math.log(nIG[i][1]/(float)(nIG[i][0]+nIG[i][1]+nIG[i][2]))/Math.log(2));
					}
					if (nIG[i][2] != 0){			
   						Ij=Ij-(nIG[i][2]/(float)(nIG[i][0]+nIG[i][1]+nIG[i][2]))*(Math.log(nIG[i][2]/(float)(nIG[i][0]+nIG[i][1]+nIG[i][2]))/Math.log(2));
   					}
					Ij=Ij*(nIG[i][0]+nIG[i][1]+nIG[i][2])/(float)a.list.size();
					LF_val=LF_val-Ij;
				}
				LF_val=LF_val*(-1.d);
				break;	
			case 2: //L2
				//read the IG for each SNPs in the sequences
				LF_val=0.d; 
	    		//Calculate mean for SNP j
        		mean=0.d;
        		for (i=0;i<a.list.size();i++){
        			mean=mean+Genotype[a.list.get(i)][snp]/a.list.size();
        		}    
        		//Calculate mean squared error
        		mean_right=0.0d;mean_left=0.0d;
        		n_right=0; n_left=0;
        		for (i=0;i<a.list.size();i++){
        			if (Genotype[a.list.get(i)][snp]<=mean){
        				mean_right=mean_right+phenotype[a.list.get(i)];
        				n_right++;
        			}else if (Genotype[a.list.get(i)][snp]>mean){
        				mean_left=mean_left+phenotype[a.list.get(i)];
        				n_left++;
        			}
        		}        		
        		mean_right=mean_right/(n_right);mean_left=mean_left/(n_left);
        		//Calculate MSE for SNP j
        		int nn=0;double temp=0.0d;
        		for (i=0;i<a.list.size();i++){
        			if (Genotype[a.list.get(i)][snp]<=mean){
        				temp=phenotype[a.list.get(i)];
        				LF_val=LF_val+( temp-mean_right )*( temp-mean_right );
        				nn++;
        			}else if (Genotype[a.list.get(i)][snp]>mean){
        				temp=phenotype[a.list.get(i)];
        				LF_val=LF_val+( temp-mean_left )*( temp-mean_left );
        				nn++;
        			}
        		}
        		LF_val=LF_val/(float)nn;
				break;				
			case 3: //pseudo-Huber loss function
				//read the IG for each SNPs in the sequences
				LF_val=0.d; 
	    		//Calculate mean for SNP j
        		mean=0.d;
        		for (i=0;i<a.list.size();i++){
        			mean=mean+Genotype[a.list.get(i)][snp]/a.list.size();
        		}    
        		//Calculate mean squared error
        		mean_right=0.0d;mean_left=0.0d;
        		n_right=0; n_left=0;
        		for (i=0;i<a.list.size();i++){
        			if (Genotype[a.list.get(i)][snp]<=mean){
        				mean_right=mean_right+phenotype[a.list.get(i)];
        				n_right++;
        			}else if (Genotype[a.list.get(i)][snp]>mean){
        				mean_left=mean_left+phenotype[a.list.get(i)];
        				n_left++;
        			}
        		}        		
        		mean_right=mean_right/(n_right);mean_left=mean_left/(n_left);
        		//Calculate MSE for SNP j
        		nn=0; temp=0.0d;
        		for (i=0;i<a.list.size();i++){
        			if (Genotype[a.list.get(i)][snp]<=mean){
        				temp=phenotype[a.list.get(i)];
        				LF_val=LF_val+Math.log(Math.cosh(temp-mean_right)); 
        				nn++;
        			}else if (Genotype[a.list.get(i)][snp]>mean){
        				temp=phenotype[a.list.get(i)];
        				LF_val=LF_val+Math.log(Math.cosh(temp-mean_left));
        				nn++;
        			}
        		}
        		LF_val=LF_val/(float)nn;
				break;	
			case 4: //False Positive and False Negative cost function
				//read the IG for each SNPs in the sequences
				LF_val=0.d; 
				
				//This is the cost variable of incorrectly classify individuals
					double [] cost= new double[2];
					cost[0]=2; //Cost of a false positive (individual incorrectly assigned y_hat=1
					cost[1]=1; //Cost of a false negative (individual incorrectly assigned y_hat=0 
					
	    		//Calculate mean for SNP j
        		mean=0.d;
        		for (i=0;i<a.list.size();i++){
        			mean=mean+Genotype[a.list.get(i)][snp]/a.list.size();
        		}    
        		//Calculate mean squared error
        		mean_right=0.0d;mean_left=0.0d;
        		n_right=0; n_left=0;
        		for (i=0;i<a.list.size();i++){
        			if (Genotype[a.list.get(i)][snp]<=mean){
        				mean_right=mean_right+phenotype[a.list.get(i)];
        				n_right++;
        			}else if (Genotype[a.list.get(i)][snp]>mean){
        				mean_left=mean_left+phenotype[a.list.get(i)];
        				n_left++;
        			}
        		}        		
        		mean_right=Math.round(mean_right/(n_right));mean_left=Math.round(mean_left/(n_left));
              //Calculate cost function for SNP j
        		nn=0; temp=0.0d;
        		for (i=0;i<a.list.size();i++){
        			if (Genotype[a.list.get(i)][snp]<=mean){
        				if ((int)phenotype[a.list.get(i)] != (int)mean_right){;
        					LF_val=LF_val+cost[(int)phenotype[a.list.get(i)]];// /(float)n_right; 
        				}
        			}else if (Genotype[a.list.get(i)][snp]>mean){
        				if ((int)phenotype[a.list.get(i)] != (int)mean_left){;
        					LF_val=LF_val+cost[(int)phenotype[a.list.get(i)]];// /(float)n_left;
        				}
        			}
				}
        		//LF_val=0.5d*LF_val;
				break;
			case 5: //Gini index
				//read the IG for each SNPs in the sequences
				LF_val=0.d;
				double GI=0.d,i_left=0.d,i_right=0.d;
				int nGI[] []=new int [3][2]; //nIG[phenotype_group][child_node]
				n_right=0; n_left=0;
	    		//Calculate mean for SNP j
        		mean=0.d;
        		for (i=0;i<a.list.size();i++){
        			mean=mean+Genotype[a.list.get(i)][snp]/a.list.size();
        		} 
           		//Calculate Gini index
        		for (i=0;i<a.list.size();i++){
        			if (Genotype[a.list.get(i)][snp]<=mean){
        				nGI[(int)phenotype[a.list.get(i)]][0]++;
        				n_left++;
        			}else if (Genotype[a.list.get(i)][snp]>mean){
        				nGI[(int)phenotype[a.list.get(i)]][1]++;
        				n_right++;
        			}
        		}
				i_left=(nGI[0][0])*(nGI[1][0])+
				  (nGI[0][0])*(nGI[2][0])+
				  (nGI[1][0])*(nGI[2][0]);
				i_right=(nGI[0][1])*(nGI[1][1])+
				  (nGI[0][1])*(nGI[2][1])+
				  (nGI[1][1])*(nGI[2][1]);	
				GI=0.5d*i_left/(float)(n_left*n_left)+0.5d*i_right/(float)(n_right*n_right);
        		LF_val=GI;
				break;			
			default: //Wrong entered number 
				System.out.println("Error!  Illegal option number of Loss Function type!  I quit!");
				System.exit(1);
		} //end of switch statement
		return LF_val;
	}
	public static double getLossFunctionOOB (String type, Branch a, double phenotype[], double yhat){
		int i=0;
		double LF_val=0;
		int nn=0;
		switch (Integer.parseInt(type)) {
			case 1: //Information gain
				LF_val=0.d;
				double IO=0.d;			
	    		IO=0.d;
	    		int nIO[] =new int [2];
	    		for (i=0;i<a.list.size();i++){
	    			nIO[(int)phenotype[a.list.get(i)]]++;
	    		}
	    		for (i=0;i<2;i++){
	    			if (nIO[i]>0){IO=IO-(nIO[i]/(float)a.list.size())*(Math.log(nIO[i]/(float)a.list.size())/Math.log(2));}
	    		}
				LF_val=IO;
				break;	
			case 2: //L2
				//read the IG for each SNPs in the sequences
				LF_val=0.d; 
        		//Calculate mean squared error
        		for (i=0;i<a.list.size();i++){
        			LF_val=LF_val+(phenotype[a.list.get(i)]-yhat)*(phenotype[a.list.get(i)]-yhat);
        		}    
				break;				
			case 3: //pseudo-Huber loss function
				//read the IG for each SNPs in the sequences
				LF_val=0.d; 
        		//Calculate huber loss function
        		for (i=0;i<a.list.size();i++){
        			LF_val=LF_val+Math.log(Math.cosh(phenotype[a.list.get(i)]-yhat));
        		}    
				break;	
			case 4: //False Positive and False Negative cost function
				//read the IG for each SNPs in the sequences
				LF_val=0.d; 
				
				//This is the cost variable of incorrectly classify individuals
					double [] cost= new double[2];
					cost[0]=2; //Cost of a false positive (individual incorrectly assigned y_hat=1  
					cost[1]=1; //Cost of a false negative (individual incorrectly assigned y_hat=0

        		for (i=0;i<a.list.size();i++){
       				if ((int)phenotype[a.list.get(i)] != (int)yhat){;
        				LF_val=LF_val+cost[(int)phenotype[a.list.get(i)]]; 
        			}
				}
				break;
			case 5: //Gini index
				//read the IG for each SNPs in the sequences
				LF_val=0.d;
				double GI=0.d;
				int nGI[] =new int [3]; //nIG[phenotype_group]
				nn=0;
           		//Calculate Gini index
        		for (i=0;i<a.list.size();i++){
        				nGI[(int)phenotype[a.list.get(i)]]++;
        				nn++;
        		}
        		GI=(nGI[0])*(nGI[1])+
        		   (nGI[0])*(nGI[2])+
        		   (nGI[1])*(nGI[2]);
        		LF_val=GI/(float)(nn);
				break;			
			default: //Wrong entered number 
				System.out.println("Error!  Illegal option number of Loss Function type!  I quit!");
				System.exit(1);
		} //end of switch statement
		return LF_val;
	}
}
