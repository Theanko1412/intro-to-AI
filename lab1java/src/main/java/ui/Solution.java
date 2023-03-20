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
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class Solution {

	public static void main(String ... args) {

		//parse input arguments
		List<String> arguments = Arrays.stream(args).map(String::toLowerCase).map(String::strip).toList();
		int algorithmIndex = arguments.indexOf("--alg");
		int spaceDescriptorIndex = arguments.indexOf("--ss");
		int heuristicDescriptorIndex = arguments.indexOf("--h");
		int optHeuristicIndex = arguments.indexOf("--check-optimistic");
		int conHeuristicIndex = arguments.indexOf("--check-consistent");

		AlgorithmsEnum algorithm = AlgorithmsEnum.noAlg;
		try {
			algorithm = AlgorithmsEnum.valueOf(arguments.get(algorithmIndex+1));
		} catch (Exception ignored) {}
		String spaceDescriptorPath = arguments.get(spaceDescriptorIndex+1);
		String heuristicDescriptorPath = arguments.get(heuristicDescriptorIndex+1);


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

		HashMap<String, Double> heuristicDescriptorObjects = new HashMap<>();
		for(String s : heuristicDescriptorFileList) {
			parseHeuristicDescriptorLine(s, heuristicDescriptorObjects);
		}

		//route to algorithm
		switch (algorithm) {
			case bfs -> bfs(startState, spaceDescriptorObjects, endStates);
			case ucs -> ucs(startState, spaceDescriptorObjects, endStates, true);
			case astar -> astar(startState, spaceDescriptorObjects, endStates, heuristicDescriptorPath, heuristicDescriptorObjects);
			default -> checkHeuristic(endStates, spaceDescriptorObjects, heuristicDescriptorObjects, heuristicDescriptorPath, optHeuristicIndex, conHeuristicIndex);
		}
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

	private static void bfs(State startState, HashMap<String, List<State>> spaceDescriptorObjects, Set<String> endStates) {
		System.out.println("# BFS");

		Set<String> closed = new HashSet<>();
		int visited = 1;
		ConcurrentLinkedQueue<State> open = new ConcurrentLinkedQueue<>();
		open.offer(startState);

		while (!open.isEmpty()) {
			State current = open.poll();
			//found solution
			if(endStates.contains(current.name)) {
				List<State> path = new ArrayList<>();
				double cost = 0.0;
				findParent(path, current);
				Collections.reverse(path);
				for(State s : path) if(s.price != null) cost+=s.price;
				printOutput(visited, path.size(), cost, path);
				return;
			}
			if(!closed.contains(current.name)) {
				visited++;
				for(State state : spaceDescriptorObjects.get(current.name)) {
					if(current.equals(current.parentState)) continue;
					State newState = new State(state.name, state.price);
					newState.setParentState(current);
					open.offer(newState);
				}
				closed.add(current.name);
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
			if(endStates.contains(current.name)) {
				List<State> path = new ArrayList<>();
				double cost = current.totalPrice;
				findParent(path, current);
				Collections.reverse(path);
				if(print) printOutput(visited, path.size(), cost, path);
				return cost;
			}
			//expand states
			if(!closed.contains(current.name)) {
				visited++;
				for(State state : spaceDescriptorObjects.get(current.name)) {
					if(current.equals(current.parentState)) continue;
					State newState = new State(state.name, state.price);
					newState.setTotalPrice(current.totalPrice + state.price);
					newState.setParentState(current);
					open.offer(newState);
				}
				closed.add(current.name);
			}
		}
		//no solution
		System.out.println("[FOUND_SOLUTION]: no");
		return -1;
	}

	private static void astar(
			State startState,
			HashMap<String, List<State>> spaceDescriptorObjects,
			Set<String> endStates,
			String heuristicDescriptorPath,
			HashMap<String, Double> heuristicDescriptorObjects)
	{
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
			if(endStates.contains(current.name)) {
				List<State> path = new ArrayList<>();
				double cost = current.totalPrice;
				findParent(path, current);
				Collections.reverse(path);
				printOutput(visited, path.size(), cost, path);
				return;
			}
			if(!closed.contains(current.name)) {
				visited++;
				for(State state : spaceDescriptorObjects.get(current.name)) {
					if(current.equals(current.parentState)) continue;
					State newState = new State(state.name, state.price);
					newState.setTotalPrice(current.totalPrice + state.price);
					newState.setTotalPriceWithHeuristic(newState.getTotalPrice() + heuristicDescriptorObjects.get(newState.name));
					newState.setParentState(current);
					open.offer(newState);
				}
				closed.add(current.name);
			}
		}
		//no solution
		System.out.println("[FOUND_SOLUTION]: no");
	}


	private static void findParent(List<State> path, State state) {
		path.add(state);
		if(state.parentState != null) {
			findParent(path, state.parentState);
		}
	}

	private static void checkHeuristic(
			Set<String> endStates,
			HashMap<String, List<State>> spaceDescriptorObjects,
			HashMap<String, Double> heuristicDescriptorObjects,
			String heuristicDescriptorPath,
			int optHeuristicIndex,
			int conHeuristicIndex)
	{
		if(optHeuristicIndex == -1 && conHeuristicIndex == -1) throw new IllegalArgumentException("Algorithm, --check-optimistic or --check-consistent flags must be given!");
		boolean isOptimistic = true;
		boolean isConsistent = true;
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
					if(Objects.equals(nextState.name, "")) continue;
					double heuristicValue = heuristicDescriptorObjects.get(state);
					double heuristicValueNextState;
					try {
						heuristicValueNextState = heuristicDescriptorObjects.get(nextState.name);
					} catch (NullPointerException e) {
						heuristicValueNextState = 0.0;
					}
					double realValueToNextState = nextState.price;
					if(heuristicValue > (heuristicValueNextState + realValueToNextState)) {
						isConsistent = false;
						System.out.printf("[CONDITION]: [ERR] h(%s) <= h(%s) + c: %s <= %s + %s%n", state, nextState.name, heuristicValue, heuristicValueNextState, realValueToNextState);
					} else {
						System.out.printf("[CONDITION]: [OK] h(%s) <= h(%s) + c: %s <= %s + %s%n", state, nextState.name, heuristicValue, heuristicValueNextState, realValueToNextState);
					}
				}
			}

			if(isConsistent) System.out.println("[CONCLUSION]: Heuristic is consistent.");
			else System.out.println("[CONCLUSION]: Heuristic is not consistent.");
		}
	}


	static void printOutput(int statesVisited, int pathLength, double totalCost, List<State> path) {
		//seting the zone to US so printf with double uses dot instead of comma
		Locale.setDefault(Locale.US);

		System.out.println("[FOUND_SOLUTION]: yes");
		System.out.printf("[STATES_VISITED]: %d%n", statesVisited);
		System.out.printf("[PATH_LENGTH]: %d%n", pathLength);
		System.out.printf("[TOTAL_COST]: %.1f%n", totalCost);
		System.out.print("[PATH]: ");

		for(int i = 0; i < path.size(); i++) {
			if(i == 0) {
				System.out.printf(path.get(i).name);
			} else {
				System.out.printf(" => %s", path.get(i).name);
			}
		}
	}
}
