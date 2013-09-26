/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package flexgridsim;

/**
 * A path is nothing more than a list of lightpaths.
 * 
 * @author andred
 */
public class Path {
    
    private LightPath[] lightpaths;
    
    /**
     * Creates a new Path object.
     * 
     * @param lightpaths list of LightPath objects that form the Path
     */
    public Path(LightPath[] lightpaths) {
        this.lightpaths = lightpaths;
    }
    
    /**
     * Returns the list of lightpaths that belong to a given Path.
     * 
     * @return lightpaths list of LightPath objects that form the Path
     */
    public LightPath[] getLightpaths() {
        return lightpaths;
    }
}
