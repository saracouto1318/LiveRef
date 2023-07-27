package liverefactoring.utils.Halstead;

public class MetricsEvaluator {
	public double PROGRAM_LENGTH,PROGRAM_VOCABULARY,ESTIMATED_LENGTH,PURITY_RATIO,VOLUME,DIFFICULTY,PROGRAM_EFFORT,PROGRAMMING_TIME;
	public double n1,n2,N1,N2;
	public void evaluate(){
		this.N1=Operators.getInstance().name.size();
		this.N2=Operands.getInstance().name.size();
		for(int i=0;i<Operators.getInstance().name.size();i++){
			this.n1+=Integer.parseInt(Operators.getInstance().count.get(i));
		}
		for(int i=0;i<Operands.getInstance().name.size();i++){
			this.n2+=Integer.parseInt(Operands.getInstance().count.get(i));
		}
		this.PROGRAM_LENGTH=this.N1+this.N2;
		this.PROGRAM_VOCABULARY=this.n1+this.n2;
		this.ESTIMATED_LENGTH=(((this.n1)*(Math.log(this.n1)/Math.log(2)))+((this.n2)*(Math.log(this.n2)/Math.log(2))));
		this.PURITY_RATIO=this.ESTIMATED_LENGTH/this.PROGRAM_LENGTH;
		this.VOLUME=((this.PROGRAM_LENGTH)*(Math.log(this.PROGRAM_LENGTH)/Math.log(2)));
		this.DIFFICULTY=(this.n1/2)*(this.N2/this.n2);
		this.PROGRAM_EFFORT=this.VOLUME*this.DIFFICULTY;
		this.PROGRAMMING_TIME=this.PROGRAM_EFFORT/18;
	}
}
