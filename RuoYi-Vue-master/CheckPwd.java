import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckPwd {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$pMxX3IF9tcRnWDdBiY4gV.hxaxhwCJMnnthjmern.IdKxrGV1oYyG";
        System.out.println("0434 matches: " + encoder.matches("0434", hash));
        System.out.println("admin123 matches: " + encoder.matches("admin123", hash));
    }
}
