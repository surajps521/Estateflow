package com.realestate.crm;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

record LoginRequest(@NotBlank @Email String email,@NotBlank String password){}
record RegisterRequest(@NotBlank @Email String email,@NotBlank @Size(min=8) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",message="Password must contain 8+ characters, uppercase and lowercase letters, a number, and a special character") String password,@NotNull Role role){}
record CustomerRegisterRequest(@NotBlank @Email String email,@NotBlank @Size(min=8) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",message="Password must contain 8+ characters, uppercase and lowercase letters, a number, and a special character") String password,@NotBlank String displayName,@NotBlank String phone,String preferredLocation,@Positive BigDecimal budgetMax,String requirements){}
record AuthResponse(String token,UUID userId,String email,String role){}
record AgentResponse(UUID id,String email,String displayName,String phone,Integer experienceYears,String specialization,String profileImageUrl,boolean available){}
record UserAdminResponse(UUID id,String email,String role,boolean active){}
record PropertyRequest(@NotBlank String title,@NotBlank String type,@NotNull @Positive BigDecimal price,@NotNull @Positive Integer areaSqft,String location,String configuration,String imageUrl){}
record PropertyResponse(UUID id,String title,String type,String status,BigDecimal price,Integer areaSqft,String location,String configuration,String imageUrl){}
record LeadRequest(@NotBlank String fullName,@NotBlank String phone,@NotNull @Positive BigDecimal budgetMax,UUID agentId){}
record LeadStageRequest(@NotNull LeadStage stage){}
record LeadResponse(UUID id,String fullName,String phone,String stage,BigDecimal budgetMax,UUID assignedAgentId,String assignedAgentName,UUID customerId,String customerName){}
record VisitRequest(@NotNull UUID leadId,@NotNull UUID propertyId,@NotNull Instant visitTime){}
record VisitUpdateRequest(@NotNull VisitStatus status,String feedback){}
record VisitResponse(UUID id,UUID leadId,String leadName,UUID propertyId,String propertyTitle,UUID agentId,Instant visitTime,String status,String feedback){}
record DealRequest(@NotNull UUID leadId,@NotNull UUID propertyId,@NotNull @Positive BigDecimal finalPrice,@NotNull LocalDate closedDate){}
record DealResponse(UUID id,UUID leadId,String leadName,UUID propertyId,String propertyTitle,BigDecimal finalPrice,LocalDate closedDate){}
record DashboardMetricsResponse(long totalProperties,long availableProperties,long totalLeads,long scheduledVisits,long closedDeals,BigDecimal closedRevenue,long totalCustomers,long totalAgents){}
record AgentRequest(@NotNull UUID agentId,@NotNull UUID propertyId,String requirements){}
record AssignmentRequestResponse(UUID id,UUID customerId,String customerName,String customerEmail,UUID agentId,UUID propertyId,String propertyTitle,String requirements,String status,Instant createdAt){}
record RecommendationRequest(@NotNull UUID customerId,@NotNull UUID propertyId,String note){}
record RecommendationResponse(UUID id,UUID propertyId,String propertyTitle,String propertyStatus,BigDecimal price,String location,String note,UUID agentId,String agentName){}
record NotificationResponse(UUID id,String title,String message,String notificationType,UUID relatedEntityId,boolean read,Instant createdAt){}
record ErrorResponse(String message,java.time.Instant timestamp,Object errors){}
