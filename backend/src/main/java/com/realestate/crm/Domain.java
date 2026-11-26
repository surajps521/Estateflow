package com.realestate.crm;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.Check;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
abstract class BaseEntity {
  @Id @GeneratedValue(strategy=GenerationType.UUID)
  @Column(nullable=false, updatable=false, columnDefinition="uuid") UUID id;
  @CreatedDate @Column(nullable=false, updatable=false) Instant createdAt;
  @LastModifiedDate @Column(nullable=false) Instant updatedAt;
  public UUID getId(){return id;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
enum Role { SUPER_ADMIN, OWNER, AGENT, CUSTOMER }
enum PropertyStatus { AVAILABLE, RESERVED, SOLD, ARCHIVED }
enum LeadStage { NEW, CONTACTED, VISIT_SCHEDULED, NEGOTIATION, WON, LOST }
enum VisitStatus { SCHEDULED, COMPLETED, CANCELLED }
enum AssignmentRequestStatus { PENDING, ACCEPTED, REJECTED, CANCELLED }
enum NotificationType { AGENT_REQUEST, REQUEST_UPDATE, RECOMMENDATION, SITE_VISIT, DEAL }

@Entity @Table(name="users", uniqueConstraints=@UniqueConstraint(columnNames="email"))
@Check(constraints="role in ('SUPER_ADMIN', 'OWNER', 'AGENT', 'CUSTOMER')")
class User extends BaseEntity {
  @Column(nullable=false) String email;
  @Column(name="password_hash", nullable=false) String passwordHash;
  @Enumerated(EnumType.STRING) @Column(nullable=false) Role role;
  @Column(nullable=false,columnDefinition="boolean default true") boolean active=true;
  protected User(){}
  User(String email,String passwordHash,Role role){this.email=email;this.passwordHash=passwordHash;this.role=role;}
  public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public Role getRole(){return role;} public boolean isActive(){return active;} public void setActive(boolean active){this.active=active;}
}

@Entity @Table(name="agent_profiles", uniqueConstraints=@UniqueConstraint(columnNames="user_id"))
class AgentProfile extends BaseEntity {
  @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false,unique=true) User user;
  @Column(name="display_name",nullable=false) String displayName;
  @Column(length=2000) String bio;
  @Column(nullable=false) String phone;
  @Column(nullable=false) Integer experienceYears=0;
  @Column(nullable=false) String specialization;
  @Column(name="profile_image_url") String profileImageUrl;
  protected AgentProfile(){}
  AgentProfile(User user,String displayName,String phone,Integer experienceYears,String specialization,String profileImageUrl){this.user=user;this.displayName=displayName;this.phone=phone;this.experienceYears=experienceYears;this.specialization=specialization;this.profileImageUrl=profileImageUrl;}
  public User getUser(){return user;} public String getDisplayName(){return displayName;} public String getBio(){return bio;} public String getPhone(){return phone;} public Integer getExperienceYears(){return experienceYears;} public String getSpecialization(){return specialization;} public String getProfileImageUrl(){return profileImageUrl;}
}

@Entity @Table(name="customer_profiles", uniqueConstraints=@UniqueConstraint(columnNames="user_id"))
class CustomerProfile extends BaseEntity {
  @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false,unique=true) User user;
  @Column(name="display_name",nullable=false) String displayName;
  @Column(nullable=false) String phone;
  @Column(name="preferred_location") String preferredLocation;
  @Column(name="budget_max",precision=14,scale=2) BigDecimal budgetMax;
  @Column(length=2000) String requirements;
  protected CustomerProfile(){}
  CustomerProfile(User user,String displayName,String phone,String preferredLocation,BigDecimal budgetMax,String requirements){this.user=user;this.displayName=displayName;this.phone=phone;this.preferredLocation=preferredLocation;this.budgetMax=budgetMax;this.requirements=requirements;}
  public User getUser(){return user;} public String getDisplayName(){return displayName;} public String getPhone(){return phone;} public String getPreferredLocation(){return preferredLocation;} public BigDecimal getBudgetMax(){return budgetMax;} public String getRequirements(){return requirements;}
}
@Entity @Table(name="properties", indexes={@Index(columnList="status"),@Index(columnList="type")})
class Property extends BaseEntity {
  @Column(nullable=false) String title;
  @Column(name="type",nullable=false) String type;
  @Enumerated(EnumType.STRING) @Column(nullable=false) PropertyStatus status=PropertyStatus.AVAILABLE;
  @Column(nullable=false,precision=14,scale=2) BigDecimal price;
  @Column(name="base_price",precision=14,scale=2) BigDecimal basePrice;
  @Column(name="area_sqft",nullable=false) Integer areaSqft;
  @Column String location;
  @Column String configuration;
  @Column(name="image_url",columnDefinition="TEXT") String imageUrl;
  protected Property(){}
  public String getTitle(){return title;} public String getType(){return type;} public PropertyStatus getStatus(){return status;}
  public BigDecimal getPrice(){return price;} public BigDecimal getBasePrice(){return basePrice;} public Integer getAreaSqft(){return areaSqft;} public String getLocation(){return location;} public String getConfiguration(){return configuration;} public String getImageUrl(){return imageUrl;}
}
@Entity @Table(name="leads")
class Lead extends BaseEntity {
  @Column(name="full_name",nullable=false) String fullName;
  @Column(nullable=false) String phone;
  @Enumerated(EnumType.STRING) @Column(nullable=false) LeadStage stage=LeadStage.NEW;
  @Column(name="budget_max",precision=14,scale=2) BigDecimal budgetMax;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="agent_id",nullable=false) User agent;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="customer_id") User customer;
  protected Lead(){}
  public String getFullName(){return fullName;} public String getPhone(){return phone;} public LeadStage getStage(){return stage;}
  public BigDecimal getBudgetMax(){return budgetMax;} public User getAgent(){return agent;} public User getCustomer(){return customer;}
}
@Entity @Table(name="site_visits")
class SiteVisit extends BaseEntity {
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="lead_id",nullable=false) Lead lead;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="property_id",nullable=false) Property property;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="agent_id",nullable=false) User agent;
  @Column(name="visit_time",nullable=false) Instant visitTime;
  @Enumerated(EnumType.STRING) @Column(nullable=false) VisitStatus status=VisitStatus.SCHEDULED;
  @Column(length=2000) String feedback;
  protected SiteVisit(){}
  public Lead getLead(){return lead;} public Property getProperty(){return property;} public User getAgent(){return agent;}
  public Instant getVisitTime(){return visitTime;} public VisitStatus getStatus(){return status;} public String getFeedback(){return feedback;}
}
@Entity @Table(name="deals", uniqueConstraints=@UniqueConstraint(columnNames="property_id"))
class Deal extends BaseEntity {
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="lead_id",nullable=false) Lead lead;
  @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="property_id",nullable=false,unique=true) Property property;
  @Column(name="final_price",nullable=false,precision=14,scale=2) BigDecimal finalPrice;
  @Column(name="closed_date",nullable=false) LocalDate closedDate;
  protected Deal(){}
  public Lead getLead(){return lead;} public Property getProperty(){return property;} public BigDecimal getFinalPrice(){return finalPrice;} public LocalDate getClosedDate(){return closedDate;}
}

@Entity @Table(name="agent_assignment_requests", indexes={@Index(columnList="agent_id,status"),@Index(columnList="customer_id,status")})
class AgentAssignmentRequest extends BaseEntity {
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) User customer;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="agent_id",nullable=false) User agent;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="property_id") Property property;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="lead_id") Lead lead;
  @Enumerated(EnumType.STRING) @Column(nullable=false) AssignmentRequestStatus status=AssignmentRequestStatus.PENDING;
  @Column(length=2000) String requirements;
  @Version long version;
  protected AgentAssignmentRequest(){}
  AgentAssignmentRequest(User customer,User agent,Property property,String requirements){this.customer=customer;this.agent=agent;this.property=property;this.requirements=requirements;}
  public User getCustomer(){return customer;} public User getAgent(){return agent;} public Property getProperty(){return property;} public Lead getLead(){return lead;} public void setLead(Lead lead){this.lead=lead;} public AssignmentRequestStatus getStatus(){return status;} public void setStatus(AssignmentRequestStatus status){this.status=status;} public String getRequirements(){return requirements;}
}

@Entity @Table(name="property_recommendations", uniqueConstraints=@UniqueConstraint(columnNames={"agent_id","customer_id","property_id"}))
class PropertyRecommendation extends BaseEntity {
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="agent_id",nullable=false) User agent;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) User customer;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="property_id",nullable=false) Property property;
  @Column(length=1000) String note;
  protected PropertyRecommendation(){}
  PropertyRecommendation(User agent,User customer,Property property,String note){this.agent=agent;this.customer=customer;this.property=property;this.note=note;}
  public User getAgent(){return agent;} public User getCustomer(){return customer;} public Property getProperty(){return property;} public String getNote(){return note;}
}

@Entity @Table(name="notifications", indexes=@Index(columnList="recipient_id,is_read"))
class Notification extends BaseEntity {
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="recipient_id",nullable=false) User recipient;
  @Column(nullable=false) String title;
  @Column(nullable=false,length=2000) String message;
  @Enumerated(EnumType.STRING) @Column(nullable=false) NotificationType notificationType;
  @Column(name="related_entity_id") UUID relatedEntityId;
  @Column(name="is_read",nullable=false) boolean read=false;
  protected Notification(){}
  Notification(User recipient,String title,String message,NotificationType type,UUID relatedEntityId){this.recipient=recipient;this.title=title;this.message=message;this.notificationType=type;this.relatedEntityId=relatedEntityId;}
  public String getTitle(){return title;} public String getMessage(){return message;} public NotificationType getNotificationType(){return notificationType;} public UUID getRelatedEntityId(){return relatedEntityId;} public boolean isRead(){return read;} public void markRead(){read=true;}
}
