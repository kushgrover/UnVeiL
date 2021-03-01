import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BuDDyFactory;
import settings.PlanningSettings;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class mainCL {

	public static void main(String[] args) throws Exception {
		new PlanningSettings(args); // set parameters
		if((boolean) PlanningSettings.get("debug"))
			PlanningSettings.outputParameters();

		int numOfRuns = (Integer) PlanningSettings.get("numberOfRuns");
		Object[][] output = new Object[numOfRuns][7];
		Object[] data = new Object[7];
		Object[] sum = new Object[]{
				new Integer(0), // num of iterations
				new Integer(0), // Total sampled points
				new Integer(0), // RRG size
				new Float(0), // movement length
				new Float(0), // path length
				new Float(0), // total length
				new Double(0), // total time
		};

		for(int i=0; i < numOfRuns; i++) {
			BDDFactory factory = BuDDyFactory.init(600000, (int) PlanningSettings.get("bddFactoryCacheSize"));
			System.out.println("Starting initialization ... ");
			Planning plan = new Planning(factory);
			System.out.println("Initialization complete\n\n");
			if ((boolean) PlanningSettings.get("firstExplThenPlan")) {
				data = plan.firstExplThenPlan();
			} else {
				data = plan.explAndPlanTogether();
			}
			System.out.println("\n\n");
			factory.done();

			String outputDir = (String) PlanningSettings.get("outputDirectory");
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir + "output.csv", true));
			for(int j=0; j<7; j++){
				output[i][j] = data[j];
				if(j<3) {
					writer.write(((Integer) data[j]) + ",");
					sum[j] = new Integer((Integer) sum[j] + (Integer) data[j]);
				}
				else if(j<6) {
					writer.write((Float) data[j] + ",");
					sum[j] = new Float((Float) sum[j] + (Float) data[j]);
				}
				else if(j<7) {
					writer.write(((Double) data[j])/1000000 + ",");
					sum[j] = new Double((Double) sum[j] + (Double) data[j]);
				}
			}
			writer.newLine();
			writer.close();
		}
	}

}
