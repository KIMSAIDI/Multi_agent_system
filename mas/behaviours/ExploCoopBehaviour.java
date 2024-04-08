package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.IEnvironment;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


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
public class ExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	private List<String> list_agentNames;
	

/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public ExploCoopBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,List<String> agentNames) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
		
		
	}

	@Override
	public void action() {

		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			//this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,500,this.myMap,list_agentNames));
			this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent, 500, list_agentNames, "HelloProtocol"));
			
		}
		
		System.out.println("~~~~~~~~~");
		
		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			List<Location> locations = new ArrayList<>();
	        for (Couple<Location, List<Couple<Observation, Integer>>> observable : lobs) {
	            locations.add(observable.getLeft()); 
	        }
			
			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNodeId=null;
			Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			
			
//			ArrayList<String> lobsString = new ArrayList<String>();
//			for (Couple<Location, List<Couple<Observation, Integer>>> couple : lobs) {
//				lobsString.add(couple.getLeft().toString());
//				
//			}
			//System.out.println("-------------------------------");
//			System.out.println(this.myAgent.getLocalName()+" - Exploration in progress");
//			System.out.println("Liste des observables : "+lobsString);			
//			System.out.println("Position actuelle : "+myPosition);
			List<String> liste_noeuds_accessibles = new ArrayList<String>();
			//System.out.println("~~~~~~~~");
			while(iter.hasNext()){
				Location accessibleNode=iter.next().getLeft();
				//System.out.println("Noeud accessible : "+accessibleNode);
				
				boolean isNewNode=this.myMap.addNewNode(accessibleNode.getLocationId());
				//the node may exist, but not necessarily the edge
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				liste_noeuds_accessibles.add(accessibleNode.getLocationId());
				}
			}
			
			
				
			
	        
//			if (this.myMap.hasOpenNode()) {
//				//System.out.println(this.myAgent.getLocalName()+" - Exploration in progress");
//				 // log les noeuds qui manquent
//				//System.out.println("Noeuds manquants : "+this.myMap.getOpenNodes());
//				//System.out.println("-------------------------------");
//			}
			
			
			

			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()) {
				//Explo finished
				finished=true;
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
				//System.out.println("ON A FINIT");
				
				
				// Si je ne suis pas le dernier agent
				
				//System.out.println("Liste des agents manquants : "+list_agentNames);
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				// set the protocol of the message
				msg.setProtocol("SHARE-TOPO");
				msg.setSender(this.myAgent.getAID());
				for (String agentName : list_agentNames) {
					msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
				}
				
				SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
				try {					
					msg.setContentObject(sg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				
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
				
				//5) At each time step, the agent check if he received a graph from a teammate. 	
				// If it was written properly, this sharing action should be in a dedicated behaviour set.
				
				
			    
			   this.myAgent.addBehaviour(new ReceiveMsg(this.myAgent, this.myMap, list_agentNames));

				

				
				
			    
			    ((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
				
				
				
			}

		}
	}
	
	

	
	
	@Override
	public boolean done() {
		System.out.println("End of behaviour");
		return finished;
	}

}