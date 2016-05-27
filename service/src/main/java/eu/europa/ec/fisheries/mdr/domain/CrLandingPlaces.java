package eu.europa.ec.fisheries.mdr.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.codehaus.plexus.util.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import xeu.ec.fisheries.flux_bl.flux_mdr_codelist._1.FieldType;

/**
 * @author kovian
 *
 */
@Entity
@Table(name = "cr_landing_places")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrLandingPlaces extends ExtendedMasterDataRegistry {
	private static final long serialVersionUID = 1L;
	
	// TODO : See the excel for this class; further implementation is needed;
	
	@Override
	public String getAcronym() {
		return "LANDING_PLACE";
	}
	
	@Override
	public void populate(List<FieldType> fields) {
		for(FieldType field : fields){
			String fieldName  = field.getFieldName().getValue();
			String fieldValue = field.getFieldValue().getValue();
			if(StringUtils.equalsIgnoreCase(CODE, fieldName)){
				this.setCode(fieldValue);
			} else if(StringUtils.equalsIgnoreCase(DESCRIPTION, fieldName)){
				this.setDescription(fieldValue);
			}
		}
		
	}
}
