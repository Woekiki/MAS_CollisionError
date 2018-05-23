//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package taxi;

import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.*;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.PathNotFoundException;
import com.github.rinde.rinsim.geom.Point;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;

public class Taxi extends Vehicle {
    private Queue<Point> cur_path;
    private RandomGenerator rng;
    private final static boolean VERBOSE = true;


    public Taxi(Point startPosition, int capacity, RandomGenerator rng) {
        super(VehicleDTO.builder().capacity(capacity).startPosition(startPosition).speed(1000.0D).build());
        this.rng = rng;
        this.cur_path = new LinkedList<>();
    }

    public void print(Object message) {
        if (VERBOSE) {
            System.out.println(this + ": " + message);
        }
    }

    public void afterTick(TimeLapse timeLapse) {
    }


    protected void delegate_mas_impl(TimeLapse time) {
        GraphRoadModelImpl rm = (GraphRoadModelImpl) this.getRoadModel();

        /*
         * This code will make the taxis move along a random path
         * This random path is manually followed point by point since we had to check for our own created roadblocks
         * If we were to use plain "followPath", then the taxi wouldn't stop for a roadblock
         */
        while (time.hasTimeLeft()) {
            while (cur_path.isEmpty()) {
                try {
                    // If currently, you're not moving, choose a random path
                    cur_path = new LinkedList<>(rm.getShortestPathTo(rm.getPosition(this), rm.getRandomPosition(rng)));
                    cur_path.poll();
                    print("New random path " + cur_path);

                } catch (PathNotFoundException e) {
                } catch (IllegalArgumentException e) {}

            }

            // We are now sure our taxi has a path it can follow
            if (rm.getConnection(this).isPresent()) {
                print(rm.getConnection(this));
                print(time.getTime());
            }

            try {
                print("Moving to " + cur_path.peek());
                // Finds the shortest path since turning around on an edge you're already on is not possible.
                List<Point> path = rm.getShortestPathTo(rm.getPosition(this), cur_path.peek());

                MoveProgress mp;
                try {
                    mp = rm.followPath(this, new LinkedList<>(path), time);
                } catch (IllegalArgumentException e) {
                    // TODO BUGFIX!!!!
                    print("Current intended path: " + cur_path);
                    print("Following path " + path);
                    print("Current position " + rm.getPosition(this));
                    print("Current connection " + rm.getConnection(this));

                    for (Taxi t: rm.getObjectsOfType(Taxi.class)) {
                        print("Taxi " + t + " at pos " + rm.getPosition(t));
                    }

                    e.printStackTrace();
                    //cur_path.poll();
                    break;
                }
                if (mp.travelledNodes().contains(cur_path.peek())) {
                    cur_path.poll();
                }
            } catch (PathNotFoundException e) {
                cur_path.remove();
            }
        }
    }

    protected void tickImpl(TimeLapse time) {
        delegate_mas_impl(time);
    }
}
