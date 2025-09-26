package org.example;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Data
public class MMU {
    private final List<PhysicalPage> freePhysicalPages;
    private final List<PhysicalPage> busyPhysicalPages;
    private final Random random = new Random();
    private final ReplacementPolicy policy;
    private final Stats stats = new Stats();
    private final int resetPeriod;
    private long accessesSinceReset = 0;

    public MMU(int numPhysicalPages, ReplacementPolicy policy, int resetPeriod) {
        this.policy = policy;
        this.resetPeriod = Math.max(1, resetPeriod);

        freePhysicalPages = new ArrayList<>();
        busyPhysicalPages = new ArrayList<>();

        for (int i = 0; i < numPhysicalPages; i++) {
            PhysicalPage physicalPage = new PhysicalPage();
            physicalPage.setId(i);
            freePhysicalPages.add(physicalPage);
        }
    }

    public void accessPage(Process process, int pageNum, boolean isWrite) {
        stats.incAccess(isWrite);
        accessesSinceReset++;

        VirtualPage virtualPage = process.getPageTable().getVirtualPages().get(pageNum);
        if (virtualPage.isPresent()) {
            virtualPage.setReferenced(true);
            if (isWrite) virtualPage.setModified(true);
        } else {
            stats.setPageFaults(stats.getPageFaults() + 1);
            handlePageFault(virtualPage, isWrite);
        }

        if (accessesSinceReset >= resetPeriod) {
            resetReferencedBits();
            accessesSinceReset = 0;
        }
    }

    private void handlePageFault(VirtualPage virtualPage, boolean isWrite) {
        if (!freePhysicalPages.isEmpty()) {
            PhysicalPage physicalPage = freePhysicalPages.remove(0);
            loadPage(virtualPage, physicalPage, isWrite);
            return;
        }

        PhysicalPage victim;
        if (Objects.requireNonNull(policy) == ReplacementPolicy.NRU) {
            victim = pickNRUVictim();
        } else {
            victim = pickRandomVictim();
        }

        stats.setEvictions(stats.getEvictions() + 1);

        unloadPage(victim.getVirtualPage());
        loadPage(virtualPage, victim, isWrite);
    }

    private PhysicalPage pickRandomVictim() {
        return busyPhysicalPages.get(random.nextInt(busyPhysicalPages.size()));
    }

    private PhysicalPage pickNRUVictim() {
        List<PhysicalPage> class0 = new ArrayList<>();
        List<PhysicalPage> class1 = new ArrayList<>();
        List<PhysicalPage> class2 = new ArrayList<>();
        List<PhysicalPage> class3 = new ArrayList<>();

        for (PhysicalPage physicalPage : busyPhysicalPages) {
            VirtualPage virtualPage = physicalPage.getVirtualPage();
            boolean referenced = virtualPage.isReferenced();
            boolean modified = virtualPage.isModified();
            if (!referenced && !modified) class0.add(physicalPage);
            else if (!referenced && modified) class1.add(physicalPage);
            else if (referenced && !modified) class2.add(physicalPage);
            else class3.add(physicalPage);
        }

        if (!class0.isEmpty()) {
            stats.setNruClass0(stats.getNruClass0() + 1);
            return class0.get(random.nextInt(class0.size()));
        }
        if (!class1.isEmpty()) {
            stats.setNruClass1(stats.getNruClass1() + 1);
            return class1.get(random.nextInt(class1.size()));
        }
        if (!class2.isEmpty()) {
            stats.setNruClass2(stats.getNruClass2() + 1);
            return class2.get(random.nextInt(class2.size()));
        }
        stats.setNruClass3(stats.getNruClass3() + 1);
        return class3.get(random.nextInt(class3.size()));
    }

    private void loadPage(VirtualPage virtualPage, PhysicalPage physicalPage, boolean isWrite) {
        freePhysicalPages.remove(physicalPage);
        if (!busyPhysicalPages.contains(physicalPage)) {
            busyPhysicalPages.add(physicalPage);
        }

        physicalPage.setVirtualPage(virtualPage);
        virtualPage.setPresent(true);
        virtualPage.setReferenced(true);
        virtualPage.setModified(isWrite);
        virtualPage.setPpn(physicalPage.getId());
    }


    private void unloadPage(VirtualPage virtualPage) {
        PhysicalPage physicalPage = busyPhysicalPages.stream()
                .filter(pp -> pp.getVirtualPage() == virtualPage)
                .findFirst()
                .orElse(null);

        if (physicalPage != null) {
            busyPhysicalPages.remove(physicalPage);
            freePhysicalPages.add(physicalPage);
        }

        virtualPage.setPresent(false);
        virtualPage.setPpn(-1);
    }

    public void resetReferencedBits() {
        for (PhysicalPage physicalPage : busyPhysicalPages) {
            if (physicalPage.getVirtualPage() != null) {
                physicalPage.getVirtualPage().setReferenced(false);
            }
        }
    }
}
