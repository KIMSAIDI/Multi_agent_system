package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;

import eu.su.mas.dedaleEtu.mas.behaviours.FollowGolemBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class HunterAgent extends AbstractDedaleAgent{
	
	private static final long serialVersionUID = -6431752665590433727L;
	private MapRepresentation myMap;
	
	
	protected void setup() {
		super.setup();
		
		final Object[] args = getArguments();
		System.out.println("Arg given by the user to "+this.getLocalName()+": "+args[2]);
		
		
		List<String> list_agentNames=new ArrayList<String>();
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}
		
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		lb.add(new FollowGolemBehaviour(this, list_agentNames, this.myMap)); 
		
		
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	
	
	
	
	
}