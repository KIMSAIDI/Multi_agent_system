package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;



/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class ExploCoopBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

    private static final int sayHello = 5; // pour limiter le nombre de ping, un ping toutes les X actions

	private boolean finished = false;
	private int exitValue;
	private List<Couple<String,Integer>> list_spam = new ArrayList<>();
	private List<Couple<String, SerializableSimpleGraph<String, MapAttribute> >> list_friends_map = new ArrayList<>();
	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private int nbActions;
	private List<String> list_agentNames;

/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 * @param nbActions number of actions done by the agent
 * @param list_spam list of agents with whom the agent has already talked
 * @param list_friends_map list of agents and their maps
 */
	public ExploCoopBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,List<String> agentNames, int nbActions, List<Couple<String,Integer>> list_spam, List<Couple<String, SerializableSimpleGraph<String, MapAttribute> >> list_friends_map) {
		super(myagent);
		this.myMap = ((AgentFsm)this.myAgent).getMyMap();
		this.list_agentNames = agentNames;
		this.nbActions = nbActions;
		this.list_spam = list_spam;
		this.list_friends_map = list_friends_map;
	}

	@Override
	public void action() {
		this.myMap=((AgentFsm)this.myAgent).getMyMap();
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			//this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent, 500, this.myMap, this.list_agentNames));
		}
		this.exitValue = 0;

		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				// Check if the agent has received a message
				checkFalseInformation();
				if (checkReceivedMessage()) {
					return;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNodeId=null;
			List<Location> noeuds_observable = new ArrayList<Location>();
			Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			 for (Couple<Location, List<Couple<Observation, Integer>>> observable : lobs) {
		            noeuds_observable.add(observable.getLeft()); 
			 }   
			while(iter.hasNext()){
				Location accessibleNode=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(accessibleNode.getLocationId());
				//the node may exist, but not necessarily the edge
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				}
			}
			//update the map
			((AgentFsm)this.myAgent).setMyMap(this.myMap); 

			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
				//Explo finished
				this.exitValue = 10;
				((AgentFsm)this.myAgent).setExploDone(true);
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNodeId==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNodeId=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				((AgentFsm)this.myAgent).incNbActions();
				if (this.nbActions % sayHello == 0) {
					//System.out.println("SayHelloBehaviour");
					this.exitValue = 1;
				}

				((AgentFsm)this.myAgent).majList_spam();
				//System.out.println("List_spam de "+ this.myAgent.getLocalName() + " : " + list_spam);
				((AgentFsm)this.myAgent).setMyMap(this.myMap);
				if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
					//System.out.println("je suis bloqué");
					Random rand = new Random();
					int randomIndex = rand.nextInt(noeuds_observable.size());
					nextNodeId = noeuds_observable.get(randomIndex).getLocationId();
					((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nextNodeId));
				}
			}

		}
		this.myAgent.doWait(100);
		System.out.println(" ------------------- " + this.myAgent.getLocalName() + "ExploCoopBehaviour");
	}
	
    public boolean checkFalseInformation() {
    	Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        
    	MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("I_Am_An_AgentBlockGolemProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        if (msgReceived != null) {
            // je vérifie que l'agent ne me prend pas pour un golem
        	try {
        		String loc = msgReceived.getContent(); // loc du golem
        		String maLoc = myPosition.getLocationId();
        		
        		if (loc.equals(maLoc)) { // on me prend pour un golem
        			System.out.println(this.myAgent.getLocalName() + "JE NE SUIS PAS LE GOLEM AARRRG");
        			// je ne suis pas un golem
        			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        	        msg.setProtocol("Je_Ne_Suis_Pas_Un_GolemProtocol");
        	        msg.setSender(this.myAgent.getAID());
        	        for (String agentName : this.list_agentNames) {
        				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        				
        			}
					try {
						msg.setContentObject(((AbstractDedaleAgent) this.myAgent).getCurrentPosition());
						((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
        			//this.exitValue = 4; // je retourne en patrouille
					
        			return true;
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}		

	// Check les messages reçus
	@SuppressWarnings("unchecked")
	public Boolean checkReceivedMessage() throws IOException {

		// recu: protocol SHARE-TOPO
		ACLMessage msgShareMap;
		do {
			MessageTemplate mtSM = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchProtocol("SHARE-TOPO"));
			msgShareMap = this.myAgent.receive(mtSM);
			if (msgShareMap != null) {
				SerializableSimpleGraph<String, MapAttribute> autreMap=null;
				try {
					autreMap = (SerializableSimpleGraph<String, MapAttribute>) msgShareMap.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				((AgentFsm)this.myAgent).addList_spam(msgShareMap.getSender().getLocalName());
				//System.out.println(this.myAgent.getLocalName() + " received a map from " + msgShareMap.getSender().getLocalName());
				this.myMap.mergeMap(autreMap);
				((AgentFsm)this.myAgent).setMyMap(this.myMap);
				SerializableSimpleGraph<String, MapAttribute> mergedMap = this.copyGraph(autreMap);
				// ajouter la map de l'agent expéditeur à la liste des maps des amis (mergées)
				((AgentFsm)this.myAgent).addList_friends_map(msgShareMap.getSender().getLocalName(), autreMap); 
				// renvoyer les noeuds non visités de l'agent expéditeur si ils ne sont pas dans notre map
				//((AgentFsm)this.myAgent).setReceiver(msgShareMap.getSender().getLocalName());
				//this.exitValue = 3;


				// changer de chemin si on a reçu une map ?????
				//Boolean changePath = true;
			}
		} while (msgShareMap != null);

		// recu: protocol SHARE-EXCLUSIVE-NODES
		ACLMessage msgExNodes;
		do {
			MessageTemplate mtExNods = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchProtocol("SHARE-EXCLUSIVE-NODES"));
			msgExNodes = this.myAgent.receive(mtExNods);
			if (msgExNodes != null) {
				SerializableSimpleGraph<String, MapAttribute> subGraph = null;
				try {
					subGraph = (SerializableSimpleGraph<String, MapAttribute>) msgExNodes.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				//System.out.println(this.myAgent.getLocalName() + " received exclu map " + subGraph + "from " + msgExNodes.getSender().getLocalName());

				this.myMap.mergeMap(subGraph);
				((AgentFsm)this.myAgent).setMyMap(this.myMap);
				SerializableSimpleGraph<String, MapAttribute> otherMap = ((AgentFsm)this.myAgent).getMap_friends_map(msgExNodes.getSender().getLocalName());
				SerializableSimpleGraph<String, MapAttribute> mergedMap = this.getMergeGraph(otherMap, subGraph);
				((AgentFsm)this.myAgent).majList_friends_map(msgExNodes.getSender().getLocalName(), mergedMap);
			}

		} while (msgExNodes != null);

		// recu: protocol HelloProtocol
		MessageTemplate mtH = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchProtocol("HelloProtocol"));
		ACLMessage msgHello = this.myAgent.receive(mtH);
		if (msgHello != null && !isInList_spam(msgHello.getSender().getLocalName())) {
			((AgentFsm)this.myAgent).addList_spam(msgHello.getSender().getLocalName());
			System.out.println(this.myAgent.getLocalName() + " received a message from " + msgHello.getSender().getLocalName() + " : " + msgHello.getContent());
			((AgentFsm)this.myAgent).setReceiver(msgHello.getSender().getLocalName());
			// si l'agent n'est pas dans la liste des amis, on partage toute la map
			if (!((AgentFsm)this.myAgent).isInList_friends_map(msgHello.getSender().getLocalName())) {
				this.exitValue = 2;
				return true;
			}
			// sinon, partager les noeuds exclusifs
			this.exitValue = 3;
			return true;
		}
		return false;
		
	}
	public boolean isInList_spam(String agent_name) {
		Iterator<Couple<String,  Integer>> iter=list_spam.iterator();

		while(iter.hasNext()){
			Couple<String, Integer> agent = iter.next();
			String name= agent.getLeft();
			if ( name.equals(agent_name)) {
				return true;
			}
		}
		return false;
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
					//System.out.println("Error adding edge " + n.getNodeId() + " -> " + s);
				}
			}
		}

		
		return map1;
	}

	@Override
	public int onEnd() {
		return exitValue;
	}


}
