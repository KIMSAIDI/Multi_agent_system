package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;

import eu.su.mas.dedaleEtu.mas.behaviours.FollowGolemBehaviour;

import jade.core.behaviours.Behaviour;

public class HunterAgent extends AbstractDedaleAgent{
	
	private static final long serialVersionUID = -2991562876411096907L;
	
	
	protected void setup() {
		super.setup();
		
		final Object[] args = getArguments();
		System.out.println("Arg given by the user to "+this.getLocalName()+": "+args[2]);
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		lb.add(new FollowGolemBehaviour(this, new ArrayList<String>())); 
		
		addBehaviour(new startMyBehaviours(this,lb));
	}
	
	
	protected void beforeMove() {
		super.beforeMove();
	}
	
	protected void afterMove() {
		super.afterMove();
	}
	
	
	protected void takeDown(){
		super.takeDown();
	}
	
	
	
	
	
}