package eu.europeana.apikey.keycloak;

import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeycloakTokenVerifier {
	/**
     * Public key of the realm that is used to verify the token signature
     */
    private static PublicKey publicKey;
    private static String realmPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAneYnCp5jELQvRwRt5NmpfI+NIKSVyHHiRCCeZiQQOhB/x48a9nwLO6ubChKqFLl5frVs7gX6oAu/mSZ5u9UAonCts8JPlrEaMSL3NIY+qQKqJh8UFN1TodSOjgQi90Fp2MUmnwy/lTRaWnn92bmSGyZtfIAxgWq+bVrnhrWJKQdJUVKVLOSPaxmSF0BsdNVztjrfUZfaFaWyfYb/ccL3DcIgjxFizBhj4C9xOhO+uJw+0UdTAlrIe7D1aFVu4pQ7DPx6eY0j1ZlduhbSZYKv5EEc9xre9323V6N4ad6oDtM9JZ3IhIk5gggQUhxpmaud+nhXc7V0g7VOt5CyX+6mEQIDAQAB";

    /**
     * Verify JWT token with the realm public key. Return an AccessToken that can be used to authorize further requests.
     *
     * @param token base64 encoded JWT token
     * @return access token object
     * @throws VerificationException
     */
    public static AccessToken verifyToken(String token) throws VerificationException {
        TokenVerifier<AccessToken> verifier = TokenVerifier.create(token, AccessToken.class);
        return verifier.publicKey(publicKey).verify().getToken();
    }

    /**
     * Convert base64 realm public key to PublicKey object that can be used for signature verification.
     */


    public static void toPublicKey() {
        try {
            byte[] publicBytes = Base64.getDecoder().decode(realmPublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("public key err");
        }
    }

    /**
     * Return the realm public key
     *
     * @return public key of the realm
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
