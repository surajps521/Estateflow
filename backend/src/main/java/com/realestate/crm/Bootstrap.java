package com.realestate.crm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
class Bootstrap {
 @Bean CommandLineRunner createInitialAdmin(UserRepository users,PasswordEncoder encoder,
   @Value("${app.bootstrap.admin-email}") String email,
   @Value("${app.bootstrap.admin-password}") String password) {
   return args -> { if(users.findByEmail(email).isEmpty()) users.save(new User(email,encoder.encode(password),Role.ADMIN)); };
 }

 @Bean CommandLineRunner createDemoData(UserRepository users, AgentProfileRepository agents,
   PropertyRepository properties, PasswordEncoder encoder) {
   return args -> {
     if (users.findByRoleAndActiveTrue(Role.AGENT).isEmpty()) {
       createAgent(users,agents,encoder,"aarav@estateflow.com","AgentPass123!","Aarav Mehta","9876543210",8,"Luxury Homes");
       createAgent(users,agents,encoder,"meera@estateflow.com","AgentPass123!","Meera Shah","9876501234",5,"Residential");
       createAgent(users,agents,encoder,"rohan@estateflow.com","AgentPass123!","Rohan Kapoor","9876512345",6,"Commercial");
     }
     seedProperty(properties,"Runtime Verification Residence","Apartment",2_500_000,"25","Bengaluru","2 BHK","https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=900&q=80");
     seedProperty(properties,"Skyline Heights","Apartment",12_500_000,"1850","Marathahalli, Bengaluru","3 BHK","https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=900&q=80");
     seedProperty(properties,"Palm Grove Villa","Villa",28_500_000,"3200","Whitefield, Bengaluru","4 BHK","https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=900&q=80");
     seedProperty(properties,"Urban Nest","Apartment",8_750_000,"1250","Indiranagar, Bengaluru","2 BHK","https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=900&q=80");
     seedProperty(properties,"Prime Trade Centre","Commercial space",19_000_000,"2400","MG Road, Bengaluru","Office floor","https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=900&q=80");
     seedProperty(properties,"Lakeview Residency","Apartment",9_800_000,"1400","Hebbal, Bengaluru","3 BHK","https://images.unsplash.com/photo-1600566753086-00f18fb6b3ea?auto=format&fit=crop&w=900&q=80");
   };
 }

 private void createAgent(UserRepository users,AgentProfileRepository agents,PasswordEncoder encoder,
   String email,String password,String name,String phone,int experience,String specialization) {
   if(users.findByEmail(email).isPresent()) return;
   User user=users.save(new User(email,encoder.encode(password),Role.AGENT));
   agents.save(new AgentProfile(user,name,phone,experience,specialization,null));
 }

 private Property demoProperty(String title,String type,long price,String area,String location,String configuration,String image) {
   Property p=new Property();
   p.title=title;p.type=type;p.price=new java.math.BigDecimal(price);p.areaSqft=Integer.valueOf(area);
   p.location=location;p.configuration=configuration;p.imageUrl=image;
   return p;
 }

 private void seedProperty(PropertyRepository properties,String title,String type,long price,String area,String location,String configuration,String image) {
   if(properties.findAll().stream().noneMatch(p->p.getTitle().equalsIgnoreCase(title))) {
     properties.save(demoProperty(title,type,price,area,location,configuration,image));
   }
 }
}
