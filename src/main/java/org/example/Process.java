package org.example;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

@Data
public class Process {
    int id;
    PageTable pageTable = new PageTable(new ArrayList<>());
    WorkingSet workingSet = new WorkingSet(new HashSet<>());

    public Process(int id, int numPages) {
        this.id = id;
        for (int i = 0; i < numPages; i++) {
            VirtualPage vp = new VirtualPage();
            vp.setPid(id);
            vp.setVpn(i);
            pageTable.getVirtualPages().add(vp);
        }
    }

    public void initWorkingSet(int workingSetSize, Random rnd) {
        workingSet.getWorkingSet().clear();
        while (workingSet.getWorkingSet().size() < Math.min(workingSetSize, pageTable.getVirtualPages().size())) {
            workingSet.getWorkingSet().add(rnd.nextInt(pageTable.getVirtualPages().size()));
        }
    }

    public void refreshWorkingSet(int workingSetSize, Random rnd) {
        initWorkingSet(workingSetSize, rnd);
    }

    public int pickVirtualPageForAccess(double workingSetBias, Random rnd) {
        boolean fromWS = rnd.nextDouble() < workingSetBias && !workingSet.getWorkingSet().isEmpty();
        if (fromWS) {
            int idx = rnd.nextInt(workingSet.getWorkingSet().size());
            return workingSet.getWorkingSet().stream().skip(idx).findFirst().orElse(rnd.nextInt(pageTable.getVirtualPages().size()));
        } else {
            return rnd.nextInt(pageTable.getVirtualPages().size());
        }
    }

}
