package modules.printing;

import java.util.ArrayList;

import net.sf.javabdd.BDD;
import transitionSystem.ProductAutomaton;

public class PrintAcceptingPath {
	public PrintAcceptingPath(ArrayList<BDD> path) throws Exception {
		BDD next;
		for(int i=0;i<path.size();i++) {
			next=path.get(i);
			printAPList(next);
			
		}
	}

	private void printAPList(BDD state) throws Exception {
		ArrayList<String> apList=findAPList(state);
		System.out.print("[");
		for(int j=0;j<apList.size();j++) {
			if(j<apList.size()-1) {
				System.out.print(apList.get(j)+",");
			}
			else {
				System.out.print(apList.get(j));
			}
		}
		System.out.println("]");
	}

	private ArrayList<String> findAPList(BDD state) throws Exception {
		ArrayList<String> list=new ArrayList<String>();
		for(int i=0;i<ProductAutomaton.numAPSystem;i++) {
			if(! state.and(ProductAutomaton.ithVarSystemPre(i)).isZero()) {
				list.add(ProductAutomaton.apListSystem.get(i));
			}
		}
		list.add(Integer.toString(state.scanVar(ProductAutomaton.propertyDomainPre()).intValue()));
		return list;
	}
}
