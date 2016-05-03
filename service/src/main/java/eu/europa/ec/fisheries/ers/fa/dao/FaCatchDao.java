package eu.europa.ec.fisheries.ers.fa.dao;

import eu.europa.ec.fisheries.ers.fa.entities.FaCatch;
import eu.europa.ec.fisheries.uvms.service.AbstractDAO;

import javax.persistence.EntityManager;

/**
 * Created by padhyad on 5/3/2016.
 */
public class FaCatchDao extends AbstractDAO<FaCatch> {

    private EntityManager em;

    public FaCatchDao(EntityManager em) {
        this.em = em;
    }

    @Override
    public EntityManager getEntityManager() {
        return null;
    }
}
