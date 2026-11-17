package com.realestate.crm;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
interface UserRepository extends JpaRepository<User,UUID>{Optional<User> findByEmail(String email); List<User> findByRoleAndActiveTrue(Role role);}
interface AgentProfileRepository extends JpaRepository<AgentProfile,UUID>{Optional<AgentProfile> findByUserId(UUID userId);}
interface CustomerProfileRepository extends JpaRepository<CustomerProfile,UUID>{Optional<CustomerProfile> findByUserId(UUID userId);}
interface PropertyRepository extends JpaRepository<Property,UUID>{List<Property> findByStatusAndType(PropertyStatus status,String type); List<Property> findByStatus(PropertyStatus status);}
interface LeadRepository extends JpaRepository<Lead,UUID>{List<Lead> findByAgentId(UUID agentId); List<Lead> findByCustomerId(UUID customerId);}
interface SiteVisitRepository extends JpaRepository<SiteVisit,UUID>{List<SiteVisit> findAllByOrderByVisitTimeAsc(); List<SiteVisit> findByAgentIdOrderByVisitTimeAsc(UUID agentId); List<SiteVisit> findByLeadCustomerIdOrderByVisitTimeAsc(UUID customerId);}
interface DealRepository extends JpaRepository<Deal,UUID>{List<Deal> findByLeadCustomerId(UUID customerId);}
interface AssignmentRequestRepository extends JpaRepository<AgentAssignmentRequest,UUID>{List<AgentAssignmentRequest> findByAgentIdAndStatusOrderByCreatedAtDesc(UUID agentId,AssignmentRequestStatus status); List<AgentAssignmentRequest> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);}
interface RecommendationRepository extends JpaRepository<PropertyRecommendation,UUID>{List<PropertyRecommendation> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);}
interface NotificationRepository extends JpaRepository<Notification,UUID>{List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId); long countByRecipientIdAndReadFalse(UUID recipientId);}
