package eu.europa.ec.fisheries.ers.fa.entities;

// Generated 06-May-2016 14:29:53 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * ActivityContactPerson generated by hbm2java
 */
@Entity
@Table(name = "activity_contact_person", schema = "activity")
public class ContactPerson implements java.io.Serializable {

	@Id
	@Column(name = "id", unique = true, nullable = false)
	private int id;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "given_name")
	private String givenName;
	
	@Column(name = "middle_name")
	private String middleName;
	
	@Column(name = "family_name")
	private String familyName;
	
	@Column(name = "family_name_prefix")
	private String familyNamePrefix;
	
	@Column(name = "name_suffix")
	private String nameSuffix;
	
	@Column(name = "gender")
	private String gender;
	
	@Column(name = "alias")
	private String alias;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "contactPerson")
	private Set<ContactParty> contactParties = new HashSet<ContactParty>(
			0);

	public ContactPerson() {
	}

	public ContactPerson(int id) {
		this.id = id;
	}

	public ContactPerson(int id, String title, String givenName,
			String middleName, String familyName, String familyNamePrefix,
			String nameSuffix, String gender, String alias,
			Set<ContactParty> contactParties) {
		this.id = id;
		this.title = title;
		this.givenName = givenName;
		this.middleName = middleName;
		this.familyName = familyName;
		this.familyNamePrefix = familyNamePrefix;
		this.nameSuffix = nameSuffix;
		this.gender = gender;
		this.alias = alias;
		this.contactParties = contactParties;
	}


	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public String getGivenName() {
		return this.givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	
	public String getMiddleName() {
		return this.middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	
	public String getFamilyName() {
		return this.familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	
	public String getFamilyNamePrefix() {
		return this.familyNamePrefix;
	}

	public void setFamilyNamePrefix(String familyNamePrefix) {
		this.familyNamePrefix = familyNamePrefix;
	}

	
	public String getNameSuffix() {
		return this.nameSuffix;
	}

	public void setNameSuffix(String nameSuffix) {
		this.nameSuffix = nameSuffix;
	}

	
	public String getGender() {
		return this.gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	
	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	
	public Set<ContactParty> getContactParties() {
		return this.contactParties;
	}

	public void setContactParties(
			Set<ContactParty> contactParties) {
		this.contactParties = contactParties;
	}

}
