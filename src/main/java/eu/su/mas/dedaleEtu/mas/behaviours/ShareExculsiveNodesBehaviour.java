package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import jade.lang.acl.ACLMessage;

public class ShareExculsiveNodesBehaviour extends OneShotBehaviour {
    
    private static final long serialVersionUID = -568863390879327961L;

    private MapRepresentation myMap;
	private String receiver;

    public ShareExculsiveNodesBehaviour(Agent a,MapRepresentation myMap, String receiver) {
		super(a);
		this.myMap=((AgentFsm)this.myAgent).getMyMap();
		this.receiver=receiver;	
	}

    @Override
    public void action() {
        this.myMap=((AgentFsm)this.myAgent).getMyMap();
        this.receiver = ((AgentFsm)this.myAgent).getReceiver();
        System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to share its exclusive nodes with agent: "+receiver);


		List<Couple<String, SerializableSimpleGraph<String, MapAttribute>>> list_friends_map = ((AgentFsm)this.myAgent).getList_friends_map();
        // Get the exclusive nodes for the agent in the list
        for (Couple<String, SerializableSimpleGraph<String, MapAttribute>> couple : list_friends_map) {
            if (couple.getLeft().equals(receiver)) {
                SerializableSimpleGraph<String, MapAttribute> otherMap = couple.getRight();
				
                //List<Couple<String, List<String>>> exclusiveNodes = this.myMap.getNodesAndEdgesExclusiveToMyMap(otherMap);
                //if (exclusiveNodes.isEmpty()) {
                //    System.out.println("No exclusive nodes to share with agent: "+receiver);
                //    break;
                //}
                //System.out.println("Exclusive nodes to share with agent: "+exclusiveNodes);
                SerializableSimpleGraph<String, MapAttribute> subGraph = this.myMap.getExclusiveMap(otherMap);
                if (subGraph.getAllNodes().isEmpty()) {
                    System.out.println("No exclusive nodes to share with agent: "+receiver);
                    break;
                }
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setProtocol("SHARE-EXCLUSIVE-NODES");
                msg.setSender(this.myAgent.getAID());
                
                msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
                try {
                    msg.setContentObject(subGraph);
                    ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SerializableSimpleGraph<String, MapAttribute> mergedMap = this.getMergeGraph(otherMap, subGraph);
                ((AgentFsm)this.myAgent).majList_friends_map(receiver, mergedMap);
                break;
            }
        }
        //block(2000);
    
        
    }
    public SerializableSimpleGraph<String, MapAttribute> copyGraph(SerializableSimpleGraph<String, MapAttribute> originalMap) {
		SerializableSimpleGraph<String, MapAttribute> copyMap = new SerializableSimpleGraph<>();
		// Parcourir tous les nœuds de la carte d'origine
		for (SerializableNode<String, MapAttribute> n : originalMap.getAllNodes()) {
			// Ajouter le nœud à la carte copiée
			copyMap.addNode(n.getNodeId(), n.getNodeContent());
		}
		//4 now that all nodes are added, we can add edges
		for (SerializableNode<String, MapAttribute> n: originalMap.getAllNodes()){
			for(String s:originalMap.getEdges(n.getNodeId())){
				copyMap.addEdge(null, n.getNodeId(),s);
			}
		}
		return copyMap;
	}

	public SerializableSimpleGraph<String, MapAttribute> getSubGraph(List<Couple<String, List<String>>> nodes) {
		SerializableSimpleGraph<String, MapAttribute> subGraph = new SerializableSimpleGraph<>();
		// Parcourir tous les nœuds 
		for (Couple<String, List<String>> n : nodes) {
			// Ajouter le nœud à la carte copiée
			subGraph.addNode(n.getLeft());
		}
		//4 now that all nodes are added, we can add edges
		for (Couple<String, List<String>> n : nodes) {
			for (String s : n.getRight()) {
				subGraph.addEdge(null, n.getLeft(), s);
			}
		}
		
		return subGraph;
	}

	public SerializableSimpleGraph<String, MapAttribute> getMergeGraph(SerializableSimpleGraph<String, MapAttribute> map1, SerializableSimpleGraph<String, MapAttribute> map2) {
		//SerializableSimpleGraph<String, MapAttribute> subGraph = copyGraph(map1);
		// Parcourir tous les nœuds 
		//System.out.println("map1 : " + map1);
		//System.out.println("map2 : " + map2);
		if (map1 == null) {
			return map2;
		}
		for (SerializableNode<String, MapAttribute> n : map2.getAllNodes()) {
			// Ajouter le nœud à la carte copiée
			map1.addNode(n.getNodeId(), n.getNodeContent());
		}
		//4 now that all nodes are added, we can add edges
		for (SerializableNode<String, MapAttribute> n : map2.getAllNodes()) {
			for (String s : map2.getEdges(n.getNodeId())) {
				try {
					map1.addEdge(null, n.getNodeId(), s);
				} catch (NullPointerException e) {
					System.out.println("Error adding edge " + n.getNodeId() + " -> " + s);
				}
			}
		}

		
		return map1;
	}

}
