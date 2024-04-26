package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import jade.lang.acl.ACLMessage;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class ShareMapBehaviour extends OneShotBehaviour {
	
	private MapRepresentation myMap;
	private String receiver;
	/**
	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receiver the list of agents to send the map to
	 */
	public ShareMapBehaviour(Agent a,MapRepresentation mymap, String receiver) {
		super(a);
		this.myMap=mymap;
		this.receiver=receiver;	
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {
		//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
		// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.
		this.myMap = ((AgentFsm)this.myAgent).getMyMap();
		this.receiver = ((AgentFsm)this.myAgent).getReceiver();
		System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to share its map with agent: "+receiver);
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-ALL-MAP");
		msg.setSender(this.myAgent.getAID());
		
		msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
			
		SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
		try {					
			msg.setContentObject(sg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (((AgentFsm)this.myAgent).getAgent_friends_map(receiver) == null){
			((AgentFsm)this.myAgent).addList_friends_map(receiver, sg);
		}
		else{
			((AgentFsm)this.myAgent).majList_friends_map(receiver, sg);
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		this.myAgent.doWait(400);
	}	

}
