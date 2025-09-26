package org.example;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        int numPhysicalPages = 12;
        int numProcesses = 22;
        int numVirtualPagesPerProcess = 16;
        int workingSetSize = 5;
        int accessesPerProcess = 200;
        int workingSetChangePeriod = 50;
        int resetPeriod = 100;
        double workingSetBias = 0.90;

        try (PrintWriter log = new PrintWriter(new FileWriter("simulation_log.txt"))) {
            runScenario(
                    "RANDOM",
                    ReplacementPolicy.RANDOM,
                    numPhysicalPages,
                    numProcesses,
                    numVirtualPagesPerProcess,
                    workingSetSize,
                    accessesPerProcess,
                    workingSetChangePeriod,
                    resetPeriod,
                    workingSetBias,
                    log
            );
            log.println("\n========================================================\n");
            runScenario(
                    "NRU",
                    ReplacementPolicy.NRU,
                    numPhysicalPages,
                    numProcesses,
                    numVirtualPagesPerProcess,
                    workingSetSize,
                    accessesPerProcess,
                    workingSetChangePeriod,
                    resetPeriod,
                    workingSetBias,
                    log
            );
        }
        System.out.println("Лог збережено у simulation_log.txt");
    }

    private static void runScenario(
            String title,
            ReplacementPolicy policy,
            int numPhysicalPages,
            int numProcesses,
            int numVirtualPagesPerProcess,
            int workingSetSize,
            int accessesPerProcess,
            int workingSetChangePeriod,
            int resetPeriod,
            double workingSetBias,
            PrintWriter log
    ) {
        Random rnd = new Random(42);
        MMU mmu = new MMU(numPhysicalPages, policy, resetPeriod);

        List<Process> processes = new ArrayList<>();
        for (int pid = 1; pid <= numProcesses; pid++) {
            Process p = new Process(pid, numVirtualPagesPerProcess);
            p.initWorkingSet(workingSetSize, rnd);
            processes.add(p);
        }

        log.println("=== SCENARIO: " + title + " ===");
        log.printf("Frames=%d, Processes=%d, VPages/Proc=%d, WS=%d, Accesses/Proc=%d, WSchange=%d, resetR=%d, bias=%.2f%n",
                numPhysicalPages, numProcesses, numVirtualPagesPerProcess, workingSetSize, accessesPerProcess,
                workingSetChangePeriod, resetPeriod, workingSetBias);
        log.println();

        int round = 0;
        for (int step = 1; step <= accessesPerProcess; step++) {
            for (Process p : processes) {
                boolean write = rnd.nextDouble() < 0.3;
                int pageNum = p.pickVirtualPageForAccess(workingSetBias, rnd);

                mmu.accessPage(p, pageNum, write);

                if (step % workingSetChangePeriod == 0) {
                    p.refreshWorkingSet(workingSetSize, rnd);
                }
            }

            round++;
            if (round % 50 == 0) {
                log.printf("\n[Round %d]\n", round);

                log.println("Free frames:");
                for (PhysicalPage pp : mmu.getFreePhysicalPages()) {
                    log.printf("  Frame %d [FREE]%n", pp.getId());
                }

                log.println("Busy frames:");
                for (PhysicalPage pp : mmu.getBusyPhysicalPages()) {
                    VirtualPage vp = pp.getVirtualPage();
                    log.printf("  Frame %d -> VPage(ppn=%d, pres=%b, ref=%b, mod=%b)%n",
                            pp.getId(),
                            vp.getPpn(),
                            vp.isPresent(),
                            vp.isReferenced(),
                            vp.isModified()
                    );
                }
            }

        }

        Stats s = mmu.getStats();
        double missRate = s.getAccesses() == 0 ? 0.0 : (double) s.getPageFaults() / s.getAccesses();
        log.println();
        log.println("--- RESULT: " + title + " ---");
        log.printf("Accesses: %d (reads=%d, writes=%d)%n", s.getAccesses(), s.getReads(), s.getWrites());
        log.printf("PageFaults: %d, Evictions: %d, MissRate: %.4f%n", s.getPageFaults(), s.getEvictions(), missRate);

        if (policy == ReplacementPolicy.NRU) {
            log.printf("NRU classes picked: C0=%d, C1=%d, C2=%d, C3=%d%n",
                    s.getNruClass0(), s.getNruClass1(), s.getNruClass2(), s.getNruClass3());
        }
    }
}
