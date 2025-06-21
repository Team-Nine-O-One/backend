package com.team901.CapstoneDesign.service;



import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;

public class TSPSolver {

    static {
        System.loadLibrary("jniortools");
    }

    public static long solveTsp(double[][] distanceMatrix) {
        int nodeCount = distanceMatrix.length;

        RoutingIndexManager manager = new RoutingIndexManager(nodeCount, 1, 0);
        RoutingModel routing = new RoutingModel(manager);

        int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return (long) distanceMatrix[fromNode][toNode];
        });

        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        RoutingSearchParameters searchParameters = RoutingSearchParameters.newBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .build();

        Assignment solution = routing.solveWithParameters(searchParameters);
        if (solution == null) return Long.MAX_VALUE;

        long index = routing.start(0);
        long totalDistance = 0;
        while (!routing.isEnd(index)) {
            long nextIndex = solution.value(routing.nextVar(index));
            totalDistance += routing.getArcCostForVehicle(index, nextIndex, 0);
            index = nextIndex;
        }
        return totalDistance;
    }
}

