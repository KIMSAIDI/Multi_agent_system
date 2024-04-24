package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class SendPosition extends OneShotBehaviour {
    
    private static final long serialVersionUID = 8567689731496787661L;

    private List<String> list_agentNames;

    public SendPosition(final AbstractDedaleAgent myagent, List<String> list_agentNames) {
        super(myagent);
        this.list_agentNames = list_agentNames;
    }

    @Override
    public void action() {
    

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("SendPositionProtocol");
        msg.setSender(this.myAgent.getAID());
        for (String agentName : this.list_agentNames) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			
		}
        try {
            msg.setContent(((AbstractDedaleAgent)this.myAgent).getCurrentPosition().toString());
            ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
          
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
}
