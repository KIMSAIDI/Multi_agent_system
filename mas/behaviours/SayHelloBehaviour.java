package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agents.dedaleDummyAgents.Explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
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
	private String protocol;
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;

	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public SayHelloBehaviour (final Agent myagent, long period, List<String> receivers, String protocol) {
		super(myagent, period);
		this.receivers=receivers;
		this.protocol=protocol;
		
		//super(myagent);
	}

	@Override
	public void onTick() {
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		
            
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(protocol);
		msg.setSender(this.myAgent.getAID());
		
		// liste des receivers
		for (String agentName : receivers) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			
		}
		
		
		if (protocol == "HelloProtocol") {
			try {
				msg.setContentObject("Hello World, I'm at "+myPosition + " I am "+this.myAgent.getAID()) ;
				System.out.println("hello");
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (protocol == "Ping") {
			try {
				msg.setContentObject("Ping");
				System.out.println("je suis" + this.myAgent.getLocalName() + " et je Ping");
				((AbstractDedaleAgent) this.myAgent).sendMessage(msg);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			
		
}
	
	

}