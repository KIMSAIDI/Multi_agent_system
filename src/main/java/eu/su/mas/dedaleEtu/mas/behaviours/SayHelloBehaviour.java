package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class SayHelloBehaviour extends OneShotBehaviour{
	
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
	public SayHelloBehaviour (final Agent myagent, List<String> receivers) {
		super(myagent);
		this.receivers=receivers;
	}

	@Override
	public void action() {
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("HelloProtocol");
		msg.setSender(this.myAgent.getAID());
		for (String agentName : receivers) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			
		}
		
		msg.setContent(myPosition.toString());
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
		System.out.println("----------- " + this.myAgent.getLocalName()+" sent a message to its friend");
		//block(1000);
		
	}
	
	

}