import settings.PlanningSettings;

public class mainCL {

	public static void main(String[] args) throws Exception {
		new PlanningSettings(); // use default values for all variables
		Planning plan = new Planning();
		
		if((boolean) PlanningSettings.get("firstExplThenPlan")) {
			plan.firstExplThenPlan();
		}
		else {
			plan.explAndPlanTogether();
		}
	}

}
