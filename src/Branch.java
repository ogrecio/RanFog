import java.util.ArrayList;

public class Branch {

	double mean, mean_snp;
	int class_val;
	String status=" "; //'F' for final branch
	int Feature, Child1, Child2, Parent;	
	ArrayList<Integer> list = new ArrayList<Integer>();


	public double getMean (double phen[]){
		/**
		 * This method returns the SNP for a given position.
		 *  It needs as arguments:
		 *  @arg position, the position of the SNP in the genomic combination
		 */
		int i=0;
		this.mean=0.0d;
		for (i=0;i<list.size();i++){
			this.mean=this.mean+phen[list.get(i)];
		}
		this.mean=this.mean/list.size();
		return mean;
	}
	public int getClass (double phen[]){
		/**
		 * This method returns the SNP for a given position.
		 *  It needs as arguments:
		 *  @arg position, the position of the SNP in the genomic combination
		 */
		int i=0;
		int temp[]=new int[3];
		for (i=0;i<list.size();i++){
			temp[(int)phen[list.get(i)]]++;
		}
		if (temp[0]>temp[1] & temp[0]>temp[2]){
			this.class_val=0;
		}else if (temp[1]>temp[2]){
			this.class_val=1;
		}else{
			this.class_val=2;
		}
		return this.class_val;
	}
	public double getMSE (double phen[]){
		/**
		 * This method returns the SNP for a given position.
		 *  It needs as arguments:
		 *  @arg position, the position of the SNP in the genomic combination
		 */
		int i=0;
		this.getMean(phen);
		double MSE=0.0d;
		for (i=0;i<list.size();i++){
			MSE=MSE+( phen[list.get(i)]-this.mean )*( phen[list.get(i)]-this.mean );
		}
		//MSE=MSE/list.size();
		return MSE;
	}
	public double getMissClass (double phen[]){
		/**
		 * This method returns the SNP for a given position.
		 *  It needs as arguments:
		 *  @arg position, the position of the SNP in the genomic combination
		 */
		int i=0;
		this.getClass(phen);
		double MSE=0.0d;
		for (i=0;i<list.size();i++){
			MSE=MSE+Math.abs( (int) phen[list.get(i)]-this.class_val );
		}
		//MSE=MSE/list.size();
		return MSE;
	}
}