package eu.europa.ec.fisheries.ers.fa.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

@Entity
@Table(name = "activity_aap_process")
public class AapProcess implements Serializable {

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fa_catch_id")
	private FaCatch faCatch;
	
	@Column(name = "type_code", nullable = false)
	private String typeCode;
	
	@Column(name = "type_code_list_id", nullable = false)
	private String typeCodeListId;
	
	@Column(name = "conversion_factor")
	private Integer conversionFactor;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "aapProcess")
	private Set<AapProduct> aapProducts = new HashSet<AapProduct>(0);

	public AapProcess() {
	}

	public AapProcess(int id, String typeCode, String typeCodeListId) {
		this.id = id;
		this.typeCode = typeCode;
		this.typeCodeListId = typeCodeListId;
	}

	public AapProcess(int id, FaCatch faCatch, String typeCode,
			String typeCodeListId, Integer conversionFactor,
			Set<AapProduct> aapProducts) {
		this.id = id;
		this.faCatch = faCatch;
		this.typeCode = typeCode;
		this.typeCodeListId = typeCodeListId;
		this.conversionFactor = conversionFactor;
		this.aapProducts = aapProducts;
	}

	
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	public FaCatch getFaCatch() {
		return this.faCatch;
	}

	public void setFaCatch(FaCatch faCatch) {
		this.faCatch = faCatch;
	}

	
	public String getTypeCode() {
		return this.typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	
	public String getTypeCodeListId() {
		return this.typeCodeListId;
	}

	public void setTypeCodeListId(String typeCodeListId) {
		this.typeCodeListId = typeCodeListId;
	}

	
	public Integer getConversionFactor() {
		return this.conversionFactor;
	}

	public void setConversionFactor(Integer conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	
	public Set<AapProduct> getAapProducts() {
		return this.aapProducts;
	}

	public void setAapProducts(Set<AapProduct> aapProducts) {
		this.aapProducts = aapProducts;
	}

}
