package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
//import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class ReceivePing extends TickerBehaviour {
   
	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;

	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public ReceivePing(final Agent myagent) {
		super(myagent, 3000);
        
		//super(myagent);
	}

	@Override
	public void onTick() {
		MessageTemplate template= MessageTemplate.and(
            MessageTemplate.MatchProtocol("ReceivePing"),
            MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
            )
        ) ;
        // takes the message if the template is verified
        ACLMessage msg=this.myAgent.receive(template);
        if (msg!=null){
            // Processing of the message
            String textMessage=msg.getContent();
            Myobject o = (Myobject) msg.getContentObject();
        } else {
            block() ;
        }
	}
}