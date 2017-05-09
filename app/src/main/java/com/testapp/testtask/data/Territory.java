package com.testapp.testtask.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 04.05.17.
 * Recursive abstract data type to store the regions from xml file.
 */

public class Territory implements Serializable {
    private String name;
    private List<Territory> regions;

    public Territory(String name, List<Territory> regions) {
        this.name = name;
        this.regions = regions;
    }

    /**
     * Capitalized representation of the name of the region
     * @return formatted string
     */
    public String getName() {
        String nameParts[] = name.split("-");
        if (name.length() == 1) {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        else {
            String result = "";
            for (String namePart : nameParts) {
                if(!namePart.isEmpty()) {
                    result += namePart.substring(0, 1).toUpperCase() + namePart.substring(1) + " ";
                }

            }
            return result;
        }
    }

    /**
     * Region inside this region.
     * @return list of sub-regions inside this region, otherwise empty list.
     */
    public List<Territory> getRegions() {
        return Collections.unmodifiableList(regions);
    }

    /**
     * Indicates if the region has sub-regions.
     * @return true if there are sub-regions, otherwise false.
     */
    public boolean hasChildren() {
        if (regions.isEmpty() || regions == null) {
            return false;
        }
        else {
            return true;
        }
    }
}
