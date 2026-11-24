package com.realestate.crm;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
class CrmService {
  final UserRepository users; final AgentProfileRepository agentProfiles; final CustomerProfileRepository customerProfiles;
  final PropertyRepository properties; final LeadRepository leads; final SiteVisitRepository visits; final DealRepository deals;
  final AssignmentRequestRepository assignmentRequests; final RecommendationRepository recommendations; final NotificationRepository notifications;
  final PasswordEncoder encoder; final AuthenticationManager authManager; final JwtTokenProvider tokens;
  CrmService(UserRepository u,AgentProfileRepository ap,CustomerProfileRepository cp,PropertyRepository p,LeadRepository l,SiteVisitRepository v,DealRepository d,AssignmentRequestRepository ar,RecommendationRepository rr,NotificationRepository n,PasswordEncoder e,AuthenticationManager a,JwtTokenProvider t){
    users=u;agentProfiles=ap;customerProfiles=cp;properties=p;leads=l;visits=v;deals=d;assignmentRequests=ar;recommendations=rr;notifications=n;encoder=e;authManager=a;tokens=t;
  }
  AuthResponse login(LoginRequest r){
    String email=r.email().trim().toLowerCase(Locale.ROOT);
    try {
      authManager.authenticate(new UsernamePasswordAuthenticationToken(email,r.password()));
    } catch(BadCredentialsException|DisabledException|LockedException e){
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");
    }
    User u=users.findByEmail(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password"));
    if(!u.isActive())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Account is inactive");
    return new AuthResponse(tokens.generate(u),u.getId(),u.getEmail(),u.getRole().name());
  }
  User register(RegisterRequest r){if(r.role()==Role.CUSTOMER)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use customer registration for customer accounts");return createUser(r.email(),r.password(),r.role(),r.email(),null,null,null,null,null);}
  @Transactional
  User registerCustomer(CustomerRegisterRequest r){return createUser(r.email().trim().toLowerCase(Locale.ROOT),r.password(),Role.CUSTOMER,r.displayName(),r.phone(),r.preferredLocation(),r.budgetMax(),r.requirements(),null);}
  private User createUser(String email,String password,Role role,String displayName,String phone,String location,BigDecimal budget,String requirements,String specialization){
    if(users.findByEmail(email).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
    User u=users.save(new User(email,encoder.encode(password),role));
    if(role==Role.AGENT)agentProfiles.save(new AgentProfile(u,displayName,phone==null?"":phone,0,specialization==null?"Residential":specialization,null));
    if(role==Role.CUSTOMER)customerProfiles.save(new CustomerProfile(u,displayName,phone,location,budget,requirements));
    return u;
  }
  List<AgentResponse> agents(){return users.findByRoleAndActiveTrue(Role.AGENT).stream().map(this::agentDto).toList();}
  AgentResponse agent(UUID id){User u=users.findById(id).filter(x->x.getRole()==Role.AGENT&&x.isActive()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Agent not found"));return agentDto(u);}
  AgentResponse agentDto(User u){AgentProfile p=agentProfiles.findByUserId(u.getId()).orElse(new AgentProfile(u,u.getEmail(),"",0,"Residential",null));return new AgentResponse(u.getId(),u.getEmail(),p.getDisplayName(),p.getPhone(),p.getExperienceYears(),p.getSpecialization(),p.getProfileImageUrl(),u.isActive());}
  List<UserAdminResponse> adminUsers(){return users.findAll().stream().map(u->new UserAdminResponse(u.getId(),u.getEmail(),u.getRole().name(),u.isActive())).toList();}
  UserAdminResponse setUserActive(UUID id,boolean active){User u=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));if(u.getRole()==Role.ADMIN&& !active)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Admin account cannot be deactivated");u.setActive(active);users.save(u);return new UserAdminResponse(u.getId(),u.getEmail(),u.getRole().name(),u.isActive());}
  PropertyResponse createProperty(PropertyRequest r){Property p=new Property();p.title=r.title();p.type=r.type();p.price=r.price();p.areaSqft=r.areaSqft();p.location=r.location();p.configuration=r.configuration();p.imageUrl=r.imageUrl();return propertyDto(properties.save(p));}
  List<PropertyResponse> properties(PropertyStatus status,String type){List<Property> rows=status!=null&&type!=null?properties.findByStatusAndType(status,type):status!=null?properties.findByStatus(status):properties.findAll();return rows.stream().map(this::propertyDto).toList();}
  PropertyResponse propertyDto(Property p){return new PropertyResponse(p.getId(),p.getTitle(),p.getType(),p.getStatus().name(),p.getPrice(),p.getAreaSqft(),p.getLocation(),p.getConfiguration(),p.getImageUrl());}
  LeadResponse createLead(LeadRequest r,UUID caller,boolean admin){User agent=users.findById(admin&&r.agentId()!=null?r.agentId():caller).orElseThrow();if(agent.getRole()!=Role.AGENT)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Lead must be assigned to an agent");Lead l=new Lead();l.fullName=r.fullName();l.phone=r.phone();l.budgetMax=r.budgetMax();l.agent=agent;return leadDto(leads.save(l));}
  @Transactional(readOnly=true) List<LeadResponse> leads(UUID caller,boolean admin){return (admin?leads.findAll():leads.findByAgentId(caller)).stream().map(this::leadDto).toList();}
  @Transactional(readOnly=true) List<LeadResponse> customerLeads(UUID customer){return leads.findByCustomerId(customer).stream().map(this::leadDto).toList();}
  LeadResponse updateStage(UUID id,LeadStageRequest r,UUID caller,boolean admin){Lead l=leads.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Lead not found"));if(!admin&&!l.agent.getId().equals(caller))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not assigned to this lead");if(l.getStage()==LeadStage.WON||l.getStage()==LeadStage.LOST)throw new ResponseStatusException(HttpStatus.CONFLICT,"Closed leads cannot change stage");l.stage=r.stage();return leadDto(leads.save(l));}
  LeadResponse leadDto(Lead l){User c=l.getCustomer();return new LeadResponse(l.getId(),l.getFullName(),l.getPhone(),l.getStage().name(),l.getBudgetMax(),l.getAgent().getId(),l.getAgent().getEmail(),c==null?null:c.getId(),c==null?null:customerName(c));}
  String customerName(User u){return customerProfiles.findByUserId(u.getId()).map(CustomerProfile::getDisplayName).orElse(u.getEmail());}

  @Transactional AssignmentRequestResponse createAssignment(AgentRequest r,UUID customerId){
    User customer=requiredUser(customerId,Role.CUSTOMER);User agent=users.findById(r.agentId()).filter(u->u.getRole()==Role.AGENT&&u.isActive()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Agent not found"));
    Property p=properties.findById(r.propertyId()).filter(x->x.getStatus()==PropertyStatus.AVAILABLE).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Property is not available"));
    AgentAssignmentRequest request=assignmentRequests.save(new AgentAssignmentRequest(customer,agent,p,r.requirements()));
    notifications.save(new Notification(agent,"New customer request",customerName(customer)+" is interested in "+p.getTitle(),NotificationType.AGENT_REQUEST,request.getId()));
    return assignmentDto(request);
  }
  @Transactional(readOnly=true) List<AssignmentRequestResponse> myAssignments(UUID customer){return assignmentRequests.findByCustomerIdOrderByCreatedAtDesc(customer).stream().map(this::assignmentDto).toList();}
  @Transactional(readOnly=true) List<AssignmentRequestResponse> pendingAssignments(UUID agent){return assignmentRequests.findByAgentIdAndStatusOrderByCreatedAtDesc(agent,AssignmentRequestStatus.PENDING).stream().map(this::assignmentDto).toList();}
  @Transactional AssignmentRequestResponse updateAssignment(UUID id,UUID agentId,AssignmentRequestStatus status){
    AgentAssignmentRequest r=assignmentRequests.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Request not found"));if(!r.getAgent().getId().equals(agentId))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Request is not assigned to this agent");if(r.getStatus()!=AssignmentRequestStatus.PENDING)throw new ResponseStatusException(HttpStatus.CONFLICT,"Request has already been resolved");r.setStatus(status);assignmentRequests.save(r);
    if(status==AssignmentRequestStatus.ACCEPTED){
      Lead lead=r.getLead();
      if(lead==null){
        User customer=r.getCustomer();
        lead=new Lead();
        lead.fullName=customerName(customer);
        lead.phone=customerProfiles.findByUserId(customer.getId()).map(CustomerProfile::getPhone).orElse("");
        lead.budgetMax=customerProfiles.findByUserId(customer.getId()).map(CustomerProfile::getBudgetMax).orElse(null);
        lead.agent=r.getAgent();
        lead.customer=customer;
        lead.stage=LeadStage.CONTACTED;
        lead=leads.save(lead);
        r.setLead(lead);
        assignmentRequests.save(r);
      }else{
        lead.stage=LeadStage.CONTACTED;
        leads.save(lead);
      }
    }
    notifications.save(new Notification(r.getCustomer(),"Agent request "+status.name().toLowerCase(),r.getAgent().getEmail()+" has "+status.name().toLowerCase()+" your request",NotificationType.REQUEST_UPDATE,r.getId()));
    return assignmentDto(r);
  }
  AssignmentRequestResponse assignmentDto(AgentAssignmentRequest r){Property p=r.getProperty();return new AssignmentRequestResponse(r.getId(),r.getCustomer().getId(),customerName(r.getCustomer()),r.getCustomer().getEmail(),r.getAgent().getId(),p==null?null:p.getId(),p==null?null:p.getTitle(),r.getRequirements(),r.getStatus().name(),r.getCreatedAt());}

  @Transactional RecommendationResponse recommend(RecommendationRequest r,UUID agentId){User agent=requiredUser(agentId,Role.AGENT);User customer=requiredUser(r.customerId(),Role.CUSTOMER);ensureAssigned(agentId,r.customerId());Property p=properties.findById(r.propertyId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Property not found"));PropertyRecommendation saved=recommendations.save(new PropertyRecommendation(agent,customer,p,r.note()));notifications.save(new Notification(customer,"New property recommendation",p.getTitle()+" was recommended by your agent",NotificationType.RECOMMENDATION,p.getId()));return recommendationDto(saved);}
  @Transactional(readOnly=true) List<RecommendationResponse> recommendations(UUID customer){return recommendations.findByCustomerIdOrderByCreatedAtDesc(customer).stream().map(this::recommendationDto).toList();}
  RecommendationResponse recommendationDto(PropertyRecommendation r){Property p=r.getProperty();return new RecommendationResponse(r.getId(),p.getId(),p.getTitle(),p.getStatus().name(),p.getPrice(),p.getLocation(),r.getNote(),r.getAgent().getId(),r.getAgent().getEmail());}
  void ensureAssigned(UUID agentId,UUID customerId){if(leads.findByAgentId(agentId).stream().noneMatch(l->l.getCustomer()!=null&&l.getCustomer().getId().equals(customerId)))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Customer is not assigned to this agent");}

  @Transactional(readOnly=true) List<NotificationResponse> notifications(UUID recipient){return notifications.findByRecipientIdOrderByCreatedAtDesc(recipient).stream().map(this::notificationDto).toList();}
  NotificationResponse readNotification(UUID id,UUID recipient){Notification n=notifications.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found"));if(!n.recipient.getId().equals(recipient))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Notification does not belong to this user");n.markRead();return notificationDto(notifications.save(n));}
  NotificationResponse notificationDto(Notification n){return new NotificationResponse(n.getId(),n.getTitle(),n.getMessage(),n.getNotificationType().name(),n.getRelatedEntityId(),n.isRead(),n.getCreatedAt());}

  VisitResponse createVisit(VisitRequest r,UUID caller,boolean admin,boolean customer){Lead l=leads.findById(r.leadId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Lead not found"));if(customer&& (l.getCustomer()==null||!l.getCustomer().getId().equals(caller)))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your lead");if(!customer&&!admin&&!l.agent.getId().equals(caller))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not assigned to this lead");Property p=properties.findById(r.propertyId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Property not found"));User a=l.getAgent();SiteVisit v=new SiteVisit();v.lead=l;v.property=p;v.agent=a;v.visitTime=r.visitTime();l.stage=LeadStage.VISIT_SCHEDULED;leads.save(l);return visitDto(visits.save(v));}
  @Transactional(readOnly=true) List<VisitResponse> visits(UUID caller,boolean admin,boolean customer){List<SiteVisit> rows=admin?visits.findAllByOrderByVisitTimeAsc():customer?visits.findByLeadCustomerIdOrderByVisitTimeAsc(caller):visits.findByAgentIdOrderByVisitTimeAsc(caller);return rows.stream().map(this::visitDto).toList();}
  VisitResponse updateVisit(UUID id,VisitUpdateRequest r,UUID caller,boolean admin){SiteVisit v=visits.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Visit not found"));if(!admin&&!v.agent.getId().equals(caller))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not assigned to this visit");v.status=r.status();v.feedback=r.feedback();return visitDto(visits.save(v));}
  VisitResponse visitDto(SiteVisit v){return new VisitResponse(v.getId(),v.getLead().getId(),v.getLead().getFullName(),v.getProperty().getId(),v.getProperty().getTitle(),v.getAgent().getId(),v.getVisitTime(),v.getStatus().name(),v.getFeedback());}
  @Transactional DealResponse closeDeal(DealRequest r,UUID caller,boolean admin){Lead l=leads.findById(r.leadId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Lead not found"));if(!admin&&!l.agent.getId().equals(caller))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not assigned to this lead");if(l.getCustomer()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Deal requires a customer lead");Property p=properties.findById(r.propertyId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Property not found"));if(p.status==PropertyStatus.SOLD)throw new ResponseStatusException(HttpStatus.CONFLICT,"Property already sold");Deal d=new Deal();d.lead=l;d.property=p;d.finalPrice=r.finalPrice();d.closedDate=r.closedDate();p.status=PropertyStatus.SOLD;l.stage=LeadStage.WON;properties.save(p);leads.save(l);Deal saved=deals.save(d);notifications.save(new Notification(l.getCustomer(),"Purchase completed","Your purchase of "+p.getTitle()+" has been completed",NotificationType.DEAL,saved.getId()));return new DealResponse(saved.getId(),l.getId(),l.getFullName(),p.getId(),p.getTitle(),saved.getFinalPrice(),saved.getClosedDate());}
  @Transactional(readOnly=true) List<DealResponse> deals(UUID caller,boolean admin,boolean customer){List<Deal> rows=customer?deals.findByLeadCustomerId(caller):deals.findAll();return rows.stream().map(this::dealDto).toList();}
  DealResponse dealDto(Deal d){return new DealResponse(d.getId(),d.getLead().getId(),d.getLead().getFullName(),d.getProperty().getId(),d.getProperty().getTitle(),d.getFinalPrice(),d.getClosedDate());}
  User requiredUser(UUID id,Role role){return users.findById(id).filter(u->u.getRole()==role&&u.isActive()).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"Invalid account role"));}
  DashboardMetricsResponse metrics(){var all=deals.findAll();BigDecimal revenue=all.stream().map(Deal::getFinalPrice).reduce(BigDecimal.ZERO,BigDecimal::add);return new DashboardMetricsResponse(properties.count(),properties.findByStatus(PropertyStatus.AVAILABLE).size(),leads.count(),visits.findAllByOrderByVisitTimeAsc().stream().filter(v->v.getStatus()==VisitStatus.SCHEDULED).count(),deals.count(),revenue,users.findByRoleAndActiveTrue(Role.CUSTOMER).size(),users.findByRoleAndActiveTrue(Role.AGENT).size());}
}
