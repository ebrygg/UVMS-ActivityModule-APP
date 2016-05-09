package eu.europa.ec.fisheries.ers.fa.entities;

// Generated 06-May-2016 14:29:53 by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * ActivityFluxReportDocument generated by hbm2java
 */
@Entity
@Table(name = "activity_flux_report_document", schema = "activity")
public class FluxReportDocument implements java.io.Serializable {

	private int id;
	private String fluxReportDocumentId;
	private String referenceId;
	private Date creationDatetime;
	private String purposeCode;
	private String purposeCodeListId;
	private String purpose;
	private String ownerFluxPartyId;
	private String ownerFluxPartyName;
	private Set<FaReportDocument> faReportDocuments = new HashSet<FaReportDocument>(
			0);
	private Set<FluxFaReportMessage> fluxFaReportMessages = new HashSet<FluxFaReportMessage>(
			0);

	public FluxReportDocument() {
	}

	public FluxReportDocument(int id, Date creationDatetime,
			String purposeCode, String purposeCodeListId,
			String ownerFluxPartyId) {
		this.id = id;
		this.creationDatetime = creationDatetime;
		this.purposeCode = purposeCode;
		this.purposeCodeListId = purposeCodeListId;
		this.ownerFluxPartyId = ownerFluxPartyId;
	}

	public FluxReportDocument(int id, String fluxReportDocumentId,
			String referenceId, Date creationDatetime, String purposeCode,
			String purposeCodeListId, String purpose, String ownerFluxPartyId,
			String ownerFluxPartyName,
			Set<FaReportDocument> FaReportDocuments,
			Set<FluxFaReportMessage> FluxFaReportMessages) {
		this.id = id;
		this.fluxReportDocumentId = fluxReportDocumentId;
		this.referenceId = referenceId;
		this.creationDatetime = creationDatetime;
		this.purposeCode = purposeCode;
		this.purposeCodeListId = purposeCodeListId;
		this.purpose = purpose;
		this.ownerFluxPartyId = ownerFluxPartyId;
		this.ownerFluxPartyName = ownerFluxPartyName;
		this.faReportDocuments = faReportDocuments;
		this.fluxFaReportMessages = fluxFaReportMessages;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "flux_report_document_id")
	public String getFluxReportDocumentId() {
		return this.fluxReportDocumentId;
	}

	public void setFluxReportDocumentId(String fluxReportDocumentId) {
		this.fluxReportDocumentId = fluxReportDocumentId;
	}

	@Column(name = "reference_id")
	public String getReferenceId() {
		return this.referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_datetime", nullable = false, length = 29)
	public Date getCreationDatetime() {
		return this.creationDatetime;
	}

	public void setCreationDatetime(Date creationDatetime) {
		this.creationDatetime = creationDatetime;
	}

	@Column(name = "purpose_code", nullable = false)
	public String getPurposeCode() {
		return this.purposeCode;
	}

	public void setPurposeCode(String purposeCode) {
		this.purposeCode = purposeCode;
	}

	@Column(name = "purpose_code_list_id", nullable = false)
	public String getPurposeCodeListId() {
		return this.purposeCodeListId;
	}

	public void setPurposeCodeListId(String purposeCodeListId) {
		this.purposeCodeListId = purposeCodeListId;
	}

	@Column(name = "purpose")
	public String getPurpose() {
		return this.purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	@Column(name = "owner_flux_party_id", nullable = false)
	public String getOwnerFluxPartyId() {
		return this.ownerFluxPartyId;
	}

	public void setOwnerFluxPartyId(String ownerFluxPartyId) {
		this.ownerFluxPartyId = ownerFluxPartyId;
	}

	@Column(name = "owner_flux_party_name")
	public String getOwnerFluxPartyName() {
		return this.ownerFluxPartyName;
	}

	public void setOwnerFluxPartyName(String ownerFluxPartyName) {
		this.ownerFluxPartyName = ownerFluxPartyName;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "fluxReportDocument")
	public Set<FaReportDocument> getFaReportDocuments() {
		return this.faReportDocuments;
	}

	public void setFaReportDocuments(
			Set<FaReportDocument> faReportDocuments) {
		this.faReportDocuments = faReportDocuments;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "fluxReportDocument")
	public Set<FluxFaReportMessage> getFluxFaReportMessages() {
		return this.fluxFaReportMessages;
	}

	public void setFluxFaReportMessages(
			Set<FluxFaReportMessage> fluxFaReportMessages) {
		this.fluxFaReportMessages = fluxFaReportMessages;
	}

}
