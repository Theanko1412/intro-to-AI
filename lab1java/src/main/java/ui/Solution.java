package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class Solution {

	public static void main(String ... args) {
//		System.out.printf("Passed arguments: %s%n", Arrays.toString(args));

		//parse input arguments
		List<String> arguments = Arrays.stream(args).map(String::toLowerCase).map(String::strip).toList();
		int algorithmIndex = arguments.indexOf("--alg");
		int spaceDescriptorIndex = arguments.indexOf("--ss");
		int heuristicDescriptorIndex = arguments.indexOf("--h");
		int optHeuristicIndex = arguments.indexOf("--check-optimistic");
		int conHeuristicIndex = arguments.indexOf("--check-consistent");

		AlgorithmsEnum algorithm = AlgorithmsEnum.check;
		try {
			algorithm = AlgorithmsEnum.valueOf(arguments.get(algorithmIndex+1));
		} catch (Exception ignored) {}
		String spaceDescriptorPath = arguments.get(spaceDescriptorIndex+1);
		String heuristicDescriptorPath = arguments.get(heuristicDescriptorIndex+1);

//		System.out.printf("[alg]: %s %d%n", algorithm, algorithmIndex);
//		System.out.printf("[ss]: %s %d%n", spaceDescriptorPath, spaceDescriptorIndex);
//		System.out.printf("[h]: %s %d%n", heuristicDescriptorPath, heuristicDescriptorIndex);
//		System.out.printf("[co]: %s %d%n", optimisticFlag, optHeuristicIndex);
//		System.out.printf("[cc]: %s %d%n", consistentFlag, conHeuristicIndex);

//		if(algorithm.equals(AlgorithmsEnum.ucs) && spaceDescriptorPath.startsWith("3x3_puzzle")) return;


		//read files
		List<String> spaceDescriptorFileList = new ArrayList<>();
		List<String> heuristicDescriptorFileList = new ArrayList<>();

		try {
			File spaceDescriptorFile = new File(spaceDescriptorPath);
			Scanner spaceDescriptorScanner = new Scanner(spaceDescriptorFile);

			while(spaceDescriptorScanner.hasNextLine()) {
				String nextLine = spaceDescriptorScanner.nextLine();
				if(!nextLine.startsWith("#"))
					spaceDescriptorFileList.add(nextLine);
			}

			if(heuristicDescriptorIndex != -1) {
				File heuristicDescriptorFile = new File(heuristicDescriptorPath);
				Scanner heuristicDescriptorScanner = new Scanner(heuristicDescriptorFile);

				while(heuristicDescriptorScanner.hasNextLine()) {
					String nextLine = heuristicDescriptorScanner.nextLine();
					if(!nextLine.startsWith("#"))
						heuristicDescriptorFileList.add(nextLine);
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found: ", e.getCause());
		}

//		System.out.printf("[ssFile]: %s%n", spaceDescriptorFileList);
//		System.out.printf("[hFile]: %s%n", heuristicDescriptorFileList);


		//parse files from lists
		HashMap<String, List<State>> spaceDescriptorObjects = new HashMap<>();
		State startState = null;
		Set<String> endStates = new HashSet<>();
		for(int i = 0; i < spaceDescriptorFileList.size(); i++) {
			if(i == 0) {
				startState = new State(spaceDescriptorFileList.get(i));
			} else if(i == 1) {
				endStates = Arrays.stream(spaceDescriptorFileList.get(i).split(" ")).collect(Collectors.toSet());
			} else {
				parseSpaceDescriptorLine(spaceDescriptorFileList.get(i), spaceDescriptorObjects);
			}
		}
//		System.out.print("[objects]: [");
//		for (String key : spaceDescriptorObjects.keySet()) {
//			System.out.printf("%s%n", spaceDescriptorObjects.get(key));
//		}
//		System.out.printf("]%n");


		HashMap<String, Double> heuristicDescriptorObjects = new HashMap<String, Double>();
		for(String s : heuristicDescriptorFileList) {
			parseHeuristicDescriptorLine(s, heuristicDescriptorObjects);
		}
//		System.out.print("[heuristic-objects]: [");
//		System.out.println(heuristicDescriptorObjects);
//		System.out.printf("]%n");
//		System.out.println(startState);
//		System.out.println(endStates);

		//route to algorithm
		switch (algorithm) {
			case bfs -> bfs(startState, spaceDescriptorObjects, endStates);
			case ucs -> ucs(startState, spaceDescriptorObjects, endStates, true);
			case astar -> astar(startState, spaceDescriptorObjects, endStates, heuristicDescriptorPath, heuristicDescriptorObjects);
			default -> checkHeuristic(endStates, spaceDescriptorObjects, heuristicDescriptorObjects, heuristicDescriptorPath, optHeuristicIndex, conHeuristicIndex);
		}


		//printOutput(true, 14, 5, 100.0, List.of("Pula", "Barban", "Labin", "Lupoglav", "Buzet"));
	}

	private static void checkHeuristic(Set<String> endStates, HashMap<String, List<State>> spaceDescriptorObjects, HashMap<String, Double> heuristicDescriptorObjects, String heuristicDescriptorPath, int optHeuristicIndex,
									   int conHeuristicIndex) {
		if(optHeuristicIndex == -1 && conHeuristicIndex == -1) throw new IllegalArgumentException("Algorithm, --check-optimistic or --check-consistent flags must be given!");
		Boolean isOptimistic = true;
		Boolean isConsistent = true;
		TreeMap<String, Double> states = new TreeMap<>();

		for(String state : heuristicDescriptorObjects.keySet()) {
			double price = ucs(new State(state, 0.0), spaceDescriptorObjects, endStates, false);
			states.put(state, price);
		}
		if(optHeuristicIndex != -1) {
			System.out.printf("# HEURISTIC-OPTIMISTIC %s%n", heuristicDescriptorPath);

			for(String state : states.keySet()) {
				double heuristicValue = heuristicDescriptorObjects.get(state);
				double realValue = states.get(state);
				if(heuristicValue > realValue) {
					isOptimistic = false;
					System.out.printf("[CONDITION]: [ERR] h(%s) <= h*: %s <= %s%n", state, heuristicValue, realValue);
				} else {
					System.out.printf("[CONDITION]: [OK] h(%s) <= h*: %s <= %s%n", state, heuristicValue, realValue);
				}
			}
			if(isOptimistic) System.out.println("[CONCLUSION]: Heuristic is optimistic.");
			else System.out.println("[CONCLUSION]: Heuristic is not optimistic.");
		} else {
			System.out.printf("# HEURISTIC-CONSISTENT %s%n", heuristicDescriptorPath);

			for(String state : states.keySet()) {
				for(State nextState : spaceDescriptorObjects.get(state)) {
					if(Objects.equals(nextState.state, "")) continue;
					double heuristicValue = heuristicDescriptorObjects.get(state);
					double heuristicValueNextState;
					try {
						heuristicValueNextState = heuristicDescriptorObjects.get(nextState.state);
					} catch (NullPointerException e) {
						heuristicValueNextState = 0.0;
					}
					double realValueToNextState = nextState.price;
					if(heuristicValue > (heuristicValueNextState + realValueToNextState)) {
						isConsistent = false;
						System.out.printf("[CONDITION]: [ERR] h(%s) <= h(%s) + c: %s <= %s + %s%n", state, nextState.state, heuristicValue, heuristicValueNextState, realValueToNextState);
					} else {
						System.out.printf("[CONDITION]: [OK] h(%s) <= h(%s) + c: %s <= %s + %s%n", state, nextState.state, heuristicValue, heuristicValueNextState, realValueToNextState);
					}
				}
			}

			if(isConsistent) System.out.println("[CONCLUSION]: Heuristic is consistent.");
			else System.out.println("[CONCLUSION]: Heuristic is not consistent.");
		}
	}

	private static void findParent(List<State> path, State state) {
		path.add(state);
		if(state.parentState != null) {
			findParent(path, state.parentState);
		}
	}

	private static void bfs(State startState, HashMap<String, List<State>> spaceDescriptorObjects, Set<String> endStates) {
		System.out.println("# BFS");

		Set<String> closed = new HashSet<>();
		int visited = 1;
		ConcurrentLinkedQueue<State> open = new ConcurrentLinkedQueue<>();
		open.offer(startState);

		while (!open.isEmpty()) {
			State current = open.poll();
			//found solution
			if(endStates.contains(current.state)) {
				List<State> path = new ArrayList<>();
				double cost = 0.0;
				findParent(path, current);
				Collections.reverse(path);
				for(State s : path) if(s.price != null) cost+=s.price;
				printOutput("yes", visited, path.size(), cost, path);
				return;
			}
			if(!closed.contains(current.state)) {
				visited++;
				for(State state : spaceDescriptorObjects.get(current.state)) {
					if(current.equals(current.parentState)) continue;
					State newState = new State(state.state, state.price);
					newState.setParentState(current);
					open.offer(newState);
				}
				closed.add(current.state);
			}
		}
		//no solution
		System.out.println("[FOUND_SOLUTION]: no");
		return;
	}


	private static double ucs(State startState, HashMap<String, List<State>> spaceDescriptorObjects, Set<String> endStates, Boolean print) {
		if(print) System.out.println("# UCS");

		Set<String> closed = new HashSet<>();
		int visited = 1;
		PriorityQueue<State> open = new PriorityQueue<>();
		startState.price = 0.0;
		startState.totalPrice = 0.0;
		open.offer(startState);

		while (!open.isEmpty()) {
			State current = open.poll();
			//found solution
			if(endStates.contains(current.state)) {
				List<State> path = new ArrayList<>();
				double cost = current.totalPrice;
				findParent(path, current);
				Collections.reverse(path);
				if(print) printOutput("yes", visited, path.size(), cost, path);
				return cost;
			}
			if(!closed.contains(current.state)) {
				visited++;
				for(State state : spaceDescriptorObjects.get(current.state)) {
					if(current.equals(current.parentState)) continue;
					State newState = new State(state.state, state.price);
					newState.setTotalPrice(current.totalPrice + state.price);
					newState.setParentState(current);
					open.offer(newState);
				}
				closed.add(current.state);
			}
		}
		//no solution
		System.out.println("[FOUND_SOLUTION]: no");
		return -1;
	}

	private static void astar(State startState, HashMap<String, List<State>> spaceDescriptorObjects, Set<String> endStates, String heuristicDescriptorPath, HashMap<String, Double> heuristicDescriptorObjects) {
		System.out.printf("# A-STAR %s%n", heuristicDescriptorPath);

		Set<String> closed = new HashSet<>();
		int visited = 1;
		PriorityQueue<State> open = new PriorityQueue<>();
		startState.price = 0.0;
		startState.totalPrice = 0.0;
		open.offer(startState);

		while (!open.isEmpty()) {
			State current = open.poll();
			//found solution
			if(endStates.contains(current.state)) {
				List<State> path = new ArrayList<>();
				double cost = current.totalPrice;
				findParent(path, current);
				Collections.reverse(path);
				printOutput("yes", visited, path.size(), cost, path);
				return;
			}
			if(!closed.contains(current.state)) {
				visited++;
				for(State state : spaceDescriptorObjects.get(current.state)) {
					if(current.equals(current.parentState)) continue;
					State newState = new State(state.state, state.price);
					newState.setTotalPrice(current.totalPrice + state.price);
					newState.setTotalPriceWithHeuristic(newState.getTotalPrice() + heuristicDescriptorObjects.get(newState.state));
					newState.setParentState(current);
					open.offer(newState);
				}
				closed.add(current.state);
			}
		}
		//no solution
		System.out.println("[FOUND_SOLUTION]: no");
		return;
	}


	//parse lines
	static void parseSpaceDescriptorLine(String line, HashMap<String, List<State>> map) {
		List<State> list = new ArrayList<>();


		String[] array = Arrays.stream(line.split(":")).map(String::strip).toArray(String[]::new);
		String state = array[0];
		if(array.length == 1) {
			map.put(state, List.of(new State("", 0.0)));
			return;
		}
		List<String> states = Arrays.stream(array[1].split(" ")).toList();

		for (String s : states) {
			String[] ssplit = s.split(",");
			String stateState = ssplit[0];
			Double doubleState = Double.parseDouble(ssplit[1]);
			State obj = new State(stateState, doubleState);
			list.add(obj);
		}

		map.put(state, list);
	}

	static void parseHeuristicDescriptorLine(String line, HashMap<String, Double> heuristicDescriptorObjects) {
		String[] array = Arrays.stream(line.split(":")).map(String::strip).toArray(String[]::new);
		String state = array[0];
		Double doubleState = Double.parseDouble(array[1]);
		heuristicDescriptorObjects.put(state, doubleState);
	}

	static void printOutput(String foundSolution, int statesVisited, int pathLength, double totalCost, List<State> path) {
		//seting the zone to US so printf with double uses dot instead of comma
		Locale.setDefault(Locale.US);

		System.out.printf("[FOUND_SOLUTION]: %s%n", foundSolution);
		System.out.printf("[STATES_VISITED]: %d%n", statesVisited);
		System.out.printf("[PATH_LENGTH]: %d%n", pathLength);
		System.out.printf("[TOTAL_COST]: %.1f%n", totalCost);
		System.out.print("[PATH]: ");
		for(int i = 0; i < path.size(); i++) {
			if(i == 0) {
				System.out.printf(path.get(i).state);
			} else {
				System.out.printf(" => %s", path.get(i).state);
			}
		}
	}
}
