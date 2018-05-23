package taxi;

import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;

public class Customer extends Parcel implements TickListener {
    private boolean register = false;

    public Customer(ParcelDTO dto) {
        super(dto);
    }

    public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public boolean getRegister() {
        return this.register;
    }

    public PDPModel get_pdp_model() {
        return this.getPDPModel();
    }

    @Override
    public void tick(TimeLapse timeLapse) {}

    @Override
    public void afterTick(TimeLapse timeLapse) {
    }
}
