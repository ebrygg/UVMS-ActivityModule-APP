package eu.europa.ec.fisheries.mdr.domain_temp;

import eu.europa.ec.fisheries.mdr.domain.base.MasterDataRegistry;
import eu.europa.ec.fisheries.mdr.exception.FieldNotMappedException;
import un.unece.uncefact.data.standard.response.MDRDataNodeType;

/**
 * Created by georgige on 11/22/2016.
 */
public class FluxGpParty extends MasterDataRegistry {

    private String CODE2;
    private String ENNAME;

    @Override
    public void populate(MDRDataNodeType mdrDataType) throws FieldNotMappedException {
        super.populateCommonFields(mdrDataType);
    }

    @Override
    public String getAcronym() {
        return "FLUX_GP_PARTY";
    }
}
