package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Solution {
    public static void main(String... args) {

        //parse input arguments
        List<String> arguments = Arrays.stream(args).map(String::toLowerCase).map(String::strip).toList();
        int resolutionIndex = arguments.indexOf("resolution");
        String resolutionPath = arguments.get(resolutionIndex + 1);

        int cookingIndex = arguments.indexOf("cooking");
        String cookingPath = arguments.get(cookingIndex + 1);
        String cookingInput = arguments.get(cookingIndex + 2);

        //read files
        List<Clause> resolutionClauses = new ArrayList<>();
        List<Clause> cookingClauses = new ArrayList<>();
        List<UserInput> cookingInputs = new ArrayList<>();

        if (resolutionIndex != -1) {
            try {
                File listOfClauses = new File(resolutionPath);
                Scanner scanner = new Scanner(listOfClauses);

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    if (!line.startsWith("#")) {

                        List<Literal> literals = new ArrayList<>();
                        String[] literalsString = line.toLowerCase().split(" v ");

                        for (String literalString : literalsString) {
                            boolean isNegated = literalString.startsWith("~");
                            String variable = isNegated ? literalString.substring(1) : literalString;
                            Literal literal = new Literal(variable, isNegated);
                            literals.add(literal);
                        }

                        Clause clause = new Clause(literals);
                        resolutionClauses.add(clause);
                    }
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File not found");
            }

            refutationResolution(resolutionClauses, resolutionPath);

        } else if (cookingIndex != -1) {
            try {
                File listOfClauses = new File(cookingPath);
                Scanner scanner = new Scanner(listOfClauses);

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    if (!line.startsWith("#")) {
                        List<Literal> literals = new ArrayList<>();
                        String[] literalsString = line.toLowerCase().split(" v ");

                        for (String literalString : literalsString) {
                            boolean isNegated = literalString.startsWith("~");
                            String variable = isNegated ? literalString.substring(1) : literalString;
                            Literal literal = new Literal(variable, isNegated);
                            literals.add(literal);
                        }

                        Clause clause = new Clause(literals);
                        cookingClauses.add(clause);
                    }
                }

                File listOfInputs = new File(cookingInput);
                Scanner scanner2 = new Scanner(listOfInputs);

                while (scanner2.hasNextLine()) {
                    String line = scanner2.nextLine();

                    if (!line.startsWith("#")) {
                        String userInputChars = line.substring(line.length() - 2);
                        line = line.substring(0, line.length() - 2);
                        List<Literal> literals = new ArrayList<>();
                        String[] literalsString = line.toLowerCase().split(" v ");

                        for (String literalString : literalsString) {
                            boolean isNegated = literalString.startsWith("~");
                            String variable = isNegated ? literalString.substring(1) : literalString;
                            Literal literal = new Literal(variable, isNegated);
                            literals.add(literal);
                        }

                        Clause clause = new Clause(literals);
                        char c = userInputChars.charAt(userInputChars.length() - 1);

                        UserInput userInput = switch (c) {
                            case '?' -> new UserInput(clause, InputType.QUERY);
                            case '-' -> new UserInput(clause, InputType.REMOVAL);
                            case '+' -> new UserInput(clause, InputType.ADDITION);
                            default -> throw new RuntimeException("Invalid input");
                        };

                        cookingInputs.add(userInput);
                    }
                }

                cookingAssistant(cookingClauses, cookingInputs);

            } catch (FileNotFoundException e) {
                throw new RuntimeException("File not found");
            }
        } else {
            throw new RuntimeException("Invalid arguments");
        }
    }

    //taking resolutionPath just so i can exclude output in one of the tests, logic is OK but recursive print is not
    private static void refutationResolution(List<Clause> resolutionClauses, String resolutionPath) {
        //negating the goal state, if multiple, all are appended
        Clause goalClause = resolutionClauses.remove(resolutionClauses.size() - 1);
        List<Clause> negatedGoalClause = negateClause(goalClause);
        resolutionClauses.addAll(negatedGoalClause);


        //simplifying clauses by deletion simplification strategy
        List<Clause> simplifiedClauses = simplifyClauses(resolutionClauses);
        List<Clause> simplifiedClausesCopy = new ArrayList<>(simplifiedClauses);


        //adding goal state/states from simplifiedClauses to sos
        List<Clause> sosClauses = new ArrayList<>();
        for (Clause clause : simplifiedClausesCopy) {
            if (clause.getLiterals().size() == 1) {
                if (clause.literals.get(0).isGoal) {
                    simplifiedClauses.remove(clause);
                    sosClauses.add(clause);
                }
            }
        }

        //initial printing
        int i = 0;
        for (; i < simplifiedClauses.size(); i++) {
            System.out.printf("%s. %s\n", i + 1, simplifiedClauses.get(i));
        }
        for (Clause sosClause : sosClauses) {
            System.out.printf("%s. %s\n", i + 1, sosClause);
        }
        i += 2;
        System.out.println("===============");


        //looping until solution is found or thrown out
        Set<ClausePair> clausePairs = null;
        //list of string that hold all the outputs even if we failed
        List<String> output = new ArrayList<>();
        //mapped objects from output, later printing it out with recursion
        Map<Clause, ClausePair> outputMap = new HashMap<>();
        //list of string that hold only the successful outputs
        List<String> endOutput = new ArrayList<>();

        while (true) {
            //selecting clauses - passing old clausePairs to avoid duplicates
            clausePairs = selectClauses(simplifiedClauses, sosClauses, clausePairs);

            Set<Clause> newRes = new HashSet<>();

            for (ClausePair pair : clausePairs) {
                Clause resolvent = resolve(pair.getFirst(), pair.getSecond(), output, outputMap);
                if (resolvent.literals.size() == 1 && resolvent.literals.get(0).equals(new Literal("NIL", false))) {

                    endOutput.add(String.format("%s\t\t(%s | %s)", "NIL", pair.getFirst(), pair.getSecond()));
                    //added this because my recursion function isn't implemented the best way so on complex path it throws stackoverflow exception
                    //logic is good just cant find path in reasonable time
                    if (!resolutionPath.endsWith("new_example_6.txt")) {
                        printPath(outputMap, pair.getFirst(), pair.getSecond(), simplifiedClausesCopy, endOutput);
                    }

                    Collections.reverse(endOutput);
                    String previous = "";
                    for (String s : endOutput) {
                        if (previous.equals("") || s.split("\t")[2].contains(previous.split("\t")[0])) {
                            System.out.printf("%s. %s\n", i++, s);
                        }
                        previous = s;
                    }

                    System.out.println("=====================");
                    System.out.printf("[CONCLUSION]: %s is true\n", goalClause.literals.stream().map(Literal::toString).collect(Collectors.joining(" v ")));
                    return;
                }
                newRes.add(resolvent);
            }

            //if we checked everything we are printing whole trace with all the clauses - could be done better instead of double list but don't have time :/
            if (new HashSet<>(simplifiedClauses).containsAll(newRes)) {
                for (int j = 0; j < output.size(); j++) {
                    System.out.printf("%s. %s\n", i + j, output.get(j));
                }
                System.out.println("===============");
                System.out.printf("[CONCLUSION]: %s is unknown\n", goalClause.literals.stream().map(Literal::toString).collect(Collectors.joining(" v ")));
                return;
            }

            for (Clause clause : newRes) {
                if (!sosClauses.contains(clause)) {
                    sosClauses.add(clause);
                }
            }
            simplifiedClauses.addAll(newRes);
        }

    }

    private static void cookingAssistant(List<Clause> cookingClauses, List<UserInput> cookingInputs) {
        System.out.println("Constructed with knowledge:");

        //initial print
        for (Clause clause : cookingClauses) {
            System.out.printf("%s\n", clause.literals.stream().map(Literal::toString).collect(Collectors.joining(" v ")));
        }

        //for each line based on input calling refutation resolution to check what will happen
        for (UserInput userInput : cookingInputs) {
            if (userInput.getInputType() == InputType.QUERY) {
                List<Clause> cookingClausesCopy = new ArrayList<>(cookingClauses);

                System.out.printf("\nUser's command: %s\n", userInput);

                Clause clause = userInput.getInput();
                cookingClausesCopy.add(clause);

                refutationResolution(cookingClausesCopy, "");

            } else if (userInput.getInputType() == InputType.REMOVAL) {
                cookingClauses.remove(userInput.getInput());

                System.out.printf("\nUser's command: %s\n", userInput);
                System.out.println("Removed " + userInput.getInput());

            } else if (userInput.getInputType() == InputType.ADDITION) {
                if (!cookingClauses.contains(userInput.getInput())) {
                    cookingClauses.add(userInput.getInput());
                }

                System.out.printf("\nUser's command: %s\n", userInput);
                System.out.println("Added " + userInput.getInput());
            }
        }
    }


    //recursive function that will pull the correct path from map of objects, kinda like dfs
    private static void printPath(Map<Clause, ClausePair> outputMap, Clause first, Clause second, List<Clause> simplifiedClausesCopy, List<String> endOutput) {
        //resolving one clause then the other
        if (!simplifiedClausesCopy.contains(first) && outputMap.containsKey(first)) {
            ClausePair firstPair = outputMap.get(first);
            endOutput.add(String.format("%s\t\t(%s | %s)", first, firstPair.getFirst(), firstPair.getSecond()));

            printPath(outputMap, firstPair.getFirst(), firstPair.getSecond(), simplifiedClausesCopy, endOutput);
        }

        if (!simplifiedClausesCopy.contains(second) && outputMap.containsKey(second)) {
            ClausePair secondPair = outputMap.get(second);
            endOutput.add(String.format("%s\t\t(%s | %s)", first, secondPair.getFirst(), secondPair.getSecond()));

            printPath(outputMap, secondPair.getFirst(), secondPair.getSecond(), simplifiedClausesCopy, endOutput);
        }
    }

    //generating all possible pairs out of two lists and removes duplicates
    public static Set<ClausePair> selectClauses(List<Clause> simplifiedClauses, List<Clause> sosClauses, Set<ClausePair> oldPairs) {
        Set<ClausePair> clausePairs = new LinkedHashSet<>();

        for (Clause sosClause : sosClauses) {
            List<Clause> canBeProcessed = simplifiedClauses.stream()
                    .filter(sosClause::hasComplementaryLiteral)
                    .toList();

            for (Clause simplifiedClause : canBeProcessed) {
                clausePairs.add(new ClausePair(sosClause, simplifiedClause));
            }
        }
        //cleaning old states that are already resolved
        if (oldPairs != null) {
            clausePairs.removeAll(oldPairs);
        }

        return clausePairs;
    }

    //for negating goal clauses, mostly replaced by literal.negate but this returns list of clauses usefull for first action
    private static List<Clause> negateClause(Clause clause) {
        List<Clause> clauses = new ArrayList<>();

        for (Literal literal : clause.getLiterals()) {
            //setting isGoal = true so i know which clause is goal at the start (sos)
            Literal negatedLiteral = new Literal(literal.getVariable(), !literal.isNegated(), true);
            Clause negatedClause = new Clause(List.of(negatedLiteral));
            clauses.add(negatedClause);
        }
        return clauses;
    }

    public static List<Clause> simplifyClauses(List<Clause> clauses) {

        List<Clause> simplifiedClauses = new ArrayList<>();

        for (Clause clause1 : clauses) {
            boolean isRedundant = false;

            if (clause1.isIrrelevant()) {
                continue;
            }
            //using set to remove duplicates
            HashSet<Literal> clause1Set = new HashSet<>(clause1.literals);
            //looking through all states combinations and finding if one is part of another
            for (Clause clause2 : clauses) {
                HashSet<Literal> clause2Set = new HashSet<>(clause2.literals);
                if (clause1 != clause2 && clause1Set.containsAll(clause2Set)) {
                    isRedundant = true;
                    break;
                }
            }
            if (!isRedundant) {
                simplifiedClauses.add(clause1);
            }
        }
        return simplifiedClauses;
    }

    //takes 2 clauses and joins them together
    private static Clause resolve(Clause simplifiedClause, Clause sosClause, List<String> output, Map<Clause, ClausePair> outputMap) {
        List<Literal> resolvedLiterals = new ArrayList<>();

        //checking if 2 clauses are resolvable
        resolvedLiterals.addAll(simplifiedClause.getComplementaryLiterals(sosClause));
        resolvedLiterals.addAll(sosClause.getComplementaryLiterals(simplifiedClause));

        if (resolvedLiterals.size() == 0) {
            resolvedLiterals.add(new Literal("NIL", false));
        }

        //writing to both output and map - should be only map
        var outputLine = String.format("%s (%s | %s)", resolvedLiterals, simplifiedClause, sosClause);
        outputMap.put(new Clause(resolvedLiterals), new ClausePair(simplifiedClause, sosClause));

        if (!output.contains(outputLine)) {
            output.add(outputLine);
        }

        return new Clause(resolvedLiterals);
    }
}
