package testcode.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class Auth0JwtTest {
    String username= "2333";
    String secret = "secret001";
    private void badJwt(String token) {
        // TODO:
        DecodedJWT jwt1 = JWT.decode(token);
        DecodedJWT jwt2 = new JWT().decodeJwt(token);
        DecodedJWT jwt3 = JWT.decode(token);
        DecodedJWT jwt4 = new JWT().decodeJwt(token);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();

        DecodedJWT jwt5 = verifier.verify(token);
        DecodedJWT jwt6 = verifier.verify(jwt2);
        algorithm.verify(jwt2);


        jwt1.getClaim("username").asString(); // $hasMissingJwtSignatureCheck
        jwt2.getClaim("username").asString(); // !$hasMissingJwtSignatureCheck
        jwt3.getClaim("username").asString(); // $hasMissingJwtSignatureCheck
        jwt4.getClaim("username").asString(); // $hasMissingJwtSignatureCheck
        jwt5.getClaim("username").asString(); // !$hasMissingJwtSignatureCheck
        jwt6.getClaim("username").asString(); // !$hasMissingJwtSignatureCheck
    }

}
