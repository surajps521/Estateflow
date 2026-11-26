package com.realestate.crm;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth")
class AuthController {
  final CrmService service; AuthController(CrmService s){service=s;}
  @PostMapping("/login") AuthResponse login(@Valid @RequestBody LoginRequest r){return service.login(r);}
  @PostMapping("/register") @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPER_ADMIN')")
  ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r){User u=service.register(r);return ResponseEntity.status(201).body(Map.of("id",u.getId(),"email",u.getEmail(),"role",u.getRole()));}
  @PostMapping("/register/customer") ResponseEntity<?> registerCustomer(@Valid @RequestBody CustomerRegisterRequest r){User u=service.registerCustomer(r);return ResponseEntity.status(201).body(Map.of("id",u.getId(),"email",u.getEmail(),"role",u.getRole()));}
  @PostMapping("/register/owner") ResponseEntity<?> registerOwner(@Valid @RequestBody OwnerRegisterRequest r){User u=service.registerOwner(r);return ResponseEntity.status(201).body(Map.of("id",u.getId(),"email",u.getEmail(),"role",u.getRole()));}
}

@RestController @RequestMapping("/api/v1/agents")
class AgentController {
  final CrmService s; AgentController(CrmService s){this.s=s;}
  @GetMapping List<AgentResponse> list(){return s.agents();}
  @GetMapping("/{id}") AgentResponse get(@PathVariable UUID id){return s.agent(id);}
}

@RestController @RequestMapping("/api/v1/admin/users")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPER_ADMIN')")
class AdminUserController {
  final CrmService s;AdminUserController(CrmService s){this.s=s;}
  @GetMapping List<UserAdminResponse> list(){return s.adminUsers();}
  @PatchMapping("/{id}/active") UserAdminResponse active(@PathVariable UUID id,@RequestParam boolean value){return s.setUserActive(id,value);}
}

@RestController @RequestMapping("/api/v1/properties")
class PropertyController {
  final CrmService s;PropertyController(CrmService s){this.s=s;}
  @GetMapping List<PropertyResponse> list(@RequestParam(required=false) PropertyStatus status,@RequestParam(required=false) String type){return s.properties(status,type);}
  @PostMapping @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER')") ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest r){return ResponseEntity.status(201).body(s.createProperty(r));}
  @PutMapping("/{id}") @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER')") PropertyResponse update(@PathVariable UUID id,@Valid @RequestBody PropertyUpdateRequest r){return s.updateProperty(id,r);}
  @DeleteMapping("/{id}") @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER')") ResponseEntity<Void> archive(@PathVariable UUID id){s.archiveProperty(id);return ResponseEntity.noContent().build();}
}

@RestController @RequestMapping("/api/v1/leads")
class LeadController {
  final CrmService s;LeadController(CrmService s){this.s=s;}
  @GetMapping @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','AGENT')") List<LeadResponse> list(Authentication a,@RequestAttribute("currentUserId") UUID id){return s.leads(id,a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPER_ADMIN")));}
  @GetMapping("/my") @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')") List<LeadResponse> mine(@RequestAttribute("currentUserId") UUID id){return s.customerLeads(id);}
  @PostMapping @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','AGENT')") ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadRequest r,Authentication a,@RequestAttribute("currentUserId") UUID id){return ResponseEntity.status(201).body(s.createLead(r,id,a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPER_ADMIN"))));}
  @PatchMapping("/{id}/stage") @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','AGENT')") LeadResponse stage(@PathVariable UUID id,@Valid @RequestBody LeadStageRequest r,Authentication a,@RequestAttribute("currentUserId") UUID caller){return s.updateStage(id,r,caller,a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPER_ADMIN")));}
}

@RestController @RequestMapping("/api/v1/agent-requests")
class AgentRequestController {
  final CrmService s;AgentRequestController(CrmService s){this.s=s;}
  @PostMapping @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')") ResponseEntity<AssignmentRequestResponse> create(@Valid @RequestBody AgentRequest r,@RequestAttribute("currentUserId") UUID customer){return ResponseEntity.status(201).body(s.createAssignment(r,customer));}
  @GetMapping("/my") @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')") List<AssignmentRequestResponse> mine(@RequestAttribute("currentUserId") UUID customer){return s.myAssignments(customer);}
  @GetMapping("/pending") @org.springframework.security.access.prepost.PreAuthorize("hasRole('AGENT')") List<AssignmentRequestResponse> pending(@RequestAttribute("currentUserId") UUID agent){return s.pendingAssignments(agent);}
  @PatchMapping("/{id}/accept") @org.springframework.security.access.prepost.PreAuthorize("hasRole('AGENT')") AssignmentRequestResponse accept(@PathVariable UUID id,@RequestAttribute("currentUserId") UUID agent){return s.updateAssignment(id,agent,AssignmentRequestStatus.ACCEPTED);}
  @PatchMapping("/{id}/reject") @org.springframework.security.access.prepost.PreAuthorize("hasRole('AGENT')") AssignmentRequestResponse reject(@PathVariable UUID id,@RequestAttribute("currentUserId") UUID agent){return s.updateAssignment(id,agent,AssignmentRequestStatus.REJECTED);}
}

@RestController @RequestMapping("/api/v1/recommendations")
class RecommendationController {
  final CrmService s;RecommendationController(CrmService s){this.s=s;}
  @PostMapping @org.springframework.security.access.prepost.PreAuthorize("hasRole('AGENT')") ResponseEntity<RecommendationResponse> create(@Valid @RequestBody RecommendationRequest r,@RequestAttribute("currentUserId") UUID agent){return ResponseEntity.status(201).body(s.recommend(r,agent));}
  @GetMapping("/my") @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')") List<RecommendationResponse> mine(@RequestAttribute("currentUserId") UUID customer){return s.recommendations(customer);}
}

@RestController @RequestMapping("/api/v1/notifications")
class NotificationController {
  final CrmService s;NotificationController(CrmService s){this.s=s;}
  @GetMapping List<NotificationResponse> list(@RequestAttribute("currentUserId") UUID recipient){return s.notifications(recipient);}
  @PatchMapping("/{id}/read") NotificationResponse read(@PathVariable UUID id,@RequestAttribute("currentUserId") UUID recipient){return s.readNotification(id,recipient);}
}

@RestController @RequestMapping("/api/v1/site-visits")
class SiteVisitController {
  final CrmService s;SiteVisitController(CrmService s){this.s=s;}
  @GetMapping @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','AGENT')") List<VisitResponse> list(Authentication a,@RequestAttribute("currentUserId") UUID id){return s.visits(id,a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPER_ADMIN")),false);}
  @GetMapping("/my") @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')") List<VisitResponse> mine(@RequestAttribute("currentUserId") UUID id){return s.visits(id,false,true);}
  @PostMapping @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','AGENT','CUSTOMER')") ResponseEntity<VisitResponse> create(@Valid @RequestBody VisitRequest r,Authentication a,@RequestAttribute("currentUserId") UUID id){boolean admin=a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPER_ADMIN"));boolean customer=a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_CUSTOMER"));return ResponseEntity.status(201).body(s.createVisit(r,id,admin,customer));}
  @PatchMapping("/{id}") @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','AGENT')") VisitResponse update(@PathVariable UUID id,@Valid @RequestBody VisitUpdateRequest r,Authentication a,@RequestAttribute("currentUserId") UUID caller){return s.updateVisit(id,r,caller,a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPER_ADMIN")));}
}

@RestController @RequestMapping("/api/v1/deals")
class DealController {
  final CrmService s;DealController(CrmService s){this.s=s;}
  @GetMapping @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','AGENT')") List<DealResponse> list(Authentication a,@RequestAttribute("currentUserId") UUID id){return s.deals(id,a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPER_ADMIN")),false);}
  @GetMapping("/my") @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')") List<DealResponse> mine(@RequestAttribute("currentUserId") UUID id){return s.deals(id,false,true);}
  @PostMapping @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','AGENT')") ResponseEntity<DealResponse> close(@Valid @RequestBody DealRequest r,Authentication a,@RequestAttribute("currentUserId") UUID id){return ResponseEntity.status(201).body(s.closeDeal(r,id,a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPER_ADMIN"))));}
}

@RestController @RequestMapping("/api/v1/dashboard")
class DashboardController {final CrmService s;DashboardController(CrmService s){this.s=s;} @GetMapping @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','AGENT')") DashboardMetricsResponse metrics(){return s.metrics();}}
