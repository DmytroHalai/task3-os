package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@AllArgsConstructor
@Data
public class WorkingSet {
    private final Set<Integer> workingSet;
}
