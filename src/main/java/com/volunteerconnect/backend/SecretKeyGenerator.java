import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Base64;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        // Generate a 256-bit (32-byte) key for HS256 algorithm
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        System.out.println("Generated Base64 Secret Key: " + base64Key);
    }
}