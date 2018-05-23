//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.*;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.event.Listener;
import com.github.rinde.rinsim.examples.taxi.TaxiExample;
import com.github.rinde.rinsim.geom.*;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.github.rinde.rinsim.geom.io.Filters;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.View.Builder;
import com.github.rinde.rinsim.ui.renderers.GraphRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.collect.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nullable;

import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import taxi.*;
import taxi.TaxiRenderer.Language;

public final class WorldBuilder {
    private static final int NUM_TAXIS = 10;  // 10
    private static final int TAXI_CAPACITY = 1;
    private static final long END_TIME = 92233000L;  //50000
    private static final Map<String, Graph<MultiAttributeData>> GRAPH_CACHE = Maps.newHashMap();
    private static final boolean VERBOSE = true;
    private static ArrayList<Taxi> taxis;

    private WorldBuilder() {
    }

    public static void main(@Nullable String[] args) {
        taxis = new ArrayList<>();
        long endTime = args != null && args.length >= 1 ? Long.parseLong(args[0]) : END_TIME;

        final Simulator simulator = Simulator.builder().build();
        final RandomGenerator rng = simulator.getRandomGenerator();

        run(false, endTime, (Display) null, (Monitor) null, (Listener) null);
    }

    public static void print(String msg) {
        if (VERBOSE) {
            System.out.println(msg);
        }
    }

    public static Simulator run(boolean testing, final long endTime, @Nullable Display display, @Nullable Monitor m, @Nullable Listener list) {
        Builder view = createGui(testing, display, m, list);

        Graph<?> graph = WorldBuilder.GraphCreator.createGraph();

        final Simulator simulator = Simulator.builder().addModel(RoadModelBuilders.staticGraph(graph))
                .addModel(DefaultPDPModel.builder()).addModel(view).build();
        final RandomGenerator rng = simulator.getRandomGenerator();
        final RoadModel roadModel = simulator.getModelProvider().getModel(RoadModel.class);
        int i;
        print("Generating world");

        for (i = 0; i < NUM_TAXIS; ++i) {
            Taxi taxi = new Taxi(roadModel.getRandomPosition(rng), TAXI_CAPACITY, rng);
            simulator.register(taxi);
            taxis.add(taxi);
        }
        print("Taxis generated");

        PDPModel model = (PDPModel)simulator.getModels().asList().get(1);

        simulator.addTickListener(new TickListener() {
            public void tick(TimeLapse time) {
                if (time.getStartTime() > endTime) {
                    simulator.stop();
                }
            }

            public void afterTick(TimeLapse timeLapse) {
            }
        });
        simulator.start();
        return simulator;
    }

    static Builder createGui(boolean testing, @Nullable Display display, @Nullable Monitor m, @Nullable Listener list) {
        Builder view = View.builder().with(GraphRoadModelRenderer.builder().withNodeCoordinates())
                .with(RoadUserRenderer.builder()
                        .withImageAssociation(Taxi.class, "/graphics/flat/taxi-32.png").withToStringLabel()
                        .withImageAssociation(Customer.class, "/graphics/flat/person-red-32.png").withToStringLabel())
                .with(TaxiRenderer.builder(Language.ENGLISH)).withTitleAppendix("Taxi example");
        if (testing) {
            view = view.withAutoClose().withAutoPlay().withSimulatorEndTime(1200000L).withSpeedUp(64);
        } else if (m != null && list != null && display != null) {
            view = view.withMonitor(m).withSpeedUp(4).withResolution(m.getClientArea().width, m.getClientArea().height).withDisplay(display).withCallback(list).withAsync().withAutoPlay().withAutoClose();
        }

        return view;
    }

    static Graph<MultiAttributeData> loadGraph(String name) {
        try {
            if (GRAPH_CACHE.containsKey(name)) {
                return (Graph) GRAPH_CACHE.get(name);
            } else {
                Graph<MultiAttributeData> g = DotGraphIO.getMultiAttributeGraphIO(Filters.selfCycleFilter()).read(TaxiExample.class.getResourceAsStream(name));
                GRAPH_CACHE.put(name, g);
                return g;
            }
        } catch (FileNotFoundException var2) {
            throw new IllegalStateException(var2);
        } catch (IOException var3) {
            throw new IllegalStateException(var3);
        }
    }

    static class GraphCreator {
        GraphCreator() {
        }

        static ImmutableTable<Integer, Integer, Point> createMatrix(int cols, int rows, Point offset) {
            com.google.common.collect.ImmutableTable.Builder<Integer, Integer, Point> builder = ImmutableTable.builder();

            for(int c = 0; c < cols; ++c) {
                for(int r = 0; r < rows; ++r) {
                    builder.put(r, c, new Point(offset.x + (double)c * 2.0D * 2.0D, offset.y + (double)r * 2.0D * 2.0D));
                }
            }

            return builder.build();
        }

        static ListenableGraph<LengthData> createGraph() {
            Graph<LengthData> g = new TableGraph();
            Table<Integer, Integer, Point> leftMatrix = createMatrix(5, 10, new Point(0.0D, 0.0D));
            Iterator var2 = leftMatrix.columnMap().values().iterator();

            while(var2.hasNext()) {
                Map<Integer, Point> column = (Map)var2.next();
                Graphs.addBiPath(g, column.values());
            }

            Graphs.addBiPath(g, leftMatrix.row(4).values());
            Graphs.addBiPath(g, leftMatrix.row(5).values());
            Table<Integer, Integer, Point> rightMatrix = createMatrix(10, 7, new Point(30.0D, 6.0D));
            Iterator var6 = rightMatrix.rowMap().values().iterator();

            while(var6.hasNext()) {
                Map<Integer, Point> row = (Map)var6.next();
                Graphs.addBiPath(g, row.values());
            }

            Graphs.addBiPath(g, rightMatrix.column(0).values());
            Graphs.addBiPath(g, rightMatrix.column(rightMatrix.columnKeySet().size() - 1).values());
            Graphs.addPath(g, new Point[]{(Point)rightMatrix.get(2, 0), (Point)leftMatrix.get(4, 4)});
            Graphs.addPath(g, new Point[]{(Point)leftMatrix.get(5, 4), (Point)rightMatrix.get(4, 0)});
            return new ListenableGraph(g);
        }
    }
}
