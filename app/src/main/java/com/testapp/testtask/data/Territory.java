package com.testapp.testtask.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 04.05.17.
 * Recursive data type to store all the
 */

public class Territory implements Serializable {
    private String name;
    private List<Territory> regions;
    private boolean hasChildren;

    public Territory(String name, List<Territory> regions, boolean hasChildren) {
        String nameParts[] = name.split("-");
        if (name.length() == 1) {
            this.name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        else {
            String result = "";
            for (String namePart : nameParts) {
                if(!namePart.isEmpty()) {
                    result += namePart.substring(0, 1).toUpperCase() + namePart.substring(1) + " ";
                }

            }
            this.name = result;
        }

        this.regions = regions;
        this.hasChildren = hasChildren;
    }

    public String getName() {
        return String.valueOf(name);
    }

    public List<Territory> getRegions() {
        return Collections.unmodifiableList(regions);
    }

    public boolean hasChildren() {
        return hasChildren;
    }
}
