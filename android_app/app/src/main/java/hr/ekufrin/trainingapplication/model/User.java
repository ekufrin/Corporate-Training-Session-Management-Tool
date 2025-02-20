package hr.ekufrin.trainingapplication.model;

public class User {
        private String firstName;
        private String lastName;
        private Role role;

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getRole() { return role.getName(); }

}
