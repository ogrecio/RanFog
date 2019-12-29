import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.*;
import java.text.DecimalFormat;

//////////////////////////////////////////////////////////////////////////////
// Title:            Random Forest for classified and regression problems 
// Files:            RanFog.java; Branch.java; Permutator.java
//
// Author:           Oscar Gonzalez-Recio 
// email: 			 gonzalez.oscar@inia.es
//
// 					 Madrid, 2010
//
//////////////////////////// 80 columns wide //////////////////////////////////

public class RanFog{

	public static void main(String[] args) throws IOException {
		/**
		 * Program execution starts here. 
		 * This method construct a random forest (Breiman, 2001. Machine Learning, 45)
		 * for classification data (should be score as 0 or 1).
		 *  Results are written to files:
		 *    "Trees.txt" stores the miss-classification rate in the training set and the oob set at each tree
		 *    "Trees.test" stores the miss-classification rate in the testing set at each tree
		 *    "Variable_Importance.txt" stores the importance variable for each feature
		 *    "TimesSelected.txt" stores the number of times each feature was selected
		 *
		 *The methods required a parameter file called 'params.txt' that must be located in the same folder as RanFog
		 * The main method loads different parameters from this file 
		 */

/**%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
   Load parameter file                                                           */

		Properties demoProperties = new Properties();
		try {
			FileInputStream stream =new FileInputStream("params.txt");
			demoProperties.load(stream);
		} catch (FileNotFoundException e) {
			System.out.println("Parameter file 'params.txt' not found. ");
		}

		//Max number of trees to be constructed
		int max_tree=Integer.parseInt(demoProperties.getProperty("ForestSize"));
		//Number of classified Features
		int N_SNP = Integer.parseInt(demoProperties.getProperty("N_features"));
		//Name of training file; Load training file
		File trnFile = new File(demoProperties.getProperty("training"));
		//Name of testing file; Load testing file
		File tstFile = new File(demoProperties.getProperty("testing"));
		//Number of Features randomly selected at each node
		double m=Double.parseDouble(demoProperties.getProperty("m")); //Percentage of Features randomly selected at each node
		//Max number of branches allowed
		int max_branch=Integer.parseInt(demoProperties.getProperty("max_branch"));
		//Loss function used for discrete features
//		String LF_d=demoProperties.getProperty("LossFunction_discrete");
		//Loss function used for continuous features
		String LF_c=demoProperties.getProperty("LossFunction");
		double false_positive_cost; double false_negative_cost;
		if (null == demoProperties.getProperty("false_positive_cost")) {
			false_positive_cost=0;
		}else{
			false_positive_cost=Double.parseDouble(demoProperties.getProperty("false_positive_cost"));
		}
		if (null == demoProperties.getProperty("false_negative_cost")) {
			false_negative_cost=0;
		}else{
			false_negative_cost=Double.parseDouble(demoProperties.getProperty("false_negative_cost"));
		}

/**End loading parameter file
 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

		//Set the arguments to the respective variables.
		System.out.println("Number of trees to be grown: "+max_tree);
		//int N_SNP=Integer.parseInt(args[3]); //Number of classified Features
		int N_attributes=0; //Number of total features
		System.out.println("Number of SNPs (classified Features): "+N_SNP);
		System.out.print("RanFoG will run with Loss Function option: ");
		if (Integer.parseInt(LF_c) == 1){
			System.out.println("Information Gain");
		}else if(Integer.parseInt(LF_c) ==2){
			System.out.println("Mean Squared Error (L2 function)");
		}else if(Integer.parseInt(LF_c) ==3){
			System.out.println("Pseudo Huber");
		}else if(Integer.parseInt(LF_c) ==4){
			System.out.println("Personalized Cost Function for categories");
		}else if(Integer.parseInt(LF_c) ==5){
			System.out.println("Gini Index");
		}
		System.out.println();

		
		/**Initialize counter variables*/
		int  j=0, k=0, i=0, N_tot=0, N_tst=0, N_oob=0;

		// read the number of lines in the training file
		try {
			BufferedReader inFile = new BufferedReader(new FileReader(trnFile));
			String line;
			while ( (line=inFile.readLine()) != null ) {
				N_tot = N_tot+1;
	        	StringTokenizer st = new StringTokenizer(line, " ");
	        	// b/w "" put the delimiter(,). 
	            // st.nextToken() is a String, so you can manipulate on it e.g.:
	        	//System.out.println("number of columns="+st.countTokens());
	        	if (N_tot ==1) {N_attributes=st.countTokens()-2;}
	        	if (st.countTokens() != N_attributes+2){
	        		System.out.println("Training file with less columns than expected at line "+N_tot);
	        		System.runFinalization();
	        	}
			}
			inFile.close();
		} catch (FileNotFoundException e) {
			System.out.println("Training file for training set not found. ");
		}
		System.out.println("Number of genotypes (lines) in training set: "+N_tot);
		System.out.println("Number of total Attributes detected: "+N_attributes);
		System.out.println();

		// read the number of lines in the testing file
		try {
			BufferedReader testing = new BufferedReader(new FileReader(tstFile));
			String line;
			while ( (line=testing.readLine()) != null ) {
				N_tst = N_tst+1;
	        	StringTokenizer st = new StringTokenizer(line, " ");
	        	if (st.countTokens() != N_attributes+2){
	        		System.out.println("Testing file with less columns than expected at line "+i);
	        		System.runFinalization();
	        	}
			}
			testing.close();
		} catch (FileNotFoundException e) {
			System.out.println("Testing file for training set not found. ");
		}
		System.out.println("Number of genotypes (lines) in testing set: "+N_tst);
		
/**%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 Declaracion de variables                                                 */

		//Variables read from files
		double phenotype[]= new double [N_tot];String ID[]= new String [N_tot];
		double phenotype_tst[]= new double [N_tst];String ID_tst[]= new String [N_tst];
		double Genotype [] [] = new double [N_tot][N_attributes];
		double Genotype_tst [] [] = new double [N_tst][N_attributes];
		
		//Variables involved in the trees
		double mean_j,  minLoss, MSE_tree, MSEval_tree, node_mse, temp;
		int node, n_branch, n_tree;
		int FeatureSel [] =new int[2];//Feature selected at each node [regression feature,classified features]
		//out of bag variables
		int oob[]=new int[N_tot];
		double MSE_oob, MSE_vi, MSE_oob_ave=0.d;
		//Predictive and estimated variables
		double GEBV[][]= new double [N_tot][2]; //Predicted phenotype in training set
		double y_hat[]= new double [N_tst]; //Predicted phenotype in testing set
		int Selected[]=new int[N_attributes]; //number of times SNPs are selected
		double VI[]=new double[N_attributes];

		//Random number generator
		Random x1 =new Random(); 

		//Information gain variables
		double Loss=0.d;

		//Output files
		PrintWriter outTree = new PrintWriter ("Trees.txt");
		PrintWriter outTreeTest = new PrintWriter ("Trees.test");
		PrintWriter outSel = new PrintWriter ("TimesSelected.txt");
		PrintWriter outVI = new PrintWriter ("Variable_Importance.txt");
		PrintWriter outEGBV = new PrintWriter ("EGBV.txt");
		PrintWriter outPred = new PrintWriter ("Predictions.txt");
/**%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
		
/**
 * 1. Start reading files*/
		//read the training set file
		i=0;
		try {
			BufferedReader inFile = new BufferedReader(new FileReader(trnFile));
	        String line;
	        //inFile.readLine(); //Read header
	        while ((line=inFile.readLine()) != null ) {			 
				i++;
	        	StringTokenizer st = new StringTokenizer(line, " ");
	        	// b/w "" put the delimiter(,). In case there are arbitrary spaces
	            // in addition to commas, e.g., a, b, c, add a single space after t$
	            // st.nextToken() is a String, so you can manipulate on it e.g.:
	        	phenotype[i-1]=Double.parseDouble(st.nextToken());
	        	ID[i-1]=st.nextToken();
	        	//st.nextToken();//***** to not read other fields!!****
				for (j=0; j<N_attributes;j++){  //for each SNP two dummy variables are created
					Genotype[i-1][j]=Double.parseDouble(st.nextToken());
	        	}
			}
			inFile.close();	
		} catch (FileNotFoundException e) {
			System.out.println("Training file for Features with wrong format. ");
		} //end reading training file
		
		//read the testing set file
		i=0;
		try {
			BufferedReader testing = new BufferedReader(new FileReader(tstFile));
	        String line;
	        //inFile.readLine(); //Read header
	        while ((line=testing.readLine()) != null ) {
				i++;
	        	StringTokenizer st = new StringTokenizer(line, " ");
	        	phenotype_tst[i-1]=Double.parseDouble(st.nextToken());
	        	ID_tst[i-1]=st.nextToken();
	        	//st.nextToken();//***** to not read other fields!!****
	        	for (j=0; j<N_attributes;j++){  //for each SNP
	        		Genotype_tst[i-1][j]=Double.parseDouble(st.nextToken());
	        	}
			}
			testing.close();	
		} catch (FileNotFoundException e) {
			System.out.println("Testing file for Features with wrong format. ");
		} //end reading testing file	
/**
   1. End of reading files
   **/
		
/**
 * 2. Starts the forest */
	n_tree=0;
	while (n_tree < max_tree){
		j=0;k=0;i=0;n_branch=0;N_oob=0;
		MSE_tree=0.d;MSEval_tree=0.d;MSE_oob=0.d;
		oob=new int[N_tot];
		ArrayList<Integer> SNP_tree = new ArrayList<Integer>();
		Branch branch[]= new Branch [max_branch];
		Branch branch_tst[]= new Branch [max_branch];		
		Branch branch_oob[]= new Branch [max_branch];		
		branch[n_branch]=new Branch();branch_tst[n_branch]=new Branch();branch_oob[n_branch]=new Branch();
	
	//Get bootstrapped sample from data, and store pointer-positions in the list of branch zero
		for (i=0;i<N_tot;i++){ 
			int u=x1.nextInt( N_tot );
			branch[n_branch].list.add(i, u);
		}		
	//Get out of bag data
		for (i=0;i<N_tot;i++){ 
			oob[branch[n_branch].list.get(i)]=1;
		}
		for (i=0;i<N_tot;i++){
			if (oob[i]==0) {branch_oob[n_branch].list.add(i);N_oob++;}
		}
		int pointer_oob[]=new int[N_tot];
		for (i=0;i<N_tot;i++){ //Store positions of oob observations
			if (oob[i]==0) {pointer_oob[k]=i;k++;}
		}
		k=0;
	//Store pointer-positions of testing set in the branch zero for testing set.
		for (i=0;i<N_tst;i++){ 
			branch_tst[n_branch].list.add(i, i);
		}
	//Construct the tree. Grow branches until size<5 or not better classification is achieved	
		for (k=0;k<n_branch+1;k++){ 
          if (branch[k].list.size()>5){ //Minimum size=5
        	node=N_attributes;minLoss=99999999.d;
    		//Calculate Entropy in branch[k]
    		node_mse=0.d;	
    		node_mse=LossFunction.getLossFunctionNode(LF_c, branch[k], phenotype, Genotype, false_positive_cost, false_negative_cost);

    		for (j=0; j<N_attributes;j++){  //calculate MSE, and select that SNP minimizing MSE
        	 if (x1.nextDouble()< m/(1.d*N_attributes) ){ //Select only sqrt(N_attributes) variables
            	Loss=LossFunction.getLossFunctionSplit(LF_c, j, branch[k], phenotype, Genotype, false_positive_cost, false_negative_cost);		 
            	if (Loss<minLoss){  //For non-classified attributes
                   	 //Calculate mean for SNP j
                   	mean_j=0.d;
                   	for (i=0;i<branch[k].list.size();i++){
                   		mean_j=mean_j+Genotype[branch[k].list.get(i)][j]/branch[k].list.size();
                   	} 
                   	FeatureSel[0]=j;
           			minLoss=Loss;
                }
        		
        		//Decide keeping the classified or the non-classified attribute
        		if (node_mse<=minLoss){
        			 node=N_attributes;
        		}else{  
        			//Select non-classified Feature, and calculate its mean
            		mean_j=0.d;
            		for (i=0;i<branch[k].list.size();i++){
            			mean_j=mean_j+Genotype[branch[k].list.get(i)][FeatureSel[0]]/branch[k].list.size();
            		} 
            		node=FeatureSel[0];
        			branch[k].Feature=FeatureSel[0];
        			branch[k].mean_snp=mean_j;		
        		}
        	  }
        	}
        	if (node!=N_attributes){ //Create a new branch, only if MSE of the previous branch is minimized
        		Selected[node]++;
        		SNP_tree.add(node); //add the selected SNP to the end of the list
        		
        		//Create right branch for the bootstrapped sample of the training set and for the testing set 
        		n_branch++;branch[k].Child1=n_branch;
        		branch[n_branch]=new Branch();branch_tst[n_branch]=new Branch();
        		for (i=0;i<branch[k].list.size();i++){
        			if (Genotype[branch[k].list.get(i)][node]<=branch[k].mean_snp){
        				branch[n_branch].list.add( branch[k].list.get(i) );
        			}
        		}
        		for (i=0;i<branch_tst[k].list.size();i++){
        			if (Genotype_tst[branch_tst[k].list.get(i)][node]<=branch[k].mean_snp){
        				branch_tst[n_branch].list.add( branch_tst[k].list.get(i) );
        			}
        		}
        		
        		//Create left branch for the bootstrapped sample of the training set and for the testing set 
        		n_branch++;branch[k].Child2=n_branch;
        		branch[n_branch]=new Branch();branch_tst[n_branch]=new Branch();
        		for (i=0;i<branch[k].list.size();i++){
        			if (Genotype[branch[k].list.get(i)][node]>branch[k].mean_snp){
        				branch[n_branch].list.add(  branch[k].list.get(i) );
        			}
        		}
        		for (i=0;i<branch_tst[k].list.size();i++){
        			if (Genotype_tst[branch_tst[k].list.get(i)][node]>branch[k].mean_snp){
        				branch_tst[n_branch].list.add( branch_tst[k].list.get(i) );
        			}
        		}
        		//outBranch.print( branch[k].snp+" "+branch[k].child1+" "+branch[k].child2+" "+branch[k].getMean(phenotype)+" " );
        	}else { //No-SNP has reduced miss-classification of previous branch
        	  branch[k].status="F"; //the branch is set as dead-end branch
        	  //outBranchMSE.print( branch[k].list.size()+" "+branch[k].getMSE(phenotype)+" " );
    		  MSE_tree=MSE_tree+branch[k].getMSE(phenotype);
      		  for (i=0;i<branch[k].list.size();i++){ //Accumulate classification to estimate genomic value
    			  GEBV[branch[k].list.get(i)][0]=GEBV[branch[k].list.get(i)][0]+branch[k].getMean(phenotype);
    			  GEBV[branch[k].list.get(i)][1]++;
    		  }
      		  for (i=0;i<branch_tst[k].list.size();i++){ //Predict phenotypes in the testing set as mean of the corresponding branch in the training bootstrapped sample
    			  y_hat[branch_tst[k].list.get(i)]=y_hat[branch_tst[k].list.get(i)]+branch[k].getMean(phenotype);
    		  }
      		} //decision on creating a new branch
          }else { //If branch size is <=5 stop growing the tree
      		//outBranchMSE.print( branch[k].list.size()+" "+branch[k].getMSE(phenotype)+" " );
      		branch[k].status="F";//the branch is set as dead-end branch
      		if (branch[k].list.size()!=0){MSE_tree=MSE_tree+branch[k].getMSE(phenotype);}
    		for (i=0;i<branch[k].list.size();i++){ //Accumulate classification to estimate genomic value
    		  GEBV[branch[k].list.get(i)][0]=GEBV[branch[k].list.get(i)][0]+branch[k].getMean(phenotype);
    		  GEBV[branch[k].list.get(i)][1]++;
    		}
    		if (branch_tst[k].list.size()!=0 && branch[k].list.size()!=0){
    			  temp=branch[k].getMean(phenotype);
        		  for (i=0;i<branch_tst[k].list.size();i++){ //Predict phenotypes in the testing set as mean of the corresponding branch in the training bootstrapped sample
        			  y_hat[branch_tst[k].list.get(i)]=y_hat[branch_tst[k].list.get(i)]+temp;
        		  }
    		}else{//if training branch has size=0
  			  temp=branch[0].getMean(phenotype);
    		  for (i=0;i<branch_tst[k].list.size();i++){ //Predict phenotypes in the testing set as mean branch 0
    			  y_hat[branch_tst[k].list.get(i)]=y_hat[branch_tst[k].list.get(i)]+temp;
    		  }
    		}
          } //checking branch size
        } //for over n_branch
	    
	//Construct the oob-tree following nodes selected previously, and calculate miss-classification rate in the oob sample
		MSE_oob=0.d;
		for (k=0;k<n_branch+1;k++){ 
			if (branch[k].status.compareTo("F") != 0){ 
				branch_oob[branch[k].Child1]=new Branch();
				branch_oob[branch[k].Child2]=new Branch();
				for (i=0;i<branch_oob[k].list.size();i++){
					if (Genotype[branch_oob[k].list.get(i)][branch[k].Feature]<=branch[k].mean_snp){
						branch_oob[branch[k].Child1].list.add( branch_oob[k].list.get(i) );
					}else{
						branch_oob[branch[k].Child2].list.add( branch_oob[k].list.get(i) );
					}
				}
			}else{
				if (branch[k].list.size()==0){
					temp=branch[0].getMean(phenotype);
				}else{
					temp=branch[k].getMean(phenotype);
				}
				MSE_oob=MSE_oob+LossFunction.getLossFunctionOOB(LF_c, branch_oob[k], phenotype, temp, false_positive_cost, false_negative_cost); //Math.abs(phenotype[branch_oob[k].list.get(i)]-temp);
			}
		}
		k=0;
		MSE_oob=MSE_oob/N_oob;
		MSE_oob_ave=MSE_oob_ave+MSE_oob;
	
	//Calculate VARIABLE IMPORTANCE using the oob set, and permutating corresponding feature observations
		int pointer_oob_perm[] = Permutator.permute(pointer_oob);
		int pointer_perm[]=new int[N_tot];
		for (i=0;i<N_tot;i++){ //Get out-of-bag data
			if (oob[i]==0) {
				pointer_perm[i]=pointer_oob_perm[k];k++;
			}
		}
		k=0;
		for (j=0;j<N_attributes;j++){
			MSE_vi=0.d;
			for (k=0;k<n_branch+1;k++){ //Construct the oob-tree
				if (branch[k].Child1 != 0 || branch[k].Child2 != 0){
					branch_oob[branch[k].Child1]=new Branch();branch_oob[branch[k].Child2]=new Branch();
					for (i=0;i<branch_oob[k].list.size();i++){
						if (branch[k].Feature==j){
							if (Genotype[pointer_perm[branch_oob[k].list.get(i)]][branch[k].Feature]<=branch[k].mean_snp){
								branch_oob[branch[k].Child1].list.add( branch_oob[k].list.get(i) );
							}else{
								branch_oob[branch[k].Child2].list.add( branch_oob[k].list.get(i) );
							}	    				
						}else{
							if (Genotype[branch_oob[k].list.get(i)][branch[k].Feature]<=branch[k].mean_snp){
								branch_oob[branch[k].Child1].list.add( branch_oob[k].list.get(i) );
							}else{
								branch_oob[branch[k].Child2].list.add( branch_oob[k].list.get(i) );
							}	    				
						}
					}
				}else{
					if (branch[k].list.size()==0){
						temp=branch[0].getMean(phenotype);
					}else{
						temp=branch[k].getMean(phenotype);
					}
					MSE_vi=MSE_vi+LossFunction.getLossFunctionOOB(LF_c, branch_oob[k], phenotype, temp, false_positive_cost, false_negative_cost); //Math.abs(phenotype[branch_oob[k].list.get(i)]-temp);
				}
			}
			MSE_vi=MSE_vi/N_oob; //compare miss-classification rate permuting Feature j on MSE_oob
			VI[j]=VI[j]+((MSE_vi-MSE_oob))/max_tree; //Add variable importance and average over total trees
		}
		
		//Calculate miss-classification rate at this tree for the testing set
		MSEval_tree=0.d;
		for (i=0;i<N_tst;i++){
			MSEval_tree=MSEval_tree+(phenotype_tst[i]-(y_hat[i]/(n_tree+1)))*(phenotype_tst[i]-(y_hat[i]/(n_tree+1)));
		}
		System.out.println("Iteration #"+(n_tree+1)+";MSE in testing set="+MSEval_tree/N_tst);
		System.out.println("average Loss Function in OOB="+MSE_oob_ave/(float)(n_tree+1)+"; N_oob="+N_oob);
		outTree.println( MSE_oob_ave/(float)(n_tree+1) + " " + MSE_oob );
		outTreeTest.println( MSEval_tree/N_tst);
	    n_tree++;//go to next tree
     } //over n_tree
/**
   2. Ends the forest 
   **/
	
	System.out.println("Writing output files");
	outTree.close();outTreeTest.close();
		
	//Prepare the output files and its format
	DecimalFormat formatter =new DecimalFormat ("##.########");
	//Write file with number of times each Feature was selected and its relative importance 
	for (j=0; j<N_attributes;j++){  //for each Feature
		outSel.println( (j+1)+" "+Selected[j] );
		outVI.println( (j+1)+" "+formatter.format(VI[j]) );
	}
	for (i=0;i<N_tot;i++){ //Predicted GBV in training set
		outEGBV.println( ID[i]+" "+formatter.format(GEBV[i][0]/(float)(GEBV[i][1])) );
	}
	for (i=0;i<N_tst;i++){//Predicted GBV in training set
		outPred.println( ID_tst[i]+" "+formatter.format(y_hat[i]/(n_tree+1)) );
	}
	outSel.close();outVI.close();outPred.close();outEGBV.close();
	System.out.println("TERMINATED WITHOUT ERRORS");
	System.out.println("Random Forest algorithm for regression and classification problems (Ver.Beta)");
	System.out.println("by Oscar Gonzalez-Recio (2019) ");
	System.runFinalization();
	} // end main method	
} //end program




