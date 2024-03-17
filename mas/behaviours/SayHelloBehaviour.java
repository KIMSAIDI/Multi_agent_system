package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class SayHelloBehaviour extends TickerBehaviour{
	
	private List<String> receivers;

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;

	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public SayHelloBehaviour (final Agent myagent, long period, List<String> receivers) {
		super(myagent, period);
		this.receivers=receivers;
		//super(myagent);
	}

	@Override
	public void onTick() {
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("UselessProtocol");
		msg.setSender(this.myAgent.getAID());
		
		if (myPosition!=null && myPosition.getLocationId()!=""){
			msg.setContent("Hello World, I'm at "+myPosition);
		}
		
		// liste des receivers
		for (String agentName : receivers) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		}
		
		
		
		try {
			msg.setContentObject("Hello World, I'm at "+myPosition + " I am "+this.myAgent.getAID()) ;
		
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
	}
}