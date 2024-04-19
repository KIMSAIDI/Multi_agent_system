package eu.su.mas.dedaleEtu.mas.behaviours;


import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

public class CheckGolem extends OneShotBehaviour {
        
        private static final long serialVersionUID = 8567689731496787661L;
    
        private int exitValue = 0; // on ne fait rien par défaut
        private List<String> list_agentNames;
    
        public CheckGolem(final AbstractDedaleAgent myagent, List<String> list_agentNames) {
            super(myagent);
            this.list_agentNames = list_agentNames;
            
        }

        public void action(){
            // On vérifie déjà si on a reçu un message
            MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("I_Am_An_AgentBlockGolemProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
            if (msgReceived != null) {
                // Un agent est arrivé avant moi, je retourne patrouiller
            	System.out.println("Un agent est arrivé avant moi, je retourne patrouiller");
            	((AgentFsm)this.myAgent).setPosition_golem(""); 
            	this.exitValue = 0;
            }else{
                // On renvoie un message de controle
//                 ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//                 msg.setProtocol("CheckGolemProtocol");
//                 msg.setSender(this.myAgent.getAID());
//                 for (String agentName : this.list_agentNames) {
//                     msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
//                 }
//                 try {
//                     ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//                 this.myAgent.doWait(500);

                // On vérifie si on a reçu un message

               // msgReceived = this.myAgent.receive(msgTemplate);

                // Si on reçoit rien alors c'est un golem
            	
                this.exitValue = 7;
            }
            System.out.println("------ CHECKGOLEM ----");
        }

        public int onEnd(){
            return this.exitValue;
        }
    
}
